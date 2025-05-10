package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;

@Slf4j
public class LoginSelectionController {

    @FXML
    private Button backButton;

    @FXML
    public void onUserLoginClick(MouseEvent event) {
        navigateToLoginPage("/fxml/user/login.fxml", "TunTransport - Connexion Utilisateur", event);
    }
    
    @FXML
    public void onUserRegisterClick(MouseEvent event) {
        navigateToLoginPage("/fxml/user/register.fxml", "TunTransport - Inscription Utilisateur", event);
    }

    @FXML
    public void onOrganisationLoginClick(MouseEvent event) {
        navigateToLoginPage("/fxml/organisation/login.fxml", "TunTransport - Connexion Organisation", event);
    }

    @FXML
    public void onAdminLoginClick(MouseEvent event) {
        navigateToLoginPage("/fxml/admin/login.fxml", "TunTransport - Connexion Admin", event);
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
            showError(e.getMessage());
        }
    }
    
    private void navigateToLoginPage(String fxmlPath, String title, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            log.error("Error loading login view: {}", fxmlPath, e);
            showErrorAlert("Navigation Error", "Impossible de charger la page de connexion.");
        }
    }
    
    private void showError(String message) {
        log.error("Error: {}", message);
        showErrorAlert("Error", message);
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 