package school.faang.promotionservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.RequiredArgsConstructor;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.exception.SearchServiceExceptions;
import school.faang.promotionservice.model.search.PromotionUserDocument;
import school.faang.promotionservice.service.search.filter.impl.ExcludeViewedUsersFilter;
import school.faang.promotionservice.service.search.filter.impl.ExperienceRangeFilter;
import school.faang.promotionservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.promotionservice.service.search.filter.impl.TextMatchFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public abstract class PromotionResourceProcessor {

    private final String promotionResourceIndex;
    private final ElasticsearchClient elasticsearchClient;

    private List<PromotionUserDocument> searchPromotionsByFilters(UserSearchRequest searchRequest,
                                                                  List<Long> excludedUserIds) {

        SearchRequest request = new UserSearchQueryBuilder()
                .indexName(promotionResourceIndex)
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
}
