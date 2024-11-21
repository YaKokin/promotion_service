package school.faang.promotionservice.repository.jpa;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.faang.promotionservice.model.jpa.Promotion;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByIdIn(List<Long> promotionIds);

    @Query(nativeQuery = true,
            value = """
                    SELECT promotion.*
                    FROM promotion
                             JOIN resource ON promotion.resource_id = resource.id
                    WHERE resource.source_id = :sourceId
                      AND resource.owner_id = :ownerId
                    LIMIT 1;
                    """)
    Optional<Promotion> findBySourceIdAndOwnerId(@Param("sourceOd") Long sourceId, @Param("ownerId") Long ownerId);
}
