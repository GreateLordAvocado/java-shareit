package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemUpdateDto;

import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemClient client;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) Long ownerId,
                                         @Valid @RequestBody ItemCreateDto dto) {
        log.debug("POST /items by ownerId={}, dto={}", ownerId, dto);
        return client.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_HEADER) Long ownerId,
                                         @PathVariable Long itemId,
                                         @Valid @RequestBody ItemUpdateDto dto) {
        log.debug("PATCH /items/{} by ownerId={}, dto={}", itemId, ownerId, dto);

        if (dto.getName() == null
                && dto.getDescription() == null
                && dto.getAvailable() == null
                && dto.getRequestId() == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", "At least one field must be provided"));
        }

        return client.patch(ownerId, itemId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(USER_HEADER) Long ownerId,
                                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                                @RequestParam(defaultValue = "20") @Positive int size) {
        log.debug("GET /items by ownerId={}, from={}, size={}", ownerId, from, size);
        return client.getOwnerItems(ownerId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) Long userId,
                                          @PathVariable Long itemId) {
        log.debug("GET /items/{} by userId={}", itemId, userId);
        return client.getById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(value = USER_HEADER, required = false) Long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "20") @Positive int size) {
        log.debug("GET /items/search by userId={}, text='{}', from={}, size={}", userId, text, from, size);
        if (text == null || text.trim().isEmpty()) {
            // Возвращаем пустой JSON-массив как объект
            return ResponseEntity.ok().body(new Object[0]);
        }
        return client.search(userId, text.trim(), from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentCreateDto dto) {
        log.debug("POST /items/{}/comment by userId={}, dto={}", itemId, userId, dto);
        return client.addComment(userId, itemId, dto);
    }
}
