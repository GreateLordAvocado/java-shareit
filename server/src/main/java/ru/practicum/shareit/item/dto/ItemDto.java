package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;
    Long requestId;
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
    List<CommentDto> comments;

    @Value
    @Builder
    public static class BookingShortDto {
        Long id;
        Long bookerId;
        LocalDateTime start;
        LocalDateTime end;
    }
}
