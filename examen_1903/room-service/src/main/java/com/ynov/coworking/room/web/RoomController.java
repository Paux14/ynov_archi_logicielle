package com.ynov.coworking.room.web;

import com.ynov.coworking.room.model.Room;
import com.ynov.coworking.room.service.RoomService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<Room> list() {
        return roomService.findAll();
    }

    @GetMapping("/{id}")
    public Room get(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room create(@RequestBody Room room) {
        return roomService.create(room);
    }

    @PutMapping("/{id}")
    public Room update(@PathVariable Long id, @RequestBody Room room) {
        return roomService.update(id, room);
    }

    @PatchMapping("/{id}/availability")
    public Room setAvailability(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object availableRaw = body.get("available");
        boolean available = availableRaw instanceof Boolean b && b;
        if (availableRaw == null) {
            available = false;
        } else if (!(availableRaw instanceof Boolean)) {
            available = Boolean.parseBoolean(String.valueOf(availableRaw));
        }
        return roomService.setAvailability(id, available);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}

