package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository("itemDbRepository")
@Primary
@RequiredArgsConstructor
public class ItemDbRepository implements ItemRepository {

    private final ItemJpaRepository jpa;

    @Override
    public Item save(Item item) {
        return jpa.save(item);
    }

    @Override
    public Item update(Item item) {
        return jpa.save(item);
    }

    @Override
    public Optional<Item> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return jpa.findByOwnerIdOrderByIdAsc(ownerId);
    }

    @Override
    public List<Item> searchAvailableByText(String text) {
        return jpa.searchAvailableByText(text);
    }
}
