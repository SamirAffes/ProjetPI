package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AdminLoginController {

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

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir un nom d'utilisateur et un mot de passe.");
            return;
        }

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/dashboard.fxml"));
                Parent root = loader.load();
                
                AdminDashboardController controller = loader.getController();
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setTitle("TuniTransport - Administration");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.show();
                
                log.info("Successful admin login: {}", username);
            } catch (IOException e) {
                log.error("Error loading admin dashboard", e);
                showError("Impossible de charger le tableau de bord administrateur.");
            }
        } else {
            log.warn("Failed login attempt for admin user: {}", username);
            showError("Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    @FXML
    public void onBackButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport");
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
}