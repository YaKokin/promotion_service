package school.faang.promotionservice.model.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    USER("user"),
    EVENT("event"),
    PROJECT("project");

    private final String name;
}
