package com.ynov.coworking.reservation.pattern;

import com.ynov.coworking.reservation.model.Reservation;

/** État COMPLETED : transitions interdites. */
public class CompletedState implements ReservationState {

    @Override
    public Reservation cancel(Reservation reservation) {
        throw new IllegalStateException("Impossible d'annuler une réservation déjà complétée.");
    }

    @Override
    public Reservation complete(Reservation reservation) {
        throw new IllegalStateException("La réservation est déjà complétée.");
    }
}
