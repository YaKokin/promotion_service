package school.faang.promotionservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GoalSearchResponse(
        Long goalId,
        String title,
        String description,
        //TODO подумать над тем, что передавать прямо enum
        String status,
        LocalDateTime deadLine,
        LocalDateTime createdAt,
        List<String> skillsToAchieveNames
) {
}
