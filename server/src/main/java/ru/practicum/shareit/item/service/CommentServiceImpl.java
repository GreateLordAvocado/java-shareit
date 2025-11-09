package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingJpaRepository;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentJpaRepository comments;
    private final BookingJpaRepository bookings;

    private final UserService userService;
    private final ItemService itemService;

    @Override
    public CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getText())) {
            throw new ValidationException("Текст комментария не должен быть пустым");
        }

        User author = userService.requireEntity(authorId);
        Item item   = itemService.requireEntity(itemId);

        boolean allowed = bookings.existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                author.getId(), item.getId(), LocalDateTime.now(), BookingStatus.APPROVED
        );
        if (!allowed) {
            throw new ValidationException("Комментировать вещь может только пользователь, " +
                    "который ранее бронировал вещь и завершил бронирование");
        }

        Comment entity = Comment.builder()
                .text(dto.getText().trim())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment saved = comments.save(entity);
        return CommentMapper.toDto(saved);
    }

    @Override
    public List<CommentDto> findByItemId(Long itemId) {
        return comments.findByItem_IdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Override
    public Map<Long, List<CommentDto>> findByItemIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return comments.findByItem_IdInOrderByCreatedDesc(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())
                ));
    }
}
