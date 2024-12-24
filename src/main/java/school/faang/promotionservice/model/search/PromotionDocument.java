package school.faang.promotionservice.model.search;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@SuperBuilder
public class PromotionDocument {

    @Id
    protected Long promotionId;

    @Field(type = FieldType.Long)
    protected Long tariffId;

    @Field(type = FieldType.Double)
    protected Double priority;

    @Field(type = FieldType.Long)
    protected Long resourceId;

    public boolean isSamePromotionId(Long promotionId) {
        return this.promotionId.equals(promotionId);
    }
}
