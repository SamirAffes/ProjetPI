package controllers;

import entities.Reclamation;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import services.ReclamationService;
import utils.UserContext;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserReclamationsController {
    @FXML private VBox allReclamationsContainer;
    @FXML private VBox pendingReclamationsContainer;
    @FXML private VBox inProgressReclamationsContainer;
    @FXML private VBox resolvedReclamationsContainer;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label pendingReclamationsLabel;
    @FXML private Label resolvedReclamationsLabel;
    @FXML private ComboBox<String> statusFilter;

    private final ReclamationService reclamationService = new ReclamationService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        loadReclamations();
    }

    @FXML
    private void handleNewReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("New Complaint");
            stage.setScene(new Scene(root));

            stage.showAndWait();
            loadReclamations(); // Refresh the list after creating new complaint
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReclamations() {
        // Clear all containers
        if (allReclamationsContainer != null) allReclamationsContainer.getChildren().clear();
        if (pendingReclamationsContainer != null) pendingReclamationsContainer.getChildren().clear();
        if (inProgressReclamationsContainer != null) inProgressReclamationsContainer.getChildren().clear();
        if (resolvedReclamationsContainer != null) resolvedReclamationsContainer.getChildren().clear();

        User currentUser = UserContext.getInstance().getCurrentUser();
        List<Reclamation> reclamations = reclamationService.getUserReclamations(currentUser);

        int totalCount = 0;
        int pendingCount = 0;
        int resolvedCount = 0;

        for (Reclamation reclamation : reclamations) {
            VBox card = createReclamationCard(reclamation);
            totalCount++;

            // Add to all reclamations container
            if (allReclamationsContainer != null) {
                allReclamationsContainer.getChildren().add(card);
            }

            // Add to specific container based on status
            switch (reclamation.getStatus()) {
                case PENDING:
                    pendingCount++;
                    if (pendingReclamationsContainer != null) {
                        pendingReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
                case IN_PROGRESS:
                    if (inProgressReclamationsContainer != null) {
                        inProgressReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
                case RESOLVED:
                    resolvedCount++;
                    if (resolvedReclamationsContainer != null) {
                        resolvedReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
            }
        }

        // Update statistics labels
        if (totalReclamationsLabel != null) totalReclamationsLabel.setText(String.valueOf(totalCount));
        if (pendingReclamationsLabel != null) pendingReclamationsLabel.setText(String.valueOf(pendingCount));
        if (resolvedReclamationsLabel != null) resolvedReclamationsLabel.setText(String.valueOf(resolvedCount));
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reclamation-card");

        HBox header = new HBox(10);
        Label titleLabel = new Label(reclamation.getTitle());
        titleLabel.getStyleClass().add("card-title");
        Label statusLabel = new Label(reclamation.getStatus().toString());
        statusLabel.getStyleClass().addAll("status-label", "status-" + reclamation.getStatus().toString().toLowerCase());

        header.getChildren().addAll(titleLabel, statusLabel);

        Label descriptionLabel = new Label(reclamation.getDescription());
        descriptionLabel.setWrapText(true);

        Label dateLabel = new Label("Created on: " + reclamation.getCreationDate().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("date-label");

        VBox responseBox = new VBox(5);
        if (reclamation.getResponse() != null) {
            Label responseLabel = new Label("Response: " + reclamation.getResponse());
            responseLabel.setWrapText(true);
            responseLabel.getStyleClass().add("response-label");

            Label responseDateLabel = new Label("Responded on: " + 
                reclamation.getResponseDate().format(DATE_FORMATTER));
            responseDateLabel.getStyleClass().add("date-label");

            responseBox.getChildren().addAll(responseLabel, responseDateLabel);
        }

        card.getChildren().addAll(header, descriptionLabel, dateLabel);
        if (!responseBox.getChildren().isEmpty()) {
            card.getChildren().add(responseBox);
        }

        return card;
    }
}
