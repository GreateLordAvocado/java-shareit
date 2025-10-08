package ru.practicum.shareit.item.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;

    // по ТЗ: пока ссылки храним как id (до БД)
    private Long ownerId;
    private Long requestId; // может быть null, если не по запросу
}
