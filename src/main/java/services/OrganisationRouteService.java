package services;

import entities.OrganisationRoute;
import entities.Route;
import entities.Organisation;
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
    private final OrganisationService organisationService = new OrganisationService();

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
            // First try a direct count query
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(or) FROM OrganisationRoute or " +
                "WHERE or.routeId = :routeId AND or.organisationId = :orgId", 
                Long.class);
            query.setParameter("routeId", routeId);
            query.setParameter("orgId", organisationId);

            Long count = query.getSingleResult();
            boolean isAssigned = count > 0;

            if (isAssigned) {
                logger.info("Route {} is already assigned to organisation {}", 
                          routeId, organisationId);
            } else {
                logger.debug("Route {} is not assigned to organisation {}", 
                          routeId, organisationId);

                // Double-check by looking for the actual records
                // This is a safety measure in case there are any issues with the count query
                TypedQuery<OrganisationRoute> routeQuery = entityManager.createQuery(
                    "SELECT or FROM OrganisationRoute or " +
                    "WHERE or.routeId = :routeId AND or.organisationId = :orgId", 
                    OrganisationRoute.class);
                routeQuery.setParameter("routeId", routeId);
                routeQuery.setParameter("orgId", organisationId);

                List<OrganisationRoute> results = routeQuery.getResultList();
                if (!results.isEmpty()) {
                    logger.warn("Found inconsistency! Count query returned 0 but found {} records for route {} and organisation {}", 
                              results.size(), routeId, organisationId);
                    return true;
                }
            }

            return isAssigned;
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

    /**
     * Find organisation(s) that have this route assigned to them
     * Returns a list of organisations that offer this route
     */
    public List<Organisation> findOrganisationsByRouteId(int routeId) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<Organisation> organisations = new ArrayList<>();

        try {
            TypedQuery<OrganisationRoute> query = entityManager.createQuery(
                "SELECT or FROM OrganisationRoute or WHERE or.routeId = :routeId AND or.isActive = true", 
                OrganisationRoute.class);
            query.setParameter("routeId", routeId);

            List<OrganisationRoute> orgRoutes = query.getResultList();
            logger.debug("Found {} organisation routes for route ID: {}", orgRoutes.size(), routeId);

            for (OrganisationRoute orgRoute : orgRoutes) {
                Organisation org = organisationService.afficher(orgRoute.getOrganisationId());
                if (org != null) {
                    organisations.add(org);
                    logger.debug("Found organisation {} for route {}", org.getNom(), routeId);
                }
            }

            return organisations;
        } catch (Exception e) {
            logger.error("Error finding organisations for route ID: {}", routeId, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Get departure and arrival times for a specific route and organisation
     * @return String[] with [departureTime, arrivalTime] or null if not found
     */
    public String[] getRouteTimesForOrganisation(int routeId, int organisationId) {
        OrganisationRoute orgRoute = findByRouteAndOrganisation(routeId, organisationId);
        if (orgRoute != null && orgRoute.isActive() && 
            orgRoute.getDepartureTime() != null && orgRoute.getArrivalTime() != null) {
            return new String[] {orgRoute.getDepartureTime(), orgRoute.getArrivalTime()};
        }
        return null;
    }

    /**
     * Find active organisation routes by origin and destination
     * @param origin Origin location name
     * @param destination Destination location name
     * @return List of available OrganisationRoute objects
     */
    public List<OrganisationRoute> findOrganisationRoutesByLocations(String origin, String destination) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
        List<OrganisationRoute> results = new ArrayList<>();

        try {
            // First find all routes that match the origin and destination
            TypedQuery<Route> routeQuery = entityManager.createQuery(
                "SELECT r FROM Route r WHERE r.origin = :origin AND r.destination = :destination", 
                Route.class);
            routeQuery.setParameter("origin", origin);
            routeQuery.setParameter("destination", destination);

            List<Route> matchingRoutes = routeQuery.getResultList();
            logger.info("Found {} base routes from {} to {}", matchingRoutes.size(), origin, destination);

            if (matchingRoutes.isEmpty()) {
                return results;
            }

            // For each matching route, find active organisation routes
            for (Route route : matchingRoutes) {
                TypedQuery<OrganisationRoute> orgRouteQuery = entityManager.createQuery(
                    "SELECT or FROM OrganisationRoute or WHERE or.routeId = :routeId AND or.isActive = true", 
                    OrganisationRoute.class);
                orgRouteQuery.setParameter("routeId", route.getId());

                List<OrganisationRoute> orgRoutes = orgRouteQuery.getResultList();

                if (!orgRoutes.isEmpty()) {
                    logger.debug("Found {} active organisation routes for route {} from {} to {}", 
                        orgRoutes.size(), route.getId(), origin, destination);
                    results.addAll(orgRoutes);
                }
            }

            logger.info("Found a total of {} active organisation routes from {} to {}", 
                results.size(), origin, destination);
            return results;
        } catch (Exception e) {
            logger.error("Error finding organisation routes from {} to {}", origin, destination, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }
} 
