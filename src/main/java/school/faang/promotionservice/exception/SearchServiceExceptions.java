package school.faang.promotionservice.exception;

public class SearchServiceExceptions extends RuntimeException {
    public SearchServiceExceptions(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchServiceExceptions(Throwable cause) {
        super(cause);
    }
}
