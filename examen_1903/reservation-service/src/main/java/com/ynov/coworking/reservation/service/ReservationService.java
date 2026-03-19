package com.ynov.coworking.reservation.service;

import com.ynov.coworking.reservation.client.MemberClient;
import com.ynov.coworking.reservation.client.RoomClient;
import com.ynov.coworking.reservation.exception.BusinessException;
import com.ynov.coworking.reservation.exception.ResourceNotFoundException;
import com.ynov.coworking.reservation.model.Reservation;
import com.ynov.coworking.reservation.model.ReservationStatus;
import com.ynov.coworking.reservation.pattern.ReservationStateFactory;
import com.ynov.coworking.reservation.repository.ReservationRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final MemberClient memberClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public ReservationService(ReservationRepository reservationRepository,
                               RoomClient roomClient,
                               MemberClient memberClient,
                               KafkaTemplate<String, String> kafkaTemplate) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.memberClient = memberClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation " + id + " introuvable"));
    }

    @Transactional
    public Reservation create(Reservation input) {
        // 1. Vérifier que la salle est disponible
        if (!roomClient.isAvailable(input.getRoomId())) {
            throw new BusinessException("La salle " + input.getRoomId() + " n'est pas disponible.");
        }

        // 2. Vérifier qu'il n'y a pas de chevauchement de créneaux
        var overlapping = reservationRepository.findOverlapping(
                input.getRoomId(), input.getStartDateTime(), input.getEndDateTime());
        if (!overlapping.isEmpty()) {
            throw new BusinessException("La salle est déjà réservée sur ce créneau.");
        }

        // 3. Vérifier que le membre n'est pas suspendu
        if (memberClient.isSuspended(input.getMemberId())) {
            throw new BusinessException("Le membre " + input.getMemberId()
                    + " est suspendu et ne peut pas réserver.");
        }

        // 4. Créer la réservation CONFIRMED
        input.setId(null);
        input.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(input);

        // 5. Marquer la salle comme indisponible
        roomClient.setAvailability(input.getRoomId(), false);

        // 6. Vérifier si le quota du membre est atteint → suspension
        long activeCount = reservationRepository
                .findByMemberIdAndStatus(input.getMemberId(), ReservationStatus.CONFIRMED)
                .size();
        int max = memberClient.getMaxConcurrentBookings(input.getMemberId());
        if (activeCount >= max) {
            sendKafka("member-suspend", String.valueOf(input.getMemberId()));
        }

        return saved;
    }

    @Transactional
    public Reservation cancel(Long id) {
        Reservation reservation = findById(id);
        var state = ReservationStateFactory.of(reservation.getStatus());
        Reservation updated = state.cancel(reservation);
        Reservation saved = reservationRepository.save(updated);

        roomClient.setAvailability(reservation.getRoomId(), true);

        long activeCount = reservationRepository
                .findByMemberIdAndStatus(reservation.getMemberId(), ReservationStatus.CONFIRMED)
                .size();
        int max = memberClient.getMaxConcurrentBookings(reservation.getMemberId());
        if (activeCount < max) {
            sendKafka("member-unsuspend", String.valueOf(reservation.getMemberId()));
        }

        return saved;
    }

    @Transactional
    public Reservation complete(Long id) {
        Reservation reservation = findById(id);
        var state = ReservationStateFactory.of(reservation.getStatus());
        Reservation updated = state.complete(reservation);
        Reservation saved = reservationRepository.save(updated);

        roomClient.setAvailability(reservation.getRoomId(), true);

        long activeCount = reservationRepository
                .findByMemberIdAndStatus(reservation.getMemberId(), ReservationStatus.CONFIRMED)
                .size();
        int max = memberClient.getMaxConcurrentBookings(reservation.getMemberId());
        if (activeCount < max) {
            sendKafka("member-unsuspend", String.valueOf(reservation.getMemberId()));
        }

        return saved;
    }

    private void sendKafka(String topic, String payload) {
        try {
            kafkaTemplate.send(topic, payload);
        } catch (Exception e) {
            log.warn("Kafka indisponible — événement non envoyé sur '{}': {}", topic, e.getMessage());
        }
    }
}
