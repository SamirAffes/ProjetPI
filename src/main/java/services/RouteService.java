package services;

import entities.Route;
import entities.Organisation;
import entities.OrganisationRoute;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import utils.JPAUtil;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
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
                enrichRouteWithOrganisationData(route);
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
                enrichRouteWithOrganisationData(route);
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
    
    /**
     * Search for routes matching the given origin and destination
     */
    public List<Route> searchRoutes(String origin, String destination) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        
        try {
            String queryStr;
            TypedQuery<Route> query;
            
            // If both origin and destination are provided, use exact match search
            if (origin != null && !origin.isEmpty() && destination != null && !destination.isEmpty()) {
                queryStr = "SELECT r FROM Route r WHERE LOWER(r.origin) = LOWER(:origin) AND LOWER(r.destination) = LOWER(:destination)";
                query = entityManager.createQuery(queryStr, Route.class);
                query.setParameter("origin", origin);
                query.setParameter("destination", destination);
                
                List<Route> routes = query.getResultList();
                
                // If no results with exact match, try with LIKE operator
                if (routes.isEmpty()) {
                    logger.info("No exact matches found, trying with LIKE operator");
                    queryStr = "SELECT r FROM Route r WHERE LOWER(r.origin) LIKE LOWER(:originPattern) AND LOWER(r.destination) LIKE LOWER(:destPattern)";
                    query = entityManager.createQuery(queryStr, Route.class);
                    query.setParameter("originPattern", "%" + origin + "%");
                    query.setParameter("destPattern", "%" + destination + "%");
                    routes = query.getResultList();
                }
                
                // Eliminate duplicate routes by ID using a map
                Map<Integer, Route> uniqueRoutes = new HashMap<>();
                for (Route route : routes) {
                    if (!uniqueRoutes.containsKey(route.getId())) {
                        enrichRouteWithOrganisationData(route);
                        route.initializeProperties();
                        uniqueRoutes.put(route.getId(), route);
                    }
                }
                
                return new ArrayList<>(uniqueRoutes.values());
            } else if (origin != null && !origin.isEmpty()) {
                // Search by origin only
                queryStr = "SELECT r FROM Route r WHERE LOWER(r.origin) = LOWER(:origin)";
                query = entityManager.createQuery(queryStr, Route.class);
                query.setParameter("origin", origin);
            } else if (destination != null && !destination.isEmpty()) {
                // Search by destination only
                queryStr = "SELECT r FROM Route r WHERE LOWER(r.destination) = LOWER(:destination)";
                query = entityManager.createQuery(queryStr, Route.class);
                query.setParameter("destination", destination);
            } else {
                // If no search criteria provided, return all routes
                return afficher_tout();
            }
            
            List<Route> routes = query.getResultList();
            
            // Eliminate duplicate routes by ID using a map
            Map<Integer, Route> uniqueRoutes = new HashMap<>();
            for (Route route : routes) {
                if (!uniqueRoutes.containsKey(route.getId())) {
                    enrichRouteWithOrganisationData(route);
                    route.initializeProperties();
                    uniqueRoutes.put(route.getId(), route);
                }
            }
            
            logger.info("Search found {} unique routes", uniqueRoutes.size());
            return new ArrayList<>(uniqueRoutes.values());
        } catch (Exception e) {
            logger.error("Error searching routes from {} to {}", origin, destination, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
    
    /**
     * Enrich a route with organisation data if available
     */
    private void enrichRouteWithOrganisationData(Route route) {
        if (route == null) return;
        
        // Check if the route already has company information
        if (route.getCompanyId() > 0 && route.getCompanyName() != null && !route.getCompanyName().isEmpty()) {
            return;
        }
        
        // Find organisations for this route
        OrganisationRouteService organisationRouteService = new OrganisationRouteService();
        List<Organisation> organisations = organisationRouteService.findOrganisationsByRouteId(route.getId());
        
        // Use the first active organisation found for this route
        if (!organisations.isEmpty()) {
            Organisation org = organisations.get(0);
            route.setCompanyId(org.getId());
            route.setCompanyName(org.getNom());
            logger.debug("Enriched route {} with organisation {}", route.getId(), org.getNom());
        }
    }
    
    // Additional method for UI
    public List<Route> findAll() {
        return afficher_tout();
    }
} 