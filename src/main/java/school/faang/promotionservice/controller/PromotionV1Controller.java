package school.faang.promotionservice.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.faang.promotionservice.builder.SearchQueryBuilder;
import school.faang.promotionservice.dto.PromotionResponseDto;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.PromotionService;
import school.faang.promotionservice.service.search.UserPromotionProcessor;
import school.faang.promotionservice.service.search.filter.impl.ExperienceRangeFilter;
import school.faang.promotionservice.service.search.filter.impl.SkillFuzzyFilter;
import school.faang.promotionservice.service.search.filter.impl.TextMatchFilter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PromotionV1Controller {

    private static final String PROMOTION_USER_INDEX = "promotions";

    private final UserPromotionProcessor userPromotionProcessor;
    private final PromotionService promotionService;
    private final PromotionUserDocumentRepository repository;
    private final ElasticsearchClient elasticsearchClient;

    @PostMapping("/search/users")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> searchPromotedUsers(
            @Parameter(description = "Required resource count")
            @RequestParam @Positive Integer requiredResCount,

            @Parameter(description = "Session id")
            @RequestParam @NotBlank String sessionId,

            @Parameter(description = "User filter for search")
            @RequestBody @Validated UserSearchRequest userSearchRequest
    ) {
        return userPromotionProcessor.searchPromotions(requiredResCount, sessionId, userSearchRequest);
    }

    @GetMapping
    public void test() {
        UserSearchRequest request = new UserSearchRequest(null, null, 1, 100);
        SearchRequest request1 = new SearchQueryBuilder()
                .indexName(PROMOTION_USER_INDEX)
                .addFilter(new TextMatchFilter(request.query()))
                .addFilter(new SkillFuzzyFilter(request.skillNames()))
                .addFilter(new ExperienceRangeFilter(
                        request.experienceFrom(),
                        request.experienceTo()))
                .build();
        SearchResponse<UserPromotionDocument> response;
        try {
            response = elasticsearchClient.search(request1, UserPromotionDocument.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response);
        UserPromotionDocument result = repository.findByUserId(7L).orElse(null);
        log.info(result.toString());
    }

    @PostMapping("/buy/users")
    public PromotionResponseDto buyUserPromotion(
            @Parameter(description = "Tariff Id")
            @RequestParam @Positive long tariffId,

            @Parameter(description = "User id")
            @RequestHeader(value = "x-user-id") @Positive long userId
    ) {

        return promotionService.buyUserPromotion(tariffId, userId);
    }
}
