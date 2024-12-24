package school.faang.promotionservice.service.search.service.search.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import school.faang.promotionservice.service.cache.ImpressionCounterService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImpressionCounterServiceTest {

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOperations;

    @Mock
    private DefaultRedisScript<Long> decrementScript;

    @InjectMocks
    private ImpressionCounterService impressionCounterService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void setPromotionCounter_ShouldSetValueInRedis() {
        Long promotionId = 1L;
        Integer remainingImpressions = 100;
        String expectedKey = "promotion:1";

        impressionCounterService.setPromotionCounter(promotionId, remainingImpressions);

        verify(valueOperations).set(expectedKey, remainingImpressions);
    }

    @Test
    void getPromotionCounter_ShouldReturnValueFromRedis() {
        Long promotionId = 1L;
        Integer expectedValue = 100;
        String expectedKey = "promotion:1";

        when(valueOperations.get(expectedKey)).thenReturn(expectedValue);

        Integer result = impressionCounterService.getPromotionCounter(promotionId);

        assertEquals(expectedValue, result);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void increment_ShouldIncrementValueInRedis() {
        Long promotionId = 1L;
        String expectedKey = "promotion:1";
        Long expectedValue = 101L;

        when(valueOperations.increment(expectedKey)).thenReturn(expectedValue);

        Long result = impressionCounterService.increment(promotionId);

        assertEquals(expectedValue, result);
        verify(valueOperations).increment(expectedKey);
    }
}
