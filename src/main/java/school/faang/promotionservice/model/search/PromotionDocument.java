package school.faang.promotionservice.model.search;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class PromotionDocument {

    @Id
    private Long promotionId;

    @Field(type = FieldType.Long)
    private Long tariffId;

    @Field(type = FieldType.Double)
    private Double priority;

    @Field(type = FieldType.Long)
    private Long resourceId;

    public boolean isSamePromotionId(Long promotionId) {
        return this.promotionId.equals(promotionId);
    }
}
