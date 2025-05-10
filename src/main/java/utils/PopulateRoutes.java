package utils;

import entities.Route;
import services.RouteService;

/**
 * Simple utility class to manually populate routes.
 * This can be called from anywhere in the application to ensure routes exist.
 */
public class PopulateRoutes {
    
    /**
     * Main method to run this as a standalone script.
     */
    public static void main(String[] args) {
        System.out.println("Starting route population...");
        
        try {
            // Initialize database
            DatabaseInitializer.initializeDatabase();
            
            // Force populate routes
            RouteDataPopulator.populateRoutes();
            
            // Verify routes were created
            RouteService routeService = new RouteService();
            int routeCount = routeService.afficher_tout().size();
            
            System.out.println("Route population complete. Created " + routeCount + " routes.");
            
        } catch (Exception e) {
            System.err.println("Error populating routes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Call this method to ensure routes are populated.
     * This is safe to call multiple times as it only adds routes if none exist.
     */
    public static void ensureRoutesExist() {
        try {
            RouteDataPopulator.populateRoutesIfEmpty();
        } catch (Exception e) {
            System.err.println("Error ensuring routes exist: " + e.getMessage());
        }
    }
} 