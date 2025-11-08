package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Comment;

public class CommentMapper {

    public static CommentDto toDto(Comment c) {
        if (c == null) {
            return null;
        }
        return CommentDto.builder()
                .id(c.getId())
                .text(c.getText())
                .authorName(c.getAuthor() != null ? c.getAuthor().getName() : null)
                .created(c.getCreated())
                .build();
    }
}
