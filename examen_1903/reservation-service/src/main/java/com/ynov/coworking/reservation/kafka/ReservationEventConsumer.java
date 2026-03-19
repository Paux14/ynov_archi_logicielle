package com.ynov.coworking.reservation.kafka;

import com.ynov.coworking.reservation.client.RoomClient;
import com.ynov.coworking.reservation.model.Reservation;
import com.ynov.coworking.reservation.model.ReservationStatus;
import com.ynov.coworking.reservation.repository.ReservationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventConsumer.class);

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RoomClient roomClient;

    public ReservationEventConsumer(ReservationRepository reservationRepository,
                                    KafkaTemplate<String, String> kafkaTemplate,
                                    RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.roomClient = roomClient;
    }

    /** Salle supprimée → annuler toutes les réservations CONFIRMED de cette salle */
    @KafkaListener(topics = "room-deleted", groupId = "reservation-service-group")
    @Transactional
    public void handleRoomDeleted(String roomIdStr) {
        try {
            Long roomId = Long.parseLong(roomIdStr.trim());
            List<Reservation> confirmed = reservationRepository.findByRoomIdAndStatus(
                    roomId, ReservationStatus.CONFIRMED);

            for (Reservation r : confirmed) {
                r.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(r);
                // Notifier member-service pour réévaluer la suspension
                kafkaTemplate.send("reservation-cancelled-for-member",
                        String.valueOf(r.getMemberId()));
                log.info("Réservation {} annulée suite à suppression salle {}", r.getId(), roomId);
            }
        } catch (NumberFormatException e) {
            log.warn("roomId invalide reçu sur room-deleted: {}", roomIdStr);
        }
    }

    /** Membre supprimé → supprimer toutes ses réservations */
    @KafkaListener(topics = "member-deleted", groupId = "reservation-service-group")
    @Transactional
    public void handleMemberDeleted(String memberIdStr) {
        try {
            Long memberId = Long.parseLong(memberIdStr.trim());
            List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

            for (Reservation r : reservations) {
                // Libérer les salles concernées si la réservation était active
                if (r.getStatus() == ReservationStatus.CONFIRMED) {
                    roomClient.setAvailability(r.getRoomId(), true);
                }
                reservationRepository.delete(r);
                log.info("Réservation {} supprimée suite à suppression membre {}", r.getId(), memberId);
            }
        } catch (NumberFormatException e) {
            log.warn("memberId invalide reçu sur member-deleted: {}", memberIdStr);
        }
    }
}
