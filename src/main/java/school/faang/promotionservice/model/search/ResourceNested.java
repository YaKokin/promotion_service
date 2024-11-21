package school.faang.promotionservice.model.search;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class ResourceNested {

    @Field(type = FieldType.Long)
    private long resourceId;

    @Field(type = FieldType.Text)
    private String resourceType;

    @Field(type = FieldType.Long)
    private long ownerId;
}
