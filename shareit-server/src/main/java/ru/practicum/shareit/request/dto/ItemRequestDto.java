package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ItemRequestDto {
    Long id;
    String description;
    LocalDateTime created;
    List<ItemAnswerDto> items;

    @Value
    @Builder
    public static class ItemAnswerDto {
        Long id;
        String name;
        Long ownerId;
    }
}
