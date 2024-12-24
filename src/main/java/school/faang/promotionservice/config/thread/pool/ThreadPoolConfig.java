package school.faang.promotionservice.config.thread.pool;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Data
public class ThreadPoolConfig {

    public static final String DEFAULT_THREAD_POOL_BEAN_NAME = "default-thread-pool";

    @Value("${thread-pool.default-thread-pool.num-of-thread}")
    private int numOfThreads;

    @Bean( DEFAULT_THREAD_POOL_BEAN_NAME)
    public ExecutorService defaultThreadPool() {
        return Executors.newFixedThreadPool(numOfThreads);
    }
}
