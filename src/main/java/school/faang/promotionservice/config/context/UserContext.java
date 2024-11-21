package school.faang.promotionservice.config.context;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

    ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    private static final String USER_ID_MISSING_ERROR =
            "User ID is missing. Please make sure 'x-user-id' header is included in the request.";

    public void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    public long getUserId() {
        Long userId = userIdHolder.get();
        if (userId == null) {
            throw new IllegalArgumentException(USER_ID_MISSING_ERROR);
        }
        return userId;
    }

    public void clear() {
        userIdHolder.remove();
    }
}
