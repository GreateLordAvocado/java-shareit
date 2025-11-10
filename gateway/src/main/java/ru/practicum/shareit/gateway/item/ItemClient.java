package ru.practicum.shareit.gateway.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

@Component
@Slf4j
public class ItemClient extends BaseClient {

    public ItemClient(RestTemplateBuilder builder,
                      @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
        log.debug("ItemClient initialized with server={}", server);
    }

    public ResponseEntity<String> create(Long ownerId, Object body) {
        return post("/items", ownerId, body, String.class, null);
    }

    public ResponseEntity<String> patch(Long ownerId, Long itemId, Object body) {
        return patch("/items/" + itemId, ownerId, body, String.class, null);
    }

    public ResponseEntity<String> getOwnerItems(Long ownerId, int from, int size) {
        return get("/items", ownerId, String.class, Map.of("from", from, "size", size));
    }

    public ResponseEntity<String> getById(Long userId, Long itemId) {
        return get("/items/" + itemId, userId, String.class, null);
    }

    public ResponseEntity<String> search(Long userId, String text, int from, int size) {
        return get("/items/search", userId, String.class, Map.of("text", text, "from", from, "size", size));
    }

    public ResponseEntity<String> addComment(Long userId, Long itemId, Object body) {
        return post("/items/" + itemId + "/comment", userId, body, String.class, null);
    }
}
