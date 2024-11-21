package school.faang.promotionservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import school.faang.promotionservice.dto.PromotionResponseDto;
import school.faang.promotionservice.model.jpa.Promotion;
import school.faang.promotionservice.model.jpa.PromotionStatus;

@Mapper(componentModel = "spring", uses = ResourceMapper.class, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PromotionMapper {

    @Mapping(source = "promotion.tariff.id", target = "tariffId")
    PromotionResponseDto toResponseDto(Promotion promotion);

    default String mapPromotionStatusToString(PromotionStatus promotionStatus) {
        if (promotionStatus == null) {
            return "";
        }
        return promotionStatus.getName();
    }
}
