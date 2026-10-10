package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b "
            + "WHERE b.booker.id = :bookerId "
            + "ORDER BY b.start DESC")
    List<Booking> findAllByBookerId(@Param("bookerId") Long bookerId);

    @Query("SELECT b FROM Booking b "
            + "WHERE b.item.owner.id = :ownerId "
            + "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(b) > 0 FROM Booking b "
            + "WHERE b.item.id = :itemId "
            + "AND b.status = :status "
            + "AND b.start < :end "
            + "AND b.end > :start")
    boolean existsOverlap(@Param("itemId") Long itemId,
                          @Param("status") BookingStatus status,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b JOIN FETCH b.booker "
            + "WHERE b.item.id IN :itemIds AND b.status = :status")
    List<Booking> findApprovedByItemIds(@Param("itemIds") Collection<Long> itemIds,
                                        @Param("status") BookingStatus status);

    boolean existsByItemIdAndBookerIdAndStatusAndEndBefore(Long itemId,
                                                           Long bookerId,
                                                           BookingStatus status,
                                                           LocalDateTime end);
}
