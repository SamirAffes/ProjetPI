package utils;

import config.EnvLoader;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static final String PERSISTENCE_UNIT_NAME = "pu";
    private static EntityManagerFactory factory;
    
    static {
        // Ensure environment variables are loaded before creating EntityManagerFactory
        EnvLoader.loadEnvVariables();
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            // Initialize database schema when EntityManagerFactory is created
            DatabaseInitializer.initializeDatabase();
        }
        return factory;
    }
    
    public static void shutdown() {
        if (factory != null) {
            factory.close();
        }
    }
}
