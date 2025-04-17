package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class for displaying various alerts in the application
 */
public class AlertUtils {
    
    /**
     * Shows an information alert with the given title, header, and content
     * 
     * @param title Title of the alert dialog
     * @param header Header text for the alert
     * @param content Content text for the alert
     */
    public static void showInformation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert with the given title, header, and content
     * 
     * @param title Title of the alert dialog
     * @param header Header text for the alert
     * @param content Content text for the alert
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning alert with the given title, header, and content
     * 
     * @param title Title of the alert dialog
     * @param header Header text for the alert
     * @param content Content text for the alert
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation alert and returns whether the user confirmed the action
     * 
     * @param title Title of the alert dialog
     * @param header Header text for the alert
     * @param content Content text for the alert
     * @return true if the user confirmed, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
} 