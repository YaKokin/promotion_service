package school.faang.promotionservice.dto;

public record PromotionResponseDto(
        long id,
        ResourceResponseDto resource,
        long tariffId,
        int remainingImpressions,
        String status
) {
}
