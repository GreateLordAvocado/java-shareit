package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
// Если у тебя бывают проблемы с Lombok @Slf4j, можно заменить на обычный Logger:
// import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemService service;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_HEADER) Long ownerId,
                          @RequestBody ItemDto dto) {
        log.debug("POST /items ownerId={}, body={}", ownerId, dto);

        if (dto == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new ValidationException("Название вещи не должно быть пустым");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ValidationException("Описание вещи не должно быть пустым");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("Поле доступности вещи (available) должно быть указано");
        }

        return service.create(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto patch) {
        log.debug("PATCH /items/{} ownerId={}, body={}", itemId, ownerId, patch);
        return service.update(ownerId, itemId, patch);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId) {
        log.debug("GET /items/{}", itemId);
        return service.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_HEADER) Long ownerId) {
        log.debug("GET /items ownerId={}", ownerId);
        // Тест мокает именно getUserItems(...), используем алиас из ItemService
        return service.getUserItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        log.debug("GET /items/search text='{}'", text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return service.search(text.trim());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentCreateDto dto) {
        log.debug("POST /items/{}/comment userId={}, body={}", itemId, userId, dto);
        return service.addComment(userId, itemId, dto);
    }
}
