package ru.practicum.shareit.gateway.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BookingClient extends BaseClient {

    public BookingClient(RestTemplateBuilder builder,
                         @Value("${shareit-server.url:http://localhost:9090}") String server) {
        super(builder.build(), server);
    }

    public ResponseEntity<Object> create(Long userId, Object body) {
        log.debug("Create booking: userId={}, body={}", userId, body);
        return post("/bookings", userId, body, Object.class, null);
    }

    public ResponseEntity<Object> approve(Long ownerId, Long bookingId, boolean approved) {
        log.debug("Approve booking: ownerId={}, bookingId={}, approved={}", ownerId, bookingId, approved);
        Map<String, Object> params = new HashMap<>();
        params.put("approved", approved);
        return patch("/bookings/" + bookingId, ownerId, null, Object.class, params);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        log.debug("Get booking by id: userId={}, bookingId={}", userId, bookingId);
        return get("/bookings/" + bookingId, userId, Object.class, null);
    }

    public ResponseEntity<Object> forBooker(Long userId, String state) {
        Map<String, Object> params = null;
        if (state != null) {
            params = new HashMap<>();
            params.put("state", state);
        }
        log.debug("Get bookings for booker: userId={}, state={}, params={}", userId, state, params);
        return get("/bookings", userId, Object.class, params);
    }

    public ResponseEntity<Object> forOwner(Long ownerId, String state) {
        Map<String, Object> params = null;
        if (state != null) {
            params = new HashMap<>();
            params.put("state", state);
        }
        log.debug("Get bookings for owner: ownerId={}, state={}, params={}", ownerId, state, params);
        return get("/bookings/owner", ownerId, Object.class, params);
    }
}
