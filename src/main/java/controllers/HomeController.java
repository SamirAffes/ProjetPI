package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class HomeController {

    @FXML
    private Button adminButton;

    @FXML
    private Button orgButton;

    @FXML
    public void onAdminButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport - Admin Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Error loading admin login view", e);
            showError("Erreur", "Impossible de charger la page de connexion administrateur.");
        }
    }

    @FXML
    public void onOrgButtonClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organisation/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport - Organisation Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("Error loading organisation login view", e);
            showError("Erreur", "Impossible de charger la page de connexion organisation.");
        }
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}