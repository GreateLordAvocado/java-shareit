package ru.practicum.shareit.gateway.client;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Objects;

public class BaseClient {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final RestTemplate rest;

    public BaseClient(RestTemplateBuilder builder, String serverBaseUrl) {
        Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is null");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverBaseUrl))
                .requestFactory(() -> requestFactory)
                .build();
    }

    public ResponseEntity<String> get(String path, @Nullable Long userId,
                                      Class<String> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.GET, path, userId, null, responseType, params);
    }

    public ResponseEntity<String> post(String path, @Nullable Long userId, @Nullable Object body,
                                       Class<String> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.POST, path, userId, body, responseType, params);
    }

    public ResponseEntity<String> patch(String path, @Nullable Long userId, @Nullable Object body,
                                        Class<String> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.PATCH, path, userId, body, responseType, params);
    }

    public ResponseEntity<String> delete(String path, @Nullable Long userId,
                                         Class<String> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.DELETE, path, userId, null, responseType, params);
    }

    private ResponseEntity<String> exchange(HttpMethod method, String path, @Nullable Long userId,
                                            @Nullable Object body, Class<String> responseType,
                                            @Nullable Map<String, ?> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        if (userId != null) headers.add(USER_HEADER, String.valueOf(userId));

        HttpEntity<?> request = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);

        try {
            String url = buildUrl(path, params);
            return rest.exchange(url, method, request, responseType);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw e;
        }
    }

    private String buildUrl(String path, @Nullable Map<String, ?> params) {
        UriComponentsBuilder b = UriComponentsBuilder.fromPath(path);
        if (params != null && !params.isEmpty()) {
            params.forEach((k, v) -> {
                if (v != null) b.queryParam(k, v);
            });
        }
        return b.build(true).toUriString(); // true -> не кодировать уже закодированное
    }
}
