package services;

import entities.Reservation;
import entities.ReservationStatus;
import entities.Route;
import entities.Transport;
import entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationServiceImpl implements ReservationService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);
    private final RouteService routeService;
    private final TransportService transportService;
    private final UserService userService;

    public ReservationServiceImpl() {
        this.routeService = new RouteService();
        this.transportService = new TransportService();
        this.userService = new UserService();
    }

    @Override
    public void ajouter(Reservation reservation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // Set creation timestamp if not already set
            if (reservation.getCreatedAt() == null) {
                reservation.setCreatedAt(new java.util.Date());
            }
            
            // Set updated timestamp
            reservation.setUpdatedAt(new java.util.Date());
            
            // Make sure paid and isPaid are synchronized
            reservation.setPaid(reservation.isPaid());
            
            entityManager.persist(reservation);
            transaction.commit();
            logger.info("Added reservation with ID: {}", reservation.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error adding reservation", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(Reservation reservation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            if (!entityManager.contains(reservation)) {
                reservation = entityManager.merge(reservation);
            }
            entityManager.remove(reservation);
            transaction.commit();
            logger.info("Removed reservation with ID: {}", reservation.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error removing reservation", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(Reservation reservation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            // Update the timestamp
            reservation.setUpdatedAt(new java.util.Date());
            
            // Make sure paid and isPaid are synchronized
            reservation.setPaid(reservation.isPaid());
            
            entityManager.merge(reservation);
            transaction.commit();
            logger.info("Updated reservation with ID: {}", reservation.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating reservation", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Reservation afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            Reservation reservation = entityManager.find(Reservation.class, id);
            if (reservation != null) {
                enrichReservation(reservation);
            }
            return reservation;
        } catch (Exception e) {
            logger.error("Error finding reservation with ID: {}", id, e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Reservation> afficher_tout() {
        return getAllReservations();
    }

    @Override
    public List<Reservation> getReservationsByUserId(int userId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.userId = :userId ORDER BY r.dateTime DESC", 
                Reservation.class
            );
            query.setParameter("userId", userId);
            
            List<Reservation> reservations = query.getResultList();
            for (Reservation r : reservations) {
                enrichReservation(r);
            }
            return reservations;
        } catch (Exception e) {
            logger.error("Error finding reservations for user ID: {}", userId, e);
            return new ArrayList<>();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Reservation> getReservationsByUserIdAndStatus(int userId, String status) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.userId = :userId AND r.status = :status ORDER BY r.dateTime DESC", 
                Reservation.class
            );
            query.setParameter("userId", userId);
            query.setParameter("status", status);
            
            List<Reservation> reservations = query.getResultList();
            for (Reservation r : reservations) {
                enrichReservation(r);
            }
            return reservations;
        } catch (Exception e) {
            logger.error("Error finding reservations for user ID: {} with status: {}", userId, status, e);
            return new ArrayList<>();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean confirmReservation(Reservation reservation) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            reservation.setStatus(ReservationStatus.CONFIRMED.name());
            reservation.setUpdatedAt(new java.util.Date());
            
            // Make sure paid and isPaid are synchronized
            reservation.setPaid(reservation.isPaid());
            
            entityManager.merge(reservation);
            transaction.commit();
            logger.info("Confirmed reservation with ID: {}", reservation.getId());
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error confirming reservation with ID: {}", reservation.getId(), e);
            return false;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean cancelReservation(int reservationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            
            Reservation reservation = entityManager.find(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.setStatus(ReservationStatus.CANCELED.name());
                reservation.setUpdatedAt(new java.util.Date());
                
                // Make sure paid and isPaid are synchronized
                reservation.setPaid(reservation.isPaid());
                
                entityManager.merge(reservation);
                transaction.commit();
                logger.info("Canceled reservation with ID: {}", reservationId);
                return true;
            } else {
                transaction.rollback();
                logger.warn("Cannot cancel reservation - ID not found: {}", reservationId);
                return false;
            }
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error canceling reservation with ID: {}", reservationId, e);
            return false;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public int getTotalReservations() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            return entityManager.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                .getSingleResult().intValue();
        } catch (Exception e) {
            logger.error("Error getting total reservation count", e);
            return 0;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public double getAverageReservationPrice() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            return entityManager.createQuery("SELECT AVG(r.price) FROM Reservation r", Double.class)
                .getSingleResult();
        } catch (Exception e) {
            logger.error("Error getting average reservation price", e);
            return 0.0;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public double getCompletionRate() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            Long total = entityManager.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class)
                .getSingleResult();
            
            if (total == 0) {
                return 0.0;
            }
            
            Long completed = entityManager.createQuery(
                "SELECT COUNT(r) FROM Reservation r WHERE r.status = :status", 
                Long.class
            )
                .setParameter("status", ReservationStatus.COMPLETED.name())
                .getSingleResult();
            
            return (completed.doubleValue() / total.doubleValue()) * 100.0;
        } catch (Exception e) {
            logger.error("Error getting reservation completion rate", e);
            return 0.0;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Reservation> getReservationsByDriverId(int driverId) {
        // Implementation would depend on driver-transport relationship
        // For now returning empty list
        return new ArrayList<>();
    }

    @Override
    public List<Reservation> getReservationsByDriverIdAndStatus(int driverId, String status) {
        // Implementation would depend on driver-transport relationship
        // For now returning empty list
        return new ArrayList<>();
    }

    @Override
    public List<Reservation> getAllReservations() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r ORDER BY r.dateTime DESC", 
                Reservation.class
            );
            
            List<Reservation> reservations = query.getResultList();
            for (Reservation r : reservations) {
                enrichReservation(r);
            }
            return reservations;
        } catch (Exception e) {
            logger.error("Error finding all reservations", e);
            return new ArrayList<>();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Reservation> getAllReservationsByStatus(String status) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Reservation> query = entityManager.createQuery(
                "SELECT r FROM Reservation r WHERE r.status = :status ORDER BY r.dateTime DESC", 
                Reservation.class
            );
            query.setParameter("status", status);
            
            List<Reservation> reservations = query.getResultList();
            for (Reservation r : reservations) {
                enrichReservation(r);
            }
            return reservations;
        } catch (Exception e) {
            logger.error("Error finding reservations with status: {}", status, e);
            return new ArrayList<>();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Reservation> findByUserId(int userId) {
        return getReservationsByUserId(userId);
    }

    @Override
    public List<Reservation> findAll() {
        return getAllReservations();
    }

    @Override
    public boolean update(Reservation reservation) {
        try {
            modifier(reservation);
            return true;
        } catch (Exception e) {
            logger.error("Error updating reservation", e);
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        try {
            Reservation reservation = afficher(id);
            if (reservation != null) {
                supprimer(reservation);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting reservation", e);
            return false;
        }
    }
    
    private void enrichReservation(Reservation reservation) {
        try {
            // Load route information
            Route route = routeService.afficher(reservation.getRouteId());
            reservation.setRoute(route);
            
            // Load transport information
            Transport transport = transportService.afficher(reservation.getTransportId());
            reservation.setTransport(transport);
            
            // Load user information
            User user = userService.afficher(reservation.getUserId());
            reservation.setUser(user);
            
            // Initialize JavaFX properties
            reservation.initializeProperties();
        } catch (Exception e) {
            logger.error("Error enriching reservation data for ID: {}", reservation.getId(), e);
        }
    }
} 