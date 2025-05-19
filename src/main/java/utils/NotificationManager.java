package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to manage notifications throughout the application.
 * Uses JavaFX Alerts for a better UI experience.
 */
public class NotificationManager {
    private static final Logger logger = LoggerFactory.getLogger(NotificationManager.class);
    
    /**
     * Show a success notification
     * @param title Title of the notification
     * @param message Message to display
     */
    public static void showSuccess(String title, String message) {
        showNotification(AlertType.SUCCESS, title, message);
        logger.info("Success notification: {} - {}", title, message);
    }
    
    /**
     * Show an info notification
     * @param title Title of the notification
     * @param message Message to display
     */
    public static void showInfo(String title, String message) {
        showNotification(AlertType.INFO, title, message);
        logger.info("Info notification: {} - {}", title, message);
    }
    
    /**
     * Show a warning notification
     * @param title Title of the notification
     * @param message Message to display
     */
    public static void showWarning(String title, String message) {
        showNotification(AlertType.WARNING, title, message);
        logger.warn("Warning notification: {} - {}", title, message);
    }
    
    /**
     * Show an error notification
     * @param title Title of the notification
     * @param message Message to display
     */
    public static void showError(String title, String message) {
        showNotification(AlertType.ERROR, title, message);
        logger.error("Error notification: {} - {}", title, message);
    }
    
    /**
     * Show a notification with the specified type
     * @param type Type of notification
     * @param title Title of the notification
     * @param message Message to display
     */
    private static void showNotification(AlertType type, String title, String message) {
        // Run on JavaFX thread
        Platform.runLater(() -> {
            Alert.AlertType alertType;
            
            // Map our internal types to JavaFX types
            switch (type) {
                case SUCCESS:
                case INFO:
                    alertType = Alert.AlertType.INFORMATION;
                    break;
                case WARNING:
                    alertType = Alert.AlertType.WARNING;
                    break;
                case ERROR:
                    alertType = Alert.AlertType.ERROR;
                    break;
                default:
                    alertType = Alert.AlertType.INFORMATION;
            }
            
            // Create and configure alert
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Always show on top
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            
            // Make it modal to force user attention
            alert.initModality(Modality.APPLICATION_MODAL);
            
            // Show alert and wait for user to close it
            alert.showAndWait();
        });
    }
    
    /**
     * Enum representing different notification types
     */
    private enum AlertType {
        SUCCESS,
        INFO,
        WARNING,
        ERROR
    }
} 