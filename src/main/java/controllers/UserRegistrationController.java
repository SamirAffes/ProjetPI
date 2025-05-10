package controllers;

import entities.User;
import entities.UserRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.UserService;

import java.io.IOException;
import java.util.Date;

@Slf4j
public class UserRegistrationController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onRegisterButtonClick(ActionEvent event) {
        // Clear previous errors
        hideError();
        
        // Get form values
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        
        // Validate input
        if (username.isEmpty()) {
            showError("Le nom d'utilisateur est obligatoire.");
            return;
        }
        
        if (fullName.isEmpty()) {
            showError("Le nom complet est obligatoire.");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Le mot de passe est obligatoire.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }
        
        if (email.isEmpty()) {
            showError("L'email est obligatoire.");
            return;
        }
        
        // Check if username already exists
        if (userService.findByUsername(username) != null) {
            showError("Ce nom d'utilisateur est déjà utilisé.");
            return;
        }
        
        // Check if email already exists
        if (userService.findByEmail(email) != null) {
            showError("Cet email est déjà utilisé.");
            return;
        }
        
        try {
            // Create new user object
            User user = User.builder()
                    .username(username)
                    .password(password)
                    .fullName(fullName)
                    .email(email)
                    .phoneNumber(phone)
                    .address(address)
                    .registrationDate(new Date())
                    .role(UserRole.REGULAR_USER)
                    .organisationId(1)
                    .build();
            
            // Save user
            userService.ajouter(user);
            log.info("New user registered: {}", username);
            
            // Navigate to login page
            showLoginPage(event);
        } catch (Exception e) {
            log.error("Error registering user", e);
            showError("Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
        }
    }

    @FXML
    public void onLoginLinkClick(ActionEvent event) {
        showLoginPage(event);
    }

    private void showLoginPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TunTransport - Connexion");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            log.error("Error loading login view", e);
            showError("Impossible de charger la page de connexion.");
        }
    }

    @FXML
    public void onBackButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TunTransport");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            log.error("Error returning to home view", e);
            showError("Impossible de retourner à la page d'accueil.");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void hideError() {
        errorLabel.setVisible(false);
    }
}
