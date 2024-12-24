package school.faang.promotionservice.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import school.faang.promotionservice.model.jpa.Impression;

public interface ImpressionRepository extends JpaRepository<Impression, Long> {
}
