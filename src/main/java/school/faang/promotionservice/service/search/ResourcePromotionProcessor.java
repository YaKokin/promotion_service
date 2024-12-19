package school.faang.promotionservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import school.faang.promotionservice.builder.SearchQueryBuilder;
import school.faang.promotionservice.config.context.UserContext;
import school.faang.promotionservice.exception.ReindexPromotionException;
import school.faang.promotionservice.exception.SearchServiceExceptions;
import school.faang.promotionservice.exception.SessionResourceException;
import school.faang.promotionservice.model.jpa.Impression;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.PromotionStatus;
import school.faang.promotionservice.model.search.PromotionDocument;
import school.faang.promotionservice.repository.jpa.PromotionRepository;
import school.faang.promotionservice.service.ImpressionService;
import school.faang.promotionservice.service.cache.AbstractSessionResourceService;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.search.filter.Filter;
import school.faang.promotionservice.service.search.reindexing.ReindexService;
import school.faang.promotionservice.utils.CollectionUtils;
import school.faang.promotionservice.utils.WeightedRandomSelector;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
@Slf4j
public abstract class ResourcePromotionProcessor<DOC extends PromotionDocument> {

    private static final String SEARCHING_USERS_ERROR = "Error while searching users";

    private final ImpressionCounterService impressionCounterService;
    private final PromotionRepository promotionRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final ExecutorService defaultThreadPool;
    private final UserContext userContext;
    private final ImpressionService impressionService;
    private final WeightedRandomSelector<DOC> weightedRandomSelector;
    private final ReindexService<DOC, ?> reindexService;
    private final AbstractSessionResourceService<DOC> sessionResourceService;

    @RequiredArgsConstructor
    @SuppressWarnings("InnerClassMayBeStatic")
    private class ExcludeItemsFilter implements Filter {

        private final List<Long> excludedUserIds;

        private static final String USER_ID_FIELD = "resourceId";

        @Override
        public void apply(BoolQuery.Builder boolQuery) {
            if (CollectionUtils.isNotEmpty(excludedUserIds)) {
                List<FieldValue> fieldValues = excludedUserIds.stream()
                        .map(FieldValue::of)
                        .toList();

                Query excludeQuery = Query.of(q -> q
                        .terms(ts -> ts
                                .field(USER_ID_FIELD)
                                .terms(ts2 -> ts2.value(fieldValues))
                        ));
                boolQuery.mustNot(excludeQuery);
            }
        }
    }

    protected List<Long> searchPromotedUserIds(Integer limit,
                                               String sessionId,
                                               Class<DOC> docType,
                                               SearchQueryBuilder searchQueryBuilder
    ) {

        SearchRequest searchRequest = buildSearchRequest(sessionId, docType, searchQueryBuilder);
        List<DOC> promotionsDocsForDisplay =
                getPromotionsForDisplay(limit, searchRequest, docType);

        Map<Long, Long> updatedPromotionCounters = decrementCounters(promotionsDocsForDisplay);
        removePromotionDocIfCounterIsNegative(updatedPromotionCounters, promotionsDocsForDisplay);

        List<Long> promotionIdsToRemove = removeExpiredPromotionsFromIndex(updatedPromotionCounters, docType);

        defaultThreadPool.execute(() -> saveImpressions(promotionsDocsForDisplay));
        saveImpressions(promotionsDocsForDisplay);

        defaultThreadPool.execute(() -> deactivatePromotions(promotionIdsToRemove));
        deactivatePromotions(promotionIdsToRemove);

        return promotionsDocsForDisplay.stream()
                .map(PromotionDocument::getResourceId)
                .toList();
    }

    private SearchRequest buildSearchRequest(String sessionId, Class<DOC> docType,
                                             SearchQueryBuilder searchQueryBuilder) {

        List<Long> viewedResourceIds = sessionResourceService.getViewedUsers(sessionId);

        searchQueryBuilder.addFilter(new ExcludeItemsFilter(viewedResourceIds));
        return searchQueryBuilder.build();
    }

    private void saveImpressions(List<DOC> promotionsDocsForDisplay) {
        List<Promotion> promotionsForDisplay = promotionRepository.findByIdIn(
                promotionsDocsForDisplay.stream()
                        .map(PromotionDocument::getPromotionId)
                        .toList()
        );
        List<Impression> impressions = promotionsForDisplay.stream()
                .map(promotion -> Impression.builder()
                        .promotion(promotion)
                        .viewerUserId(userContext.getUserId())
                        .build())
                .toList();
        impressionService.saveImpressions(impressions);
    }

    private List<Long> removeExpiredPromotionsFromIndex(Map<Long, Long> updatedPromotionCounters, Class<DOC> docType) {
        List<Long> promotionIdsToRemove = updatedPromotionCounters.keySet().stream()
                .filter(promotionId -> promotionId <= 0)
                .toList();
        reindexService.deleteAllFromIndex(promotionIdsToRemove);
        return promotionIdsToRemove;
    }

    private Map<Long, Long> decrementCounters(List<DOC> promotionsForDisplay) {
        List<Long> promotionIds = promotionsForDisplay.stream()
                .map(PromotionDocument::getPromotionId)
                .toList();
        return impressionCounterService.decrementPromotionCounters(promotionIds);
    }

    private void deactivatePromotions(List<Long> promotionIdsToRemove) {
        List<Promotion> promotions = promotionRepository.findByIdIn(promotionIdsToRemove);
        promotions.forEach(promotion -> promotion.setPromotionStatus(PromotionStatus.EXPIRED));
        promotionRepository.saveAll(promotions);
    }

    private void removePromotionDocIfCounterIsNegative(Map<Long, Long> updatedPromotionCounters,
                                                       List<DOC> promotionsForDisplay) {
        updatedPromotionCounters.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .forEach(entry -> promotionsForDisplay.removeIf(
                        promotionDoc -> promotionDoc.isSamePromotionId(entry.getKey()))
                );
    }

    private List<DOC> getPromotionsForDisplay(Integer limit,
                                              SearchRequest searchRequest,
                                              Class<DOC> docType) {

        List<DOC> activePromotions = searchPromotionsByFilters(searchRequest, docType);

        return weightedRandomSelector.selectWeightedRandomElements(
                limit,
                activePromotions,
                PromotionDocument::getPriority);
    }

    private List<DOC> searchPromotionsByFilters(SearchRequest searchRequest, Class<DOC> docType) {
        try {
            SearchResponse<DOC> response = elasticsearchClient.search(searchRequest, docType);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            log.error(SEARCHING_USERS_ERROR, e);
            throw new SearchServiceExceptions(SEARCHING_USERS_ERROR, e);
        }
    }
}
