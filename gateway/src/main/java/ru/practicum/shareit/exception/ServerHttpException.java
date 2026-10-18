package ru.practicum.shareit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

@Getter
public class ServerHttpException extends RuntimeException {
    private final HttpStatusCode status;
    private final MediaType contentType;
    private final byte[] body;

    public ServerHttpException(HttpStatusCodeException cause) {
        super(cause.getStatusCode().toString(), cause);
        this.status = cause.getStatusCode();
        this.contentType = cause.getResponseHeaders() == null
                ? null
                : cause.getResponseHeaders().getContentType();
        this.body = cause.getResponseBodyAsByteArray();
    }
}
