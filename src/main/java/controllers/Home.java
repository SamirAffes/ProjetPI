package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class Home {
    
    @FXML
    private Button adminButton;
    
    @FXML
    private Button userButton;
    
    @FXML
    void handleAdminButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminView.fxml"));
            Parent adminView = loader.load();
            Scene scene = new Scene(adminView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Espace Admin - Gestion des Réclamations");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface admin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    void handleUserButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserView.fxml"));
            Parent userView = loader.load();
            Scene scene = new Scene(userView);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Espace Utilisateur - Gestion des Réclamations");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'interface utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
