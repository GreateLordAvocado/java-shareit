package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@Jacksonized
public class ItemRequestDto {
    Long id;
    String description;
    LocalDateTime created;

    @Singular("item")
    List<ItemAnswerDto> items;

    @Value
    @Builder
    @Jacksonized
    public static class ItemAnswerDto {
        Long id;
        String name;
        Long ownerId;
    }
}
