package services;

import entities.Route;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteService implements CRUD<Route> {
    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    @Override
    public void ajouter(Route route) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(route);
            transaction.commit();
            logger.info("Added route with ID: {}", route.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error adding route", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(Route route) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            if (!entityManager.contains(route)) {
                route = entityManager.merge(route);
            }
            entityManager.remove(route);
            transaction.commit();
            logger.info("Removed route with ID: {}", route.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error removing route", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(Route route) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(route);
            transaction.commit();
            logger.info("Updated route with ID: {}", route.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating route", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Route afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            Route route = entityManager.find(Route.class, id);
            if (route != null) {
                route.initializeProperties();
            }
            return route;
        } catch (Exception e) {
            logger.error("Error finding route with ID: {}", id, e);
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Route> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Route> query = entityManager.createQuery("SELECT r FROM Route r", Route.class);
            List<Route> routes = query.getResultList();
            
            for (Route route : routes) {
                route.initializeProperties();
            }
            
            return routes;
        } catch (Exception e) {
            logger.error("Error finding all routes", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    // Additional method for UI
    public List<Route> findAll() {
        return afficher_tout();
    }
} 