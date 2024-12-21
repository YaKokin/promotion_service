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
import org.jetbrains.annotations.NotNull;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import school.faang.promotionservice.builder.SearchQueryBuilder;
import school.faang.promotionservice.config.context.UserContext;
import school.faang.promotionservice.exception.SearchServiceExceptions;
import school.faang.promotionservice.model.jpa.Impression;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.PromotionStatus;
import school.faang.promotionservice.model.search.PromotionDocument;
import school.faang.promotionservice.service.ImpressionService;
import school.faang.promotionservice.service.PromotionService;
import school.faang.promotionservice.service.cache.AbstractSessionResourceService;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;
import school.faang.promotionservice.service.search.filter.Filter;
import school.faang.promotionservice.service.search.reindexing.ReindexService;
import school.faang.promotionservice.utils.CollectionUtils;
import school.faang.promotionservice.utils.WeightedRandomSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class ResourcePromotionProcessor<DOC extends PromotionDocument> {

    private final ImpressionCounterService impressionCounterService;
    private final PromotionService promotionService;
    private final ElasticsearchClient elasticsearchClient;
    private final ExecutorService defaultThreadPool;
    private final UserContext userContext;
    private final ImpressionService impressionService;
    private final WeightedRandomSelector<DOC> weightedRandomSelector;
    private final ReindexService<DOC, ?> reindexService;
    private final AbstractSessionResourceService<DOC> sessionResourceService;
    private final PriorityCalculator priorityCalculator;
    private final ElasticsearchRepository<DOC, Long> promotionDocRepository;

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

        Map<Long, Integer> updatedPromotionCounters = decrementCounters(promotionsDocsForDisplay);
        removePromotionDocIfCounterIsNegative(updatedPromotionCounters, promotionsDocsForDisplay);

        List<Long> promotionIdsToRemove = getPromotionIdsToRemove(updatedPromotionCounters);

        updatePromotionIndex(promotionsDocsForDisplay, updatedPromotionCounters, promotionIdsToRemove);

        Long userId = userContext.getUserId();
        defaultThreadPool.execute(() -> saveImpressions(promotionsDocsForDisplay, userId));
        defaultThreadPool.execute(() -> deactivatePromotions(promotionIdsToRemove));

        return promotionsDocsForDisplay.stream()
                .map(PromotionDocument::getResourceId)
                .toList();
    }

    private void updatePromotionIndex(List<DOC> promotionForDisplay,
                                      Map<Long, Integer> updatedPromotionCounters,
                                      List<Long> promotionIdsToRemove) {

        List<Long> updatedPromotionIds = new ArrayList<>(updatedPromotionCounters.keySet());

        reindexService.deleteAllFromIndex(promotionIdsToRemove);

        List<Long> promotionIdsToUpdatePriority
                = CollectionUtils.findMissingElements(updatedPromotionIds, promotionIdsToRemove);

        if (!promotionIdsToUpdatePriority.isEmpty()) {
            List<Promotion> promotionsToUpdatePriority = updateRemainingImpressions(updatedPromotionCounters, promotionIdsToUpdatePriority);
            updatePromotionPriority(promotionForDisplay, promotionsToUpdatePriority);
        }
    }

    private void updatePromotionPriority(List<DOC> promotionForDisplay, List<Promotion> promotionsToUpdate) {
        Map<Long, Promotion> idToPromotion = promotionsToUpdate.stream()
                .collect(Collectors.toMap(
                        Promotion::getId,
                        Function.identity()));

        promotionForDisplay.forEach(promotionDoc -> {
            double newPriority = priorityCalculator.calculate(
                    idToPromotion.get(promotionDoc.getPromotionId())
            );
            promotionDoc.setPriority(newPriority);
        });
        promotionDocRepository.saveAll(promotionForDisplay);
    }

    @NotNull
    private List<Promotion> updateRemainingImpressions(Map<Long, Integer> updatedPromotionCounters,
                                                       List<Long> promotionIdsToUpdate) {

        List<Promotion> promotionsToUpdate = promotionService.findByIdIn(promotionIdsToUpdate);
        promotionsToUpdate.forEach(promotion ->
                promotion.setRemainingImpressions(updatedPromotionCounters.get(promotion.getId()))
        );
        promotionService.saveAllAsync(promotionsToUpdate);
        return promotionsToUpdate;
    }

    private SearchRequest buildSearchRequest(String sessionId, Class<DOC> docType,
                                             SearchQueryBuilder searchQueryBuilder) {

        List<Long> viewedResourceIds = sessionResourceService.getViewedUsers(sessionId);
        searchQueryBuilder.addFilter(new ExcludeItemsFilter(viewedResourceIds));
        return searchQueryBuilder.build();
    }

    private void saveImpressions(List<DOC> promotionsDocsForDisplay, Long viewedUserId) {
        List<Promotion> promotionsForDisplay = promotionService.findByIdIn(
                promotionsDocsForDisplay.stream()
                        .map(PromotionDocument::getPromotionId)
                        .toList()
        );
        List<Impression> impressions = promotionsForDisplay.stream()
                .map(promotion -> Impression.builder()
                        .promotion(promotion)
                        .viewerUserId(viewedUserId)
                        .build())
                .toList();
        impressionService.saveImpressions(impressions);
    }

    @NotNull
    private static List<Long> getPromotionIdsToRemove(Map<Long, Integer> updatedPromotionCounters) {
        return updatedPromotionCounters.entrySet().stream()
                .filter(entry -> entry.getValue() <= 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Map<Long, Integer> decrementCounters(List<DOC> promotionsForDisplay) {
        List<Long> promotionIds = promotionsForDisplay.stream()
                .map(PromotionDocument::getPromotionId)
                .toList();
        return impressionCounterService.decrementPromotionCounters(promotionIds);
    }

    private void deactivatePromotions(List<Long> promotionIdsToRemove) {
        List<Promotion> promotions = promotionService.findByIdIn(promotionIdsToRemove);
        promotions.forEach(promotion -> {
            promotion.setPromotionStatus(PromotionStatus.EXPIRED);
            promotion.setRemainingImpressions(0);
        });
        promotionService.saveAllAsync(promotions);
    }

    private void removePromotionDocIfCounterIsNegative(Map<Long, Integer> updatedPromotionCounters,
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
        } catch (IOException ex) {
            throw new SearchServiceExceptions(ex, docType);
        }
    }
}
