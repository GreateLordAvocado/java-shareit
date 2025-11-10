package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;

@Validated
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private static final String USER_HDR = "X-Sharer-User-Id";
    private static final String BAD_REQUEST_MARKER = "__BAD_STATE__";

    private final BookingClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HDR) Long userId,
                                         @Valid @RequestBody BookingCreateDto dto) {
        log.debug("POST /bookings by userId={}, dto={}", userId, dto);
        return client.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(USER_HDR) Long ownerId,
                                          @PathVariable Long bookingId,
                                          @RequestParam("approved") boolean approved) {
        log.debug("PATCH /bookings/{}?approved={} by ownerId={}", bookingId, approved, ownerId);
        return client.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> get(@RequestHeader(USER_HDR) Long userId,
                                      @PathVariable Long bookingId) {
        log.debug("GET /bookings/{} by userId={}", bookingId, userId);
        return client.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getForBooker(@RequestHeader(USER_HDR) Long userId,
                                               @RequestParam(name = "state", required = false) String stateRaw) {
        String state = normalizeStateOrBadRequest(stateRaw);
        if (state == BAD_REQUEST_MARKER) {
            log.debug("GET /bookings invalid state='{}' by userId={}", stateRaw, userId);
            return ResponseEntity.badRequest().body("{\"error\":\"Unknown state: " + stateRaw + "\"}");
        }
        log.debug("GET /bookings state='{}' by userId={}", state, userId);
        return client.forBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getForOwner(@RequestHeader(USER_HDR) Long ownerId,
                                              @RequestParam(name = "state", required = false) String stateRaw) {
        String state = normalizeStateOrBadRequest(stateRaw);
        if (state == BAD_REQUEST_MARKER) {
            log.debug("GET /bookings/owner invalid state='{}' by ownerId={}", stateRaw, ownerId);
            return ResponseEntity.badRequest().body("{\"error\":\"Unknown state: " + stateRaw + "\"}");
        }
        log.debug("GET /bookings/owner state='{}' by ownerId={}", state, ownerId);
        return client.forOwner(ownerId, state);
    }

    private static String normalizeStateOrBadRequest(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return null;
        v = v.toUpperCase();
        if ("ALL".equals(v) || "CURRENT".equals(v) || "PAST".equals(v) ||
                "FUTURE".equals(v) || "WAITING".equals(v) || "REJECTED".equals(v)) {
            return v;
        }
        return BAD_REQUEST_MARKER;
    }
}
