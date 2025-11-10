package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@Jacksonized
public class ItemDto {
    Long id;

    @NotBlank(message = "Название вещи не должно быть пустым")
    String name;

    @NotBlank(message = "Описание вещи не должно быть пустым")
    String description;

    @NotNull(message = "Поле доступности вещи (available) должно быть указано")
    Boolean available;

    Long ownerId;
    Long requestId;
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;

    @Singular("comment")
    List<CommentDto> comments;

    @Value
    @Builder
    @Jacksonized
    public static class BookingShortDto {
        Long id;
        Long bookerId;
        LocalDateTime start;
        LocalDateTime end;
    }
}
