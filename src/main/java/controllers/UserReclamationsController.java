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
    @FXML private VBox allEmptyPlaceholder;
    @FXML private VBox pendingEmptyPlaceholder;
    @FXML private VBox inProgressEmptyPlaceholder;
    @FXML private VBox resolvedEmptyPlaceholder;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label pendingReclamationsLabel;
    @FXML private Label resolvedReclamationsLabel;
    @FXML private ComboBox<String> statusFilter;

    private final ReclamationService reclamationService = new ReclamationService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        // Initialize status filter dropdown
        statusFilter.setEditable(false); // Make ComboBox non-editable to prevent deleting options
        statusFilter.getItems().add("Tous");
        for (Reclamation.ReclamationStatus status : Reclamation.ReclamationStatus.values()) {
            statusFilter.getItems().add(status.toString());
        }
        statusFilter.setValue("Tous");

        // Add listener for status filter changes
        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadReclamations();
            }
        });

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

        // Apply filter if a specific status is selected
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tous")) {
            reclamations = reclamations.stream()
                .filter(r -> r.getStatus().toString().equals(selectedStatus))
                .collect(java.util.stream.Collectors.toList());
        }

        int totalCount = 0;
        int pendingCount = 0;
        int inProgressCount = 0;
        int resolvedCount = 0;

        for (Reclamation reclamation : reclamations) {
            // Only create the card once and reuse it
            VBox card = createReclamationCard(reclamation);

            // Count by status regardless of filter
            switch (reclamation.getStatus()) {
                case PENDING:
                    pendingCount++;
                    break;
                case IN_PROGRESS:
                    inProgressCount++;
                    break;
                case RESOLVED:
                    resolvedCount++;
                    break;
            }

            totalCount++;

            // Add to all reclamations container if no filter or if it matches the filter
            if (allReclamationsContainer != null) {
                allReclamationsContainer.getChildren().add(card);
            }

            // Add to specific container based on status
            switch (reclamation.getStatus()) {
                case PENDING:
                    if (pendingReclamationsContainer != null) {
                        // Clone the card for each container to avoid parent issues
                        pendingReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
                case IN_PROGRESS:
                    if (inProgressReclamationsContainer != null) {
                        // Clone the card for each container to avoid parent issues
                        inProgressReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
                case RESOLVED:
                    if (resolvedReclamationsContainer != null) {
                        // Clone the card for each container to avoid parent issues
                        resolvedReclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                    }
                    break;
            }
        }

        // Show/hide empty placeholders based on whether there are reclamations
        // For the "All" tab, check if there are any reclamations after filtering
        if (allEmptyPlaceholder != null) {
            allEmptyPlaceholder.setVisible(allReclamationsContainer.getChildren().isEmpty());
        }

        // For specific status tabs, check if there are any reclamations with that status
        // These should be shown/hidden based on the actual count, not the filtered count
        if (pendingEmptyPlaceholder != null) {
            pendingEmptyPlaceholder.setVisible(pendingReclamationsContainer.getChildren().isEmpty());
        }
        if (inProgressEmptyPlaceholder != null) {
            inProgressEmptyPlaceholder.setVisible(inProgressReclamationsContainer.getChildren().isEmpty());
        }
        if (resolvedEmptyPlaceholder != null) {
            resolvedEmptyPlaceholder.setVisible(resolvedReclamationsContainer.getChildren().isEmpty());
        }

        // Update statistics labels
        if (totalReclamationsLabel != null) totalReclamationsLabel.setText(String.valueOf(totalCount));
        if (pendingReclamationsLabel != null) pendingReclamationsLabel.setText(String.valueOf(pendingCount));
        if (resolvedReclamationsLabel != null) resolvedReclamationsLabel.setText(String.valueOf(resolvedCount));
    }

    /**
     * Resets the status filter to "Tous" and reloads all reclamations
     */
    @FXML
    private void resetFilter() {
        statusFilter.setValue("Tous");
        loadReclamations();
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
