package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;

/**
 * Controller for the account verification screen.
 * This is used when a user clicks on an account verification link in their email.
 */
public class VerifyAccountController {
    private static final Logger logger = LoggerFactory.getLogger(VerifyAccountController.class);
    
    @FXML private Label statusLabel;
    @FXML private Button loginButton;
    
    private UserService userService;
    private String token;
    private boolean verificationSuccessful = false;
    
    /**
     * Sets the user service and token for account verification.
     * Immediately attempts to verify the account.
     * 
     * @param userService the user service to use for verification
     * @param token the verification token
     */
    public void initialize(UserService userService, String token) {
        this.userService = userService;
        this.token = token;
        
        // Attempt to verify the account as soon as the controller is initialized
        if (userService != null && token != null) {
            verifyAccount();
        } else {
            statusLabel.setText("Invalid verification request. Please check the verification link.");
        }
    }
    
    /**
     * Attempts to verify the user account with the provided token.
     */
    private void verifyAccount() {
        try {
            boolean success = userService.verifyUserAccount(token);
            
            if (success) {
                statusLabel.setText("Your account has been verified successfully! You can now log in.");
                verificationSuccessful = true;
            } else {
                statusLabel.setText("Account verification failed. The verification link may have expired or is invalid.");
            }
        } catch (Exception e) {
            logger.error("Error verifying account", e);
            statusLabel.setText("An error occurred during verification: " + e.getMessage());
        }
    }
    
    /**
     * Navigates to the login screen.
     */
    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage);
            stage.show();
            
        } catch (IOException e) {
            logger.error("Failed to navigate to login screen", e);
            statusLabel.setText("Failed to navigate to login screen: " + e.getMessage());
        }
    }
}
