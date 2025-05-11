package tn.esprit.testpifx.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class containing common methods used across the application.
 */
public class Utils {
    /**
     * Default profile picture URL to use when none is provided or when the URL is invalid.
     */
    public static final String DEFAULT_PROFILE_PICTURE_URL = "https://via.placeholder.com/100";

    /**
     * Regular expression for validating email addresses.
     */
    private static final String EMAIL_REGEX = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    /**
     * Regular expression for validating phone numbers (digits only).
     */
    private static final String PHONE_REGEX = "^\\d+$";

    /**
     * Regular expression for validating zip codes (digits only).
     */
    private static final String ZIP_REGEX = "^\\d+$";

    /**
     * Regular expression for validating country prefixes (+ followed by digits).
     */
    private static final String COUNTRY_PREFIX_REGEX = "^\\+\\d+$";

    /**
     * Shows an alert dialog with the specified title and message.
     *
     * @param title     The title of the alert
     * @param message   The message to display
     * @param alertType The type of alert (e.g., ERROR, INFORMATION)
     */
    public static void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows an error alert dialog with the specified title and message.
     *
     * @param title   The title of the alert
     * @param message The message to display
     */
    public static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog with the specified title and message.
     *
     * @param title   The title of the alert
     * @param message The message to display
     */
    public static void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation alert dialog with the specified title and message.
     *
     * @param title   The title of the alert
     * @param message The message to display
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmationDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Validates an email address.
     *
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return email.matches(EMAIL_REGEX);
    }

    /**
     * Validates a phone number (digits only).
     *
     * @param phoneNumber The phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return true; // Phone number is optional
        }
        return phoneNumber.matches(PHONE_REGEX);
    }

    /**
     * Validates a zip code (digits only).
     *
     * @param zipCode The zip code to validate
     * @return true if the zip code is valid, false otherwise
     */
    public static boolean isValidZipCode(String zipCode) {
        if (zipCode == null || zipCode.isEmpty()) {
            return true; // Zip code is optional
        }
        return zipCode.matches(ZIP_REGEX);
    }

    /**
     * Validates a country prefix (+ followed by digits).
     *
     * @param countryPrefix The country prefix to validate
     * @return true if the country prefix is valid, false otherwise
     */
    public static boolean isValidCountryPrefix(String countryPrefix) {
        if (countryPrefix == null || countryPrefix.isEmpty()) {
            return true; // Country prefix is optional
        }
        return countryPrefix.matches(COUNTRY_PREFIX_REGEX);
    }

    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Utils.class.getResource("/tn/esprit/testpifx/styles/modern.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
    }

    public static void centerStage(Stage stage) {
        stage.centerOnScreen();
    }

    public static void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Configures a stage to be displayed in full-screen mode with consistent settings.
     * This method should be called whenever a new window/stage is created or shown.
     *
     * @param stage The stage to configure for full-screen display
     */
    public static void setFullScreenMode(Stage stage) {
        // Delegate to the SceneManager for consistent window behavior
        SceneManager.applyStandardWindowSettings(stage);
    }
}
