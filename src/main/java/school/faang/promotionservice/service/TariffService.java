package school.faang.promotionservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.model.jpa.Tariff;
import school.faang.promotionservice.repository.jpa.TariffRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class TariffService {

    private final TariffRepository tariffRepository;

    public Tariff findTariffById(long id) {
        return tariffRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }
}
