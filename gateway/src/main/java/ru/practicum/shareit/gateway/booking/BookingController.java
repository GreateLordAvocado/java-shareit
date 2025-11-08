package ru.practicum.shareit.gateway.booking;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;

@Validated
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final String USER_HDR = "X-Sharer-User-Id";

    private final BookingClient client;

    public BookingController(BookingClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestHeader(USER_HDR) Long userId,
                                         @Valid @RequestBody BookingCreateDto dto) {
        // дополнительная страховка, если вдруг @Valid отключён
        if (dto.getItemId() == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"Не указан itemId\"}");
        }
        if (dto.getStart() == null || dto.getEnd() == null || !dto.getEnd().isAfter(dto.getStart())) {
            return ResponseEntity.badRequest().body("{\"error\":\"Дата окончания должна быть позже даты начала\"}");
        }
        return client.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<String> approve(@RequestHeader(USER_HDR) Long ownerId,
                                          @PathVariable Long bookingId,
                                          @RequestParam("approved") boolean approved) {
        return client.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<String> get(@RequestHeader(USER_HDR) Long userId,
                                      @PathVariable Long bookingId) {
        return client.getById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<String> getForBooker(@RequestHeader(USER_HDR) Long userId,
                                               @RequestParam(name = "state", required = false) String stateRaw) {
        String state = normalizeStateOrBadRequest(stateRaw);
        if (state == BAD_REQUEST_MARKER) {
            return ResponseEntity.badRequest().body("{\"error\":\"Unknown state: " + stateRaw + "\"}");
        }
        return client.forBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<String> getForOwner(@RequestHeader(USER_HDR) Long ownerId,
                                              @RequestParam(name = "state", required = false) String stateRaw) {
        String state = normalizeStateOrBadRequest(stateRaw);
        if (state == BAD_REQUEST_MARKER) {
            return ResponseEntity.badRequest().body("{\"error\":\"Unknown state: " + stateRaw + "\"}");
        }
        return client.forOwner(ownerId, state);
    }

    private static final String BAD_REQUEST_MARKER = "__BAD_STATE__";

    private static String normalizeStateOrBadRequest(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.length() == 0) return null;
        v = v.toUpperCase();
        if ("ALL".equals(v) || "CURRENT".equals(v) || "PAST".equals(v) ||
                "FUTURE".equals(v) || "WAITING".equals(v) || "REJECTED".equals(v)) {
            return v;
        }
        return BAD_REQUEST_MARKER;
    }
}
