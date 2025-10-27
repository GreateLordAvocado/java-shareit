package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.item.storage.ItemJpaRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemJpaRepository repo;
    private final UserRepository userRepo;

    private final CommentJpaRepository commentRepo;
    private final BookingJpaRepository bookingRepo;

    @Override
    public ItemDto create(Long ownerId, ItemDto dto) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));
        validateForCreate(dto);

        Item toSave = ItemMapper.fromDto(dto);
        toSave.setOwnerId(ownerId);
        Item saved = repo.save(toSave);
        return ItemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto patch) {
        userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        Item existing = repo.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (!existing.getOwnerId().equals(ownerId)) {
            throw new NotFoundException("Редактировать вещь может только её владелец");
        }

        if (patch != null) {
            if (patch.getName() != null) {
                if (!StringUtils.hasText(patch.getName())) {
                    throw new ValidationException("Название вещи не должно быть пустым");
                }
                existing.setName(patch.getName());
            }
            if (patch.getDescription() != null) {
                if (!StringUtils.hasText(patch.getDescription())) {
                    throw new ValidationException("Описание вещи не должно быть пустым");
                }
                existing.setDescription(patch.getDescription());
            }
            if (patch.getAvailable() != null) {
                existing.setAvailable(patch.getAvailable());
            }
            if (patch.getRequestId() != null) {
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

        ItemDto dto = ItemMapper.toDto(item);

        var comments = commentRepo.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();

        dto.setComments(comments);
        return dto;
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
                    ItemDto dto = ItemMapper.toDto(item);

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

                    dto.setLastBooking(toShort(last));
                    dto.setNextBooking(toShort(next));
                    return dto;
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
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getText())) {
            throw new ValidationException("Текст комментария не должен быть пустым");
        }

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

        Comment saved = commentRepo.save(Comment.builder()
                .text(dto.getText().trim())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build());

        return CommentMapper.toDto(saved);
    }

    private void validateForCreate(ItemDto dto) {
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
    }

    private static ItemDto.BookingShortDto toShort(Booking b) {
        if (b == null) {
            return null;
        }
        return new ItemDto.BookingShortDto(
                b.getId(),
                b.getBooker().getId(),
                b.getStart(),
                b.getEnd()
        );
    }
}
