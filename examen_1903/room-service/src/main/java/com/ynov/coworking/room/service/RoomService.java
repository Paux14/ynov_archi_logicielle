package com.ynov.coworking.room.service;

import com.ynov.coworking.room.exception.ResourceNotFoundException;
import com.ynov.coworking.room.model.Room;
import com.ynov.coworking.room.repository.RoomRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RoomService(RoomRepository roomRepository,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.roomRepository = roomRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + id + " introuvable"));
    }

    @Transactional
    public Room create(Room room) {
        room.setId(null);
        return roomRepository.save(room);
    }

    @Transactional
    public Room update(Long id, Room input) {
        Room existing = findById(id);
        existing.setName(input.getName());
        existing.setCity(input.getCity());
        existing.setCapacity(input.getCapacity());
        existing.setType(input.getType());
        existing.setHourlyRate(input.getHourlyRate());
        existing.setAvailable(input.isAvailable());
        return roomRepository.save(existing);
    }

    @Transactional
    public Room setAvailability(Long id, boolean available) {
        Room existing = findById(id);
        existing.setAvailable(available);
        return roomRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Room " + id + " introuvable");
        }
        // Notifier reservation-service d'annuler toutes les réservations CONFIRMED
        try {
            kafkaTemplate.send("room-deleted", String.valueOf(id));
        } catch (Exception e) {
            log.warn("Kafka indisponible — événement room-deleted non envoyé: {}", e.getMessage());
        }
        roomRepository.deleteById(id);
    }
}

