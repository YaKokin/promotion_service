package school.faang.promotionservice.dto.user;

import java.util.List;

public record UserPromotionCreateDto(
        Long userId,
        String countryName,
        String cityName,
        Integer experience,
        List<String> skillNames,
        Double average
) {
}
