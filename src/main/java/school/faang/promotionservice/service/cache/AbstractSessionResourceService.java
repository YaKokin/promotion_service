package school.faang.promotionservice.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public abstract class AbstractSessionResourceService {

    private final RedisTemplate<String, Long> redisTemplate;

    private static final String KEY_PREFIX = "session";

    private final String resourcePrefix;

    public List<Long> getViewedUsers(String sessionId) {
        String key = toUserKey(sessionId);
        Set<Long> viewedResourceIds = redisTemplate.opsForSet().members(key);
        return viewedResourceIds == null ? new ArrayList<>()
                : new ArrayList<>(viewedResourceIds);
    }

    private String toUserKey(String sessionId) {
        return String.format("%s:%s:%s", KEY_PREFIX, resourcePrefix, sessionId);
    }
}
