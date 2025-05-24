package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the reset password screen.
 * This is used when a user clicks on a password reset link in their email.
 */
public class ResetPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);
    
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;
    @FXML private Label statusLabel;
    
    private UserService userService;
    private String token;
    
    /**
     * Sets the user service and token for password reset.
     * 
     * @param userService the user service to use for password reset
     * @param token the password reset token
     */
    public void initialize(UserService userService, String token) {
        this.userService = userService;
        this.token = token;
        statusLabel.setText("");
    }
    
    /**
     * Handles the reset password button click.
     * Validates the password and calls the user service to reset the password.
     */
    @FXML
    private void handleResetPassword() {
        if (userService == null || token == null) {
            showAlert("Error", "Invalid password reset session. Please try the reset link again.");
            return;
        }
        
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate password
        if (password.isEmpty()) {
            statusLabel.setText("Password cannot be empty");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match");
            return;
        }
        
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters long");
            return;
        }
        
        // Attempt to reset password
        boolean success = userService.resetPassword(token, password);
        
        if (success) {
            showAlert("Success", "Your password has been reset successfully. You can now log in with your new password.");
            navigateToLogin();
        } else {
            showAlert("Error", "Failed to reset password. The reset link may have expired or is invalid. Please request a new reset link.");
        }
    }
    
    /**
     * Navigates back to the login screen.
     */
    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            
            Stage stage = (Stage) resetButton.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage);
            stage.show();
            
            // Get the controller and inject services
            AuthController controller = loader.getController();
            // Note: You would typically have a service provider to get these services
            // For now, we'll rely on the AuthController to initialize its services automatically
            
        } catch (IOException e) {
            logger.error("Failed to navigate to login screen", e);
            showAlert("Error", "Failed to navigate to login screen: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog.
     * 
     * @param title the title of the alert
     * @param message the message to display
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
