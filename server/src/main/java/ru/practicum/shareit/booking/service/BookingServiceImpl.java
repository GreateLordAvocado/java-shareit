package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.exceptions.ForbiddenException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingJpaRepository bookingRepo;
    private final ItemService itemService;
    private final UserService userService;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateRequest dto) {
        if (dto == null) {
            throw new ValidationException("Тело запроса не должно быть пустым");
        }
        if (dto.getItemId() == null) {
            throw new ValidationException("Не указан itemId");
        }
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("Должны быть указаны даты начала и конца");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean bothPast = dto.getStart().isBefore(now) && dto.getEnd().isBefore(now);
        if (!bothPast) {
            if (dto.getStart().isBefore(now)) {
                throw new ValidationException("Дата начала не может быть в прошлом");
            }
            if (dto.getEnd().isBefore(now)) {
                throw new ValidationException("Дата окончания не может быть в прошлом");
            }
        }

        User booker = userService.requireEntity(userId);
        Item item   = itemService.requireEntity(dto.getItemId());

        if (item.getOwnerId() != null && item.getOwnerId().equals(userId)) {
            throw new NotFoundException("Нельзя бронировать свою вещь");
        }
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        boolean overlap = bookingRepo.hasApprovedOverlap(item.getId(), dto.getStart(), dto.getEnd());
        if (overlap) {
            throw new ValidationException("В указанный период вещь уже забронирована");
        }

        Booking toSave = Booking.builder()
                .item(item)
                .booker(booker)
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(BookingStatus.WAITING)
                .build();

        Booking saved = bookingRepo.save(toSave);
        return BookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        userService.requireEntity(ownerId);

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        Item item = booking.getItem();
        if (item == null || item.getOwnerId() == null) {
            throw new NotFoundException("Вещь для бронирования не найдена");
        }
        if (!ownerId.equals(item.getOwnerId())) {
            throw new ForbiddenException("Подтверждать/отклонять может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменён");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepo.save(booking);

        Booking reloaded = bookingRepo.findById(booking.getId())
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено после сохранения: " + bookingId));
        return BookingMapper.toDto(reloaded);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        userService.requireEntity(userId);

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        Long ownerId = booking.getItem() != null ? booking.getItem().getOwnerId() : null;
        Long bookerId = booking.getBooker() != null ? booking.getBooker().getId() : null;

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Можно смотреть только своё бронирование или бронирование своей вещи");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(Long userId, BookingState state) {
        userService.requireEntity(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> list;

        switch (state) {
            case CURRENT:
                list = bookingRepo.findCurrentByBooker(userId, now);
                break;
            case PAST:
                list = bookingRepo.findPastByBooker(userId, now);
                break;
            case FUTURE:
                list = bookingRepo.findFutureByBooker(userId, now);
                break;
            case WAITING:
                list = bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                list = bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            case ALL:
            default:
                list = bookingRepo.findByBooker_IdOrderByStartDesc(userId);
                break;
        }
        return list.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findByOwner(Long ownerId, BookingState state) {
        userService.requireEntity(ownerId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> list;

        switch (state) {
            case CURRENT:
                list = bookingRepo.findCurrentByOwner(ownerId, now);
                break;
            case PAST:
                list = bookingRepo.findPastByOwner(ownerId, now);
                break;
            case FUTURE:
                list = bookingRepo.findFutureByOwner(ownerId, now);
                break;
            case WAITING:
                list = bookingRepo.findAllByOwnerAndStatus(ownerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                list = bookingRepo.findAllByOwnerAndStatus(ownerId, BookingStatus.REJECTED);
                break;
            case ALL:
            default:
                list = bookingRepo.findAllByOwner(ownerId);
                break;
        }
        return list.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}
