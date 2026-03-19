package com.ynov.coworking.reservation.web;

import com.ynov.coworking.reservation.model.Reservation;
import com.ynov.coworking.reservation.service.ReservationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<Reservation> list() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public Reservation get(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation create(@RequestBody Reservation reservation) {
        return reservationService.create(reservation);
    }

    @PatchMapping("/{id}/cancel")
    public Reservation cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @PatchMapping("/{id}/complete")
    public Reservation complete(@PathVariable Long id) {
        return reservationService.complete(id);
    }
}
