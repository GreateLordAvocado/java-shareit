package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequest toEntity(Long requesterId, String description) {
        return ItemRequest.builder()
                .description(description)
                .requesterId(requesterId)
                .build();
    }

    public ItemRequestDto toDto(ItemRequest entity, List<Item> answers) {
        Objects.requireNonNull(entity, "ItemRequest entity must not be null");
        return ItemRequestDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .created(entity.getCreated())
                .items(answers == null ? List.of()
                        : answers.stream().map(ItemRequestMapper::toAnswer).toList())
                .build();
    }

    public List<ItemRequestDto> toDtoList(List<ItemRequest> requests,
                                          Map<Long, List<Item>> itemsByRequestId) {
        return requests.stream()
                .map(r -> toDto(r, itemsByRequestId.getOrDefault(r.getId(), List.of())))
                .toList();
    }

    public Map<Long, List<Item>> groupByRequestId(List<Item> items) {
        return items.stream().collect(Collectors.groupingBy(
                Item::getRequestId,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    private ItemRequestDto.ItemAnswerDto toAnswer(Item item) {
        return ItemRequestDto.ItemAnswerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwnerId())
                .build();
    }
}
