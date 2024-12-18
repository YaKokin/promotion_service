package school.faang.promotionservice.config.kafka;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.topics")
@Data
public class KafkaTopicsProps {

    private Topic updateUserTopic;
    private Topic deactivatePromotionTopic;

    @Data
    public static class Topic {
        private String name;
        private Integer partitions;
        private Integer replicationFactor;
    }
}
