package ru.practicum.shareit.client;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.exception.ServerHttpException;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return get(path, null, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, null, null);
    }

    protected ResponseEntity<Object> get(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, long userId, T body) {
        return post(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> post(String path, Long userId,
                                              @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId, T body) {
        return put(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> put(String path, long userId,
                                             @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PUT, path, userId, parameters, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, T body) {
        return patch(path, null, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId) {
        return patch(path, userId, null, null);
    }

    protected <T> ResponseEntity<Object> patch(String path, long userId, T body) {
        return patch(path, userId, null, body);
    }

    protected <T> ResponseEntity<Object> patch(String path, Long userId,
                                               @Nullable Map<String, Object> parameters, T body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return delete(path, null, null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return delete(path, userId, null);
    }

    protected ResponseEntity<Object> delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null);
    }

    protected <R> ResponseEntity<R> get(String path, long userId, Class<R> responseType) {
        return get(path, userId, null, responseType);
    }

    protected <R> ResponseEntity<R> get(String path, Long userId, @Nullable Map<String, Object> parameters,
                                        Class<R> responseType) {
        return send(HttpMethod.GET, path, userId, parameters, null, responseType);
    }

    protected <R> ResponseEntity<R> get(String path, long userId, ParameterizedTypeReference<R> responseType) {
        return get(path, userId, null, responseType);
    }

    protected <R> ResponseEntity<R> get(String path, Long userId, @Nullable Map<String, Object> parameters,
                                        ParameterizedTypeReference<R> responseType) {
        return send(HttpMethod.GET, path, userId, parameters, null, responseType);
    }

    protected <R, T> ResponseEntity<R> post(String path, long userId, T body, Class<R> responseType) {
        return send(HttpMethod.POST, path, userId, null, body, responseType);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String path, Long userId,
                                                          @Nullable Map<String, Object> parameters,
                                                          @Nullable T body) {
        return send(method, path, userId, parameters, body, Object.class);
    }

    private <R> ResponseEntity<R> send(HttpMethod method, String path, Long userId,
                                       @Nullable Map<String, Object> parameters, @Nullable Object body,
                                       Class<R> responseType) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        if (parameters != null) {
            return invoke(() -> rest.exchange(path, method, requestEntity, responseType, parameters));
        }
        return invoke(() -> rest.exchange(path, method, requestEntity, responseType));
    }

    private <R> ResponseEntity<R> send(HttpMethod method, String path, Long userId,
                                       @Nullable Map<String, Object> parameters, @Nullable Object body,
                                       ParameterizedTypeReference<R> responseType) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        if (parameters != null) {
            return invoke(() -> rest.exchange(path, method, requestEntity, responseType, parameters));
        }
        return invoke(() -> rest.exchange(path, method, requestEntity, responseType));
    }

    private <R> ResponseEntity<R> invoke(Supplier<ResponseEntity<R>> request) {
        try {
            return prepareGatewayResponse(request.get());
        } catch (HttpStatusCodeException e) {
            throw new ServerHttpException(e);
        }
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }

    private static <R> ResponseEntity<R> prepareGatewayResponse(ResponseEntity<R> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());

        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }

        return responseBuilder.build();
    }
}
