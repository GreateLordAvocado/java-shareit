package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDto {

    @NotBlank(message = "Текст комментария не должен быть пустым")
    @Size(max = 1000, message = "Текст комментария не должен превышать 1000 символов")
    private String text;
}
