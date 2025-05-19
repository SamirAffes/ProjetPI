package controllers;

import entities.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import services.ReclamationService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminReclamationsController {
    @FXML private VBox reclamationsContainer;
    @FXML private VBox emptyPlaceholder;
    @FXML private ComboBox<Reclamation.ReclamationStatus> statusFilter;
    @FXML private Label pendingCountLabel;
    @FXML private Label inProgressCountLabel;
    @FXML private Label resolvedCountLabel;

    private final ReclamationService reclamationService = new ReclamationService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupStatusFilter();
        loadReclamations();
        updateStatistics();
    }

    private void setupStatusFilter() {
        statusFilter.getItems().addAll(Reclamation.ReclamationStatus.values());
        statusFilter.setOnAction(e -> loadReclamations());
    }

    private void loadReclamations() {
        reclamationsContainer.getChildren().clear();
        List<Reclamation> reclamations = reclamationService.getAllReclamations();

        Reclamation.ReclamationStatus selectedStatus = statusFilter.getValue();
        int displayedCount = 0;

        for (Reclamation reclamation : reclamations) {
            if (selectedStatus == null || reclamation.getStatus() == selectedStatus) {
                reclamationsContainer.getChildren().add(createReclamationCard(reclamation));
                displayedCount++;
            }
        }

        // Show/hide placeholder based on whether there are reclamations to display
        boolean isEmpty = displayedCount == 0;
        emptyPlaceholder.setVisible(isEmpty);
        emptyPlaceholder.setManaged(isEmpty);

        // Update statistics after loading reclamations
        updateStatistics();
    }

    private void updateStatistics() {
        List<Reclamation> allReclamations = reclamationService.getAllReclamations();

        // Count reclamations by status
        long pendingCount = allReclamations.stream()
                .filter(r -> r.getStatus() == Reclamation.ReclamationStatus.PENDING)
                .count();

        long inProgressCount = allReclamations.stream()
                .filter(r -> r.getStatus() == Reclamation.ReclamationStatus.IN_PROGRESS)
                .count();

        long resolvedCount = allReclamations.stream()
                .filter(r -> r.getStatus() == Reclamation.ReclamationStatus.RESOLVED)
                .count();

        // Update labels
        pendingCountLabel.setText(String.valueOf(pendingCount));
        inProgressCountLabel.setText(String.valueOf(inProgressCount));
        resolvedCountLabel.setText(String.valueOf(resolvedCount));
    }

    private VBox createReclamationCard(Reclamation reclamation) {
        VBox card = new VBox(15);
        card.getStyleClass().add("reclamation-card");

        // Header with title and status badge
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(reclamation.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        // Status badge
        Label statusBadge = new Label(reclamation.getStatus().toString().replace("_", " "));
        statusBadge.getStyleClass().addAll("status-label", "status-" + reclamation.getStatus().toString().toLowerCase());

        header.getChildren().addAll(titleLabel, statusBadge);

        // User info and date in a separate line
        HBox metaInfo = new HBox(15);
        metaInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label userLabel = new Label("From: " + reclamation.getUser().getFullName());
        userLabel.getStyleClass().add("user-label");

        Label dateLabel = new Label("Created on: " + reclamation.getCreationDate().format(DATE_FORMATTER));
        dateLabel.getStyleClass().add("date-label");

        metaInfo.getChildren().addAll(userLabel, dateLabel);

        // Description with a border
        VBox descriptionBox = new VBox(5);
        descriptionBox.setStyle("-fx-border-color: #eee; -fx-border-radius: 5; -fx-padding: 10;");

        Label descriptionHeader = new Label("Description:");
        descriptionHeader.setStyle("-fx-font-weight: bold;");

        Label descriptionLabel = new Label(reclamation.getDescription());
        descriptionLabel.setWrapText(true);

        descriptionBox.getChildren().addAll(descriptionHeader, descriptionLabel);

        // Response section
        VBox responseSection = new VBox(10);
        responseSection.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

        Label responseHeader = new Label("Admin Response:");
        responseHeader.setStyle("-fx-font-weight: bold;");

        // Status selection with label in a HBox
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusLabel = new Label("Update Status:");
        statusLabel.setPrefWidth(100);

        ComboBox<Reclamation.ReclamationStatus> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(Reclamation.ReclamationStatus.values());
        statusComboBox.setValue(reclamation.getStatus());
        statusComboBox.setPrefWidth(200);

        statusBox.getChildren().addAll(statusLabel, statusComboBox);

        // Response area
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("Write your response here...");
        responseArea.setText(reclamation.getResponse());
        responseArea.setWrapText(true);
        responseArea.setPrefRowCount(4);

        // Buttons in a HBox aligned to the right
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> loadReclamations());

        Button updateButton = new Button("Update & Send Email");
        updateButton.getStyleClass().add("button-primary");
        updateButton.setOnAction(e -> {
            reclamation.setStatus(statusComboBox.getValue());
            reclamation.setResponse(responseArea.getText().trim());

            try {
                reclamationService.updateReclamation(reclamation);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Complaint updated successfully and email sent to user");
                loadReclamations();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update complaint: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(cancelButton, updateButton);

        responseSection.getChildren().addAll(responseHeader, statusBox, responseArea, buttonBox);

        card.getChildren().addAll(
            header,
            metaInfo,
            descriptionBox,
            responseSection
        );

        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
