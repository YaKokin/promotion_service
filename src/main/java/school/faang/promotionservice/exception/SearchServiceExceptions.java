package school.faang.promotionservice.exception;

public class SearchServiceExceptions extends RuntimeException {

    private static final String SEARCHING_ERROR = "Search error for doc %s";

    public SearchServiceExceptions(Throwable cause, Class<?> docType) {
        super(String.format(SEARCHING_ERROR, docType), cause);
    }
}
