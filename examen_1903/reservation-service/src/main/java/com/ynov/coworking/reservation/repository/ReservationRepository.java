package com.ynov.coworking.reservation.repository;

import com.ynov.coworking.reservation.model.Reservation;
import com.ynov.coworking.reservation.model.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    List<Reservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);

    /** Vérifie un chevauchement de créneau pour une salle donnée */
    @Query("SELECT r FROM Reservation r WHERE r.roomId = :roomId " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startDateTime < :end AND r.endDateTime > :start")
    List<Reservation> findOverlapping(@Param("roomId") Long roomId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}
