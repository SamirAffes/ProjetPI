package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.models.VerificationToken;
import tn.esprit.testpifx.services.TokenService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.services.ServiceProvider;
import tn.esprit.testpifx.utils.Utils;

import java.util.Optional;

import java.io.IOException;
import java.util.Objects;

public class SignupController {
    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;private UserService userService;
    
    public void setUserService(UserService userService) {
        this.userService = userService;
    }@FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String firstName = firstNameField != null ? firstNameField.getText().trim() : "";
        String lastName = lastNameField != null ? lastNameField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill all required fields");
            return;
        }

        if (userService.userExists(username)) {
            showAlert("Error", "Username already exists");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Error", "Email already registered");
            return;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters");
            return;
        }        try {
            User newUser = new User(username, password, email);
            
            // Set firstname and lastname if fields exist and have values
            if (firstName != null && !firstName.isEmpty()) {
                newUser.setFirstName(firstName);
                logger.info("Setting first name: {}", firstName);
            }
            
            if (lastName != null && !lastName.isEmpty()) {
                newUser.setLastName(lastName);
                logger.info("Setting last name: {}", lastName);
            }
            
            userService.createUser(newUser, false); // Create user without verification
            
            logger.info("User created successfully: {} with first name: '{}' and last name: '{}'", 
                username, newUser.getFirstName(), newUser.getLastName());
            
            // Get the generated verification token for the user
            TokenService tokenService = ServiceProvider.getTokenService();
            Optional<VerificationToken> tokenOpt = tokenService.getActiveTokenForUser(
                newUser.getUserId(), 
                VerificationToken.TokenType.ACCOUNT_VERIFICATION
            );
            
            if (tokenOpt.isPresent()) {
                showAlert("Account Created", "Your account has been created successfully! Please check your email for a verification code.");
                
                // Navigate to verification code screen
                navigateToVerificationCode(newUser, tokenOpt.get().getToken());
            } else {
                logger.warn("No verification token found for user: {}", username);
                showAlert("Account Created", "Your account has been created successfully! You can now log in.");
                // Fallback to login if token retrieval fails
                navigateToLogin();
            }
        } catch (Exception e) {
            logger.error("Failed to create account: {}", e.getMessage(), e);
            showAlert("Error", "Failed to create account: " + e.getMessage());
        }
    }    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            controller.setUserService(userService);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage); // Ensure consistent full-screen behavior
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToVerificationCode(User user, String fullToken) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/verification-code.fxml"));
            Parent root = loader.load();

            VerificationCodeController controller = loader.getController();
            TokenService tokenService = ServiceProvider.getTokenService();
            controller.setupForAccountVerification(userService, tokenService, user, fullToken);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage); // Ensure consistent full-screen behavior
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load verification code screen: " + e.getMessage());
            e.printStackTrace();
        }
    }private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }@FXML
    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSignUp();
        }
    }
}
