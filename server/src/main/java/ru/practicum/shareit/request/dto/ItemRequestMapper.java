package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toEntity(Long requesterId, String description) {
        return ItemRequest.builder()
                .description(description == null ? null : description.trim())
                .requesterId(requesterId)
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest entity, List<Item> answers) {
        return ItemRequestDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .created(entity.getCreated())
                .items(answers == null ? List.of()
                        : answers.stream().map(ItemRequestMapper::toAnswer).toList())
                .build();
    }

    public static List<ItemRequestDto> toDtoList(List<ItemRequest> requests,
                                                 Map<Long, List<Item>> itemsByRequestId) {
        return requests.stream()
                .map(r -> toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    public static Map<Long, List<Item>> groupByRequestId(List<Item> items) {
        if (items == null || items.isEmpty()) return Map.of();
        return items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private static ItemRequestDto.ItemAnswerDto toAnswer(Item item) {
        return ItemRequestDto.ItemAnswerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .build();
    }
}
