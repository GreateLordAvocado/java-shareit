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
        if (!storage.containsKey(item.getId())) {
            throw new NoSuchElementException("Вещь с id=" + item.getId() + " не найдена");
        }
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
        if (!StringUtils.hasText(text)) return List.of();
        final String q = text.toLowerCase(Locale.ROOT);

        return storage.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> {
                    String name = i.getName() == null ? "" : i.getName().toLowerCase(Locale.ROOT);
                    String desc = i.getDescription() == null ? "" : i.getDescription().toLowerCase(Locale.ROOT);
                    return name.contains(q) || desc.contains(q);
                })
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }
}
