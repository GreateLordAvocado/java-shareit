package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;

import java.util.List;
import java.util.Map;

public interface CommentService {
    CommentDto addComment(Long authorId, Long itemId, CommentCreateDto dto);
    List<CommentDto> findByItemId(Long itemId);
    Map<Long, List<CommentDto>> findByItemIds(List<Long> itemIds);
}
