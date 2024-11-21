package school.faang.promotionservice.dto;

public record ResourceResponseDto(
        Long resourceId,
        Long sourceId,
        String resourceType,
        Long ownerId
) {
}
