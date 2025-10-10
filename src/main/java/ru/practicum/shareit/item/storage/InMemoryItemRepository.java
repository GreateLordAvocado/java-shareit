package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> storage = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public Item save(Item item) {
        long id = seq.incrementAndGet();
        item.setId(id);
        storage.put(id, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return storage.values().stream()
                .filter(i -> Objects.equals(i.getOwnerId(), ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        final String q = text.toLowerCase();
        return storage.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i ->
                        (i.getName() != null && i.getName().toLowerCase().contains(q)) ||
                                (i.getDescription() != null && i.getDescription().toLowerCase().contains(q))
                )
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }
}
