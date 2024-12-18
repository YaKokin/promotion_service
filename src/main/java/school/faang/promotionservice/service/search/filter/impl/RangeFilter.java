package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import school.faang.promotionservice.service.search.filter.Filter;

@RequiredArgsConstructor
public class RangeFilter implements Filter {

    private final Integer from;
    private final Integer to;
    private final String field;

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (anyExpBoundIsNotNull()) {
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder()
                    .field(field);
            if (from != null) {
                rangeQuery.gte(JsonData.of(from));
            }
            if (to != null) {
                rangeQuery.lte(JsonData.of(to));
            }
            boolQuery.filter(Query.of(q -> q
                    .range(rangeQuery.build())));
        }
    }

    private boolean anyExpBoundIsNotNull() {
        return from != null || to != null;
    }
}
