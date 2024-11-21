package school.faang.promotionservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.exception.SearchServiceExceptions;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.PromotionStatus;
import school.faang.promotionservice.model.search.PromotionUserDocument;
import school.faang.promotionservice.repository.jpa.PromotionRepository;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.cache.SessionResourceService;
import school.faang.promotionservice.service.search.filter.impl.ExcludeViewedUsersFilter;
import school.faang.promotionservice.service.search.filter.impl.ExperienceRangeFilter;
import school.faang.promotionservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.promotionservice.service.search.filter.impl.TextMatchFilter;
import school.faang.promotionservice.utils.WeightedRandomSelection;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPromotionSearchService {

    private static final String PROMOTION_USER_INDEX = "promotions";
    private static final String SEARCHING_USERS_ERROR = "Error while searching users";

    private final SessionResourceService sessionResourceService;
    private final ImpressionCounterService impressionCounterService;
    private final PromotionUserDocumentRepository promotionUserDocumentRepository;
    private final PromotionRepository promotionRepository;
    private final ElasticsearchClient elasticsearchClient;

    public List<Long> searchPromotedUserIds(Integer limit,
                                            String sessionId,
                                            UserSearchRequest userSearchRequest
    ) {

        List<PromotionUserDocument> promotionsForDisplay =
                getPromotionsForDisplay(limit, sessionId, userSearchRequest);

        Map<Long, Long> updatedPromotionCounters = decrementCounters(promotionsForDisplay);
        removePromotionDocIfCounterIsNegative(updatedPromotionCounters, promotionsForDisplay);

        List<Long> promotionIdsToRemove = removeExpiredPromotionsFromIndex(updatedPromotionCounters);

        //TODO продумать сохранение Impression

        //TODO обеспечить асинхронное выполнение деактивации
        deactivatePromotions(promotionIdsToRemove);
        return promotionsForDisplay.stream()
                .map(PromotionUserDocument::getUserId)
                .toList();
    }

    private List<Long> removeExpiredPromotionsFromIndex(Map<Long, Long> updatedPromotionCounters) {
        List<Long> promotionIdsToRemove = updatedPromotionCounters.keySet().stream()
                .filter(promotionId -> promotionId <= 0)
                .toList();
        promotionUserDocumentRepository.deleteByPromotionIdIn(promotionIdsToRemove);
        return promotionIdsToRemove;
    }

    private Map<Long, Long> decrementCounters(List<PromotionUserDocument> promotionsForDisplay) {
        List<Long> promotionIds = promotionsForDisplay.stream()
                .map(PromotionUserDocument::getPromotionId)
                .toList();
        return impressionCounterService.decrementPromotionCounters(promotionIds);
    }

    private void deactivatePromotions(List<Long> promotionIdsToRemove) {
        List<Promotion> promotions = promotionRepository.findByIdIn(promotionIdsToRemove);
        promotions.forEach(promotion -> promotion.setPromotionStatus(PromotionStatus.EXPIRED));
        promotionRepository.saveAll(promotions);
    }

    private static void removePromotionDocIfCounterIsNegative(Map<Long, Long> updatedPromotionCounters,
                                                              List<PromotionUserDocument> promotionsForDisplay) {
        updatedPromotionCounters.entrySet().stream()
                .filter(entry -> entry.getValue() < 0)
                .forEach(entry -> promotionsForDisplay.removeIf(
                        promotionDoc -> promotionDoc.isSamePromotionId(entry.getKey()))
                );
    }

    private List<PromotionUserDocument> getPromotionsForDisplay(Integer limit,
                                                                String sessionId,
                                                                UserSearchRequest userSearchRequest) {

        List<Long> viewedUserIds = sessionResourceService.getViewedUsers(sessionId);

        List<PromotionUserDocument> activePromotions = searchPromotionsByFilters(userSearchRequest, viewedUserIds);

        return WeightedRandomSelection.selectWeightedRandomElements(
                limit,
                activePromotions,
                PromotionUserDocument::getPriority);
    }

    private List<PromotionUserDocument> searchPromotionsByFilters(UserSearchRequest searchRequest,
                                                                  List<Long> excludedUserIds) {

        SearchRequest request = new UserSearchQueryBuilder()
                .indexName(PROMOTION_USER_INDEX)
                .addFilter(new ExcludeViewedUsersFilter(excludedUserIds))
                .addFilter(new TextMatchFilter(searchRequest.query()))
                .addFilter(new SkillFuzzyFilter(searchRequest.skillNames()))
                .addFilter(new ExperienceRangeFilter(searchRequest.experienceFrom(), searchRequest.experienceTo()))
                .build();

        try {
            SearchResponse<PromotionUserDocument> response = elasticsearchClient.search(request, PromotionUserDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();
        } catch (IOException e) {
            log.error(SEARCHING_USERS_ERROR, e);
            throw new SearchServiceExceptions(SEARCHING_USERS_ERROR, e);
        }
    }

    public void index(PromotionUserDocument promotionUserDocument) {

    }
}
