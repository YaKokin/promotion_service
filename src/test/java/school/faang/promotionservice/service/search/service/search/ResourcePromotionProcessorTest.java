package school.faang.promotionservice.service.search.service.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.util.ReflectionTestUtils;
import school.faang.promotionservice.builder.SearchQueryBuilder;
import school.faang.promotionservice.config.context.UserContext;
import school.faang.promotionservice.dto.user.UserSearchRequest;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.exception.SearchServiceExceptions;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.search.PromotionDocument;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.ImpressionService;
import school.faang.promotionservice.service.PromotionService;
import school.faang.promotionservice.service.cache.AbstractSessionResourceService;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;
import school.faang.promotionservice.service.search.ResourcePromotionProcessor;
import school.faang.promotionservice.service.search.UserPromotionProcessor;
import school.faang.promotionservice.service.search.reindexing.ReindexService;
import school.faang.promotionservice.utils.WeightedRandomSelector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourcePromotionProcessorTest {

    @Mock
    private ImpressionCounterService impressionCounterService;

    @Mock
    private PromotionService promotionService;

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private ExecutorService defaultThreadPool;

    @Mock
    private UserContext userContext;

    @Mock
    private ImpressionService impressionService;

    @Mock
    private WeightedRandomSelector<UserPromotionDocument> weightedRandomSelector;

    @Mock
    private ReindexService<UserPromotionDocument, UserSearchResponse> reindexService;

    @Mock
    private AbstractSessionResourceService<UserPromotionDocument> sessionResourceService;

    @Mock
    private PriorityCalculator priorityCalculator;

    @Mock
    private PromotionUserDocumentRepository promotionDocRepository;

    @InjectMocks
    private UserPromotionProcessor processor;

    private String sessionId;
    private UserPromotionDocument promotionDocument1;
    private UserPromotionDocument promotionDocument2;
    private Hit<UserPromotionDocument> hit1;
    private Hit<UserPromotionDocument> hit2;
    private List<Hit<UserPromotionDocument>> hits;
    private HitsMetadata<UserPromotionDocument> hitsMetadata1;
    private SearchResponse<UserPromotionDocument> searchResponse1;
    private List<UserPromotionDocument> promotionDocuments;
    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        sessionId = "test-session";
        promotionDocument1 = UserPromotionDocument.builder()
                .promotionId(1L)
                .resourceId(1L)
                .priority(0.7)
                .build();
        promotionDocument2 = UserPromotionDocument.builder()
                .promotionId(2L)
                .resourceId(2L)
                .priority(0.2)
                .build();
        promotionDocuments = List.of(promotionDocument1, promotionDocument2);

        hit1 = new Hit.Builder<UserPromotionDocument>()
                .id("id")
                .index("index")
                .source(promotionDocument1)
                .build();
        hit2 = new Hit.Builder<UserPromotionDocument>()
                .id("id")
                .index("index")
                .source(promotionDocument2)
                .build();
        hits = Arrays.asList(hit1, hit2);

        hitsMetadata1 = new HitsMetadata.Builder<UserPromotionDocument>()
                .hits(hits)
                .build();
        searchResponse1 = new SearchResponse.Builder<UserPromotionDocument>()
                .hits(hitsMetadata1)
                .took(1000L)
                .timedOut(true)
                .shards(new ShardStatistics.Builder()
                        .failed(0)
                        .successful(1)
                        .total(1)
                        .build())
                .build();
    }

    @Test
    void searchPromotedUserIds_ShouldReturnResourceIds() throws IOException {
        int limit = 5;
        Class<UserPromotionDocument> docType = UserPromotionDocument.class;
        SearchQueryBuilder searchQueryBuilder = Mockito.mock(SearchQueryBuilder.class);
        Map<Long, Integer> updatedCounters = Map.of(1L, 2, 2L, -1);

        SearchRequest searchRequest = Mockito.mock(SearchRequest.class);
        List<Long> viewedResources = List.of(1L);

        when(sessionResourceService.getViewedUsers(sessionId)).thenReturn(viewedResources);
        when(searchQueryBuilder.build()).thenReturn(searchRequest);

        doReturn(promotionDocuments).when(weightedRandomSelector).selectWeightedRandomElements(eq(limit), anyList(), any());
        doReturn(updatedCounters).when(impressionCounterService).decrementPromotionCounters(anyList());
        doReturn(searchResponse1).when(elasticsearchClient).search(searchRequest, UserPromotionDocument.class);

        List<Long> result = processor.searchPromotedUserIds(limit, sessionId, docType, searchQueryBuilder);

        assertEquals(List.of(1L, 2L), result);
        verify(sessionResourceService).getViewedUsers(sessionId);
        verify(impressionCounterService).decrementPromotionCounters(anyList());
    }
}
