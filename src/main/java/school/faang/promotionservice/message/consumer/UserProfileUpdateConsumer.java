package school.faang.promotionservice.message.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.service.search.reindexing.impl.UserReindexService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileUpdateConsumer {

    private final UserReindexService userReindexService;

    @KafkaListener(topics = "${spring.kafka.topics.update-user-topic.name}")
    public void handle(UserSearchResponse message) {
        log.info("Received message: {}", message);
        userReindexService.reindex(message);
    }
}
