package controllers;

import entities.Organisation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationService;
import utils.OrganisationContext;

import java.io.IOException;

@Slf4j
public class OrganisationLoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button returnButton;

    private final OrganisationService organisationService = new OrganisationService();

    @FXML
    public void initialize() {
        // Initialisation des éléments si nécessaire
        loginButton.setOnAction(e -> handleLogin());
        returnButton.setOnAction(e -> handleReturn());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'authentification", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Vérifier l'authentification
            Organisation organisation = organisationService.authenticate(email, password);
            
            if (organisation != null) {
                log.info("Organisation connectée avec succès: {}", organisation.getNom());
                
                // Stocker l'organisation dans le contexte global
                OrganisationContext.getInstance().setCurrentOrganisation(organisation);
                log.info("Organisation stockée dans le contexte global: {}", organisation.getNom());
                
                // Charger le dashboard de l'organisation
                loadOrganisationDashboard(organisation);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur d'authentification", "Email ou mot de passe incorrect.");
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'authentification de l'organisation", e);
            showAlert(Alert.AlertType.ERROR, "Erreur d'authentification", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void loadOrganisationDashboard(Organisation organisation) {
        try {
            // Chargez le fichier FXML du dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrganisationDashboard.fxml"));
            Parent root = loader.load();

            // Accédez au contrôleur du dashboard et passez l'organisation
            OrganisationDashboardController controller = loader.getController();
            controller.setOrganisation(organisation);

            // Affichez la nouvelle scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Dashboard - " + organisation.getNom());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            log.error("Erreur lors du chargement du dashboard organisation", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le dashboard: " + e.getMessage());
        }
    }

    private void handleReturn() {
        try {
            // Retour à la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Home.fxml"));
            BorderPane root = loader.load();
            
            Stage stage = (Stage) returnButton.getScene().getWindow();
            stage.setTitle("Accueil");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            log.error("Erreur lors du retour à l'accueil", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'accueil: " + e.getMessage());
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