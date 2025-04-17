package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Singleton class for database connection management
 */
public class DbContext {
    private static volatile DbContext instance;
    private Connection conn;
    private static final Logger LOGGER = Logger.getLogger(DbContext.class.getName());
    
    // Database connection details from environment variables
    private static final String USER = ConfigLoader.get("DB_USER");
    private static final String PASSWORD = ConfigLoader.get("DB_PASSWORD");
    private static final String URL = ConfigLoader.get("DB_URL");

    /**
     * Private constructor - creates database connection
     */
    private DbContext() {
        try {
            LOGGER.info("Connecting to database with URL: " + URL);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.info("Connected to database successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error: " + e.getMessage(), e);
        }
    }

    /**
     * Get singleton instance with double-checked locking
     * @return DbContext instance
     */
    public static DbContext getInstance() {
        DbContext temp = instance;
        if (temp == null) {
            synchronized (DbContext.class) {
                temp = instance;
                if (temp == null) instance = temp = new DbContext();
            }
        }
        return temp;
    }

    /**
     * Get database connection
     * @return Active database connection
     */
    public Connection getConn() {
        return conn;
    }
}
