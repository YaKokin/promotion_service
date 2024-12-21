package school.faang.promotionservice.service.priority.calulator;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import school.faang.promotionservice.model.jpa.Promotion;

@Component
public interface PriorityCalculator {
    Double calculate(@NonNull Promotion promotion);
}
