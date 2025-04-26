package utils;

import entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.tool.schema.Action;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for initializing the database schema.
 * This ensures all tables are created if they don't already exist.
 */
public class DatabaseInitializer {
    
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    private static boolean initialized = false;
    
    /**
     * Initialize the database schema if it hasn't been initialized already.
     * This method uses JPA's automatic schema creation capabilities.
     */
    public static synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }
        
        logger.info("Initializing database schema...");
        
        try {
            // Get the EntityManagerFactory
            EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
            
            // Creating an EntityManager will trigger schema validation/creation
            EntityManager em = emf.createEntityManager();
            
            // Explicitly ensure all entity classes are loaded
            Class<?>[] entityClasses = {
                Conducteur.class,
                Maintenance.class,
                Organisation.class,
                Vehicule.class
            };
            
            // Register entity classes with the persistence context
            for (Class<?> entityClass : entityClasses) {
                logger.info("Registering entity: " + entityClass.getName());
            }
            
            // Using the EntityManager triggers schema validation
            em.getTransaction().begin();
            em.createNativeQuery("SELECT 1").getResultList(); // Simple query to initialize the database connection
            em.getTransaction().commit();
            
            em.close();
            
            logger.info("Database schema initialization complete.");
            initialized = true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing database schema", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    /**
     * Force recreate tables - WARNING: This will drop all tables and recreate them.
     * Only use this for development or testing scenarios when you need a fresh database.
     */
    public static synchronized void forceRecreateSchema() {
        logger.warning("CAUTION: Recreating database schema. This will delete all data!");
        
        try {
            EntityManagerFactory emf = JPAUtil.getEntityManagerFactory();
            EntityManager em = emf.createEntityManager();
            
            // Execute a schema drop-and-create operation
            em.getTransaction().begin();
            
            // Drop tables in reverse dependency order
            String[] dropTables = {
                "DROP TABLE IF EXISTS Maintenance",
                "DROP TABLE IF EXISTS Conducteur",
                "DROP TABLE IF EXISTS Vehicule",
                "DROP TABLE IF EXISTS Organisation"
            };
            
            for (String sql : dropTables) {
                em.createNativeQuery(sql).executeUpdate();
            }
            
            em.getTransaction().commit();
            em.close();
            
            // Reset initialized flag so the tables can be recreated
            initialized = false;
            
            // Reinitialize
            initializeDatabase();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error recreating database schema", e);
            throw new RuntimeException("Failed to recreate database schema", e);
        }
    }
}