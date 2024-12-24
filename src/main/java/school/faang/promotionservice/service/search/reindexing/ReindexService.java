package school.faang.promotionservice.service.search.reindexing;

import java.util.List;

public interface ReindexService<DOC, EVENT> {

    Class<DOC> getDocType();

    void addToIndex(DOC t);

    void reindex (EVENT reindexEvent);

    void deleteAllFromIndex(List<Long> promotionIds);
}
