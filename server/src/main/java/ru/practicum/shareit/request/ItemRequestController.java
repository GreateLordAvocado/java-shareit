package ru.practicum.shareit.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
public class ItemRequestController {

    private static final Logger log = LoggerFactory.getLogger(ItemRequestController.class);
    private static final String HDR = "X-Sharer-User-Id";

    private final ItemRequestService service;

    public ItemRequestController(ItemRequestService service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto create(@RequestHeader(HDR) Long userId,
                                 @RequestBody ItemRequestCreateDto dto) {
        log.debug("POST /requests userId={}, body={}", userId, dto);
        return service.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(HDR) Long userId) {
        log.debug("GET /requests userId={}", userId);
        return service.getOwn(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(HDR) Long userId,
                                       @RequestParam(name = "from", defaultValue = "0") Integer from,
                                       @RequestParam(name = "size", defaultValue = "20") Integer size) {
        log.debug("GET /requests/all userId={}, from={}, size={}", userId, from, size);
        return service.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(HDR) Long userId,
                                  @PathVariable Long requestId) {
        log.debug("GET /requests/{} userId={}", requestId, userId);
        return service.getById(userId, requestId);
    }
}
