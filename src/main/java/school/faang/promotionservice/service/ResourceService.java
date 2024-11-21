package school.faang.promotionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.repository.jpa.ResourceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public boolean existsBySourceIdAndOwnerId(long sourceId, long ownerId) {
        return resourceRepository.existsBySourceIdAndOwnerId(sourceId, ownerId);
    }
}
