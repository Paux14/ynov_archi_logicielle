package com.ynov.coworking.room.repository;

import com.ynov.coworking.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}

