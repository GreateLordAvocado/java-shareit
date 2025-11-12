package ru.practicum.shareit.gateway.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RequestClient extends BaseClient {

    public RequestClient(RestTemplateBuilder builder,
                         @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
        log.debug("RequestClient initialized with server={}", server);
    }

    public ResponseEntity<Object> create(Long userId, Object body) {
        return post("/requests", userId, body, Object.class, null);
    }

    public ResponseEntity<Object> getOwn(Long userId) {
        return get("/requests", userId, Object.class, null);
    }

    public ResponseEntity<Object> getAll(Long userId, Integer from, Integer size) {
        Map<String, Object> params = new HashMap<>();
        if (from != null) params.put("from", from);
        if (size != null) params.put("size", size);
        return get("/requests/all", userId, Object.class, params);
    }

    public ResponseEntity<Object> getById(Long userId, Long requestId) {
        return get("/requests/" + requestId, userId, Object.class, null);
    }
}
