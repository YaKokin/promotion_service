package school.faang.promotionservice.service.search.filter.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import school.faang.promotionservice.service.search.filter.Filter;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class TextMatchFilter implements Filter {

    private final String query;

    private static final String FUZZINESS_VALUE = "AUTO";
    private static final String COUNTRY_NAME_FIELD = "countryName";
    private static final String CITY_NAME_FIELD = "cityName";
    private static final String SKILL_NAMES_FIELD = "skillNames";

    @Override
    public void apply(BoolQuery.Builder boolQuery) {
        if (StringUtils.hasText(query)) {
            boolQuery.should(
                    createMatchQueries(query)
            );
        }
    }

    private List<Query> createMatchQueries(String query) {
        return Arrays.asList(
                createMatchQuery(query, COUNTRY_NAME_FIELD, null),
                createMatchQuery(query, CITY_NAME_FIELD, null),
                createMatchQuery(query, SKILL_NAMES_FIELD, 2.0f)
        );
    }

    private Query createMatchQuery(String query, String field, Float boost) {
        return Query.of(q -> q
                .match(m -> {
                            m.field(field).query(query).fuzziness(FUZZINESS_VALUE);
                            if (boost != null) {
                                m.boost(boost);
                            }
                            return m;
                        }
                ));
    }
}
