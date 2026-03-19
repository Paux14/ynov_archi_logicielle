package com.ynov.coworking.reservation.pattern;

import com.ynov.coworking.reservation.model.Reservation;
import com.ynov.coworking.reservation.model.ReservationStatus;

/** État CONFIRMED : peut être annulée ou complétée. */
public class ConfirmedState implements ReservationState {

    @Override
    public Reservation cancel(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }

    @Override
    public Reservation complete(Reservation reservation) {
        reservation.setStatus(ReservationStatus.COMPLETED);
        return reservation;
    }
}
