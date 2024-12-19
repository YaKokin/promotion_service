package school.faang.promotionservice.service.search.reindexing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.search.reindexing.ReindexService;

@Service
@RequiredArgsConstructor
public class UserReindexService implements ReindexService<UserPromotionDocument, UserSearchResponse> {

    private final PromotionUserDocumentRepository promotionUserDocumentRepository;

    @Override
    public Class<UserPromotionDocument> getDocType() {
        return UserPromotionDocument.class;
    }

    @Override
    public void index(UserPromotionDocument promotionUserDocument) {
        promotionUserDocumentRepository.save(promotionUserDocument);
    }

    @Override
    public void reindex(UserSearchResponse reindexEvent) {

    }
}
