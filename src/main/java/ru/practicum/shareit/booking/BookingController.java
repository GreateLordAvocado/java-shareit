package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exceptions.ValidationException;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private static final String HDR = "X-Sharer-User-Id";

    private final BookingService service;

    @PostMapping
    public BookingDto create(@RequestHeader(HDR) Long userId,
                             @Valid @RequestBody BookingCreateRequest dto) {
        log.debug("POST /bookings userId={}, body={}", userId, dto);

        if (dto == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }
        if (dto.itemId() == null) {
            throw new ValidationException("Не указан itemId");
        }
        if (dto.start() == null || dto.end() == null) {
            throw new ValidationException("Должны быть указаны даты начала и конца");
        }
        if (!dto.end().isAfter(dto.start())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        return service.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HDR) Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam("approved") boolean approved) {
        log.debug("PATCH /bookings/{} ownerId={}, approved={}", bookingId, ownerId, approved);
        return service.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader(HDR) Long userId,
                          @PathVariable Long bookingId) {
        log.debug("GET /bookings/{} userId={}", bookingId, userId);
        return service.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getForBooker(@RequestHeader(HDR) Long userId,
                                         @RequestParam(name = "state", required = false) String stateRaw) {
        BookingState state = BookingState.from(stateRaw);
        log.debug("GET /bookings userId={}, state={}", userId, state);
        return service.findByBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getForOwner(@RequestHeader(HDR) Long ownerId,
                                        @RequestParam(name = "state", required = false) String stateRaw) {
        BookingState state = BookingState.from(stateRaw);
        log.debug("GET /bookings/owner ownerId={}, state={}", ownerId, state);
        return service.findByOwner(ownerId, state);
    }
}
