package school.faang.promotionservice.exception;

public class ReindexPromotionException extends RuntimeException {

    public static final String NOT_FOUND_REINDEX_SERVICE_MESSAGE = "Not found reindex service for doc %s";

    public ReindexPromotionException(Class<?> docType) {
        super(String.format(NOT_FOUND_REINDEX_SERVICE_MESSAGE, docType.getName()));
    }
}
