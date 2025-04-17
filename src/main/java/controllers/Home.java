package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.util.logging.Logger;
import java.util.logging.Level;
import utils.AlertUtils;
import test.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Home implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(Home.class.getName());
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Home controller initialized");
        
        // Ensure the window is maximized when initialized
        Platform.runLater(this::ensureWindowMaximized);
    }
    
    /**
     * Ensures the window is maximized
     */
    private void ensureWindowMaximized() {
        try {
            // Get the primary stage from the static reference
            Stage stage = Main.getPrimaryStage();
            if (stage != null) {
                LOGGER.info("Ensuring window maximization using primary stage reference");
                
                // Force the stage to be maximized
                stage.setMaximized(true);
                
                // Log window dimensions for debugging
                LOGGER.info("Window dimensions after maximizing: " + 
                           stage.getWidth() + "x" + stage.getHeight());
                
                // Add a listener to ensure window stays maximized
                stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal) {
                        LOGGER.info("Window maximized state changed to false, restoring maximized state");
                        Platform.runLater(() -> stage.setMaximized(true));
                    }
                });
            } else {
                LOGGER.warning("Primary stage reference is null, cannot maximize window");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error ensuring window maximization", e);
        }
    }
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        Button button = (Button) event.getSource();
        String text = button.getText();
        
        LOGGER.info("Button clicked: " + text);
        
        try {
            // Handle both English and French button text
            switch (text) {
                case "Trajets":
                case "Routes":
                    openView("RouteView.fxml", "Routes Management");
                    break;
                case "Transports":
                    openView("TransportView.fxml", "Transport Management");
                    break;
                case "Réservations":
                case "Reservations":
                    openView("ReservationView.fxml", "Reservations Management");
                    break;
                case "Utilisateurs":
                case "Users":
                    openView("UserView.fxml", "User Management");
                    break;
                case "Nouvelle Réservation":
                    openView("NewReservationView.fxml", "New Reservation");
                    break;
                case "Ajouter un Transport":
                    openView("TransportView.fxml", "Transport Management");
                    break;
                case "Créer un Trajet":
                    openView("RouteView.fxml", "Route Management");
                    break;
                case "Rapports":
                    AlertUtils.showInformation("Reports", "Reports Feature", 
                        "This feature is coming soon in a future update.");
                    break;
                case "Accueil":
                case "Home":
                default:
                    // Stay on home screen
                    break;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening view: " + e.getMessage(), e);
            AlertUtils.showError("Error", "Cannot Open View", 
                "Error opening the requested view: " + e.getMessage());
        }
    }
    
    private void openView(String fxmlFile, String title) throws IOException {
        try {
            LOGGER.info("Opening view: " + fxmlFile);
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlFile));
            Parent root = loader.load();
            
            // Get the primary stage from the static reference
            Stage stage = Main.getPrimaryStage();
            if (stage == null) {
                LOGGER.warning("Primary stage reference is null, cannot proceed with view transition");
                return;
            }
            
            // Create scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setTitle(title);
            stage.setScene(scene);
            
            // Ensure the window is maximized
            Platform.runLater(() -> {
                stage.setMaximized(true);
                
                // Log window state for debugging
                LOGGER.info("View transition complete. Window state - Maximized: " + 
                           stage.isMaximized() + ", Dimensions: " + 
                           stage.getWidth() + "x" + stage.getHeight());
            });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not load view: " + fxmlFile, e);
            throw e;
        }
    }
} 