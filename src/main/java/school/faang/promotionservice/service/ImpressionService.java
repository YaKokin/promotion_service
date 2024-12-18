package school.faang.promotionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.model.jpa.Impression;
import school.faang.promotionservice.repository.jpa.ImpressionRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImpressionService {

    private final ImpressionRepository impressionRepository;

    public List<Impression> saveImpressions(List<Impression> impressions) {
        return impressionRepository.saveAll(impressions);
    }
}
