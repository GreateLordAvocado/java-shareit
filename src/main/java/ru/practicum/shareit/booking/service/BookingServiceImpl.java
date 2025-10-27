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
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingJpaRepository bookingRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;

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

        User booker = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        Item item = itemRepo.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + dto.getItemId()));

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
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        if (booking.getItem() == null || !ownerId.equals(booking.getItem().getOwnerId())) {
            throw new ForbiddenException("Подтверждать/отклонять может только владелец вещи");
        }
        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            throw new ValidationException("Бронирование уже подтверждено");
        }
        if (booking.getStatus() == BookingStatus.REJECTED && !approved) {
            throw new ValidationException("Бронирование уже отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepo.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено: " + bookingId));

        Long ownerId = booking.getItem() != null ? booking.getItem().getOwnerId() : null;
        Long bookerId = booking.getBooker() != null ? booking.getBooker().getId() : null;

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Доступ запрещён: можно смотреть только своё бронирование или бронирование своей вещи");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(Long userId, BookingState state) {
        userRepo.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> list = switch (state) {
            case CURRENT -> bookingRepo.findCurrentByBooker(userId, now);
            case PAST -> bookingRepo.findPastByBooker(userId, now);
            case FUTURE -> bookingRepo.findFutureByBooker(userId, now);
            case WAITING -> bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepo.findByBooker_IdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            case ALL -> bookingRepo.findByBooker_IdOrderByStartDesc(userId);
        };
        return list.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findByOwner(Long ownerId, BookingState state) {
        userRepo.findById(ownerId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> list = switch (state) {
            case CURRENT -> bookingRepo.findCurrentByOwner(ownerId, now);
            case PAST -> bookingRepo.findPastByOwner(ownerId, now);
            case FUTURE -> bookingRepo.findFutureByOwner(ownerId, now);
            case WAITING -> bookingRepo.findAllByOwnerAndStatus(ownerId, BookingStatus.WAITING);
            case REJECTED -> bookingRepo.findAllByOwnerAndStatus(ownerId, BookingStatus.REJECTED);
            case ALL -> bookingRepo.findAllByOwner(ownerId);
        };
        return list.stream().map(BookingMapper::toDto).collect(Collectors.toList());
    }
}
