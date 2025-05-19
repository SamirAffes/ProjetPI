package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class to manually update the database schema.
 * Used to add new columns to existing tables when entity model changes.
 */
public class DatabaseSchemaUpdater {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaUpdater.class);
    
    /**
     * Update the database schema to ensure all required columns exist
     * based on the latest entity models.
     */
    public static void updateSchema() {
        logger.info("Updating database schema for OrganisationRoute");
        
        try (Connection conn = db_context.getInstance().getConn();
            Statement stmt = conn.createStatement()) {
            
            // Add the new columns for enhanced route features
            addColumnIfNotExists(stmt, "OrganisationRoute", "wifiAvailable", "BOOLEAN DEFAULT FALSE");
            
            // Handling reserved keyword 'accessible' with backticks
            addColumnIfNotExists(stmt, "OrganisationRoute", "`accessible`", "BOOLEAN DEFAULT FALSE");
            
            addColumnIfNotExists(stmt, "OrganisationRoute", "airConditioned", "BOOLEAN DEFAULT FALSE");
            addColumnIfNotExists(stmt, "OrganisationRoute", "foodService", "BOOLEAN DEFAULT FALSE");
            
            // Add columns for price and duration (replacing routePrice and routeDuration)
            addColumnIfNotExists(stmt, "OrganisationRoute", "customPrice", "DOUBLE NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "customDuration", "INTEGER NULL");
            
            // Add the required routePrice field that is causing the error
            addColumnIfNotExists(stmt, "OrganisationRoute", "routePrice", "DOUBLE DEFAULT 0.0");
            
            // Add default value for the existing routeDuration field
            try {
                stmt.executeUpdate("ALTER TABLE OrganisationRoute MODIFY routeDuration INTEGER DEFAULT 0");
                logger.info("Added default value to routeDuration column");
            } catch (SQLException e) {
                logger.error("Error setting default value for routeDuration: {}", e.getMessage());
            }
            
            // Add other new fields
            addColumnIfNotExists(stmt, "OrganisationRoute", "departureStationId", "INTEGER NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "arrivalStationId", "INTEGER NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "weekdaySchedule", "VARCHAR(50) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "saturdaySchedule", "VARCHAR(50) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "sundaySchedule", "VARCHAR(50) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "holidaySchedule", "VARCHAR(50) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "firstDepartureTime", "VARCHAR(10) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "lastDepartureTime", "VARCHAR(10) NULL");
            addColumnIfNotExists(stmt, "OrganisationRoute", "operationalDays", "INTEGER DEFAULT 127");
            addColumnIfNotExists(stmt, "OrganisationRoute", "platformInfo", "VARCHAR(100) NULL");
            
            logger.info("Database schema updated successfully");
            
        } catch (SQLException e) {
            logger.error("Error updating database schema: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Add a column to a table if it doesn't already exist
     * 
     * @param stmt Statement to execute SQL
     * @param tableName Name of the table
     * @param columnName Name of the column to add (including any necessary SQL escaping)
     * @param columnDefinition SQL definition of the column (type, constraints, etc.)
     * @throws SQLException If an SQL error occurs
     */
    private static void addColumnIfNotExists(Statement stmt, String tableName, 
                                            String columnName, String columnDefinition) throws SQLException {
        try {
            String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + 
                        columnName + " " + columnDefinition;
            
            stmt.executeUpdate(sql);
            logger.info("Added column {} to table {}", columnName, tableName);
        } catch (SQLException e) {
            // Column already exists (Error code 1060 in MySQL)
            if (e.getErrorCode() == 1060) {
                logger.debug("Column {} already exists in table {}", columnName, tableName);
            } else {
                logger.error("Error adding column {} to table {}: {}", 
                            columnName, tableName, e.getMessage());
                throw e;
            }
        }
    }
}