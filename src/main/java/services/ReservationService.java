package services;

import entities.Reservation;
import entities.ReservationStatus;

import java.util.List;

public interface ReservationService extends CRUD<Reservation> {
    List<Reservation> getReservationsByUserId(int userId);
    List<Reservation> getReservationsByUserIdAndStatus(int userId, String status);
    boolean confirmReservation(Reservation reservation);
    boolean cancelReservation(int reservationId);

    // Statistics methods for dashboard
    int getTotalReservations();
    double getAverageReservationPrice();
    double getCompletionRate();

    // Methods for driver and admin operations
    List<Reservation> getReservationsByDriverId(int driverId);
    List<Reservation> getReservationsByDriverIdAndStatus(int driverId, String status);
    List<Reservation> getAllReservations(); // For admin use
    List<Reservation> getAllReservationsByStatus(String status); // For admin use
    
    // Added methods for compatibility with UI
    List<Reservation> findByUserId(int userId);
    List<Reservation> findAll();
    boolean update(Reservation reservation);
    boolean delete(int id);
}
