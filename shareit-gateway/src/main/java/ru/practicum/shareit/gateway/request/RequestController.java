package ru.practicum.shareit.gateway.request;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

@Validated
@RestController
@RequestMapping("/requests")
public class RequestController {

    private static final String HDR = "X-Sharer-User-Id";

    private final RequestClient client;

    public RequestController(RequestClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestHeader(HDR) Long userId,
                                         @Valid @RequestBody ItemRequestCreateDto dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"Описание запроса не должно быть пустым\"}");
        }
        return client.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<String> own(@RequestHeader(HDR) Long userId) {
        return client.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<String> all(@RequestHeader(HDR) Long userId,
                                      @RequestParam(name = "from", required = false) Integer from,
                                      @RequestParam(name = "size", required = false) Integer size) {
        if ((from != null && from < 0) || (size != null && size <= 0)) {
            return ResponseEntity.badRequest().body("{\"error\":\"from >= 0 и size > 0\"}");
        }
        return client.getAll(userId, from, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> byId(@RequestHeader(HDR) Long userId,
                                       @PathVariable("id") Long requestId) {
        return client.getById(userId, requestId);
    }
}
