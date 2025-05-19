package controllers;

import entities.Organisation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import services.OrganisationService;
import utils.OrganisationContext;

import java.io.File;
import java.io.IOException;

@Slf4j
public class OrganisationLoginController {

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

    @FXML
    private ImageView orgLogoView;

    @FXML
    private VBox logoContainer;

    private final OrganisationService organisationService = new OrganisationService();
    private Organisation currentOrganisation = null;

    @FXML
    public void onUsernameEntered() {
        String orgName = usernameField.getText().trim();

        if (!orgName.isEmpty()) {
            // Search for an organization with this name
            Organisation foundOrg = findOrganisationByName(orgName);

            if (foundOrg != null) {
                // Store the found organization
                this.currentOrganisation = foundOrg;

                // Display the organization's logo
                displayOrganisationLogo(foundOrg);

                // Move focus to password field
                passwordField.requestFocus();
            } else {
                // Hide any previously shown logo
                orgLogoView.setImage(null);
                this.currentOrganisation = null;
            }
        } else {
            // Hide logo when username field is empty
            orgLogoView.setImage(null);
            this.currentOrganisation = null;
        }
    }

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        String orgName = usernameField.getText().trim();
        String password = passwordField.getText();

        if (orgName.isEmpty()) {
            showError("Veuillez saisir le nom de l'organisation.");
            return;
        }

        if (password.isEmpty()) {
            showError("Veuillez saisir le mot de passe.");
            return;
        }

        // If we don't have the organization yet from the onUsernameEntered method
        if (currentOrganisation == null) {
            currentOrganisation = findOrganisationByName(orgName);
        }

        if (currentOrganisation == null) {
            showContactAdminAlert();
            return;
        }

        // Verify the password against the stored password
        if (currentOrganisation.getPassword() != null && BCrypt.checkpw(password, currentOrganisation.getPassword())) {
            try {
                // Stocker l'organisation dans le contexte global
                OrganisationContext.getInstance().setCurrentOrganisation(currentOrganisation);
                log.info("Organisation définie dans le contexte global: {}", currentOrganisation.getNom());

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organisation/dashboard.fxml"));
                Parent root = loader.load();

                // Pass the organization to the dashboard controller
                OrganisationDashboardController controller = loader.getController();
                controller.setOrganisation(currentOrganisation);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setTitle("TuniTransport - " + currentOrganisation.getNom());
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
                stage.show();

                log.info("Organisation logged in: {}", currentOrganisation.getNom());
            } catch (IOException e) {
                log.error("Error loading organisation dashboard", e);
                showError("Impossible de charger le tableau de bord.");
            }
        } else {
            log.warn("Failed login attempt for organisation: {}", orgName);
            showError("Mot de passe incorrect.");
        }
    }

    @FXML
    public void onBackButtonClick(ActionEvent event) {
        try {
            // Effacer l'organisation du contexte global lors du retour à l'accueil
            OrganisationContext.getInstance().clearCurrentOrganisation();

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

    private Organisation findOrganisationByName(String name) {
        for (Organisation org : organisationService.afficher_tout()) {
            if (org.getNom().equalsIgnoreCase(name)) {
                return org;
            }
        }
        return null;
    }

    private void displayOrganisationLogo(Organisation organisation) {
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

    private void showContactAdminAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Organisation non trouvée");
        alert.setHeaderText("Organisation non trouvée");
        alert.setContentText("Aucune organisation trouvée avec ce nom. Veuillez contacter l'administrateur à contact@tunitransport.com pour vous inscrire à la plateforme.");
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
