package school.faang.promotionservice.config.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;

public class FeignClientErrorDecoder implements ErrorDecoder {

    private static final String RESOURCE_NOT_FOUND_ERROR = "Resource not found";

    @Override
    public Exception decode(String s, Response response) {
        return switch (response.status()) {
            case 404 -> new EntityNotFoundException(RESOURCE_NOT_FOUND_ERROR);
            default -> new RuntimeException(s);
        };
    }
}
