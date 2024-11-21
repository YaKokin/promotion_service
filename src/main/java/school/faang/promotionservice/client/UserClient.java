package school.faang.promotionservice.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import school.faang.promotionservice.config.client.FeignConfig;
import school.faang.promotionservice.dto.user.UserSearchResponse;

@FeignClient(
        name = "user-service",
        url = "${user-service.service.url}",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/v1/users/{id}")
    UserSearchResponse getUserById(@PathVariable @Positive long id);
}
