package services;

import entities.Reservation;
import entities.ReservationStatus;
import utils.DbContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationServiceImpl implements ReservationService {
    private static final String INSERT_QUERY = "INSERT INTO reservations (user_id, route_id, transport_id, date_time, status, price, is_paid, round_trip, return_date_time, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE reservations SET user_id = ?, route_id = ?, transport_id = ?, date_time = ?, status = ?, price = ?, is_paid = ?, round_trip = ?, return_date_time = ?, updated_at = ? WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM reservations WHERE id = ?";
    private static final String SELECT_BY_ID_QUERY = "SELECT * FROM reservations WHERE id = ?";
    private static final String SELECT_ALL_QUERY = "SELECT * FROM reservations";
    private static final String SELECT_BY_USER_ID_QUERY = "SELECT * FROM reservations WHERE user_id = ?";
    private static final String SELECT_BY_USER_ID_AND_STATUS_QUERY = "SELECT * FROM reservations WHERE user_id = ? AND status = ?";
    private static final String SELECT_BY_DRIVER_ID_QUERY = "SELECT * FROM reservations WHERE driver_id = ?";
    private static final String SELECT_BY_DRIVER_ID_AND_STATUS_QUERY = "SELECT * FROM reservations WHERE driver_id = ? AND status = ?";
    private static final String SELECT_BY_STATUS_QUERY = "SELECT * FROM reservations WHERE status = ?";
    private static final String COUNT_ALL_QUERY = "SELECT COUNT(*) FROM reservations";
    private static final String AVG_PRICE_QUERY = "SELECT AVG(price) FROM reservations";
    private static final String COMPLETION_RATE_QUERY = "SELECT (SELECT COUNT(*) FROM reservations WHERE status = 'COMPLETED') / COUNT(*) * 100 FROM reservations";
    
    private final Connection connection;
    private static final Logger LOGGER = Logger.getLogger(ReservationServiceImpl.class.getName());
    
    public ReservationServiceImpl() {
        this.connection = DbContext.getInstance().getConn();
    }
    
    @Override
    public void ajouter(Reservation reservation) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            
            // Set parameters
            statement.setInt(1, reservation.getUserId());
            statement.setInt(2, reservation.getRouteId());
            statement.setInt(3, reservation.getTransportId());
            statement.setTimestamp(4, Timestamp.valueOf(reservation.getDateTime()));
            statement.setString(5, reservation.getStatus());
            statement.setDouble(6, reservation.getPrice());
            statement.setBoolean(7, reservation.getIsPaid());
            statement.setBoolean(8, reservation.isRoundTrip());
            
            if (reservation.getReturnDateTime() != null) {
                statement.setTimestamp(9, Timestamp.valueOf(reservation.getReturnDateTime()));
            } else {
                statement.setNull(9, Types.TIMESTAMP);
            }
            
            statement.setTimestamp(10, Timestamp.valueOf(now));
            statement.setTimestamp(11, Timestamp.valueOf(now));
            
            // Execute and retrieve generated ID
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating reservation failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getInt(1));
                    reservation.setCreatedAt(now);
                    reservation.setUpdatedAt(now);
                } else {
                    throw new SQLException("Creating reservation failed, no ID obtained.");
                }
            }
            
            LOGGER.log(Level.INFO, "Reservation added successfully with ID: {0}", reservation.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding reservation", e);
        }
    }
    
    @Override
    public void supprimer(Reservation reservation) {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_QUERY)) {
            statement.setInt(1, reservation.getId());
            statement.executeUpdate();
            LOGGER.log(Level.INFO, "Reservation deleted successfully with ID: {0}", reservation.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting reservation", e);
        }
    }
    
    @Override
    public void modifier(Reservation reservation) {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_QUERY)) {
            LocalDateTime now = LocalDateTime.now();
            
            // Set parameters
            statement.setInt(1, reservation.getUserId());
            statement.setInt(2, reservation.getRouteId());
            statement.setInt(3, reservation.getTransportId());
            statement.setTimestamp(4, Timestamp.valueOf(reservation.getDateTime()));
            statement.setString(5, reservation.getStatus());
            statement.setDouble(6, reservation.getPrice());
            statement.setBoolean(7, reservation.getIsPaid());
            statement.setBoolean(8, reservation.isRoundTrip());
            
            if (reservation.getReturnDateTime() != null) {
                statement.setTimestamp(9, Timestamp.valueOf(reservation.getReturnDateTime()));
            } else {
                statement.setNull(9, Types.TIMESTAMP);
            }
            
            statement.setTimestamp(10, Timestamp.valueOf(now));
            statement.setInt(11, reservation.getId());
            
            // Execute update
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating reservation failed, no rows affected.");
            }
            
            reservation.setUpdatedAt(now);
            LOGGER.log(Level.INFO, "Reservation updated successfully with ID: {0}", reservation.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation", e);
        }
    }
    
    @Override
    public Reservation afficher(int id) {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservation by ID", e);
        }
        return null;
    }
    
    @Override
    public List<Reservation> afficher_tout() {
        List<Reservation> reservations = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL_QUERY)) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all reservations", e);
        }
        return reservations;
    }
    
    @Override
    public List<Reservation> getReservationsByUserId(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID_QUERY)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations by user ID", e);
        }
        return reservations;
    }
    
    @Override
    public List<Reservation> getReservationsByUserIdAndStatus(int userId, String status) {
        List<Reservation> reservations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID_AND_STATUS_QUERY)) {
            statement.setInt(1, userId);
            statement.setString(2, status);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations by user ID and status", e);
        }
        return reservations;
    }
    
    @Override
    public boolean confirmReservation(Reservation reservation) {
        reservation.setStatus(ReservationStatus.CONFIRMED.name());
        try {
            modifier(reservation);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error confirming reservation", e);
            return false;
        }
    }
    
    @Override
    public boolean cancelReservation(int reservationId) {
        Reservation reservation = afficher(reservationId);
        if (reservation != null) {
            reservation.setStatus(ReservationStatus.CANCELLED.name());
            try {
                modifier(reservation);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
            }
        }
        return false;
    }
    
    @Override
    public int getTotalReservations() {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(COUNT_ALL_QUERY)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total reservations", e);
        }
        return 0;
    }
    
    @Override
    public double getAverageReservationPrice() {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(AVG_PRICE_QUERY)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting average reservation price", e);
        }
        return 0.0;
    }
    
    @Override
    public double getCompletionRate() {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(COMPLETION_RATE_QUERY)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting completion rate", e);
        }
        return 0.0;
    }
    
    @Override
    public List<Reservation> getReservationsByDriverId(int driverId) {
        List<Reservation> reservations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_DRIVER_ID_QUERY)) {
            statement.setInt(1, driverId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations by driver ID", e);
        }
        return reservations;
    }
    
    @Override
    public List<Reservation> getReservationsByDriverIdAndStatus(int driverId, String status) {
        List<Reservation> reservations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_DRIVER_ID_AND_STATUS_QUERY)) {
            statement.setInt(1, driverId);
            statement.setString(2, status);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations by driver ID and status", e);
        }
        return reservations;
    }
    
    @Override
    public List<Reservation> getAllReservations() {
        return afficher_tout();
    }
    
    @Override
    public List<Reservation> getAllReservationsByStatus(String status) {
        List<Reservation> reservations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_BY_STATUS_QUERY)) {
            statement.setString(1, status);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving reservations by status", e);
        }
        return reservations;
    }
    
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setUserId(rs.getInt("user_id"));
        reservation.setRouteId(rs.getInt("route_id"));
        reservation.setTransportId(rs.getInt("transport_id"));
        reservation.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        reservation.setStatus(rs.getString("status"));
        reservation.setPrice(rs.getDouble("price"));
        reservation.setIsPaid(rs.getBoolean("is_paid"));
        reservation.setRoundTrip(rs.getBoolean("round_trip"));
        
        Timestamp returnDateTime = rs.getTimestamp("return_date_time");
        if (returnDateTime != null) {
            reservation.setReturnDateTime(returnDateTime.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            reservation.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            reservation.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return reservation;
    }
} 