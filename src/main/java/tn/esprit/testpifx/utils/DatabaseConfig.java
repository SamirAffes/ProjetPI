package tn.esprit.testpifx.utils;

import java.io.*;
import java.util.Properties;

/**
 * Utility class for managing database configuration.
 * Allows reading and writing database connection parameters.
 */
public class DatabaseConfig {
    // Try multiple possible locations for the config file
    private static final String[] CONFIG_FILE_PATHS = {
        "database.properties", 
        "./database.properties",
        "../database.properties",
        "../../database.properties"
    };
    
    // Default MySQL configuration
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "javafx";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    
    private final Properties properties;
    private String configFilePath;
    
    /**
     * Constructor that loads the configuration from the properties file.
     * If the file doesn't exist, creates it with default values.
     */
    public DatabaseConfig() {
        properties = new Properties();
        boolean loaded = false;
        
        // Try to load from possible file paths
        for (String path : CONFIG_FILE_PATHS) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    FileInputStream input = new FileInputStream(file);
                    properties.load(input);
                    input.close();
                    configFilePath = path;
                    System.out.println("Loaded database configuration from " + file.getAbsolutePath());
                    loaded = true;
                    break;
                } catch (IOException e) {
                    System.err.println("Error loading properties from " + path + ": " + e.getMessage());
                }
            }
        }
        
        // If still not loaded, try to load from classpath
        if (!loaded) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
                if (input != null) {
                    properties.load(input);
                    configFilePath = "database.properties"; // Default to working directory for saving
                    System.out.println("Loaded database configuration from classpath");
                    loaded = true;
                }
            } catch (IOException e) {
                System.err.println("Error loading properties from classpath: " + e.getMessage());
            }
        }

        // If not loaded from any source, create default properties
        if (!loaded) {
            properties.setProperty("db.host", DEFAULT_HOST);
            properties.setProperty("db.port", DEFAULT_PORT);
            properties.setProperty("db.name", DEFAULT_DATABASE);
            properties.setProperty("db.user", DEFAULT_USERNAME);
            properties.setProperty("db.password", DEFAULT_PASSWORD);
            properties.setProperty("db.type", "MYSQL"); // Default to MySQL
            configFilePath = "database.properties"; // Default path
            
            // Save the default properties
            saveProperties();
            System.out.println("Created default database configuration at " + configFilePath);
        }
    }
    
    /**
     * Saves the current properties to the configuration file.
     */
    public void saveProperties() {
        try {
            FileOutputStream output = new FileOutputStream(configFilePath);
            properties.store(output, "Database Configuration");
            output.close();
        } catch (IOException e) {
            System.err.println("Error saving database configuration: " + e.getMessage());
        }
    }
    
    /**
     * Updates database connection parameters and saves them.
     * 
     * @param host The database host
     * @param port The database port
     * @param database The database name
     * @param username The database username
     * @param password The database password
     * @param dbType The database type (e.g., "MYSQL", "SQLITE")
     */
    public void updateDatabaseConfig(String host, String port, String database, 
                                    String username, String password, String dbType) {
        properties.setProperty("db.host", host);
        properties.setProperty("db.port", port);
        properties.setProperty("db.name", database);
        properties.setProperty("db.user", username);
        properties.setProperty("db.password", password);
        properties.setProperty("db.type", dbType);
        
        // Debug output to verify what values are being saved
        System.out.println("Saving DB config - host: " + host + ", port: " + port + 
                          ", database: " + database + ", username: " + username + 
                          ", password: [hidden], type: " + dbType);
        
        saveProperties();
    }
    
    /**
     * Gets the database host.
     * 
     * @return The database host
     */
    public String getHost() {
        return properties.getProperty("db.host", DEFAULT_HOST);
    }
    
    /**
     * Gets the database port.
     * 
     * @return The database port
     */
    public int getPort() {
        try {
            return Integer.parseInt(properties.getProperty("db.port", DEFAULT_PORT));
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULT_PORT);
        }
    }
    
    /**
     * Gets the database name.
     * 
     * @return The database name
     */
    public String getDatabase() {
        return properties.getProperty("db.name", DEFAULT_DATABASE);
    }
    
    /**
     * Gets the database username.
     * 
     * @return The database username
     */
    public String getUsername() {
        return properties.getProperty("db.user", DEFAULT_USERNAME);
    }
    
    /**
     * Gets the database password.
     * 
     * @return The database password
     */
    public String getPassword() {
        return properties.getProperty("db.password", DEFAULT_PASSWORD);
    }
    
    /**
     * Gets the database type.
     * 
     * @return The database type
     */
    public String getDatabaseType() {
        return properties.getProperty("db.type", "MYSQL");
    }
}