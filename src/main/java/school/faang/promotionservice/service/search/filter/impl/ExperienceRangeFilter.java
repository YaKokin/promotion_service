package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import school.faang.promotionservice.service.search.filter.Filter;

@RequiredArgsConstructor
public class ExperienceRangeFilter implements Filter {

    private final Integer experienceFrom;
    private final Integer experienceTo;

    private static final String EXPERIENCE_FIELD = "experience";

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (anyExpBoundIsNotNull()) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder()
                    .field(EXPERIENCE_FIELD);
            if (experienceFrom != null) {
                rangeQuery.gte(JsonData.of(experienceFrom));
            }
            if (experienceTo != null) {
                rangeQuery.lte(JsonData.of(experienceTo));
            }
            boolQuery.filter(Query.of(q -> q
                    .range(rangeQuery.build())));
        }
    }

    private boolean anyExpBoundIsNotNull() {
        return experienceFrom != null || experienceTo != null;
    }
}
