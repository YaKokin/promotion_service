package school.faang.promotionservice.service.search.service.search.reindexing.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.search.reindexing.impl.UserReindexService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserReindexServiceTest {

    @Mock
    private PromotionUserDocumentRepository promotionUserDocumentRepository;

    @InjectMocks
    private UserReindexService userReindexService;

    private Long userId;
    private UserSearchResponse response;

    @BeforeEach
    void setUp() {
        userId = 1L;
        response = UserSearchResponse.builder()
                .userId(userId)
                .username("John")
                .country("USA")
                .city("New York")
                .skillNames(Arrays.asList("Java", "Python", "C++"))
                .build();
    }

    @Test
    void getDocType_ShouldReturnCorrectClass() {
        Class<UserPromotionDocument> result = userReindexService.getDocType();

        assertEquals(UserPromotionDocument.class, result);
    }

    @Test
    void addToIndex_ShouldSaveDocument() {
        UserPromotionDocument document = new UserPromotionDocument();

        userReindexService.addToIndex(document);

        verify(promotionUserDocumentRepository).save(document);
    }

    @Test
    void reindex_WhenDocumentExists_ShouldUpdateAndSave() {
        Long userId = 1L;
        UserSearchResponse response = UserSearchResponse.builder()
                .userId(userId)
                .username("John")
                .country("USA")
                .city("New York")
                .skillNames(Arrays.asList("Java", "Python", "C++"))
                .build();

        UserPromotionDocument existingDocument = new UserPromotionDocument();
        when(promotionUserDocumentRepository.findByUserId(userId))
                .thenReturn(Optional.of(existingDocument));

        userReindexService.reindex(response);

        verify(promotionUserDocumentRepository).findByUserId(userId);
        verify(promotionUserDocumentRepository).save(existingDocument);

        assertEquals("USA", existingDocument.getCountryName());
        assertEquals("New York", existingDocument.getCityName());
        assertEquals(List.of("Java", "Python", "C++"), existingDocument.getSkillNames());
    }

    @Test
    void reindex_WhenDocumentDoesNotExist_ShouldNotSave() {

        when(promotionUserDocumentRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        userReindexService.reindex(response);

        verify(promotionUserDocumentRepository).findByUserId(userId);
        verify(promotionUserDocumentRepository, never()).save(any());
    }
}
