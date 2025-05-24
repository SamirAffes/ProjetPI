package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TokenService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Controller for the verification code screen.
 * This is used when a user needs to enter a verification code from their email.
 */
public class VerificationCodeController {
    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeController.class);
    
    @FXML private Label titleLabel;
    @FXML private Label instructionLabel;
    @FXML private TextField codeField;
    @FXML private Label statusLabel;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Button resendButton;
    
    private UserService userService;
    private TokenService tokenService;
    private String fullToken;
    private User user;
    private VerificationType verificationType;
    
    public enum VerificationType {
        ACCOUNT_VERIFICATION,
        PASSWORD_RESET
    }
    
    /**
     * Initializes the controller with basic services and data.
     */
    @FXML
    public void initialize() {
        statusLabel.setText("");
    }
    
    /**
     * Sets up the controller for account verification.
     * 
     * @param userService the user service
     * @param tokenService the token service
     * @param user the user to verify
     */
    public void setupForAccountVerification(UserService userService, TokenService tokenService, User user, String fullToken) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.user = user;
        this.fullToken = fullToken;
        this.verificationType = VerificationType.ACCOUNT_VERIFICATION;
        
        titleLabel.setText("Account Verification");
        instructionLabel.setText("Please enter the 6-digit verification code that was sent to " + user.getEmail());
    }
    
    /**
     * Sets up the controller for password reset.
     * 
     * @param userService the user service
     * @param tokenService the token service
     * @param user the user to reset password for
     */
    public void setupForPasswordReset(UserService userService, TokenService tokenService, User user, String fullToken) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.user = user;
        this.fullToken = fullToken;
        this.verificationType = VerificationType.PASSWORD_RESET;
        
        titleLabel.setText("Password Reset");
        instructionLabel.setText("Please enter the 6-digit verification code that was sent to " + user.getEmail());
    }
    
    /**
     * Handles the submit button click.
     * Validates the code and processes verification.
     */
    @FXML
    private void handleSubmit() {
        String code = codeField.getText().trim().toUpperCase();
        
        if (code.isEmpty()) {
            statusLabel.setText("Please enter the verification code");
            return;
        }
        
        if (code.length() != 6) {
            statusLabel.setText("The verification code should be 6 characters");
            return;
        }
        
        // Get first 6 chars of the full token to compare
        String expectedCode = fullToken.substring(0, 6).toUpperCase();
        
        if (!code.equals(expectedCode)) {
            statusLabel.setText("Invalid verification code. Please try again.");
            return;
        }
        
        // Code is valid, proceed with verification
        if (verificationType == VerificationType.ACCOUNT_VERIFICATION) {
            processAccountVerification();
        } else {
            processPasswordReset();
        }
    }
    
    /**
     * Process account verification.
     */
    private void processAccountVerification() {
        boolean success = tokenService.verifyAccountToken(fullToken);
        
        if (success) {
            Utils.showSuccessAlert("Success", "Your account has been successfully verified. You can now log in.");
            closeAndShowLogin();
        } else {
            statusLabel.setText("Account verification failed. Please try again or contact support.");
        }
    }
    
    /**
     * Process password reset.
     */
    private void processPasswordReset() {
        try {
            // Open the reset password screen with the token
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/reset-password.fxml"));
            Parent root = loader.load();
            
            ResetPasswordController controller = loader.getController();
            controller.initialize(userService, fullToken);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            
            Stage currentStage = (Stage) submitButton.getScene().getWindow();
            currentStage.close();
            
            Stage stage = new Stage();
            stage.setTitle("Reset Password");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load reset password screen", e);
            statusLabel.setText("Error loading password reset screen. Please try again.");
        }
    }
    
    /**
     * Handles the cancel button click.
     * Closes the dialog.
     */
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Handles the resend code button click.
     * Resends the verification code.
     */
    @FXML
    private void handleResend() {
        if (user == null || userService == null) {
            statusLabel.setText("Cannot resend code. Please try again later.");
            return;
        }
        
        if (verificationType == VerificationType.ACCOUNT_VERIFICATION) {
            userService.sendVerificationEmail(user);
            statusLabel.setText("A new verification code has been sent to your email.");
        } else {
            userService.initiatePasswordReset(user.getEmail());
            statusLabel.setText("A new password reset code has been sent to your email.");
        }
    }
    
    /**
     * Closes the current window and shows the login screen.
     */
    private void closeAndShowLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            
            Stage currentStage = (Stage) submitButton.getScene().getWindow();
            currentStage.close();
            
            Stage stage = new Stage();
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load login screen", e);
        }
    }
}
