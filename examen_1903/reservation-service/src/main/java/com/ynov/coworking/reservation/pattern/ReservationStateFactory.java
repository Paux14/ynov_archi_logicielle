package com.ynov.coworking.reservation.pattern;

import com.ynov.coworking.reservation.model.ReservationStatus;

/** Factory pour obtenir l'état courant selon le statut. */
public class ReservationStateFactory {

    private ReservationStateFactory() {}

    public static ReservationState of(ReservationStatus status) {
        return switch (status) {
            case CONFIRMED -> new ConfirmedState();
            case CANCELLED -> new CancelledState();
            case COMPLETED -> new CompletedState();
        };
    }
}
