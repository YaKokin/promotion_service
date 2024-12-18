package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import school.faang.promotionservice.utils.CollectionUtils;

import java.util.List;

public class ExcludeFilter extends AbstractFilter {

    private final List<Long> excludedUserIds;

    public ExcludeFilter(List<Long> excludedUserIds, String field) {
        super(field);
        this.excludedUserIds = excludedUserIds;
    }

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (CollectionUtils.isNotEmpty(excludedUserIds)) {
            List<FieldValue> fieldValues = excludedUserIds.stream()
                    .map(FieldValue::of)
                    .toList();

            Query excludeQuery = Query.of(q -> q
                    .terms(ts -> ts
                            .field(field)
                            .terms(ts2 -> ts2.value(fieldValues))
                    ));
            boolQuery.mustNot(excludeQuery);
        }
    }
}
