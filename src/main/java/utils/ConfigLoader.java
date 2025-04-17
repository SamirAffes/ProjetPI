package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for loading configuration from .env file
 */
public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    
    static {
        loadEnvFile();
    }
    
    /**
     * Load environment variables from .env file
     */
    private static void loadEnvFile() {
        try {
            // Default values in case .env file is not found
            ENV_VARS.put("DB_USER", "root");
            ENV_VARS.put("DB_PASSWORD", "");
            ENV_VARS.put("DB_URL", "jdbc:mysql://localhost:3306/transportation_management");
            
            // Try to read from .env file
            if (Files.exists(Paths.get(".env"))) {
                LOGGER.info("Loading configuration from .env file");
                
                // Read file line by line
                Files.lines(Paths.get(".env"))
                     .map(String::trim)
                     .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                     .forEach(line -> {
                         String[] parts = line.split("=", 2);
                         if (parts.length == 2) {
                             String key = parts[0].trim();
                             // Remove quotes if present
                             String value = parts[1].trim().replaceAll("^\"|\"$", "");
                             ENV_VARS.put(key, value);
                         }
                     });
                
                LOGGER.info("Successfully loaded configuration");
            } else {
                LOGGER.warning(".env file not found. Using default values.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading .env file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get configuration value
     * @param key Configuration key
     * @return Configuration value or empty string if not found
     */
    public static String get(String key) {
        return ENV_VARS.getOrDefault(key, "");
    }
    
    /**
     * Get configuration value with default fallback
     * @param key Configuration key
     * @param defaultValue Default value if key is not found
     * @return Configuration value or default if not found
     */
    public static String get(String key, String defaultValue) {
        return ENV_VARS.getOrDefault(key, defaultValue);
    }
} 