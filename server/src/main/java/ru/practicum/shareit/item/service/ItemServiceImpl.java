package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemJpaRepository repo;
    private final UserRepository userRepo;
    private final CommentJpaRepository commentRepo;
    private final BookingJpaRepository bookingRepo;
    private final ItemRequestRepository requestRepo;

    @Override
    @Transactional
    public ItemDto create(Long ownerId, ItemDto dto) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        if (dto.getRequestId() != null && !requestRepo.existsById(dto.getRequestId())) {
            throw new NotFoundException("Запрос не найден: " + dto.getRequestId());
        }

        Item toSave = ItemMapper.fromDto(dto);
        toSave.setOwnerId(ownerId);
        Item saved = repo.save(toSave);
        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        Item existing = repo.findByIdAndOwnerId(itemId, ownerId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена или не принадлежит пользователю: " + itemId));

        if (patch != null) {
            if (patch.getName() != null) {
                String name = patch.getName().trim();
                existing.setName(name);
            }
            if (patch.getDescription() != null) {
                String desc = patch.getDescription().trim();
                existing.setDescription(desc);
            }
            if (patch.getAvailable() != null) {
                existing.setAvailable(patch.getAvailable());
            }
            if (patch.getRequestId() != null) {
                if (!requestRepo.existsById(patch.getRequestId())) {
                    throw new NotFoundException("Запрос не найден: " + patch.getRequestId());
                }
                existing.setRequestId(patch.getRequestId());
            }
        }

        Item saved = repo.save(existing);
        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        ItemDto base = ItemMapper.toDto(item);

        List<CommentDto> comments = commentRepo.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();

        return ItemDto.builder()
                .id(base.getId())
                .name(base.getName())
                .description(base.getDescription())
                .available(base.getAvailable())
                .ownerId(base.getOwnerId())
                .requestId(base.getRequestId())
                .comments(comments)
                .build();
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        LocalDateTime now = LocalDateTime.now();

        List<Item> items = repo.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }

        var allBookings = bookingRepo.findAllByOwner(ownerId);

        var bookingsByItem = allBookings.stream()
                .filter(b -> b.getItem() != null)
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        return items.stream()
                .sorted(Comparator.comparing(Item::getId))
                .map(item -> {
                    ItemDto base = ItemMapper.toDto(item);

                    var list = bookingsByItem.getOrDefault(item.getId(), List.of())
                            .stream()
                            .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                            .toList();

                    Booking last = list.stream()
                            .filter(b -> !b.getStart().isAfter(now))
                            .max(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    Booking next = list.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    return ItemDto.builder()
                            .id(base.getId())
                            .name(base.getName())
                            .description(base.getDescription())
                            .available(base.getAvailable())
                            .ownerId(base.getOwnerId())
                            .requestId(base.getRequestId())
                            .lastBooking(toShort(last))
                            .nextBooking(toShort(next))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return repo.searchAvailableByText(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        var author = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        var item = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        boolean hadApprovedPastBooking = bookingRepo.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED
        );

        if (!hadApprovedPastBooking) {
            throw new ValidationException("Комментировать вещь может только пользователь, который её арендовал и вернул");
        }

        String text = dto.getText();
        if (text != null) {
            text = text.trim();
        }

        Comment saved = commentRepo.save(Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build());

        return CommentMapper.toDto(saved);
    }

    @Override
    public Item requireEntity(Long itemId) {
        return repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
    }

    private static ItemDto.BookingShortDto toShort(Booking b) {
        if (b == null) {
            return null;
        }
        return ItemDto.BookingShortDto.builder()
                .id(b.getId())
                .bookerId(b.getBooker().getId())
                .start(b.getStart())
                .end(b.getEnd())
                .build();
    }
}
