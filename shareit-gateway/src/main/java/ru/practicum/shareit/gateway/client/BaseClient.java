package ru.practicum.shareit.gateway.client;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

public class BaseClient {
    private final RestTemplate rest;
    private final String serverUrl;

    public BaseClient(RestTemplate rest, String serverUrl) {
        this.rest = rest;
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
    }

    protected <T> ResponseEntity<T> get(String path, Long userId, Class<T> type, Map<String, ?> params) {
        HttpEntity<?> entity = new HttpEntity<>(headers(userId));
        return rest.exchange(uri(path, params), HttpMethod.GET, entity, type);
    }

    protected <B, T> ResponseEntity<T> post(String path, Long userId, B body, Class<T> type, Map<String, ?> params) {
        HttpEntity<B> entity = new HttpEntity<>(body, headers(userId));
        return rest.exchange(uri(path, params), HttpMethod.POST, entity, type);
    }

    protected <B, T> ResponseEntity<T> patch(String path, Long userId, B body, Class<T> type, Map<String, ?> params) {
        HttpEntity<B> entity = new HttpEntity<>(body, headers(userId));
        return rest.exchange(uri(path, params), HttpMethod.PATCH, entity, type);
    }

    protected <T> ResponseEntity<T> delete(String path, Long userId, Class<T> type, Map<String, ?> params) {
        HttpEntity<?> entity = new HttpEntity<>(headers(userId));
        return rest.exchange(uri(path, params), HttpMethod.DELETE, entity, type);
    }

    private HttpHeaders headers(Long userId) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) h.set("X-Sharer-User-Id", String.valueOf(userId));
        return h;
    }

    private URI uri(String path, Map<String, ?> params) {
        StringBuilder sb = new StringBuilder(serverUrl).append(path);
        if (params != null && !params.isEmpty()) {
            sb.append("?");
            boolean first = true;
            for (Map.Entry<String, ?> e : params.entrySet()) {
                if (!first) sb.append("&");
                first = false;
                sb.append(e.getKey()).append("=").append(e.getValue());
            }
        }
        return URI.create(sb.toString());
    }
}
