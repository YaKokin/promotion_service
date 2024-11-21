package school.faang.promotionservice.mapper;

import org.mapstruct.Mapper;
import school.faang.promotionservice.dto.ResourceResponseDto;
import school.faang.promotionservice.model.jpa.Resource;
import school.faang.promotionservice.model.jpa.ResourceType;
import school.faang.promotionservice.model.search.ResourceNested;

@Mapper(componentModel = "spring")
public interface ResourceMapper {

    ResourceResponseDto toDto(ResourceNested resourceNested);

    ResourceResponseDto toDto(Resource resource);

    default String mapTypeToString(ResourceType resourceType) {
        if (resourceType == null) {
            return "";
        }
        return resourceType.getName();
    }
}
