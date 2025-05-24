package tn.esprit.testpifx.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating TeamRepository instances.
 * This class provides methods to create different types of TeamRepository implementations.
 */
public class TeamRepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(TeamRepositoryFactory.class);
    
    /**
     * Repository type enumeration
     */
    public enum RepositoryType {
        IN_MEMORY,
        MYSQL
    }
    
    /**
     * Creates a TeamRepository instance of the specified type.
     * 
     * @param type The type of repository to create
     * @return A TeamRepository instance
     */
    public static TeamRepository createRepository(RepositoryType type) {
        logger.info("Creating team repository of type: {}", type);
        
        switch (type) {
            case MYSQL:
                return new MySQLTeamRepository();
            default:
                logger.warn("Unknown repository type: {}. Defaulting to MySQL.", type);
                return new MySQLTeamRepository();
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
     * @return A MySQLTeamRepository instance
     */
    public static TeamRepository createMySQLRepository(String host, int port, String database, String username, String password) {
        logger.info("Creating MySQL team repository with custom connection parameters");
        return new MySQLTeamRepository(host, port, database, username, password);
    }
    
    /**
     * Creates a repository based on the system property "team.repository.type".
     * If the property is not set, defaults to MYSQL.
     * 
     * @return A TeamRepository instance
     */
    public static TeamRepository createRepositoryFromSystemProperty() {
        String repositoryType = System.getProperty("team.repository.type", "MYSQL");
        try {
            RepositoryType type = RepositoryType.valueOf(repositoryType.toUpperCase());
            return createRepository(type);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid repository type: {}. Defaulting to MySQL.", repositoryType);
            return createRepository(RepositoryType.MYSQL);
        }
    }
}