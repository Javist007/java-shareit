package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker
            WHERE b.booker.id = :bookerId ORDER BY b.start DESC""")
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);

    @Query(value = """
            SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker
            WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC""")
    List<Booking> findByItemOwnerIdOrderByStartDesc(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT b FROM Booking b JOIN FETCH b.item JOIN FETCH b.booker
            WHERE b.item.id IN :itemIds AND b.status = 'APPROVED'
            ORDER BY b.item.id, b.start DESC""")
    List<Booking> findAllApprovedForItems(@Param("itemIds") List<Long> itemIds);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b
            WHERE b.item.id = :itemId AND b.booker.id = :userId
            AND b.status = 'APPROVED' AND b.end < CURRENT_TIMESTAMP""")
    boolean existsByItemIdAndBookerIdAndApprovedAndEndBefore(@Param("itemId") Long itemId,
                                                             @Param("userId") Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b
            WHERE b.item.id = :itemId AND b.status = 'APPROVED'
            AND b.start < :end AND b.end > :start""")
    boolean existsOverlappingBooking(@Param("itemId") Long itemId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
