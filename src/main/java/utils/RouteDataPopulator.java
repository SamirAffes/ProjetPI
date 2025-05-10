package utils;

import entities.Route;
import services.RouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class to populate the database with sample routes.
 * This class creates a variety of routes between Tunisian cities with different transport modes.
 */
public class RouteDataPopulator {
    private static final Logger logger = LoggerFactory.getLogger(RouteDataPopulator.class);
    private static final RouteService routeService = new RouteService();
    
    // Cities in Tunisia for route generation
    private static final List<String> CITIES = Arrays.asList(
        "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabès", 
        "Ariana", "Gafsa", "Monastir", "Nabeul", "Hammamet", "Djerba", 
        "Tozeur", "Tabarka", "Mahdia", "Béja", "Kef", "Tataouine"
    );
    
    /**
     * Populate the database with sample routes if no routes exist.
     */
    public static void populateRoutesIfEmpty() {
        if (routeService.afficher_tout().isEmpty()) {
            logger.info("No routes found in database. Populating with sample data...");
            populateRoutes();
            logger.info("Sample routes have been added to the database.");
        } else {
            logger.info("Routes already exist in the database. Skipping population.");
        }
    }
    
    /**
     * Create and save sample routes to the database.
     */
    public static void populateRoutes() {
        // Bus routes
        createBusRoutes();
        
        // Train routes
        createTrainRoutes();
        
        // Metro routes in Tunis
        createMetroRoutes();
        
        // TGM routes
        createTGMRoutes();
        
        // Taxi routes
        createTaxiRoutes();
        
        // Ferry routes
        createFerryRoutes();
        
        // Airplane routes
        createAirplaneRoutes();
    }
    
    private static void createBusRoutes() {
        // Inter-city bus routes
        addRoute("Tunis", "Sousse", 140.0, 120, 15.0, "Bus", false, false);
        addRoute("Tunis", "Sfax", 270.0, 210, 25.0, "Bus", false, false);
        addRoute("Tunis", "Bizerte", 65.0, 75, 8.0, "Bus", false, false);
        addRoute("Tunis", "Nabeul", 75.0, 90, 9.0, "Bus", false, false);
        addRoute("Sousse", "Monastir", 20.0, 30, 3.5, "Bus", false, false);
        addRoute("Sousse", "Kairouan", 60.0, 75, 7.0, "Bus", false, false);
        addRoute("Sfax", "Gabès", 130.0, 120, 12.0, "Bus", false, false);
        addRoute("Sfax", "Mahdia", 60.0, 70, 7.5, "Bus", false, false);
        
        // Intra-city bus routes in Tunis
        addRoute("Tunis Centre", "La Marsa", 18.0, 45, 1.5, "Bus", false, true);
        addRoute("Tunis Centre", "Ariana", 10.0, 30, 1.0, "Bus", false, true);
        addRoute("Tunis Centre", "Ben Arous", 8.0, 25, 1.0, "Bus", false, true);
    }
    
    private static void createTrainRoutes() {
        // Main train lines
        addRoute("Tunis", "Sousse", 140.0, 100, 20.0, "Train", false, false);
        addRoute("Tunis", "Sfax", 270.0, 180, 30.0, "Train", false, false);
        addRoute("Tunis", "Gabès", 400.0, 240, 40.0, "Train", false, false);
        addRoute("Tunis", "Bizerte", 65.0, 60, 10.0, "Train", false, false);
        addRoute("Sousse", "Sfax", 130.0, 90, 15.0, "Train", false, false);
        addRoute("Sousse", "Monastir", 20.0, 20, 5.0, "Train", false, false);
    }
    
    private static void createMetroRoutes() {
        // Metro lines in Tunis
        addRoute("Tunis Marine", "Ben Arous", 12.0, 25, 0.8, "Métro", false, true);
        addRoute("Tunis Marine", "Den Den", 10.0, 20, 0.8, "Métro", false, true);
        addRoute("Tunis Marine", "Ariana", 8.0, 18, 0.8, "Métro", false, true);
        addRoute("Tunis Marine", "Ibn Khaldoun", 7.0, 15, 0.8, "Métro", false, true);
        addRoute("Tunis Marine", "Bardo", 6.0, 12, 0.8, "Métro", false, true);
    }
    
    private static void createTGMRoutes() {
        // TGM line connecting Tunis to La Marsa
        addRoute("Tunis Marine", "La Goulette", 10.0, 15, 1.0, "TGM", false, true);
        addRoute("La Goulette", "Carthage", 7.0, 10, 1.0, "TGM", false, true);
        addRoute("Carthage", "Sidi Bou Said", 3.0, 5, 1.0, "TGM", false, true);
        addRoute("Sidi Bou Said", "La Marsa", 5.0, 8, 1.0, "TGM", false, true);
        addRoute("Tunis Marine", "La Marsa", 25.0, 38, 1.5, "TGM", false, true);
    }
    
    private static void createTaxiRoutes() {
        // Taxi routes between major cities
        addRoute("Tunis", "Hammamet", 65.0, 60, 50.0, "Taxi", false, false);
        addRoute("Tunis", "Nabeul", 75.0, 70, 60.0, "Taxi", false, false);
        addRoute("Sousse", "Monastir", 20.0, 20, 25.0, "Taxi", false, false);
        addRoute("Sousse", "Hammamet", 40.0, 35, 40.0, "Taxi", false, false);
        
        // Intra-city taxi routes
        addRoute("Tunis Centre", "Carthage Airport", 10.0, 20, 15.0, "Taxi", false, true);
        addRoute("Tunis Centre", "La Marsa", 18.0, 30, 20.0, "Taxi", false, true);
        addRoute("Sousse Centre", "Sousse Airport", 12.0, 15, 15.0, "Taxi", false, true);
    }
    
    private static void createFerryRoutes() {
        // Ferry routes to islands and international
        addRoute("Tunis", "La Goulette", 10.0, 20, 5.0, "Ferry", false, false);
        addRoute("Sfax", "Kerkennah", 20.0, 60, 10.0, "Ferry", false, false);
        addRoute("Tunis", "Marseille", 800.0, 1200, 150.0, "Ferry", true, false);
        addRoute("Tunis", "Genoa", 1000.0, 1440, 180.0, "Ferry", true, false);
        addRoute("Tunis", "Naples", 700.0, 1080, 160.0, "Ferry", true, false);
    }
    
    private static void createAirplaneRoutes() {
        // Domestic flights
        addRoute("Tunis", "Djerba", 450.0, 45, 120.0, "Avion", false, false);
        addRoute("Tunis", "Tozeur", 430.0, 50, 130.0, "Avion", false, false);
        addRoute("Tunis", "Sfax", 270.0, 35, 100.0, "Avion", false, false);
        
        // International flights
        addRoute("Tunis", "Paris", 1500.0, 150, 350.0, "Avion", true, false);
        addRoute("Tunis", "Rome", 800.0, 100, 250.0, "Avion", true, false);
        addRoute("Tunis", "Istanbul", 1800.0, 180, 300.0, "Avion", true, false);
        addRoute("Tunis", "Cairo", 2200.0, 210, 320.0, "Avion", true, false);
        addRoute("Tunis", "Barcelona", 1100.0, 120, 280.0, "Avion", true, false);
    }
    
    /**
     * Helper method to create and save a route.
     */
    private static void addRoute(String origin, String destination, double distance, int duration, 
                               double price, String transportMode, boolean isInternational, boolean isIntraCity) {
        try {
            Route route = Route.builder()
                .origin(origin)
                .destination(destination)
                .distance(distance)
                .estimatedDuration(duration)
                .basePrice(price)
                .transportMode(transportMode)
                .isInternational(isInternational)
                .isIntraCity(isIntraCity)
                .build();
            
            routeService.ajouter(route);
            logger.debug("Added route: {} → {} ({})", origin, destination, transportMode);
        } catch (Exception e) {
            logger.error("Error adding route from {} to {}: {}", origin, destination, e.getMessage());
        }
    }
} 