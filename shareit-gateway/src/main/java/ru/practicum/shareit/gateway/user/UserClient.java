package ru.practicum.shareit.gateway.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

@Component
public class UserClient extends BaseClient {

    public UserClient(RestTemplateBuilder builder,
                      @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
    }

    public ResponseEntity<String> create(Object body) {
        return post("/users", null, body, String.class, null);
    }

    public ResponseEntity<String> patch(Long id, Object body) {
        return patch("/users/" + id, null, body, String.class, null);
    }

    public ResponseEntity<String> getAll() {
        return get("/users", null, String.class, null);
    }

    public ResponseEntity<String> getById(Long id) {
        return get("/users/" + id, null, String.class, null);
    }

    public ResponseEntity<String> delete(Long id) {
        return delete("/users/" + id, null, String.class, null);
    }
}
