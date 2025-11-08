package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemCreateDto;
import ru.practicum.shareit.gateway.item.dto.ItemUpdateDto;

@Validated
@RestController
@RequestMapping("/items")
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemClient client;

    public ItemController(ItemClient client) {
        this.client = client;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestHeader(USER_HEADER) Long ownerId,
                                         @Valid @RequestBody ItemCreateDto dto) {
        return client.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<String> update(@RequestHeader(USER_HEADER) Long ownerId,
                                         @PathVariable Long itemId,
                                         @Valid @RequestBody ItemUpdateDto dto) {
        if (dto.getName() == null && dto.getDescription() == null
                && dto.getAvailable() == null && dto.getRequestId() == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"At least one field must be provided\"}");
        }
        return client.patch(ownerId, itemId, dto);
    }

    @GetMapping
    public ResponseEntity<String> getOwnerItems(@RequestHeader(USER_HEADER) Long ownerId,
                                                @RequestParam(defaultValue = "0") @Min(0) int from,
                                                @RequestParam(defaultValue = "20") @Positive int size) {
        return client.getOwnerItems(ownerId, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<String> getById(@RequestHeader(USER_HEADER) Long userId,
                                          @PathVariable Long itemId) {
        return client.getById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<String> search(@RequestHeader(value = USER_HEADER, required = false) Long userId,
                                         @RequestParam String text,
                                         @RequestParam(defaultValue = "0") @Min(0) int from,
                                         @RequestParam(defaultValue = "20") @Positive int size) {
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.ok("[]");
        }
        return client.search(userId, text.trim(), from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<String> addComment(@RequestHeader(USER_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentCreateDto dto) {
        return client.addComment(userId, itemId, dto);
    }
}
