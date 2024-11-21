package school.faang.promotionservice.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import school.faang.promotionservice.model.jpa.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    boolean existsBySourceIdAndOwnerId(Long sourceId, Long ownerId);
}
