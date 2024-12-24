package school.faang.promotionservice.dto;

import school.faang.promotionservice.model.jpa.PromotionStatus;

public record PromotionResponseDto(
        long id,
        ResourceResponseDto resource,
        long tariffId,
        int remainingImpressions,
        PromotionStatus status
) {
}
