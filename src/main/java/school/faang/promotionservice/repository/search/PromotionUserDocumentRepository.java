package school.faang.promotionservice.repository.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import school.faang.promotionservice.model.search.PromotionUserDocument;
import school.faang.promotionservice.model.search.UserPromotionDocument;

import java.util.List;
import java.util.Optional;

public interface PromotionUserDocumentRepository extends ElasticsearchRepository<PromotionUserDocument, Long> {

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
    List<PromotionUserDocument> findByResourceIdNotIn(List<Long> resourceIds);

    Optional<PromotionUserDocument> findByUserId(Long userId);

    void deleteByPromotionIdIn(List<Long> promotionIdsToRemove);
}
