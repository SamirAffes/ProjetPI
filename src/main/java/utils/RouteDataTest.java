package utils;

import services.RouteService;
import entities.Route;
import java.util.List;

/**
 * Simple utility class to test route data population.
 * This class can be used to manually trigger route population and verify the results.
 */
public class RouteDataTest {
    
    public static void main(String[] args) {
        // Initialize the database schema
        DatabaseInitializer.initializeDatabase();
        
        // Force populate routes (even if some already exist)
        System.out.println("Populating routes...");
        RouteDataPopulator.populateRoutes();
        
        // Verify routes were created
        RouteService routeService = new RouteService();
        List<Route> routes = routeService.afficher_tout();
        
        System.out.println("\nTotal routes created: " + routes.size());
        System.out.println("\nSample routes:");
        
        // Display a sample of routes
        int count = 0;
        for (Route route : routes) {
            System.out.printf("- %s → %s (%s): %.1f km, %d min, %.2f DT%n",
                route.getOrigin(),
                route.getDestination(),
                route.getTransportMode(),
                route.getDistance(),
                route.getEstimatedDuration(),
                route.getBasePrice());
            
            count++;
            if (count >= 10) {
                System.out.println("... and " + (routes.size() - 10) + " more routes");
                break;
            }
        }
        
        // Count routes by transport mode
        countRoutesByTransportMode(routes);
    }
    
    private static void countRoutesByTransportMode(List<Route> routes) {
        System.out.println("\nRoutes by transport mode:");
        
        long busCount = routes.stream().filter(r -> "Bus".equals(r.getTransportMode())).count();
        long trainCount = routes.stream().filter(r -> "Train".equals(r.getTransportMode())).count();
        long metroCount = routes.stream().filter(r -> "Métro".equals(r.getTransportMode())).count();
        long tgmCount = routes.stream().filter(r -> "TGM".equals(r.getTransportMode())).count();
        long taxiCount = routes.stream().filter(r -> "Taxi".equals(r.getTransportMode())).count();
        long ferryCount = routes.stream().filter(r -> "Ferry".equals(r.getTransportMode())).count();
        long avionCount = routes.stream().filter(r -> "Avion".equals(r.getTransportMode())).count();
        
        System.out.println("- Bus: " + busCount);
        System.out.println("- Train: " + trainCount);
        System.out.println("- Métro: " + metroCount);
        System.out.println("- TGM: " + tgmCount);
        System.out.println("- Taxi: " + taxiCount);
        System.out.println("- Ferry: " + ferryCount);
        System.out.println("- Avion: " + avionCount);
    }
} 