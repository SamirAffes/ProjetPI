package controllers;

import entities.Organisation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import utils.OrganisationContext;

import java.io.IOException;

@Slf4j
public class OrganisationDashboardController {

    @FXML
    private Label organisationNameLabel;

    @FXML
    private BorderPane contentPane;

    @FXML
    private Button vehiculesBtn;

    @FXML
    private Button conducteursBtn;

    @FXML
    private Button maintenanceBtn;

    @FXML
    private Button logoutBtn;

    private Organisation organisation;

    @FXML
    public void initialize() {
        // Initialiser les boutons de navigation
        vehiculesBtn.setOnAction(e -> loadVehiculesManagement());
        conducteursBtn.setOnAction(e -> loadConducteursManagement());
        maintenanceBtn.setOnAction(e -> loadMaintenanceManagement());
        logoutBtn.setOnAction(e -> handleLogout());
        
        // Essayer de charger l'organisation depuis le contexte global
        if (OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            updateUI();
            log.info("Organisation chargée depuis le contexte global dans OrganisationDashboardController");
        }
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        updateUI();
    }

    private void updateUI() {
        if (organisation != null) {
            organisationNameLabel.setText(organisation.getNom());
        } else {
            organisationNameLabel.setText("Erreur: Organisation non identifiée");
            log.error("Tentative d'accéder au Dashboard sans organisation");
        }
    }

    private void loadVehiculesManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VehiculeManagement.fxml"));
            Parent vehiculesView = loader.load();
            
            VehiculeManagementController controller = loader.getController();
            controller.setOrganisation(organisation);
            
            contentPane.setCenter(vehiculesView);
        } catch (IOException e) {
            log.error("Erreur lors du chargement de la gestion des véhicules", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la gestion des véhicules");
        }
    }

    private void loadConducteursManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConducteurManagement.fxml"));
            Parent conducteursView = loader.load();
            
            ConducteurManagementController controller = loader.getController();
            controller.setOrganisation(organisation);
            
            contentPane.setCenter(conducteursView);
        } catch (IOException e) {
            log.error("Erreur lors du chargement de la gestion des conducteurs", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la gestion des conducteurs");
        }
    }

    private void loadMaintenanceManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MaintenanceManagement.fxml"));
            Parent maintenanceView = loader.load();
            
            MaintenanceManagementController controller = loader.getController();
            // Assurez-vous que la classe MaintenanceManagementController a une méthode setOrganisation
            if (organisation != null) {
                controller.setOrganisation(organisation);
            }
            
            contentPane.setCenter(maintenanceView);
        } catch (IOException e) {
            log.error("Erreur lors du chargement de la gestion de la maintenance", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la gestion de maintenance");
        }
    }

    private void handleLogout() {
        try {
            // Effacer l'organisation du contexte avant la déconnexion
            OrganisationContext.getInstance().clearCurrentOrganisation();
            log.info("Déconnexion de l'organisation: {}", organisation.getNom());
            
            // Retour à la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("Accueil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            log.error("Erreur lors du retour à l'accueil", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}