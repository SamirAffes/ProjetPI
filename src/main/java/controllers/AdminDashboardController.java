package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AdminDashboardController {
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button organisationsButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox dashboardView;
    
    @FXML
    private VBox organisationsView;
    
    @FXML
    public void initialize() {
        // Set default view
        showDashboard();
    }
    
    @FXML
    public void showDashboard() {
        dashboardView.setVisible(true);
        organisationsView.setVisible(false);
        setActiveButton(dashboardButton);
    }
    
    @FXML
    public void showOrganisations() {
        dashboardView.setVisible(false);
        organisationsView.setVisible(true);
        setActiveButton(organisationsButton);
    }
    
    @FXML
    public void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport");
            stage.setScene(scene);
            stage.show();
            stage.setFullScreen(true);
            stage.setMaximized(true);
            // dont show the fullscreen hint
            stage.setFullScreenExitHint("");
            log.info("Admin logged out");
        } catch (IOException e) {
            log.error("Error returning to home view", e);
        }
    }
    
    private void setActiveButton(Button button) {
        // Reset styles
        dashboardButton.getStyleClass().remove("active");
        organisationsButton.getStyleClass().remove("active");
        
        // Set active style
        button.getStyleClass().add("active");
    }
}