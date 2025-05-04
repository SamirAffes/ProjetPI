package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class EnvLoader {
    
    static {
        loadEnvVariables();
    }
    
    public static void loadEnvVariables() {
        Properties props = new Properties();
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
        
        try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
            props.load(fis);
            
            // Set system properties from .env file
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                System.setProperty(key, value);
            }
            
            System.out.println("Environment variables loaded successfully");
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
        }
    }
}
