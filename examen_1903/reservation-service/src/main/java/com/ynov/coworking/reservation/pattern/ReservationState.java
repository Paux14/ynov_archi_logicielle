package com.ynov.coworking.reservation.pattern;

import com.ynov.coworking.reservation.model.Reservation;

/**
 * Pattern State — interface représentant l'état d'une réservation.
 * Chaque état autorise ou interdit les transitions cancel() et complete().
 */
public interface ReservationState {
    Reservation cancel(Reservation reservation);
    Reservation complete(Reservation reservation);
}
