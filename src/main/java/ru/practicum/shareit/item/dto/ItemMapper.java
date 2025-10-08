package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemDto toDto(Item i) {
        if (i == null) return null;
        return ItemDto.builder()
                .id(i.getId())
                .name(i.getName())
                .description(i.getDescription())
                .available(i.getAvailable())
                .ownerId(i.getOwnerId())
                .requestId(i.getRequestId())
                .build();
    }

    public static Item fromDto(ItemDto d) {
        if (d == null) return null;
        return Item.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .available(d.getAvailable())
                .ownerId(d.getOwnerId())
                .requestId(d.getRequestId())
                .build();
    }
}
