package controllers;

import entities.Organisation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import utils.RouteDataPopulator;
import utils.OrganisationContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
    private Button routesButton;
    
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
    private VBox routesView;
    
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
    
    @FXML
    private Node routeManagementContent;
    
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
        routesView.setVisible(false);
        setActiveButton(dashboardButton);
    }
    
    @FXML
    public void showVehicules() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(true);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(false);
        routesView.setVisible(false);
        setActiveButton(vehiculesButton);
    }
    
    @FXML
    public void showConducteurs() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(true);
        maintenancesView.setVisible(false);
        routesView.setVisible(false);
        setActiveButton(conducteursButton);
    }
    
    @FXML
    public void showMaintenances() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(true);
        routesView.setVisible(false);
        setActiveButton(maintenancesButton);
    }
    
    @FXML
    public void showRoutes() {
        dashboardView.setVisible(false);
        vehiculesView.setVisible(false);
        conducteursView.setVisible(false);
        maintenancesView.setVisible(false);
        routesView.setVisible(true);
        setActiveButton(routesButton);
        
        // Ensure the organisation is passed to the route controller when tab is shown
        try {
            RouteManagementController routeController = 
                (RouteManagementController) routeManagementContent.getProperties().get("fx:controller");
            
            // If we don't have a local organisation but there's one in the context, use that
            Organisation orgToUse = organisation;
            if (orgToUse == null) {
                orgToUse = OrganisationContext.getInstance().getCurrentOrganisation();
                if (orgToUse != null) {
                    log.info("Using organisation from OrganisationContext: {}", orgToUse.getNom());
                    // Update the local reference too
                    this.organisation = orgToUse;
                }
            }
            
            if (routeController != null && orgToUse != null) {
                // Check if we need to set the organisation again
                routeController.setOrganisation(orgToUse);
                // Refresh data in case it's stale
                routeController.refreshData();
                log.info("Organisation passed to RouteManagementController when showing routes tab");
            } else if (routeController == null) {
                log.warn("RouteManagementController is null when showing routes tab");
                
                // Try to reinitialize the subcontrollers
                passOrganisationToSubcontrollers();
            } else if (orgToUse == null) {
                log.warn("Organisation is null when showing routes tab");
            }
        } catch (Exception e) {
            log.error("Error setting organisation in RouteManagementController when showing routes tab", e);
        }
    }
    
    @FXML
    public void logout(ActionEvent event) {
        try {
            // Clear any organization context data
            OrganisationContext.getInstance().setCurrentOrganisation(null);
            
            // Load the home screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport");
            stage.setScene(scene);
            stage.show();
            
            log.info("Organisation logged out: {}", organisation.getNom());
        } catch (IOException e) {
            log.error("Error returning to home view", e);
            
            // Show error alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la déconnexion: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Populate sample routes for testing purposes.
     * This can be called from a button in the UI.
     */
    @FXML
    public void populateRoutes() {
        try {
            RouteDataPopulator.populateRoutes();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Routes Populated");
            alert.setHeaderText(null);
            alert.setContentText("Sample routes have been added to the database.");
            alert.showAndWait();
            
            // Refresh route management view if visible
            if (routesView.isVisible()) {
                RouteManagementController routeController = 
                    (RouteManagementController) routeManagementContent.getProperties().get("fx:controller");
                if (routeController != null) {
                    routeController.refreshData();
                }
            }
            
            log.info("Routes populated successfully");
        } catch (Exception e) {
            log.error("Error populating routes", e);
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while populating routes: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void setActiveButton(Button button) {
        // Reset styles
        dashboardButton.getStyleClass().remove("active");
        vehiculesButton.getStyleClass().remove("active");
        conducteursButton.getStyleClass().remove("active");
        maintenancesButton.getStyleClass().remove("active");
        routesButton.getStyleClass().remove("active");
        
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
        if (organisation == null) {
            log.warn("Cannot pass null organisation to subcontrollers");
            return;
        }
        
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
            
            // Make sure we pass the organisation to the RouteManagementController
            if (routeManagementContent != null) {
                RouteManagementController routeController = 
                    (RouteManagementController) routeManagementContent.getProperties().get("fx:controller");
                
                if (routeController != null) {
                    routeController.setOrganisation(organisation);
                    log.info("Organisation passée à RouteManagementController");
                } else {
                    log.warn("RouteManagementController est null - tentative alternative d'initialisation");
                    
                    // Alternative initialization method - load the controller directly
                    try {
                        // Get the FXML file URL
                        URL fxmlUrl = getClass().getResource("/fxml/organisation/routeManagement.fxml");
                        if (fxmlUrl != null) {
                            FXMLLoader loader = new FXMLLoader(fxmlUrl);
                            Parent root = loader.load(); // This will load but not add to the scene
                            
                            // Get the controller and set the organisation
                            RouteManagementController altController = loader.getController();
                            if (altController != null) {
                                altController.setOrganisation(organisation);
                                
                                // Store the controller in the routeManagementContent properties
                                routeManagementContent.getProperties().put("fx:controller", altController);
                                
                                log.info("RouteManagementController initialisé avec succès par méthode alternative");
                            } else {
                                log.error("Impossible d'obtenir le contrôleur alternativement");
                            }
                        } else {
                            log.error("Impossible de trouver le fichier FXML pour routeManagement");
                        }
                    } catch (Exception ex) {
                        log.error("Erreur lors de l'initialisation alternative du RouteManagementController", ex);
                    }
                }
            } else {
                log.warn("routeManagementContent est null - impossible d'accéder au RouteManagementController");
            }
        } catch (Exception e) {
            log.error("Erreur lors de la transmission de l'organisation aux sous-contrôleurs", e);
        }
    }
}