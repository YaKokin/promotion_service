package school.faang.promotionservice.utils;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

@Service
public class WeightedRandomSelector<T> {

    public List<T> selectWeightedRandomElements(Integer requiredCount,
                                                           List<T> items,
                                                           Function<T, Double> priorityExtractor) {

        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        List<T> selectedItems = new ArrayList<>();
        List<T> availableItems = new ArrayList<>(items);

        for (int i = 0; i < requiredCount && !availableItems.isEmpty(); i++) {
            T randomItem = selectWeightedRandom(availableItems, priorityExtractor);
            if (randomItem != null) {
                selectedItems.add(randomItem);
                availableItems.remove(randomItem);
            }
        }
        return selectedItems;
    }

    private T selectWeightedRandom(List<T> items, Function<T, Double> function) {
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }
        Double totalWeight = items.stream()
                .map(function)
                .reduce(0.0, Double::sum);
        double random = ThreadLocalRandom.current().nextDouble(totalWeight);
        double cumulativeWeight = 0;
        for (T item : items) {
            cumulativeWeight += function.apply(item);
            if (cumulativeWeight > random) {
                return item;
            }
        }
        return null;
    }
}
