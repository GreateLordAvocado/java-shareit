package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId);

    @Query(
            "select i from Item i " +
                    "where i.available = true and (" +
                    "      lower(i.name) like lower(concat('%', :text, '%')) " +
                    "   or lower(i.description) like lower(concat('%', :text, '%'))" +
                    ") order by i.id asc"
    )
    List<Item> searchAvailableByText(@Param("text") String text);

    List<Item> findByRequestIdOrderByIdAsc(Long requestId);

    List<Item> findByRequestIdInOrderByIdAsc(Collection<Long> requestIds);

    default List<Item> findByOwnerId(Long ownerId) {
        return findByOwnerIdOrderByIdAsc(ownerId);
    }

    default List<Item> findByRequestId(Long requestId) {
        return findByRequestIdOrderByIdAsc(requestId);
    }

    Optional<Item> findByIdAndOwnerId(Long id, Long ownerId);
}
