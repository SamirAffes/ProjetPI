package services;

import entities.Transport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportService implements CRUD<Transport> {
    private static final Logger logger = LoggerFactory.getLogger(TransportService.class);

    @Override
    public void ajouter(Transport transport) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(transport);
            transaction.commit();
            logger.info("Added transport with ID: {}", transport.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error adding transport", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(Transport transport) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            if (!entityManager.contains(transport)) {
                transport = entityManager.merge(transport);
            }
            entityManager.remove(transport);
            transaction.commit();
            logger.info("Removed transport with ID: {}", transport.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error removing transport", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(Transport transport) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(transport);
            transaction.commit();
            logger.info("Updated transport with ID: {}", transport.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating transport", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Transport afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            Transport transport = entityManager.find(Transport.class, id);
            if (transport != null) {
                transport.initializeProperties();
            }
            return transport;
        } catch (Exception e) {
            logger.error("Error finding transport with ID: {}", id, e);
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Transport> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Transport> query = entityManager.createQuery("SELECT t FROM Transport t", Transport.class);
            List<Transport> transports = query.getResultList();
            
            for (Transport transport : transports) {
                transport.initializeProperties();
            }
            
            return transports;
        } catch (Exception e) {
            logger.error("Error finding all transports", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    // Additional method for UI
    public List<Transport> findAll() {
        return afficher_tout();
    }
} 