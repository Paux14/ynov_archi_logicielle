package com.ynov.coworking.reservation.pattern;

import com.ynov.coworking.reservation.model.Reservation;

/** État CANCELLED : transitions interdites. */
public class CancelledState implements ReservationState {

    @Override
    public Reservation cancel(Reservation reservation) {
        throw new IllegalStateException("La réservation est déjà annulée.");
    }

    @Override
    public Reservation complete(Reservation reservation) {
        throw new IllegalStateException("Impossible de compléter une réservation annulée.");
    }
}
