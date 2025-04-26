package controllers;

import entities.Organisation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class OrganisationDashboardController {
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button vehiculesButton;
    
    @FXML
    private Button conducteursButton;
    
    @FXML
    private Button maintenancesButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private VBox dashboardView;
    
    @FXML
    private VBox vehiculesView;
    
    @FXML
    private VBox conducteursView;
    
    @FXML
    private VBox maintenancesView;
    
    @FXML
    private Label organisationNameLabel;
    
    @FXML
    private ImageView orgLogoView;
    
    @FXML
    private Node vehiculeManagementContent;
    
    @FXML
    private Node conducteurManagementContent;
    
    @FXML
    private Node maintenanceManagementContent;
    
    private Organisation organisation;
    
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        
        // Set organization name in the UI
        organisationNameLabel.setText(organisation.getNom());
        
        // Load organization logo
        loadOrganisationLogo();
        
        // Pass the organization to the subcontrollers
        passOrganisationToSubcontrollers();
    }
    
    @FXML
    public void initialize() {
        // Set default view
        showDashboard();
    }
    
    @FXML
    public void showDashboard() {
        dashboardView.setVisible(true);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(false);
        setActiveButton(dashboardButton);
    }
    
    @FXML
    public void showVehicules() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(true);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(false);
        setActiveButton(vehiculesButton);
    }
    
    @FXML
    public void showConducteurs() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(true);
        maintenancesView.setVisible(false);
        setActiveButton(conducteursButton);
    }
    
    @FXML
    public void showMaintenances() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(true);
        setActiveButton(maintenancesButton);
    }
    
    @FXML
    public void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport");
            stage.setScene(scene);
            stage.show();
            
            log.info("Organisation logged out: {}", organisation.getNom());
        } catch (IOException e) {
            log.error("Error returning to home view", e);
        }
    }
    
    private void setActiveButton(Button button) {
        // Reset styles
        dashboardButton.getStyleClass().remove("active");
        vehiculesButton.getStyleClass().remove("active");
        conducteursButton.getStyleClass().remove("active");
        maintenancesButton.getStyleClass().remove("active");
        
        // Set active style
        button.getStyleClass().add("active");
    }
    
    private void loadOrganisationLogo() {
        if (organisation != null && organisation.getLogo() != null && !organisation.getLogo().isEmpty()) {
            try {
                File logoFile = new File(organisation.getLogo());
                if (logoFile.exists()) {
                    Image logoImage = new Image(logoFile.toURI().toString());
                    orgLogoView.setImage(logoImage);
                    return;
                }
            } catch (Exception e) {
                log.error("Error loading organisation logo", e);
            }
        }
        
        // Use default logo if organization has no logo or error occurred
        try {
            orgLogoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.png")));
        } catch (Exception e) {
            log.error("Error loading default logo", e);
        }
    }
    
    private void passOrganisationToSubcontrollers() {
        try {
            // La méthode standard pour obtenir le contrôleur d'un élément fx:include
            VehiculeManagementController vehiculeController = 
                (VehiculeManagementController) vehiculeManagementContent.getProperties().get("fx:controller");
            if (vehiculeController != null) {
                vehiculeController.setOrganisation(organisation);
                log.info("Organisation passée à VehiculeManagementController");
            } else {
                log.warn("VehiculeManagementController est null - impossible de passer l'organisation");
            }
            
            ConducteurManagementController conducteurController = 
                (ConducteurManagementController) conducteurManagementContent.getProperties().get("fx:controller");
            if (conducteurController != null) {
                conducteurController.setOrganisation(organisation);
                log.info("Organisation passée à ConducteurManagementController");
            } else {
                log.warn("ConducteurManagementController est null - impossible de passer l'organisation");
            }
            
            MaintenanceManagementController maintenanceController = 
                (MaintenanceManagementController) maintenanceManagementContent.getProperties().get("fx:controller");
            if (maintenanceController != null) {
                maintenanceController.setOrganisation(organisation);
                log.info("Organisation passée à MaintenanceManagementController");
            } else {
                log.warn("MaintenanceManagementController est null - impossible de passer l'organisation");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la transmission de l'organisation aux sous-contrôleurs", e);
        }
    }
}