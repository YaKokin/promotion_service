package school.faang.promotionservice.service.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.model.search.PromotionDocument;
import school.faang.promotionservice.model.search.UserPromotionDocument;

@Service
public class SessionUserService extends AbstractSessionResourceService<UserPromotionDocument> {

    private static final String USER_PREFIX = "user";

    @Autowired
    public SessionUserService(RedisTemplate<String, Long> redisTemplate) {
        super(redisTemplate, USER_PREFIX);
    }

    @Override
    public Class<? extends PromotionDocument> getDocType() {
        return UserPromotionDocument.class;
    }

    @Override
    public boolean isSameDocType(Class<? extends PromotionDocument> otherDocType) {
        return getDocType().equals(otherDocType);
    }
}
