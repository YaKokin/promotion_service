package school.faang.promotionservice.config.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.nio.file.Files;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.scripts.decrement-counter.path}")
    private String decrementScriptPath;

    @Bean
    public RedisTemplate<String, Long> longRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

        template.setDefaultSerializer(new GenericToStringSerializer<>(Long.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, Integer> integerRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        template.setDefaultSerializer(new GenericToStringSerializer<>(Integer.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public DefaultRedisScript<Long> decrementScript() throws IOException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(loadScript(decrementScriptPath));
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    private String loadScript(String scriptPath) throws IOException {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        return new String(Files.readAllBytes(resource.getFile().toPath()));
    }
}
