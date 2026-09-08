package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
            SELECT DISTINCT i FROM Item i
            LEFT JOIN FETCH i.bookings b
            WHERE i.owner.id = :ownerId
              AND (b.status = 'APPROVED' OR b IS NULL)
            """)
    List<Item> findByOwnerIdWithApprovedBookings(@Param("ownerId") Long ownerId);

    @Query("""
            SELECT i FROM Item i
            WHERE i.available = true AND
            (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) OR
            LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))
            """)
    List<Item> search(@Param("text") String text);
}
