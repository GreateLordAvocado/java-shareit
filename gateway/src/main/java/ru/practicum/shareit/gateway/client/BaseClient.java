package ru.practicum.shareit.gateway.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
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

@Slf4j
public class BaseClient {

    protected static final String USER_HEADER = "X-Sharer-User-Id";

    private final RestTemplate rest;

    public BaseClient(RestTemplate restTemplate, String serverBaseUrl) {
        Objects.requireNonNull(restTemplate, "restTemplate is null");
        Objects.requireNonNull(serverBaseUrl, "serverBaseUrl is null");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        restTemplate.setRequestFactory(requestFactory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(serverBaseUrl));

        this.rest = restTemplate;
        log.debug("BaseClient initialized with baseUrl={}", serverBaseUrl);
    }

    protected <T> ResponseEntity<T> get(String path, @Nullable Long userId,
                                        Class<T> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.GET, path, userId, null, responseType, params);
    }

    protected <T> ResponseEntity<T> post(String path, @Nullable Long userId, @Nullable Object body,
                                         Class<T> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.POST, path, userId, body, responseType, params);
    }

    protected <T> ResponseEntity<T> patch(String path, @Nullable Long userId, @Nullable Object body,
                                          Class<T> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.PATCH, path, userId, body, responseType, params);
    }

    protected <T> ResponseEntity<T> delete(String path, @Nullable Long userId,
                                           Class<T> responseType, @Nullable Map<String, ?> params) {
        return exchange(HttpMethod.DELETE, path, userId, null, responseType, params);
    }

    private <T> ResponseEntity<T> exchange(HttpMethod method, String path, @Nullable Long userId,
                                           @Nullable Object body, Class<T> responseType,
                                           @Nullable Map<String, ?> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        if (userId != null) {
            headers.add(USER_HEADER, String.valueOf(userId));
        }

        HttpEntity<?> request = (body == null) ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);

        try {
            String url = buildUrl(path, params);
            log.debug("HTTP {} {} | headers={} | params={} | body={}", method, url, headers, params, body);
            ResponseEntity<T> response = rest.exchange(url, method, request, responseType);
            log.debug("HTTP {} {} -> {} {}", method, url, response.getStatusCode(), response.getBody());
            return response;
        } catch (HttpStatusCodeException e) {
            log.debug("HTTP {} {} -> {} {}", method, path, e.getStatusCode(), e.getResponseBodyAsString());
            @SuppressWarnings("unchecked")
            T bodyStr = (T) e.getResponseBodyAsString();
            return ResponseEntity
                    .status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(bodyStr);
        } catch (ResourceAccessException e) {
            log.error("Resource access error for {} {}: {}", method, path, e.getMessage());
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
        return b.build(true).toUriString();
    }
}
