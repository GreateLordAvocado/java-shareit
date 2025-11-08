package ru.practicum.shareit.gateway.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestClient extends BaseClient {

    public RequestClient(RestTemplateBuilder builder,
                         @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
    }

    public ResponseEntity<String> create(Long userId, Object body) {
        return post("/requests", userId, body, String.class, null);
    }

    public ResponseEntity<String> getOwn(Long userId) {
        return get("/requests", userId, String.class, null);
    }

    public ResponseEntity<String> getAll(Long userId, Integer from, Integer size) {
        Map<String, Object> params = new HashMap<>();
        if (from != null) params.put("from", from);
        if (size != null) params.put("size", size);
        return get("/requests/all", userId, String.class, params);
    }

    public ResponseEntity<String> getById(Long userId, Long requestId) {
        return get("/requests/" + requestId, userId, String.class, null);
    }
}
