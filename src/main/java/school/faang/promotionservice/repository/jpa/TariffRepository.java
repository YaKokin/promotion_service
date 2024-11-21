package school.faang.promotionservice.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import school.faang.promotionservice.model.jpa.Tariff;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
}
