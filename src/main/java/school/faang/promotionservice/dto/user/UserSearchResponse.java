package school.faang.promotionservice.dto.user;

import school.faang.promotionservice.dto.EventSearchResponse;
import school.faang.promotionservice.dto.GoalSearchResponse;

import java.util.List;

public record UserSearchResponse(
        Long userId,
        String username,
        String country,
        String city,
        Integer experience,
        List<GoalSearchResponse> goals,
        List<String> skillNames,
        List<EventSearchResponse> events,
        Double averageRating
) {
}
