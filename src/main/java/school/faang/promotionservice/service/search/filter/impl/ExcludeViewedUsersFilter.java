package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import school.faang.promotionservice.service.search.filter.Filter;
import school.faang.promotionservice.utils.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
public class ExcludeViewedUsersFilter implements Filter {

    private final List<Long> excludedUserIds;

    private static final String USER_ID_FIELD = "userId";

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (CollectionUtils.isNotEmpty(excludedUserIds)) {
            List<FieldValue> fieldValues = excludedUserIds.stream()
                    .map(FieldValue::of)
                    .toList();

            Query excludeQuery = Query.of(q -> q
                    .terms(ts -> ts
                            .field(USER_ID_FIELD)
                            .terms(ts2 -> ts2.value(fieldValues))
                    ));
            boolQuery.mustNot(excludeQuery);
        }
    }
}
