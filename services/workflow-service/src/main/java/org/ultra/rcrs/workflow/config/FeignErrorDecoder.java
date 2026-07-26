package org.ultra.rcrs.workflow.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.ConflictException;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;

/**
 * Translates downstream HTTP errors into the shared typed exceptions, so a 404/400/409 coming from
 * metadata-write, media or search service is reported back to the caller with the same status
 * instead of collapsing into a generic 500.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.resolve(response.status());
        String message = String.format("%s failed with status %s", methodKey, response.status());

        if (status == null) {
            return defaultDecoder.decode(methodKey, response);
        }

        log.warn(message);

        return switch (status) {
            case NOT_FOUND -> new NotFoundException(message);
            case BAD_REQUEST, UNPROCESSABLE_ENTITY -> new BadRequestException(message);
            case CONFLICT -> new ConflictException(message);
            case SERVICE_UNAVAILABLE, BAD_GATEWAY, GATEWAY_TIMEOUT -> new ServiceUnavailableException(message);
            default -> defaultDecoder.decode(methodKey, response);
        };
    }
}
