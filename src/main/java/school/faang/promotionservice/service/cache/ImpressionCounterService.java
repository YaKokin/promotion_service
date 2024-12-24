package school.faang.promotionservice.service.cache;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpressionCounterService {

    private final RedisTemplate<String, Integer> redisTemplate;
    private final DefaultRedisScript<Long> decrementScript;

    private static final String KEY_PREFIX = "promotion";

    public void setPromotionCounter(Long promotionId, Integer remainingImpression) {
        String key = toKey(promotionId);
        redisTemplate.opsForValue().set(key, remainingImpression);
    }

    public Integer getPromotionCounter(Long promotionId) {
        String key = toKey(promotionId);
        return redisTemplate.opsForValue().get(key);
    }

    public Long increment(Long promotionId) {
        String key = toKey(promotionId);
        return redisTemplate.opsForValue().increment(key);
    }

    private Integer decrement(@Positive Long promotionId) {
        String key = toKey(promotionId);
        return Math.toIntExact(redisTemplate.execute(decrementScript, Collections.singletonList(key)));
    }

    public Map<Long, Integer> decrementPromotionCounters(List<Long> promotionIds) {
        if (promotionIds == null) {
            throw new IllegalArgumentException("promotionIds cannot be null");
        }
        List<Integer> results = promotionIds.stream()
                .map(this::decrement)
                .toList();

        return promotionIds.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> results.get(promotionIds.indexOf(key))
                ));
    }

    private String toKey(Long promotionId) {
        return String.format("%s:%s", KEY_PREFIX, promotionId);
    }
}
