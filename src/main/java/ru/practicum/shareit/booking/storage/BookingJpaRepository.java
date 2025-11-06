package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingJpaRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("""
        select b from Booking b
        where b.booker.id = :bookerId
          and b.start <= :now and b.end >= :now
        order by b.start desc
        """)
    List<Booking> findCurrentByBooker(Long bookerId, LocalDateTime now);

    @Query("""
        select b from Booking b
        where b.booker.id = :bookerId
          and b.end < :now
        order by b.start desc
        """)
    List<Booking> findPastByBooker(Long bookerId, LocalDateTime now);

    @Query("""
        select b from Booking b
        where b.booker.id = :bookerId
          and b.start > :now
        order by b.start desc
        """)
    List<Booking> findFutureByBooker(Long bookerId, LocalDateTime now);

    @Query("""
        select b from Booking b
        where b.item.ownerId = :ownerId
        order by b.start desc
        """)
    List<Booking> findAllByOwner(Long ownerId);

    @Query("""
        select b from Booking b
        where b.item.ownerId = :ownerId
          and b.status = :status
        order by b.start desc
        """)
    List<Booking> findAllByOwnerAndStatus(Long ownerId, BookingStatus status);

    @Query("""
        select b from Booking b
        where b.item.ownerId = :ownerId
          and b.start <= :now and b.end >= :now
        order by b.start desc
        """)
    List<Booking> findCurrentByOwner(Long ownerId, LocalDateTime now);

    @Query("""
        select b from Booking b
        where b.item.ownerId = :ownerId
          and b.end < :now
        order by b.start desc
        """)
    List<Booking> findPastByOwner(Long ownerId, LocalDateTime now);

    @Query("""
        select b from Booking b
        where b.item.ownerId = :ownerId
          and b.start > :now
        order by b.start desc
        """)
    List<Booking> findFutureByOwner(Long ownerId, LocalDateTime now);

    @Query("""
        select count(b) > 0 from Booking b
        where b.item.id = :itemId
          and b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED
          and not (b.end <= :start or b.start >= :end)
        """)
    boolean hasApprovedOverlap(Long itemId,
                               LocalDateTime start,
                               LocalDateTime end);

    boolean existsByBooker_IdAndItem_IdAndEndBeforeAndStatus(Long bookerId,
                                                             Long itemId,
                                                             LocalDateTime endBefore,
                                                             BookingStatus status);

    Optional<Booking> findFirstByItem_IdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status
    );

    Optional<Booking> findFirstByItem_IdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status
    );
}
