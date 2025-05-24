package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.models.VerificationToken;
import tn.esprit.testpifx.services.ServiceProvider;
import tn.esprit.testpifx.services.TokenService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for the forgot password dialog.
 */
public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;
    
    private UserService userService;
    
    @FXML
    public void initialize() {
        statusLabel.setText("");
    }
    
    /**
     * Sets the user service.
     * 
     * @param userService the user service to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Handles the submit button click.
     * Validates the email and initiates the password reset process.
     */
    @FXML
    private void handleSubmit() {
        if (userService == null) {
            showAlert("Error", "Application error: User service not initialized");
            return;
        }
        
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            statusLabel.setText("Please enter your email address");
            return;
        }
        
        if (!Utils.isValidEmail(email)) {
            statusLabel.setText("Please enter a valid email address");
            return;
        }
          // Attempt to send password reset email
        boolean sent = userService.initiatePasswordReset(email);
        
        if (sent) {
            // Get the user
            Optional<User> userOpt = userService.getUserByEmail(email);
            if (userOpt.isPresent()) {
                showAlert("Password Reset", "A verification code has been sent to your email. Please check your inbox and enter the code in the next screen.");
                showVerificationScreen(userOpt.get());
            } else {
                // This should not happen but just in case
                showAlert("Password Reset", "If an account exists with this email, a verification code has been sent. Please check your email inbox.");
                closeDialog();
            }
        } else {
            // Using the same message for non-existent emails to prevent user enumeration
            showAlert("Password Reset", "If an account exists with this email, a verification code has been sent. Please check your email inbox.");
            closeDialog();
        }
    }
    
    /**
     * Handles the cancel button click.
     * Closes the dialog.
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
      /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
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
    
    /**
     * Shows the verification code screen for password reset.
     * 
     * @param user the user to reset password for
     */
    private void showVerificationScreen(User user) {
        try {
            // First, we need to get the verification token
            TokenService tokenService = ServiceProvider.getTokenService();
            Optional<VerificationToken> tokenOpt = tokenService.getActiveTokenForUser(
                user.getUserId(), 
                VerificationToken.TokenType.PASSWORD_RESET
            );
            
            if (tokenOpt.isEmpty()) {
                showAlert("Error", "Could not find verification token. Please try again.");
                return;
            }
            
            String token = tokenOpt.get().getToken();
            
            // Show the verification code screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/verification-code.fxml"));
            Parent root = loader.load();
            
            VerificationCodeController controller = loader.getController();
            controller.setupForPasswordReset(userService, tokenService, user, token);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load verification screen: " + e.getMessage());
            e.printStackTrace();
            closeDialog();
        }
    }
}
