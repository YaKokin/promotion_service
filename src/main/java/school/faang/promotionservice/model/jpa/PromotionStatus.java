package school.faang.promotionservice.model.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionStatus {
    ACTIVE("active"),
    EXPIRED("expired"),
    PAUSED("paused"),
    ;

    private final String name;
}
