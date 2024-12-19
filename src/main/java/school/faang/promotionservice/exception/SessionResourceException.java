package school.faang.promotionservice.exception;

public class SessionResourceException extends RuntimeException {

    private static final String NOT_FOUND_SESSION_RESOURCE_SERVICE = "Not found session resource service for doc %s";

    public SessionResourceException(Class<?> docType) {
        super(String.format(NOT_FOUND_SESSION_RESOURCE_SERVICE, docType.getSimpleName()));
    }
}
