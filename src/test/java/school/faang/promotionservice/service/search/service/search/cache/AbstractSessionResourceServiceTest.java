package school.faang.promotionservice.service.search.service.search.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.service.cache.AbstractSessionResourceService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractSessionResourceServiceTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private SetOperations<String, Long> setOperations;

    private AbstractSessionResourceService<UserPromotionDocument> sessionResourceService;

    private static final String TEST_RESOURCE_PREFIX = "test";

    @BeforeEach
    void setUp() {
        sessionResourceService = new AbstractSessionResourceService<>(redisTemplate, TEST_RESOURCE_PREFIX) {};
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void getViewedUsers_WhenUsersExist_ShouldReturnList() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";
        Set<Long> mockViewedUsers = new HashSet<>(Arrays.asList(1L, 2L, 3L));

        when(setOperations.members(expectedKey)).thenReturn(mockViewedUsers);

        List<Long> result = sessionResourceService.getViewedUsers(sessionId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1L, 2L, 3L)));
        verify(redisTemplate).opsForSet();
        verify(setOperations).members(expectedKey);
    }

    @Test
    void getViewedUsers_WhenNoUsers_ShouldReturnEmptyList() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";

        when(setOperations.members(expectedKey)).thenReturn(null);

        List<Long> result = sessionResourceService.getViewedUsers(sessionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate).opsForSet();
        verify(setOperations).members(expectedKey);
    }

    @Test
    void getViewedUsers_WithEmptySet_ShouldReturnEmptyList() {
        String sessionId = "test-session";
        String expectedKey = "session:test:test-session";

        when(setOperations.members(expectedKey)).thenReturn(Collections.emptySet());

        List<Long> result = sessionResourceService.getViewedUsers(sessionId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(redisTemplate).opsForSet();
        verify(setOperations).members(expectedKey);
    }
}
