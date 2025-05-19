package services;

import entities.Station;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationService implements CRUD<Station> {
    private static final Logger logger = LoggerFactory.getLogger(StationService.class);

    @Override
    public void ajouter(Station station) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(station);
            transaction.commit();
            logger.info("Added station with ID: {}", station.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error adding station", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(Station station) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            if (!entityManager.contains(station)) {
                station = entityManager.merge(station);
            }
            entityManager.remove(station);
            transaction.commit();
            logger.info("Removed station with ID: {}", station.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error removing station", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(Station station) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(station);
            transaction.commit();
            logger.info("Updated station with ID: {}", station.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating station", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Station afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            Station station = entityManager.find(Station.class, id);
            return station;
        } catch (Exception e) {
            logger.error("Error finding station with ID: {}", id, e);
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Station> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Station> query = entityManager.createQuery(
                "SELECT s FROM Station s", Station.class);
            List<Station> stations = query.getResultList();
            return stations;
        } catch (Exception e) {
            logger.error("Error finding all stations", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find stations by city
     */
    public List<Station> findByCity(String city) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Station> query = entityManager.createQuery(
                "SELECT s FROM Station s WHERE s.city = :city", 
                Station.class);
            query.setParameter("city", city);
            
            List<Station> stations = query.getResultList();
            logger.info("Found {} stations in city: {}", 
                       stations.size(), city);
            return stations;
        } catch (Exception e) {
            logger.error("Error finding stations in city: {}", city, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find stations by type
     */
    public List<Station> findByType(String stationType) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Station> query = entityManager.createQuery(
                "SELECT s FROM Station s WHERE s.stationType = :stationType", 
                Station.class);
            query.setParameter("stationType", stationType);
            
            List<Station> stations = query.getResultList();
            logger.info("Found {} stations with type: {}", 
                       stations.size(), stationType);
            return stations;
        } catch (Exception e) {
            logger.error("Error finding stations with type: {}", stationType, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find stations by organization ID
     */
    public List<Station> findByOrganisationId(int organisationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Station> query = entityManager.createQuery(
                "SELECT s FROM Station s WHERE s.organisationId = :organisationId", 
                Station.class);
            query.setParameter("organisationId", organisationId);
            
            List<Station> stations = query.getResultList();
            logger.info("Found {} stations for organisation ID: {}", 
                       stations.size(), organisationId);
            return stations;
        } catch (Exception e) {
            logger.error("Error finding stations for organisation ID: {}", organisationId, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
} 