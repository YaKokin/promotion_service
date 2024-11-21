package school.faang.promotionservice.service.priority.calulator.impl;

import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.Tariff;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;

public class TariffAndImpressionsPriorityCalculator implements PriorityCalculator {

    @Override
    public Double calculate(Promotion promotion) {
        Tariff tariff = promotion.getTariff();
        return tariff.getPrice() * promotion.getRemainingImpressions();
    }
}
