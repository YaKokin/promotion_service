package school.faang.promotionservice.dto.user;

import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;

@Builder
public record UserSearchRequest(
        String query,
        List<String> skillNames,
        @Positive Integer experienceFrom,
        @Positive Integer experienceTo
) {
    public boolean queryIsNotBlank() {
        return query != null && !query.isBlank();
    }

    public boolean skillNamesIsNotEmpty() {
        return skillNames != null && !skillNames.isEmpty();
    }

    public boolean expBoundsIsNotNull() {
        return experienceFrom != null && experienceTo != null;
    }

    public boolean anyExpBoundIsNotNull() {
        return experienceFrom != null || experienceTo != null;
    }
}