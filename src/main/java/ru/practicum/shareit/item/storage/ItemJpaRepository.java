package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("""
            select i from Item i
            where i.available = true
              and (
                    lower(i.name) like lower(concat('%', :text, '%'))
                 or lower(i.description) like lower(concat('%', :text, '%'))
              )
            order by i.id asc
            """)
    List<Item> searchAvailableByText(String text);

    default List<Item> findByOwnerId(Long ownerId) {
        return findByOwnerIdOrderByIdAsc(ownerId);
    }
}
