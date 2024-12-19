package school.faang.promotionservice.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionUserService extends AbstractSessionResourceService {

    private static final String USER_PREFIX = "user";

    @Autowired
    public SessionUserService(RedisTemplate<String, Long> redisTemplate) {
        super(redisTemplate, USER_PREFIX);
    }
}
