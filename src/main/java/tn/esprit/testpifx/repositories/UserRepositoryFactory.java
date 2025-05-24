package tn.esprit.testpifx.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Factory class for creating UserRepository instances.
 * This class provides methods to create different types of UserRepository implementations.
 */
public class UserRepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryFactory.class);
    
    /**
     * Repository type enumeration
     */
    public enum RepositoryType {
        SQLITE,
        MYSQL,
        IN_MEMORY
    }
    
    /**
     * Creates a UserRepository instance of the specified type.
     * 
     * @param type The type of repository to create
     * @return A UserRepository instance
     */
    public static UserRepository createRepository(RepositoryType type) {
        logger.info("Creating repository of type: {}", type);

        switch (type) {
            case MYSQL:
                return createMySQLRepositoryFromProperties();
            default:
                logger.warn("Unknown repository type: {}. Defaulting to MySQL.", type);
                return createMySQLRepositoryFromProperties();
        }
    }
    
    /**
     * Creates a MySQL repository with custom connection parameters.
     * 
     * @param host The database host
     * @param port The database port
     * @param database The database name
     * @param username The database username
     * @param password The database password
     * @return A MySQLUserRepository instance
     */
    public static UserRepository createMySQLRepository(String host, int port, String database, String username, String password) {
        logger.info("Creating MySQL repository with custom connection parameters");
        return new MySQLUserRepository(host, port, database, username, password);
    }
    
    /**
     * Loads MySQL connection parameters from database.properties and creates a repository.
     */
    private static UserRepository createMySQLRepositoryFromProperties() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("database.properties")) {
            props.load(fis);
        } catch (IOException e) {
            logger.error("Could not load database.properties, using defaults: {}", e.getMessage());
        }
        String host = props.getProperty("db.host", "localhost");
        int port = Integer.parseInt(props.getProperty("db.port", "3306"));
        String database = props.getProperty("db.name", "javafx");
        String username = props.getProperty("db.user", "root");
        String password = props.getProperty("db.password", "");
        return new MySQLUserRepository(host, port, database, username, password);
    }

    /**
     * Creates a repository based on the system property "repository.type".
     * If the property is not set, defaults to SQLite.
     * 
     * @return A UserRepository instance
     */
    public static UserRepository createRepositoryFromSystemProperty() {
        String repositoryType = System.getProperty("repository.type", "MYSQL");
        try {
            RepositoryType type = RepositoryType.valueOf(repositoryType.toUpperCase());
            return createRepository(type);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid repository type: {}. Defaulting to MySQL.", repositoryType);
            return createRepository(RepositoryType.MYSQL);
        }
    }
}