package school.faang.promotionservice.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.builder.SearchQueryBuilder;
import school.faang.promotionservice.config.context.UserContext;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.ImpressionService;
import school.faang.promotionservice.service.PromotionService;
import school.faang.promotionservice.service.cache.AbstractSessionResourceService;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;
import school.faang.promotionservice.service.search.filter.impl.ExperienceRangeFilter;
import school.faang.promotionservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.promotionservice.service.search.filter.impl.TextMatchFilter;
import school.faang.promotionservice.service.search.reindexing.ReindexService;
import school.faang.promotionservice.utils.WeightedRandomSelector;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Service
public class UserPromotionProcessor extends ResourcePromotionProcessor<UserPromotionDocument> {

    private static final String PROMOTION_USER_INDEX = "promotions";

    @Autowired
    public UserPromotionProcessor(
            ImpressionCounterService impressionCounterService,
            PromotionService promotionService,
            ElasticsearchClient elasticsearchClient,
            ExecutorService defaultThreadPool,
            UserContext userContext,
            ImpressionService impressionService,
            WeightedRandomSelector<UserPromotionDocument> weightedRandomSelector,
            ReindexService<UserPromotionDocument, UserSearchResponse> reindexService,
            AbstractSessionResourceService<UserPromotionDocument> sessionResourceService,
            PriorityCalculator priorityCalculator,
            PromotionUserDocumentRepository promotionUserDocumentRepository) {

        super(impressionCounterService,
                promotionService,
                elasticsearchClient,
                defaultThreadPool,
                userContext,
                impressionService,
                weightedRandomSelector,
                reindexService,
                sessionResourceService,
                priorityCalculator,
                promotionUserDocumentRepository);
    }

    public List<Long> searchPromotions(Integer limit, String sessionId, UserSearchRequest userSearchRequest) {
        SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder()
                .indexName(PROMOTION_USER_INDEX)
                .addFilter(new TextMatchFilter(userSearchRequest.query()))
                .addFilter(new SkillFuzzyFilter(userSearchRequest.skillNames()))
                .addFilter(new ExperienceRangeFilter(
                        userSearchRequest.experienceFrom(),
                        userSearchRequest.experienceTo()));


        List<Long> result = searchPromotedUserIds(limit, sessionId, UserPromotionDocument.class, searchQueryBuilder);
        return result;
    }
}
