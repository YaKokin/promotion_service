package school.faang.promotionservice.service.search.reindexing;

public interface ReindexService<DOC, EVENT> {

    Class<DOC> getDocType();

    void index(DOC t);

    void reindex (EVENT reindexEvent);
}
