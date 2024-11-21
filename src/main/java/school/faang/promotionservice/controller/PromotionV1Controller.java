package school.faang.promotionservice.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
import school.faang.promotionservice.dto.PromotionResponseDto;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.service.PromotionService;
import school.faang.promotionservice.service.search.UserPromotionSearchService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@Validated
public class PromotionV1Controller {

    private final UserPromotionSearchService userPromotionSearchService;
    private final PromotionService promotionService;

    @GetMapping("/search/users")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> searchPromotedUsers(
            @Parameter(description = "Required resource count")
            @RequestParam @Positive Integer requiredResCount,

            @Parameter(description = "Session id")
            @RequestParam @NotBlank String sessionId,

            @Parameter(description = "User filter for search")
            @RequestBody @Validated UserSearchRequest userSearchRequest
    ) {
        return userPromotionSearchService.searchPromotedUserIds(requiredResCount, sessionId, userSearchRequest);
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
