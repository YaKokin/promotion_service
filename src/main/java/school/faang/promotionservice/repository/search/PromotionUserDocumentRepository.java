package school.faang.promotionservice.repository.search;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import school.faang.promotionservice.model.search.PromotionUserDocument;

import java.util.List;

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

    void deleteByPromotionIdIn(List<Long> promotionIdsToRemove);
}
