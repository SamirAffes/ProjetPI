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
        // Major cities
        "Tunis", "Sfax", "Sousse", "Kairouan", "Bizerte", "Gabès",
        "Ariana", "Gafsa", "Monastir", "Nabeul", "Hammamet", "Djerba",
        "Tozeur", "Tabarka", "Mahdia", "Béja", "Kef", "Tataouine",
        // Additional cities
        "Manouba", "Ben Arous", "Zaghouan", "Siliana", "Jendouba", 
        "Médenine", "Kebili", "Kasserine", "Sidi Bouzid", "Zarzis",
        // Tunis metropolitan area
        "La Marsa", "Carthage", "Sidi Bou Said", "La Goulette", 
        "Radès", "Mégrine", "Le Bardo", "Den Den", "El Kram",
        // Sousse metropolitan area
        "Hammam Sousse", "Akouda", "Kalâa Kebira", "Hergla", "Enfidha",
        // Sfax metropolitan area
        "Sakiet Ezzit", "Sakiet Eddaier", "Chihia", "Gremda", "Thyna"
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
        
        // Add additional modern routes
        addModernRoutes();
    }
    
    private static void createBusRoutes() {
        // Major intercity bus routes (SNTRI, etc.)
        addRoute("Tunis", "Sousse", 140.0, 120, 15.0, "Bus", false, false);
        addRoute("Sousse", "Tunis", 140.0, 120, 15.0, "Bus", false, false);
        addRoute("Tunis", "Sfax", 270.0, 210, 25.0, "Bus", false, false);
        addRoute("Sfax", "Tunis", 270.0, 210, 25.0, "Bus", false, false);
        addRoute("Tunis", "Bizerte", 65.0, 75, 8.0, "Bus", false, false);
        addRoute("Bizerte", "Tunis", 65.0, 75, 8.0, "Bus", false, false);
        addRoute("Tunis", "Nabeul", 75.0, 90, 9.0, "Bus", false, false);
        addRoute("Nabeul", "Tunis", 75.0, 90, 9.0, "Bus", false, false);
        addRoute("Tunis", "Hammamet", 65.0, 80, 8.5, "Bus", false, false);
        addRoute("Hammamet", "Tunis", 65.0, 80, 8.5, "Bus", false, false);
        addRoute("Tunis", "Kairouan", 150.0, 150, 16.0, "Bus", false, false);
        addRoute("Kairouan", "Tunis", 150.0, 150, 16.0, "Bus", false, false);
        
        // Coastal routes
        addRoute("Sousse", "Monastir", 20.0, 30, 3.5, "Bus", false, false);
        addRoute("Monastir", "Sousse", 20.0, 30, 3.5, "Bus", false, false);
        addRoute("Monastir", "Mahdia", 45.0, 50, 5.5, "Bus", false, false);
        addRoute("Mahdia", "Monastir", 45.0, 50, 5.5, "Bus", false, false);
        addRoute("Sousse", "Mahdia", 60.0, 70, 7.0, "Bus", false, false);
        addRoute("Mahdia", "Sousse", 60.0, 70, 7.0, "Bus", false, false);
        addRoute("Sousse", "Sfax", 130.0, 135, 13.5, "Bus", false, false);
        addRoute("Sfax", "Sousse", 130.0, 135, 13.5, "Bus", false, false);
        addRoute("Sfax", "Gabès", 130.0, 120, 12.0, "Bus", false, false);
        addRoute("Gabès", "Sfax", 130.0, 120, 12.0, "Bus", false, false);
        addRoute("Gabès", "Médenine", 75.0, 80, 8.0, "Bus", false, false);
        addRoute("Médenine", "Gabès", 75.0, 80, 8.0, "Bus", false, false);
        addRoute("Médenine", "Tataouine", 65.0, 70, 7.5, "Bus", false, false);
        addRoute("Tataouine", "Médenine", 65.0, 70, 7.5, "Bus", false, false);
        
        // Northern routes
        addRoute("Tunis", "Béja", 105.0, 110, 11.0, "Bus", false, false);
        addRoute("Béja", "Tunis", 105.0, 110, 11.0, "Bus", false, false);
        addRoute("Tunis", "Jendouba", 160.0, 160, 16.5, "Bus", false, false);
        addRoute("Jendouba", "Tunis", 160.0, 160, 16.5, "Bus", false, false);
        addRoute("Tunis", "Kef", 170.0, 165, 17.0, "Bus", false, false);
        addRoute("Kef", "Tunis", 170.0, 165, 17.0, "Bus", false, false);
        addRoute("Tunis", "Tabarka", 175.0, 180, 18.0, "Bus", false, false);
        addRoute("Tabarka", "Tunis", 175.0, 180, 18.0, "Bus", false, false);
        
        // Central routes
        addRoute("Sousse", "Kairouan", 60.0, 75, 7.0, "Bus", false, false);
        addRoute("Kairouan", "Sousse", 60.0, 75, 7.0, "Bus", false, false);
        addRoute("Kairouan", "Sidi Bouzid", 100.0, 110, 11.0, "Bus", false, false);
        addRoute("Sidi Bouzid", "Kairouan", 100.0, 110, 11.0, "Bus", false, false);
        addRoute("Sidi Bouzid", "Gafsa", 110.0, 120, 12.0, "Bus", false, false);
        addRoute("Gafsa", "Sidi Bouzid", 110.0, 120, 12.0, "Bus", false, false);
        addRoute("Gafsa", "Tozeur", 90.0, 105, 10.0, "Bus", false, false);
        addRoute("Tozeur", "Gafsa", 90.0, 105, 10.0, "Bus", false, false);
        
        // Southern routes
        addRoute("Tozeur", "Kebili", 110.0, 120, 12.0, "Bus", false, false);
        addRoute("Kebili", "Tozeur", 110.0, 120, 12.0, "Bus", false, false);
        addRoute("Kebili", "Gabès", 160.0, 170, 16.0, "Bus", false, false);
        addRoute("Gabès", "Kebili", 160.0, 170, 16.0, "Bus", false, false);
        
        // Intra-city bus routes in Tunis (Transtu)
        addRoute("Tunis Centre", "La Marsa", 18.0, 45, 1.5, "Bus", false, true);
        addRoute("La Marsa", "Tunis Centre", 18.0, 45, 1.5, "Bus", false, true);
        addRoute("Tunis Centre", "Ariana", 10.0, 30, 1.0, "Bus", false, true);
        addRoute("Ariana", "Tunis Centre", 10.0, 30, 1.0, "Bus", false, true);
        addRoute("Tunis Centre", "Ben Arous", 8.0, 25, 1.0, "Bus", false, true);
        addRoute("Ben Arous", "Tunis Centre", 8.0, 25, 1.0, "Bus", false, true);
        addRoute("Tunis Centre", "Le Bardo", 7.0, 20, 1.0, "Bus", false, true);
        addRoute("Le Bardo", "Tunis Centre", 7.0, 20, 1.0, "Bus", false, true);
        addRoute("Tunis Centre", "La Goulette", 10.0, 30, 1.0, "Bus", false, true);
        addRoute("La Goulette", "Tunis Centre", 10.0, 30, 1.0, "Bus", false, true);
        addRoute("Tunis Centre", "Carthage", 15.0, 40, 1.5, "Bus", false, true);
        addRoute("Carthage", "Tunis Centre", 15.0, 40, 1.5, "Bus", false, true);
        
        // Intra-city bus routes in Sousse
        addRoute("Sousse Centre", "Hammam Sousse", 6.0, 15, 1.0, "Bus", false, true);
        addRoute("Hammam Sousse", "Sousse Centre", 6.0, 15, 1.0, "Bus", false, true);
        addRoute("Sousse Centre", "Akouda", 8.0, 20, 1.0, "Bus", false, true);
        addRoute("Akouda", "Sousse Centre", 8.0, 20, 1.0, "Bus", false, true);
        
        // Intra-city bus routes in Sfax
        addRoute("Sfax Centre", "Sakiet Ezzit", 7.0, 20, 1.0, "Bus", false, true);
        addRoute("Sakiet Ezzit", "Sfax Centre", 7.0, 20, 1.0, "Bus", false, true);
        addRoute("Sfax Centre", "Thyna", 9.0, 25, 1.0, "Bus", false, true);
        addRoute("Thyna", "Sfax Centre", 9.0, 25, 1.0, "Bus", false, true);
    }
    
    private static void createTrainRoutes() {
        // Main SNCFT lines
        
        // Ligne Tunis - Gabès (south line)
        addRoute("Tunis", "Hammam-Lif", 15.0, 20, 2.0, "Train", false, false);
        addRoute("Hammam-Lif", "Tunis", 15.0, 20, 2.0, "Train", false, false);
        addRoute("Tunis", "Sousse", 140.0, 100, 20.0, "Train", false, false);
        addRoute("Sousse", "Tunis", 140.0, 100, 20.0, "Train", false, false);
        addRoute("Tunis", "Sfax", 270.0, 180, 30.0, "Train", false, false);
        addRoute("Sfax", "Tunis", 270.0, 180, 30.0, "Train", false, false);
        addRoute("Tunis", "Gabès", 400.0, 240, 40.0, "Train", false, false);
        addRoute("Gabès", "Tunis", 400.0, 240, 40.0, "Train", false, false);
        addRoute("Sousse", "Sfax", 130.0, 90, 15.0, "Train", false, false);
        addRoute("Sfax", "Sousse", 130.0, 90, 15.0, "Train", false, false);
        addRoute("Sfax", "Gabès", 130.0, 80, 15.0, "Train", false, false);
        addRoute("Gabès", "Sfax", 130.0, 80, 15.0, "Train", false, false);
        
        // Ligne Tunis - Ghardimaou (northwest line)
        addRoute("Tunis", "Béja", 105.0, 90, 12.0, "Train", false, false);
        addRoute("Béja", "Tunis", 105.0, 90, 12.0, "Train", false, false);
        addRoute("Tunis", "Jendouba", 160.0, 140, 18.0, "Train", false, false);
        addRoute("Jendouba", "Tunis", 160.0, 140, 18.0, "Train", false, false);
        addRoute("Tunis", "Ghardimaou", 190.0, 160, 22.0, "Train", false, false);
        addRoute("Ghardimaou", "Tunis", 190.0, 160, 22.0, "Train", false, false);
        addRoute("Béja", "Jendouba", 55.0, 50, 8.0, "Train", false, false);
        addRoute("Jendouba", "Béja", 55.0, 50, 8.0, "Train", false, false);
        
        // Ligne Tunis - Kalaa Khasba (north central line)
        addRoute("Tunis", "Kef", 170.0, 150, 20.0, "Train", false, false);
        addRoute("Kef", "Tunis", 170.0, 150, 20.0, "Train", false, false);
        addRoute("Tunis", "Dahmani", 190.0, 165, 22.0, "Train", false, false);
        addRoute("Dahmani", "Tunis", 190.0, 165, 22.0, "Train", false, false);
        
        // Ligne Sahel (coastal link)
        addRoute("Sousse", "Monastir", 20.0, 20, 5.0, "Train", false, false);
        addRoute("Monastir", "Sousse", 20.0, 20, 5.0, "Train", false, false);
        addRoute("Sousse", "Mahdia", 60.0, 50, 9.0, "Train", false, false);
        addRoute("Mahdia", "Sousse", 60.0, 50, 9.0, "Train", false, false);
        addRoute("Monastir", "Mahdia", 45.0, 35, 7.0, "Train", false, false);
        addRoute("Mahdia", "Monastir", 45.0, 35, 7.0, "Train", false, false);
        
        // Ligne Banlieue Sud (Tunis suburbs)
        addRoute("Tunis", "Ben Arous", 8.0, 15, 1.5, "Train", false, true);
        addRoute("Ben Arous", "Tunis", 8.0, 15, 1.5, "Train", false, true);
        addRoute("Tunis", "Radès", 12.0, 18, 2.0, "Train", false, true);
        addRoute("Radès", "Tunis", 12.0, 18, 2.0, "Train", false, true);
        addRoute("Tunis", "Hammam-Lif", 15.0, 20, 2.0, "Train", false, true);
        addRoute("Hammam-Lif", "Tunis", 15.0, 20, 2.0, "Train", false, true);
        
        // Ligne Banlieue Nord (Tunis northern suburbs)
        addRoute("Tunis", "La Marsa", 18.0, 25, 2.5, "Train", false, true);
        addRoute("La Marsa", "Tunis", 18.0, 25, 2.5, "Train", false, true);
    }
    
    private static void createMetroRoutes() {
        // Actual Metro lines in Tunis (Transtu)
        
        // Line 1: Tunis Marine - Ben Arous
        addRoute("Tunis Marine", "Place Barcelone", 2.0, 5, 0.7, "Métro", false, true);
        addRoute("Place Barcelone", "Tunis Marine", 2.0, 5, 0.7, "Métro", false, true);
        addRoute("Place Barcelone", "Mohamed Ali", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Mohamed Ali", "Place Barcelone", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Mohamed Ali", "Ben Arous", 7.0, 15, 0.7, "Métro", false, true);
        addRoute("Ben Arous", "Mohamed Ali", 7.0, 15, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Ben Arous", 12.0, 25, 0.7, "Métro", false, true);
        addRoute("Ben Arous", "Tunis Marine", 12.0, 25, 0.7, "Métro", false, true);
        
        // Line 2: Tunis Marine - Ariana
        addRoute("Tunis Marine", "Place de la République", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Place de la République", "Tunis Marine", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Place de la République", "Place Pasteur", 2.0, 4, 0.7, "Métro", false, true);
        addRoute("Place Pasteur", "Place de la République", 2.0, 4, 0.7, "Métro", false, true);
        addRoute("Place Pasteur", "Ariana", 5.0, 10, 0.7, "Métro", false, true);
        addRoute("Ariana", "Place Pasteur", 5.0, 10, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Ariana", 10.0, 20, 0.7, "Métro", false, true);
        addRoute("Ariana", "Tunis Marine", 10.0, 20, 0.7, "Métro", false, true);
        
        // Line 3: Tunis Marine - Ibn Khaldoun
        addRoute("Tunis Marine", "Place de la République", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Place de la République", "Place Bab Saadoun", 3.0, 5, 0.7, "Métro", false, true);
        addRoute("Place Bab Saadoun", "Place de la République", 3.0, 5, 0.7, "Métro", false, true);
        addRoute("Place Bab Saadoun", "Ibn Khaldoun", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("Ibn Khaldoun", "Place Bab Saadoun", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Ibn Khaldoun", 10.0, 18, 0.7, "Métro", false, true);
        addRoute("Ibn Khaldoun", "Tunis Marine", 10.0, 18, 0.7, "Métro", false, true);
        
        // Line 4: Tunis Marine - Den Den
        addRoute("Tunis Marine", "Place de la République", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Place de la République", "Bab El Khadra", 2.0, 4, 0.7, "Métro", false, true);
        addRoute("Bab El Khadra", "Place de la République", 2.0, 4, 0.7, "Métro", false, true);
        addRoute("Bab El Khadra", "Den Den", 5.0, 10, 0.7, "Métro", false, true);
        addRoute("Den Den", "Bab El Khadra", 5.0, 10, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Den Den", 10.0, 20, 0.7, "Métro", false, true);
        addRoute("Den Den", "Tunis Marine", 10.0, 20, 0.7, "Métro", false, true);
        
        // Line 5: Tunis Marine - Intilaka
        addRoute("Tunis Marine", "Place de la République", 3.0, 6, 0.7, "Métro", false, true);
        addRoute("Place de la République", "El Ouardia", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("El Ouardia", "Place de la République", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("El Ouardia", "Intilaka", 3.0, 7, 0.7, "Métro", false, true);
        addRoute("Intilaka", "El Ouardia", 3.0, 7, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Intilaka", 10.0, 21, 0.7, "Métro", false, true);
        addRoute("Intilaka", "Tunis Marine", 10.0, 21, 0.7, "Métro", false, true);
        
        // Line 6: Tunis Marine - Le Bardo - La Manouba
        addRoute("Tunis Marine", "Place Barcelone", 2.0, 5, 0.7, "Métro", false, true);
        addRoute("Place Barcelone", "Le Bardo", 4.0, 7, 0.7, "Métro", false, true);
        addRoute("Le Bardo", "Place Barcelone", 4.0, 7, 0.7, "Métro", false, true);
        addRoute("Le Bardo", "La Manouba", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("La Manouba", "Le Bardo", 4.0, 8, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "Le Bardo", 6.0, 12, 0.7, "Métro", false, true);
        addRoute("Le Bardo", "Tunis Marine", 6.0, 12, 0.7, "Métro", false, true);
        addRoute("Tunis Marine", "La Manouba", 10.0, 20, 0.7, "Métro", false, true);
        addRoute("La Manouba", "Tunis Marine", 10.0, 20, 0.7, "Métro", false, true);
    }
    
    private static void createTGMRoutes() {
        // TGM line connecting Tunis to La Marsa (stations in order)
        addRoute("Tunis Marine", "Le Kram", 8.0, 12, 1.0, "TGM", false, true);
        addRoute("Le Kram", "Tunis Marine", 8.0, 12, 1.0, "TGM", false, true);
        addRoute("Le Kram", "Carthage Salammbô", 5.0, 7, 1.0, "TGM", false, true);
        addRoute("Carthage Salammbô", "Le Kram", 5.0, 7, 1.0, "TGM", false, true);
        addRoute("Carthage Salammbô", "Carthage Présidence", 2.0, 3, 1.0, "TGM", false, true);
        addRoute("Carthage Présidence", "Carthage Salammbô", 2.0, 3, 1.0, "TGM", false, true);
        addRoute("Carthage Présidence", "Carthage Hannibal", 2.0, 3, 1.0, "TGM", false, true);
        addRoute("Carthage Hannibal", "Carthage Présidence", 2.0, 3, 1.0, "TGM", false, true);
        addRoute("Carthage Hannibal", "Sidi Bou Said", 3.0, 4, 1.0, "TGM", false, true);
        addRoute("Sidi Bou Said", "Carthage Hannibal", 3.0, 4, 1.0, "TGM", false, true);
        addRoute("Sidi Bou Said", "La Marsa", 5.0, 8, 1.0, "TGM", false, true);
        addRoute("La Marsa", "Sidi Bou Said", 5.0, 8, 1.0, "TGM", false, true);
        
        // Direct routes
        addRoute("Tunis Marine", "Carthage Salammbô", 13.0, 19, 1.0, "TGM", false, true);
        addRoute("Carthage Salammbô", "Tunis Marine", 13.0, 19, 1.0, "TGM", false, true);
        addRoute("Tunis Marine", "Carthage Hannibal", 17.0, 25, 1.0, "TGM", false, true);
        addRoute("Carthage Hannibal", "Tunis Marine", 17.0, 25, 1.0, "TGM", false, true);
        addRoute("Tunis Marine", "Sidi Bou Said", 20.0, 30, 1.5, "TGM", false, true);
        addRoute("Sidi Bou Said", "Tunis Marine", 20.0, 30, 1.5, "TGM", false, true);
        addRoute("Tunis Marine", "La Marsa", 25.0, 37, 1.5, "TGM", false, true);
        addRoute("La Marsa", "Tunis Marine", 25.0, 37, 1.5, "TGM", false, true);
    }
    
    private static void createTaxiRoutes() {
        // Taxi routes between major cities (Louage service)
        addRoute("Tunis", "Hammamet", 65.0, 60, 15.0, "Taxi", false, false);
        addRoute("Hammamet", "Tunis", 65.0, 60, 15.0, "Taxi", false, false);
        addRoute("Tunis", "Nabeul", 75.0, 70, 18.0, "Taxi", false, false);
        addRoute("Nabeul", "Tunis", 75.0, 70, 18.0, "Taxi", false, false);
        addRoute("Tunis", "Bizerte", 65.0, 65, 15.0, "Taxi", false, false);
        addRoute("Bizerte", "Tunis", 65.0, 65, 15.0, "Taxi", false, false);
        addRoute("Tunis", "Zaghouan", 60.0, 55, 14.0, "Taxi", false, false);
        addRoute("Zaghouan", "Tunis", 60.0, 55, 14.0, "Taxi", false, false);
        addRoute("Sousse", "Monastir", 20.0, 20, 6.0, "Taxi", false, false);
        addRoute("Monastir", "Sousse", 20.0, 20, 6.0, "Taxi", false, false);
        addRoute("Sousse", "Hammamet", 40.0, 35, 10.0, "Taxi", false, false);
        addRoute("Hammamet", "Sousse", 40.0, 35, 10.0, "Taxi", false, false);
        addRoute("Sousse", "Kairouan", 60.0, 55, 15.0, "Taxi", false, false);
        addRoute("Kairouan", "Sousse", 60.0, 55, 15.0, "Taxi", false, false);
        addRoute("Sfax", "Mahdia", 60.0, 60, 15.0, "Taxi", false, false);
        addRoute("Mahdia", "Sfax", 60.0, 60, 15.0, "Taxi", false, false);
        
        // Airport taxi services
        addRoute("Tunis Carthage Airport", "Tunis Centre", 8.0, 20, 20.0, "Taxi", false, true);
        addRoute("Tunis Centre", "Tunis Carthage Airport", 8.0, 20, 20.0, "Taxi", false, true);
        addRoute("Tunis Carthage Airport", "Sidi Bou Said", 10.0, 20, 25.0, "Taxi", false, true);
        addRoute("Sidi Bou Said", "Tunis Carthage Airport", 10.0, 20, 25.0, "Taxi", false, true);
        addRoute("Tunis Carthage Airport", "La Marsa", 12.0, 25, 30.0, "Taxi", false, true);
        addRoute("La Marsa", "Tunis Carthage Airport", 12.0, 25, 30.0, "Taxi", false, true);
        addRoute("Tunis Carthage Airport", "Gammarth", 15.0, 30, 35.0, "Taxi", false, true);
        addRoute("Gammarth", "Tunis Carthage Airport", 15.0, 30, 35.0, "Taxi", false, true);
        addRoute("Monastir Habib Bourguiba Airport", "Sousse", 15.0, 25, 30.0, "Taxi", false, true);
        addRoute("Sousse", "Monastir Habib Bourguiba Airport", 15.0, 25, 30.0, "Taxi", false, true);
        addRoute("Monastir Habib Bourguiba Airport", "Monastir", 5.0, 10, 15.0, "Taxi", false, true);
        addRoute("Monastir", "Monastir Habib Bourguiba Airport", 5.0, 10, 15.0, "Taxi", false, true);
        addRoute("Enfidha Airport", "Hammamet", 40.0, 40, 50.0, "Taxi", false, true);
        addRoute("Hammamet", "Enfidha Airport", 40.0, 40, 50.0, "Taxi", false, true);
        addRoute("Enfidha Airport", "Sousse", 60.0, 60, 70.0, "Taxi", false, true);
        addRoute("Sousse", "Enfidha Airport", 60.0, 60, 70.0, "Taxi", false, true);
        addRoute("Djerba Airport", "Houmt Souk", 10.0, 15, 20.0, "Taxi", false, true);
        addRoute("Houmt Souk", "Djerba Airport", 10.0, 15, 20.0, "Taxi", false, true);
        
        // Intra-city taxi routes (Individual taxis)
        addRoute("Tunis Centre", "La Marsa", 18.0, 30, 15.0, "Taxi", false, true);
        addRoute("La Marsa", "Tunis Centre", 18.0, 30, 15.0, "Taxi", false, true);
        addRoute("Tunis Centre", "Carthage", 15.0, 25, 12.0, "Taxi", false, true);
        addRoute("Carthage", "Tunis Centre", 15.0, 25, 12.0, "Taxi", false, true);
        addRoute("Tunis Centre", "Sidi Bou Said", 20.0, 30, 18.0, "Taxi", false, true);
        addRoute("Sidi Bou Said", "Tunis Centre", 20.0, 30, 18.0, "Taxi", false, true);
        addRoute("Tunis Centre", "Le Bardo", 7.0, 15, 8.0, "Taxi", false, true);
        addRoute("Le Bardo", "Tunis Centre", 7.0, 15, 8.0, "Taxi", false, true);
        addRoute("Tunis Centre", "Ariana", 10.0, 20, 10.0, "Taxi", false, true);
        addRoute("Ariana", "Tunis Centre", 10.0, 20, 10.0, "Taxi", false, true);
        addRoute("Sousse Centre", "Hammam Sousse", 6.0, 12, 7.0, "Taxi", false, true);
        addRoute("Hammam Sousse", "Sousse Centre", 6.0, 12, 7.0, "Taxi", false, true);
        addRoute("Sousse Centre", "Port El Kantaoui", 10.0, 15, 12.0, "Taxi", false, true);
        addRoute("Port El Kantaoui", "Sousse Centre", 10.0, 15, 12.0, "Taxi", false, true);
    }
    
    private static void createFerryRoutes() {
        // Domestic ferry routes
        addRoute("Sfax", "Kerkennah", 20.0, 60, 4.0, "Ferry", false, false);
        addRoute("Kerkennah", "Sfax", 20.0, 60, 4.0, "Ferry", false, false);
        addRoute("El Jorf", "Djerba", 3.0, 20, 2.0, "Ferry", false, false);
        addRoute("Djerba", "El Jorf", 3.0, 20, 2.0, "Ferry", false, false);
        
        // International ferry routes (CTN - Compagnie Tunisienne de Navigation)
        addRoute("Tunis", "Marseille", 800.0, 1200, 150.0, "Ferry", true, false);
        addRoute("Marseille", "Tunis", 800.0, 1200, 150.0, "Ferry", true, false);
        addRoute("Tunis", "Genoa", 1000.0, 1440, 180.0, "Ferry", true, false);
        addRoute("Genoa", "Tunis", 1000.0, 1440, 180.0, "Ferry", true, false);
        addRoute("Tunis", "Naples", 700.0, 1080, 160.0, "Ferry", true, false);
        addRoute("Naples", "Tunis", 700.0, 1080, 160.0, "Ferry", true, false);
        addRoute("Tunis", "Civitavecchia", 800.0, 1200, 170.0, "Ferry", true, false);
        addRoute("Civitavecchia", "Tunis", 800.0, 1200, 170.0, "Ferry", true, false);
        addRoute("Tunis", "Palermo", 350.0, 600, 100.0, "Ferry", true, false);
        addRoute("Palermo", "Tunis", 350.0, 600, 100.0, "Ferry", true, false);
        
        // Seasonal routes (summer)
        addRoute("Zarzis", "Marseille", 850.0, 1320, 160.0, "Ferry", true, false);
        addRoute("Marseille", "Zarzis", 850.0, 1320, 160.0, "Ferry", true, false);
        addRoute("Sfax", "Marseille", 820.0, 1260, 155.0, "Ferry", true, false);
        addRoute("Marseille", "Sfax", 820.0, 1260, 155.0, "Ferry", true, false);
    }
    
    private static void createAirplaneRoutes() {
        // Domestic flights (Tunisair, Nouvelair, etc.)
        addRoute("Tunis Carthage Airport", "Djerba Airport", 450.0, 45, 120.0, "Avion", false, false);
        addRoute("Djerba Airport", "Tunis Carthage Airport", 450.0, 45, 120.0, "Avion", false, false);
        addRoute("Tunis Carthage Airport", "Sfax Thyna Airport", 270.0, 35, 100.0, "Avion", false, false);
        addRoute("Sfax Thyna Airport", "Tunis Carthage Airport", 270.0, 35, 100.0, "Avion", false, false);
        addRoute("Tunis Carthage Airport", "Tozeur Airport", 430.0, 55, 130.0, "Avion", false, false);
        addRoute("Tozeur Airport", "Tunis Carthage Airport", 430.0, 55, 130.0, "Avion", false, false);
        addRoute("Tunis Carthage Airport", "Monastir Habib Bourguiba Airport", 160.0, 30, 90.0, "Avion", false, false);
        addRoute("Monastir Habib Bourguiba Airport", "Tunis Carthage Airport", 160.0, 30, 90.0, "Avion", false, false);
        
        // International flights - Europe
        addRoute("Tunis Carthage Airport", "Paris Charles de Gaulle", 1500.0, 150, 350.0, "Avion", true, false);
        addRoute("Paris Charles de Gaulle", "Tunis Carthage Airport", 1500.0, 150, 350.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Paris Orly", 1500.0, 150, 340.0, "Avion", true, false);
        addRoute("Paris Orly", "Tunis Carthage Airport", 1500.0, 150, 340.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Marseille", 1300.0, 120, 280.0, "Avion", true, false);
        addRoute("Marseille", "Tunis Carthage Airport", 1300.0, 120, 280.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Lyon", 1400.0, 135, 300.0, "Avion", true, false);
        addRoute("Lyon", "Tunis Carthage Airport", 1400.0, 135, 300.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Rome", 800.0, 100, 250.0, "Avion", true, false);
        addRoute("Rome", "Tunis Carthage Airport", 800.0, 100, 250.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Milan", 900.0, 110, 260.0, "Avion", true, false);
        addRoute("Milan", "Tunis Carthage Airport", 900.0, 110, 260.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Barcelona", 1100.0, 120, 280.0, "Avion", true, false);
        addRoute("Barcelona", "Tunis Carthage Airport", 1100.0, 120, 280.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Madrid", 1300.0, 130, 290.0, "Avion", true, false);
        addRoute("Madrid", "Tunis Carthage Airport", 1300.0, 130, 290.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Frankfurt", 1700.0, 160, 320.0, "Avion", true, false);
        addRoute("Frankfurt", "Tunis Carthage Airport", 1700.0, 160, 320.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Brussels", 1800.0, 165, 330.0, "Avion", true, false);
        addRoute("Brussels", "Tunis Carthage Airport", 1800.0, 165, 330.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "London", 2000.0, 180, 350.0, "Avion", true, false);
        addRoute("London", "Tunis Carthage Airport", 2000.0, 180, 350.0, "Avion", true, false);
        
        // International flights - Middle East & North Africa
        addRoute("Tunis Carthage Airport", "Istanbul", 1800.0, 180, 300.0, "Avion", true, false);
        addRoute("Istanbul", "Tunis Carthage Airport", 1800.0, 180, 300.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Cairo", 2200.0, 210, 320.0, "Avion", true, false);
        addRoute("Cairo", "Tunis Carthage Airport", 2200.0, 210, 320.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Dubai", 4500.0, 330, 420.0, "Avion", true, false);
        addRoute("Dubai", "Tunis Carthage Airport", 4500.0, 330, 420.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Algiers", 750.0, 90, 230.0, "Avion", true, false);
        addRoute("Algiers", "Tunis Carthage Airport", 750.0, 90, 230.0, "Avion", true, false);
        addRoute("Tunis Carthage Airport", "Casablanca", 1800.0, 170, 290.0, "Avion", true, false);
        addRoute("Casablanca", "Tunis Carthage Airport", 1800.0, 170, 290.0, "Avion", true, false);
        
        // Routes from Monastir and Enfidha (Tourist airports)
        addRoute("Monastir Habib Bourguiba Airport", "Paris Orly", 1550.0, 155, 330.0, "Avion", true, false);
        addRoute("Paris Orly", "Monastir Habib Bourguiba Airport", 1550.0, 155, 330.0, "Avion", true, false);
        addRoute("Monastir Habib Bourguiba Airport", "London", 2050.0, 185, 340.0, "Avion", true, false);
        addRoute("London", "Monastir Habib Bourguiba Airport", 2050.0, 185, 340.0, "Avion", true, false);
        addRoute("Enfidha Airport", "Paris Charles de Gaulle", 1530.0, 152, 340.0, "Avion", true, false);
        addRoute("Paris Charles de Gaulle", "Enfidha Airport", 1530.0, 152, 340.0, "Avion", true, false);
        addRoute("Enfidha Airport", "Berlin", 1900.0, 175, 330.0, "Avion", true, false);
        addRoute("Berlin", "Enfidha Airport", 1900.0, 175, 330.0, "Avion", true, false);
    }
    
    /**
     * Add additional modern routes that are commonly searched for by users
     */
    private static void addModernRoutes() {
        logger.info("Adding modern routes frequently searched by users");
        
        // Express bus routes (long-distance, higher comfort)
        addRoute("Tunis", "Sfax", 270.0, 180, 35.0, "Bus", false, false);
        addRoute("Tunis", "Sousse", 140.0, 90, 20.0, "Bus", false, false);
        addRoute("Tunis", "Monastir", 160.0, 110, 23.0, "Bus", false, false);
        addRoute("Tunis", "Hammamet", 60.0, 50, 12.0, "Bus", false, false);
        addRoute("Sfax", "Monastir", 90.0, 70, 15.0, "Bus", false, false);
        
        // Premium bus services
        addRoute("Tunis", "Tabarka", 175.0, 140, 40.0, "Bus", false, false);
        addRoute("Tunis", "Djerba", 520.0, 360, 80.0, "Bus", false, false);
        addRoute("Tunis", "Tozeur", 430.0, 300, 65.0, "Bus", false, false);
        
        // Major Métro lines in Tunis with realistic details
        addRoute("Tunis Marine", "Place Barcelone", 2.5, 6, 0.7, "Métro", false, true);
        addRoute("Place Barcelone", "Mohamed Manachou", 1.5, 4, 0.7, "Métro", false, true);
        addRoute("Mohamed Manachou", "Bab Alioua", 2.0, 5, 0.7, "Métro", false, true);
        addRoute("Bab Alioua", "Ibn Khaldoun", 4.5, 12, 0.7, "Métro", false, true);
        
        // TGM line
        addRoute("Tunis Marine", "La Goulette", 6.0, 12, 1.0, "TGM", false, true);
        addRoute("La Goulette", "Carthage", 4.5, 10, 1.0, "TGM", false, true);
        addRoute("Carthage", "Sidi Bou Said", 3.0, 7, 1.0, "TGM", false, true);
        addRoute("Sidi Bou Said", "La Marsa", 4.0, 8, 1.0, "TGM", false, true);
        
        // Ferry routes from Tunis
        addRoute("La Goulette", "Naples", 550.0, 1200, 190.0, "Ferry", true, false);
        addRoute("La Goulette", "Marseille", 800.0, 1440, 220.0, "Ferry", true, false);
        addRoute("La Goulette", "Gênes", 750.0, 1320, 210.0, "Ferry", true, false);
        addRoute("Sfax", "Djerba", 120.0, 180, 35.0, "Ferry", false, false);
        
        // Taxi shared routes (Louage)
        addRoute("Tunis", "Nabeul", 75.0, 60, 15.0, "Taxi", false, false);
        addRoute("Tunis", "Hammamet", 70.0, 55, 14.0, "Taxi", false, false);
        addRoute("Tunis", "Bizerte", 60.0, 50, 13.0, "Taxi", false, false);
        addRoute("Sousse", "Monastir", 20.0, 15, 5.0, "Taxi", false, false);
        addRoute("Sousse", "Mahdia", 60.0, 45, 12.0, "Taxi", false, false);
        
        // Domestic air routes
        addRoute("Tunis", "Djerba", 520.0, 55, 150.0, "Avion", false, false);
        addRoute("Tunis", "Tozeur", 430.0, 50, 140.0, "Avion", false, false);
        addRoute("Tunis", "Sfax", 270.0, 40, 120.0, "Avion", false, false);
        
        // Intercity express train routes
        addRoute("Tunis", "Sousse", 140.0, 80, 25.0, "Train", false, false);
        addRoute("Tunis", "Sfax", 270.0, 150, 35.0, "Train", false, false);
        addRoute("Sousse", "Sfax", 130.0, 70, 18.0, "Train", false, false);
        
        logger.info("Modern routes added successfully");
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