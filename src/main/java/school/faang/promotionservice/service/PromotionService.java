package school.faang.promotionservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.faang.promotionservice.client.PaymentClient;
import school.faang.promotionservice.client.UserClient;
import school.faang.promotionservice.dto.PromotionResponseDto;
import school.faang.promotionservice.dto.payment.Currency;
import school.faang.promotionservice.dto.payment.PaymentRequest;
import school.faang.promotionservice.dto.user.UserSearchResponse;
import school.faang.promotionservice.exception.DataValidationException;
import school.faang.promotionservice.mapper.PromotionMapper;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.PromotionStatus;
import school.faang.promotionservice.model.jpa.Resource;
import school.faang.promotionservice.model.jpa.ResourceType;
import school.faang.promotionservice.model.jpa.Tariff;
import school.faang.promotionservice.model.search.PromotionUserDocument;
import school.faang.promotionservice.repository.jpa.PromotionRepository;
import school.faang.promotionservice.service.cache.ImpressionCounterService;
import school.faang.promotionservice.service.priority.calulator.PriorityCalculator;
import school.faang.promotionservice.service.search.UserPromotionSearchService;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final TariffService tariffService;
    private final UserPromotionSearchService userPromotionSearchService;
    private final ImpressionCounterService impressionCounterService;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final PriorityCalculator priorityCalculator;
    private final PromotionMapper promotionMapper;

    private static final String USER_IS_ALREADY_PROMOTED_ERROR = "The user is already promoted";

    @Transactional
    public PromotionResponseDto buyUserPromotion(long tariffId, long userId) {
        Tariff tariff = tariffService.findTariffById(tariffId);
        UserSearchResponse user = userClient.getUserById(userId);

        Promotion promotion = createOrActivatePromotion(userId, tariff);

        int paymentNumber = Objects.hash(tariffId, userId);
        sendPaymentRequest(paymentNumber, tariff);

        promotionRepository.save(promotion);
        impressionCounterService.setPromotionCounter(promotion.getId(), (long) promotion.getRemainingImpressions());
        userPromotionSearchService.index(buildPromotionUserDoc(promotion, user));

        return promotionMapper.toResponseDto(promotion);
    }

    private Promotion createOrActivatePromotion(long userId, Tariff tariff) {
        Promotion promotion = promotionRepository.findBySourceIdAndOwnerId(userId, userId)
                .orElse(null);
        if (promotion != null) {
            validatePromotionForActive(promotion);

            promotion.setPromotionStatus(PromotionStatus.ACTIVE);
            promotion.setRemainingImpressions(tariff.getTotalImpressions());
            promotion.setTariff(tariff);

            return promotion;
        }
        return buildUserPromotion(userId, tariff);
    }

    private static void validatePromotionForActive(Promotion promotion) {
        if (promotion.isActive()) {
            throw new DataValidationException(USER_IS_ALREADY_PROMOTED_ERROR);
        }
    }

    private PromotionUserDocument buildPromotionUserDoc(Promotion promotion, UserSearchResponse user) {
        return PromotionUserDocument.builder()
                .promotionId(promotion.getId())
                .tariffId(promotion.getTariffId())
                .priority(priorityCalculator.calculate(promotion))
                .userId(user.userId())
                .countryName(user.country())
                .cityName(user.city())
                .experience(user.experience())
                .skillNames(user.skillNames())
                .averageRating(user.averageRating())
                .build();
    }

    private Promotion buildUserPromotion(long userId, Tariff tariff) {
        Resource resource = Resource.builder()
                .sourceId(userId)
                .resourceType(ResourceType.USER)
                .ownerId(userId)
                .build();

        return Promotion.builder()
                .resource(resource)
                .tariff(tariff)
                .remainingImpressions(tariff.getTotalImpressions())
                .promotionStatus(PromotionStatus.ACTIVE)
                .build();
    }

    private void sendPaymentRequest(int paymentNumber, Tariff tariff) {
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentNumber(paymentNumber)
                .amount(BigDecimal.valueOf(tariff.getPrice()))
                .currency(Currency.USD)
                .build();
        paymentClient.sendPayment(paymentRequest);
    }
}
