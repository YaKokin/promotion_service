package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractFilter {
    protected final String field;

    public abstract void apply(BoolQuery.Builder boolQuery);
}
