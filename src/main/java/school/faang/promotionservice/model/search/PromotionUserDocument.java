package school.faang.promotionservice.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Data
@Document(indexName = "promotions")
@Setting(settingPath = "elasticsearch/settings.json")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromotionUserDocument {

    @Id
    private Long promotionId;

    @Field(type = FieldType.Long)
    private Long tariffId;

    @Field(type = FieldType.Double)
    private Double priority;

    @Field(type = FieldType.Long)
    private Long userId;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String countryName;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String cityName;

    @Field(type = FieldType.Integer)
    private Integer experience;

    @MultiField(mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            })
    private List<String> skillNames;

    @Field(type = FieldType.Double)
    private Double averageRating;

    public boolean isSamePromotionId(Long promotionId) {
        return this.promotionId.equals(promotionId);
    }
}
