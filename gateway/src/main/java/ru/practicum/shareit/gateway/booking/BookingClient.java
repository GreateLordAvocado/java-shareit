package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    public BookingClient(RestTemplateBuilder builder,
                         @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder, server);
    }

    public ResponseEntity<String> create(Long userId, Object body) {
        return post("/bookings", userId, body, String.class, null);
    }

    public ResponseEntity<String> approve(Long ownerId, Long bookingId, boolean approved) {
        Map<String, Object> params = new HashMap<>();
        params.put("approved", approved);
        return patch("/bookings/" + bookingId, ownerId, null, String.class, params);
    }

    public ResponseEntity<String> getById(Long userId, Long bookingId) {
        return get("/bookings/" + bookingId, userId, String.class, null);
    }

    public ResponseEntity<String> forBooker(Long userId, String state) {
        Map<String, Object> params = null;
        if (state != null) {
            params = new HashMap<>();
            params.put("state", state);
        }
        return get("/bookings", userId, String.class, params);
    }

    public ResponseEntity<String> forOwner(Long ownerId, String state) {
        Map<String, Object> params = null;
        if (state != null) {
            params = new HashMap<>();
            params.put("state", state);
        }
        return get("/bookings/owner", ownerId, String.class, params);
    }
}