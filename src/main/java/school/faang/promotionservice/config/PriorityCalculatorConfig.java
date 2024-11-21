package school.faang.promotionservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;
import school.faang.promotionservice.service.priority.calulator.impl.TariffAndImpressionsPriorityCalculator;

@Configuration
public class PriorityCalculatorConfig {

    @Bean
    public PriorityCalculator priorityCalculator() {
        return new TariffAndImpressionsPriorityCalculator();
    }
}
