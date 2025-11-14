package ru.practicum.shareit.gateway.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

@Component
@Slf4j
public class UserClient extends BaseClient {

    public UserClient(RestTemplateBuilder builder,
                      @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
        log.debug("UserClient initialized with server={}", server);
    }

    public ResponseEntity<Object> create(Object body) {
        log.debug("Creating user with body={}", body);
        return post("/users", null, body, Object.class, null);
    }

    public ResponseEntity<Object> patch(Long id, Object body) {
        log.debug("Patching user id={} with body={}", id, body);
        return patch("/users/" + id, null, body, Object.class, null);
    }

    public ResponseEntity<Object> getAll() {
        log.debug("Fetching all users");
        return get("/users", null, Object.class, null);
    }

    public ResponseEntity<Object> getById(Long id) {
        log.debug("Fetching user by id={}", id);
        return get("/users/" + id, null, Object.class, null);
    }

    public ResponseEntity<Object> delete(Long id) {
        log.debug("Deleting user id={}", id);
        return delete("/users/" + id, null, Object.class, null);
    }
}
