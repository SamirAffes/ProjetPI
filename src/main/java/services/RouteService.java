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

    /**
     * Get all unique origins from routes in the database
     * @return List of all unique origins
     */
    public List<String> getAllOrigins() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            TypedQuery<String> query = entityManager.createQuery(
                "SELECT DISTINCT r.origin FROM Route r ORDER BY r.origin", String.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error getting all origins", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Get all unique destinations from routes in the database
     * @return List of all unique destinations
     */
    public List<String> getAllDestinations() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            TypedQuery<String> query = entityManager.createQuery(
                "SELECT DISTINCT r.destination FROM Route r ORDER BY r.destination", String.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error getting all destinations", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Get all unique locations (both origins and destinations) from routes in the database
     * @return List of all unique locations
     */
    public List<String> getAllLocations() {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            TypedQuery<String> originsQuery = entityManager.createQuery(
                "SELECT DISTINCT r.origin FROM Route r", String.class);
            List<String> origins = originsQuery.getResultList();

            TypedQuery<String> destinationsQuery = entityManager.createQuery(
                "SELECT DISTINCT r.destination FROM Route r", String.class);
            List<String> destinations = destinationsQuery.getResultList();

            // Combine origins and destinations and remove duplicates
            List<String> allLocations = new ArrayList<>(origins);
            for (String destination : destinations) {
                if (!allLocations.contains(destination)) {
                    allLocations.add(destination);
                }
            }

            // Sort alphabetically
            java.util.Collections.sort(allLocations);

            logger.info("Found {} unique locations in routes", allLocations.size());
            return allLocations;
        } catch (Exception e) {
            logger.error("Error getting all locations", e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Get all unique locations for a specific transport mode
     * @param transportMode The transport mode to filter by
     * @return List of all unique locations for the specified transport mode
     */
    public List<String> getLocationsByTransportMode(String transportMode) {
        EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();

        try {
            if (transportMode == null || transportMode.isEmpty() || transportMode.equals("Tous")) {
                // If no transport mode specified or "All" selected, return all locations
                return getAllLocations();
            }

            TypedQuery<String> originsQuery = entityManager.createQuery(
                "SELECT DISTINCT r.origin FROM Route r WHERE r.transportMode = :mode", String.class);
            originsQuery.setParameter("mode", transportMode);
            List<String> origins = originsQuery.getResultList();

            TypedQuery<String> destinationsQuery = entityManager.createQuery(
                "SELECT DISTINCT r.destination FROM Route r WHERE r.transportMode = :mode", String.class);
            destinationsQuery.setParameter("mode", transportMode);
            List<String> destinations = destinationsQuery.getResultList();

            // Combine origins and destinations and remove duplicates
            List<String> filteredLocations = new ArrayList<>(origins);
            for (String destination : destinations) {
                if (!filteredLocations.contains(destination)) {
                    filteredLocations.add(destination);
                }
            }

            // Sort alphabetically
            java.util.Collections.sort(filteredLocations);

            logger.info("Found {} unique locations for transport mode: {}", filteredLocations.size(), transportMode);
            return filteredLocations;
        } catch (Exception e) {
            logger.error("Error getting locations for transport mode: {}", transportMode, e);
            return List.of();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Get relevant route data for RAG (Retrieval-Augmented Generation) functionality.
     * This method collects information about routes, transport modes, and locations
     * to provide context for the chatbot.
     * 
     * @return A string containing relevant route data
     */
    public String getRelevantRouteData() {
        logger.info("Gathering relevant route data for RAG");
        StringBuilder routeData = new StringBuilder();

        try {
            // Get all routes
            List<Route> routes = afficher_tout();

            // Get unique locations
            List<String> locations = getAllLocations();

            // Get unique transport modes
            EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
            TypedQuery<String> modesQuery = entityManager.createQuery(
                "SELECT DISTINCT r.transportMode FROM Route r", String.class);
            List<String> transportModes = modesQuery.getResultList();
            entityManager.close();

            // Add summary information
            routeData.append("Transport System Information:\n");
            routeData.append("- Total routes: ").append(routes.size()).append("\n");
            routeData.append("- Available locations: ").append(String.join(", ", locations)).append("\n");
            routeData.append("- Transport modes: ").append(String.join(", ", transportModes)).append("\n\n");

            // Add detailed route information (limit to 20 routes to avoid too much data)
            routeData.append("Sample Routes:\n");
            int count = 0;
            for (Route route : routes) {
                if (count >= 20) break;

                routeData.append("- Route ").append(route.getId()).append(": ")
                        .append(route.getOrigin()).append(" to ").append(route.getDestination())
                        .append(" via ").append(route.getTransportMode())
                        .append(", operated by ").append(route.getCompanyName() != null ? route.getCompanyName() : "Unknown")
                        .append(", price: ").append(route.getPrice()).append(" TND")
                        .append(", duration: ").append(route.getEstimatedDuration()).append(" minutes")
                        .append("\n");
                count++;
            }

            // Add some common route patterns
            routeData.append("\nCommon Route Patterns:\n");
            Map<String, Integer> transportModeCount = new HashMap<>();
            Map<String, Integer> originCount = new HashMap<>();
            Map<String, Integer> destinationCount = new HashMap<>();

            for (Route route : routes) {
                // Count transport modes
                String mode = route.getTransportMode();
                transportModeCount.put(mode, transportModeCount.getOrDefault(mode, 0) + 1);

                // Count origins
                String origin = route.getOrigin();
                originCount.put(origin, originCount.getOrDefault(origin, 0) + 1);

                // Count destinations
                String destination = route.getDestination();
                destinationCount.put(destination, destinationCount.getOrDefault(destination, 0) + 1);
            }

            // Add most common transport modes
            routeData.append("- Most common transport modes: ");
            transportModeCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .forEach(e -> routeData.append(e.getKey()).append(" (").append(e.getValue()).append(" routes), "));
            routeData.append("\n");

            // Add most common origins
            routeData.append("- Most common origins: ");
            originCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .forEach(e -> routeData.append(e.getKey()).append(" (").append(e.getValue()).append(" routes), "));
            routeData.append("\n");

            // Add most common destinations
            routeData.append("- Most common destinations: ");
            destinationCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .forEach(e -> routeData.append(e.getKey()).append(" (").append(e.getValue()).append(" routes), "));
            routeData.append("\n");

            logger.info("Successfully gathered route data for RAG");
            return routeData.toString();
        } catch (Exception e) {
            logger.error("Error gathering route data for RAG", e);
            return "Error retrieving route data. Basic information: Routes connect various cities in Tunisia using different transport modes like bus, train, and metro.";
        }
    }
} 
