package school.faang.promotionservice.repository.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import school.faang.promotionservice.model.search.UserPromotionDocument;

import java.util.List;
import java.util.Optional;

public interface PromotionUserDocumentRepository extends ElasticsearchRepository<UserPromotionDocument, Long> {

    @Query("""
                {
                  "bool": {
                    "must_not": {
                      "terms": {
                        "resource.id": ?0
                      }
                    }
                  }
                }
            """)
    List<UserPromotionDocument> findByResourceIdNotIn(List<Long> resourceIds);

    Optional<UserPromotionDocument> findByUserId(Long userId);

    void deleteByPromotionIdIn(List<Long> promotionIdsToRemove);
}
