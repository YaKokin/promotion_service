package school.faang.promotionservice.service.search.reindexing.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.model.search.UserPromotionDocument;
import school.faang.promotionservice.repository.search.PromotionUserDocumentRepository;
import school.faang.promotionservice.service.search.reindexing.ReindexService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserReindexService implements ReindexService<UserPromotionDocument, UserSearchResponse> {

    private final PromotionUserDocumentRepository promotionUserDocumentRepository;

    @Override
    public Class<UserPromotionDocument> getDocType() {
        return UserPromotionDocument.class;
    }

    @Override
    public void addToIndex(UserPromotionDocument promotionUserDocument) {
        promotionUserDocumentRepository.save(promotionUserDocument);
    }

    @Override
    public void reindex(UserSearchResponse user) {
        Optional<UserPromotionDocument> promotionUserDocument =
                promotionUserDocumentRepository.findByUserId(user.userId());
        if (promotionUserDocument.isEmpty()) {
            return;
        }
        UserPromotionDocument doc = promotionUserDocument.get();
        Optional.ofNullable(user.country()).ifPresent(doc::setCountryName);
        Optional.ofNullable(user.city()).ifPresent(doc::setCityName);
        Optional.ofNullable(user.skillNames()).ifPresent(doc::setSkillNames);
        Optional.ofNullable(user.experience()).ifPresent(doc::setExperience);
        Optional.ofNullable(user.averageRating()).ifPresent(doc::setAverageRating);

        promotionUserDocumentRepository.save(promotionUserDocument.get());
    }

    @Override
    public void deleteAllFromIndex(List<Long> promotionIds) {
        promotionUserDocumentRepository.deleteByPromotionIdIn(promotionIds);
    }
}
