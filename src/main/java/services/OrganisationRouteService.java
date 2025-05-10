package services;

import entities.OrganisationRoute;
import entities.Route;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganisationRouteService implements CRUD<OrganisationRoute> {
    private static final Logger logger = LoggerFactory.getLogger(OrganisationRouteService.class);
    private final RouteService routeService = new RouteService();

    @Override
    public void ajouter(OrganisationRoute organisationRoute) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.persist(organisationRoute);
            transaction.commit();
            logger.info("Added organisation route mapping with ID: {}", organisationRoute.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error adding organisation route mapping", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void supprimer(OrganisationRoute organisationRoute) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            if (!entityManager.contains(organisationRoute)) {
                organisationRoute = entityManager.merge(organisationRoute);
            }
            entityManager.remove(organisationRoute);
            transaction.commit();
            logger.info("Removed organisation route mapping with ID: {}", organisationRoute.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error removing organisation route mapping", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void modifier(OrganisationRoute organisationRoute) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        
        try {
            transaction.begin();
            entityManager.merge(organisationRoute);
            transaction.commit();
            logger.info("Updated organisation route mapping with ID: {}", organisationRoute.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error updating organisation route mapping", e);
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public OrganisationRoute afficher(int id) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            OrganisationRoute organisationRoute = entityManager.find(OrganisationRoute.class, id);
            return organisationRoute;
        } catch (Exception e) {
            logger.error("Error finding organisation route mapping with ID: {}", id, e);
            return null;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<OrganisationRoute> afficher_tout() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<OrganisationRoute> query = entityManager.createQuery(
                "SELECT or FROM OrganisationRoute or", OrganisationRoute.class);
            List<OrganisationRoute> organisationRoutes = query.getResultList();
            return organisationRoutes;
        } catch (Exception e) {
            logger.error("Error finding all organisation route mappings", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find all routes assigned to a specific organisation
     */
    public List<Route> findRoutesByOrganisationId(int organisationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Route> routes = new ArrayList<>();
        
        try {
            TypedQuery<OrganisationRoute> query = entityManager.createQuery(
                "SELECT or FROM OrganisationRoute or WHERE or.organisationId = :orgId", 
                OrganisationRoute.class);
            query.setParameter("orgId", organisationId);
            
            List<OrganisationRoute> organisationRoutes = query.getResultList();
            
            for (OrganisationRoute orgRoute : organisationRoutes) {
                Route route = routeService.afficher(orgRoute.getRouteId());
                if (route != null) {
                    routes.add(route);
                }
            }
            
            logger.info("Found {} routes for organisation ID: {}", routes.size(), organisationId);
            return routes;
        } catch (Exception e) {
            logger.error("Error finding routes for organisation ID: {}", organisationId, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find all organisation routes by organisation ID
     */
    public List<OrganisationRoute> findByOrganisationId(int organisationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<OrganisationRoute> query = entityManager.createQuery(
                "SELECT or FROM OrganisationRoute or WHERE or.organisationId = :orgId", 
                OrganisationRoute.class);
            query.setParameter("orgId", organisationId);
            
            List<OrganisationRoute> organisationRoutes = query.getResultList();
            logger.info("Found {} organisation routes for organisation ID: {}", 
                       organisationRoutes.size(), organisationId);
            return organisationRoutes;
        } catch (Exception e) {
            logger.error("Error finding organisation routes for organisation ID: {}", organisationId, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Check if a route is already assigned to an organisation
     */
    public boolean isRouteAssignedToOrganisation(int routeId, int organisationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(or) FROM OrganisationRoute or " +
                "WHERE or.routeId = :routeId AND or.organisationId = :orgId", 
                Long.class);
            query.setParameter("routeId", routeId);
            query.setParameter("orgId", organisationId);
            
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking if route {} is assigned to organisation {}", 
                       routeId, organisationId, e);
            return false;
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Find organisation route by route ID and organisation ID
     */
    public OrganisationRoute findByRouteAndOrganisation(int routeId, int organisationId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            TypedQuery<OrganisationRoute> query = entityManager.createQuery(
                "SELECT or FROM OrganisationRoute or " +
                "WHERE or.routeId = :routeId AND or.organisationId = :orgId", 
                OrganisationRoute.class);
            query.setParameter("routeId", routeId);
            query.setParameter("orgId", organisationId);
            
            List<OrganisationRoute> results = query.getResultList();
            if (results.isEmpty()) {
                return null;
            }
            return results.get(0);
        } catch (Exception e) {
            logger.error("Error finding organisation route for route {} and organisation {}", 
                       routeId, organisationId, e);
            return null;
        } finally {
            entityManager.close();
        }
    }
} 