package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

@Validated
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private static final String HDR = "X-Sharer-User-Id";

    private final RequestClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HDR) Long userId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        log.debug("POST /requests by userId={}, dto={}", userId, dto);
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> own(@RequestHeader(HDR) Long userId) {
        log.debug("GET /requests (own) by userId={}", userId);
        return client.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> all(@RequestHeader(HDR) Long userId,
                                      @RequestParam(defaultValue = "0") @Min(0) int from,
                                      @RequestParam(defaultValue = "20") @Positive int size) {
        log.debug("GET /requests/all by userId={}, from={}, size={}", userId, from, size);
        return client.getAll(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> byId(@RequestHeader(HDR) Long userId,
                                       @PathVariable("id") Long requestId) {
        log.debug("GET /requests/{} by userId={}", requestId, userId);
        return client.getById(userId, requestId);
    }
}
