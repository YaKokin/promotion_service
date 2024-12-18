package school.faang.promotionservice.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.service.search.UserPromotionSearchService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileUpdateConsumer {

    private final UserPromotionSearchService userPromotionSearchService;

    @KafkaListener(topics = "${spring.kafka.topics.update-user-topic.name}")
    public void handle(UserSearchResponse message) {
        log.info("Received message: {}", message);
        userPromotionSearchService.reindex(message);
    }
}
