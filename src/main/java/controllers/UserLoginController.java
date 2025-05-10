package controllers;

import entities.User;
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
import utils.UserContext;

import java.io.IOException;

@Slf4j
public class UserLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button backButton;

    @FXML
    private Label errorLabel;
    
    @FXML
    private Hyperlink registerLink;

    private final UserService userService = new UserService();

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) {
            showError("Veuillez saisir un nom d'utilisateur.");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Veuillez saisir un mot de passe.");
            return;
        }

        // Check if user exists and password is correct
        User user = userService.findByUsername(username);
        
        if (user != null && user.getPassword().equals(password)) {
            try {
                // Store user in global context
                UserContext.getInstance().setCurrentUser(user);
                log.info("User logged in: {}", user.getUsername());
                
                // Load user dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/dashboard.fxml"));
                Parent root = loader.load();
                
                // Pass user to dashboard controller
                UserDashboardController controller = loader.getController();
                controller.setUser(user);
                
                // Switch to dashboard scene
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setTitle("TunTransport - Tableau de bord");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.show();
            } catch (IOException e) {
                log.error("Error loading user dashboard", e);
                showError("Impossible de charger le tableau de bord utilisateur.");
            }
        } else {
            log.warn("Failed login attempt for username: {}", username);
            showError("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    @FXML
    public void onRegisterLinkClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TunTransport - Créer un compte");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Error loading registration view", e);
            showError("Impossible de charger la page d'inscription.");
        }
    }

    @FXML
    public void onBackButtonClick(ActionEvent event) {
        try {
            // Clear any user from context when returning to home
            UserContext.getInstance().clearCurrentUser();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TunTransport");
            stage.setScene(scene);
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
}
