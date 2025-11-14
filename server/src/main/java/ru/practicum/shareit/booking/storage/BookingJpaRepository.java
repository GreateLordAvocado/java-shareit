package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);
    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status
    );

    Optional<Booking> findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status
    );

    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(Long bookerId,
                                                             Long itemId,
                                                             LocalDateTime endBefore,
                                                             BookingStatus status);

    List<Booking> findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now);

    List<Booking> findByItem_OwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_OwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status);

    List<Booking> findByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItem_OwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime now);

    List<Booking> findByItem_OwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now);

    boolean existsByItem_IdAndStatusAndEndGreaterThanAndStartLessThan(
            Long itemId, BookingStatus status, LocalDateTime start, LocalDateTime end);

    Optional<Booking> findByIdAndBooker_Id(Long id, Long bookerId);

    Optional<Booking> findByIdAndItem_OwnerId(Long id, Long ownerId);

    default List<Booking> findCurrentByBooker(Long bookerId, LocalDateTime now) {
        return findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(bookerId, now, now);
    }

    default List<Booking> findPastByBooker(Long bookerId, LocalDateTime now) {
        return findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now);
    }

    default List<Booking> findFutureByBooker(Long bookerId, LocalDateTime now) {
        return findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now);
    }

    default List<Booking> findAllByOwner(Long ownerId) {
        return findByItem_OwnerIdOrderByStartDesc(ownerId);
    }

    default List<Booking> findAllByOwnerAndStatus(Long ownerId, BookingStatus status) {
        return findByItem_OwnerIdAndStatusOrderByStartDesc(ownerId, status);
    }

    default List<Booking> findCurrentByOwner(Long ownerId, LocalDateTime now) {
        return findByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(ownerId, now, now);
    }

    default List<Booking> findPastByOwner(Long ownerId, LocalDateTime now) {
        return findByItem_OwnerIdAndEndBeforeOrderByStartDesc(ownerId, now);
    }

    default List<Booking> findFutureByOwner(Long ownerId, LocalDateTime now) {
        return findByItem_OwnerIdAndStartAfterOrderByStartDesc(ownerId, now);
    }

    default boolean hasApprovedOverlap(Long itemId, LocalDateTime start, LocalDateTime end) {
        return existsByItem_IdAndStatusAndEndGreaterThanAndStartLessThan(
                itemId, BookingStatus.APPROVED, start, end
        );
    }
}
