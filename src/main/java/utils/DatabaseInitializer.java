package utils;

import entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

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
            logger.info("Database already initialized, skipping");
            return;
        }
        
        logger.info("Initializing database schema...");
        
        try (EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            
            try {
                transaction.begin();
                
                // Check if schema already exists, if not create it
                String[] tables = {"organisation", "user", "route", "organisationroute", "vehicle", "driver", "maintenance", "reservation", "station"};
                
                boolean needsInitialization = !tablesExist(entityManager, tables);
                
                if (needsInitialization) {
                    logger.info("Initializing database schema");
                    
                    // Create schema
                    entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS organisation (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nom VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "telephone VARCHAR(20), " +
                        "address VARCHAR(200), " +
                        "logo VARCHAR(200), " +
                        "website VARCHAR(100), " +
                        "description TEXT, " +
                        "password VARCHAR(100) NOT NULL" +
                        ")").executeUpdate();
                    
                    entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS user (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nom VARCHAR(50) NOT NULL, " +
                        "prenom VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "telephone VARCHAR(20), " +
                        "address VARCHAR(200), " +
                        "password VARCHAR(100) NOT NULL, " +
                        "role VARCHAR(20) DEFAULT 'USER'," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ")").executeUpdate();
                    
                    entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS route (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "origin VARCHAR(100) NOT NULL, " +
                        "destination VARCHAR(100) NOT NULL, " +
                        "distance DOUBLE, " +
                        "estimated_duration INT, " +
                        "base_price DOUBLE NOT NULL, " +
                        "company_id INT, " +
                        "company_name VARCHAR(100), " +
                        "route_type VARCHAR(50), " +
                        "transport_mode VARCHAR(50), " +
                        "is_international BOOLEAN DEFAULT FALSE, " +
                        "is_intra_city BOOLEAN DEFAULT FALSE" +
                        ")").executeUpdate();
                    
                    entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS organisationroute (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "organisation_id INT NOT NULL, " +
                        "route_id INT NOT NULL, " +
                        "is_active BOOLEAN DEFAULT TRUE, " +
                        "internal_route_code VARCHAR(50), " +
                        "assigned_vehicle_type VARCHAR(50), " +
                        "frequency_per_day INT DEFAULT 1, " +
                        "departure_time VARCHAR(10), " +
                        "arrival_time VARCHAR(10), " +
                        "notes TEXT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                        "departure_station_id INT, " +
                        "arrival_station_id INT, " +
                        "weekday_schedule VARCHAR(20), " +
                        "saturday_schedule VARCHAR(20), " +
                        "sunday_schedule VARCHAR(20), " +
                        "holiday_schedule VARCHAR(20), " +
                        "first_departure_time VARCHAR(10), " +
                        "last_departure_time VARCHAR(10), " +
                        "operational_days INT DEFAULT 127, " +
                        "route_price DOUBLE, " +
                        "route_duration INT, " +
                        "platform_info VARCHAR(50), " +
                        "FOREIGN KEY (organisation_id) REFERENCES organisation(id), " +
                        "FOREIGN KEY (route_id) REFERENCES route(id), " +
                        "UNIQUE KEY org_route_unique (organisation_id, route_id)" +
                        ")").executeUpdate();
                    
                    entityManager.createNativeQuery("CREATE TABLE IF NOT EXISTS station (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "city VARCHAR(100) NOT NULL, " +
                        "address VARCHAR(200), " +
                        "latitude DOUBLE, " +
                        "longitude DOUBLE, " +
                        "station_type VARCHAR(50), " +
                        "organisation_id INT, " +
                        "station_code VARCHAR(20), " +
                        "opening_hours VARCHAR(100)" +
                        ")").executeUpdate();
                    
                    // Add other table creation queries as needed
                    
                    // Commit the transaction
                    transaction.commit();
                    logger.info("Database schema initialized successfully");
                } else {
                    // Tables already exist, just commit and continue
                    transaction.commit();
                    logger.info("Database schema already exists, skipping initialization");
                }
                
                initialized = true;
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                logger.log(Level.SEVERE, "Error initializing database schema: " + e.getMessage(), e);
                throw new RuntimeException("Failed to initialize database", e);
            }
        }
        
        // Check if routes exist, and populate sample routes if needed
        try {
            logger.info("Checking if routes need to be populated...");
            RouteDataPopulator.populateRoutesIfEmpty();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while populating routes: " + e.getMessage(), e);
        }
        
        // Check if stations exist, and create default stations if needed
        try {
            createDefaultStationsIfNeeded();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while creating default stations: " + e.getMessage(), e);
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
                "DROP TABLE IF EXISTS OrganisationRoute",
                "DROP TABLE IF EXISTS Maintenance",
                "DROP TABLE IF EXISTS Conducteur",
                "DROP TABLE IF EXISTS Vehicule",
                "DROP TABLE IF EXISTS Route",
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

    /**
     * Check if all the specified tables exist in the database
     * @param entityManager The EntityManager
     * @param tables Array of table names to check
     * @return true if all tables exist, false otherwise
     */
    private static boolean tablesExist(EntityManager entityManager, String[] tables) {
        try {
            for (String table : tables) {
                // Try to query the table to see if it exists
                try {
                    entityManager.createNativeQuery("SELECT 1 FROM " + table + " LIMIT 1").getResultList();
                } catch (Exception e) {
                    // If the query fails, the table doesn't exist
                    logger.info("Table " + table + " does not exist");
                    return false;
                }
            }
            // All tables exist
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking if tables exist", e);
            return false;
        }
    }

    /**
     * Creates default stations if none exist in the database
     */
    private static void createDefaultStationsIfNeeded() {
        try {
            // Check if we already have stations
            EntityManager entityManager = JPAUtil.getEntityManagerFactory().createEntityManager();
            Long stationCount = entityManager.createQuery("SELECT COUNT(s) FROM Station s", Long.class).getSingleResult();
            entityManager.close();
            
            if (stationCount > 0) {
                logger.info("Stations already exist in database, skipping creation of default stations");
                return;
            }
            
            logger.info("No stations found, creating default stations");
            
            // Create a service to handle station persistence
            services.StationService stationService = new services.StationService();
            
            // Create some default stations
            // Bus stations
            Station tunisBusStation = Station.builder()
                .name("Gare Routière de Tunis")
                .city("Tunis")
                .stationType("Bus")
                .build();
            stationService.ajouter(tunisBusStation);
            
            Station sousseStation = Station.builder()
                .name("Gare Routière de Sousse")
                .city("Sousse")
                .stationType("Bus")
                .build();
            stationService.ajouter(sousseStation);
            
            // Train stations
            Station tunisTrainStation = Station.builder()
                .name("Gare de Tunis")
                .city("Tunis")
                .stationType("Train")
                .build();
            stationService.ajouter(tunisTrainStation);
            
            // Airport
            Station tunisAirport = Station.builder()
                .name("Aéroport Tunis-Carthage")
                .city("Tunis")
                .stationType("Avion")
                .stationCode("TUN")
                .build();
            stationService.ajouter(tunisAirport);
            
            logger.info("Created default stations successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating default stations", e);
        }
    }
}