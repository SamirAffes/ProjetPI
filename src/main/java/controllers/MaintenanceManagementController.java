package controllers;

import entities.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.MaintenanceService;
import services.PdfGenerationService;
import services.VehiculeService;
import utils.OrganisationContext;
import java.awt.Desktop;
import java.io.File;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
public class MaintenanceManagementController {

    @FXML
    private FlowPane maintenancesContainer;

    @FXML
    private Button addButton;

    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final PdfGenerationService pdfGenerationService = new PdfGenerationService();

    private Organisation organisation;

    @FXML
    public void initialize() {
        // Try to get organization from global context
        if (OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            loadMaintenances();
            log.info("Organisation chargée depuis le contexte global dans MaintenanceManagementController");
        }
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        // Load maintenances for this organization's vehicles
        loadMaintenances();
    }

    @FXML
    public void onAddButtonClicked() {
        // Check for organisation in global context if not already set
        if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
        }
        showMaintenanceForm(null);
    }

    private void loadMaintenances() {
        maintenancesContainer.getChildren().clear();

        if (organisation != null) {
            // Get all maintenances and filter by organization's vehicles
            List<Maintenance> allMaintenances = maintenanceService.afficher_tout();
            List<Vehicule> organisationVehicles = getOrganisationVehicles();

            boolean found = false;

            for (Maintenance maintenance : allMaintenances) {
                // Check if this maintenance belongs to one of the organization's vehicles
                for (Vehicule vehicule : organisationVehicles) {
                    if (maintenance.getVehiculeId() == vehicule.getId()) {
                        maintenancesContainer.getChildren().add(createMaintenanceCard(maintenance, vehicule));
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                Label emptyLabel = new Label("Aucune maintenance trouvée");
                emptyLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                maintenancesContainer.getChildren().add(emptyLabel);
            }
        } else {
            Label errorLabel = new Label("Erreur: Organisation non définie");
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            maintenancesContainer.getChildren().add(errorLabel);
        }
    }

    private List<Vehicule> getOrganisationVehicles() {
        List<Vehicule> organisationVehicles = new ArrayList<>();
        List<Vehicule> allVehicules = vehiculeService.afficher_tout();

        for (Vehicule vehicule : allVehicules) {
            if (vehicule.getOrganisationId() == organisation.getId()) {
                organisationVehicles.add(vehicule);
            }
        }

        return organisationVehicles;
    }

    private VBox createMaintenanceCard(Maintenance maintenance, Vehicule vehicule) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(300);
        card.setMinWidth(300);
        card.setPrefWidth(300);
        card.setPrefHeight(220);

        // Create a layout with info
        VBox infoContainer = new VBox(8);
        infoContainer.setAlignment(Pos.TOP_LEFT);

        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

        // Format dates
        String dateDebutStr = "N/A";
        String dateFinStr = "N/A";

        if (maintenance.getDateDebut() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateDebutStr = sdf.format(maintenance.getDateDebut());
        }

        if (maintenance.getDateFin() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateFinStr = sdf.format(maintenance.getDateFin());
        }

        // Status label with color
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label(maintenance.getStatus().toString());
        statusLabel.setPadding(new Insets(2, 8, 2, 8));
        statusLabel.setTextFill(Color.WHITE);

        // Set background color based on status
        String statusStyle = "";
        switch (maintenance.getStatus()) {
            case EN_ATTENTE:
                statusStyle = "-fx-background-color: #FF9800;";
                break;
            case EN_COURS:
                statusStyle = "-fx-background-color: #2196F3;";
                break;
            case TERMINE:
                statusStyle = "-fx-background-color: #4CAF50;";
                break;
            case ANNULE:
                statusStyle = "-fx-background-color: #F44336;";
                break;
        }
        statusLabel.setStyle(statusStyle);
        statusBox.getChildren().add(statusLabel);

        // Vehicle info
        Label vehiculeLabel = new Label(vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")");
        vehiculeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        vehiculeLabel.setWrapText(true);

        // Maintenance type
        Label typeLabel = new Label("Type: " + maintenance.getTypeMaintenance().toString());

        // Dates
        Label dateLabel = new Label("Début: " + dateDebutStr + " → Fin: " + dateFinStr);

        // Cost
        Label costLabel = new Label("Coût: " + currencyFormat.format(maintenance.getPrix()));
        costLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Description (limited to 2 lines)
        Label descLabel = new Label("Description: " + 
            (maintenance.getDescription() != null ? 
                (maintenance.getDescription().length() > 50 ? 
                    maintenance.getDescription().substring(0, 50) + "..." : 
                    maintenance.getDescription()) : 
                "N/A"));

        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);

        infoContainer.getChildren().addAll(vehiculeLabel, statusBox, typeLabel, dateLabel, descLabel, costLabel);

        // Add some spacing for better appearance
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // Detail button
        Button detailBtn = new Button("Détails");
        detailBtn.getStyleClass().add("detail-button");
        detailBtn.setOnAction(e -> showMaintenanceDetails(maintenance, vehicule));

        // Edit button
        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> showMaintenanceForm(maintenance));

        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> deleteMaintenance(maintenance));

        buttonBox.getChildren().addAll(detailBtn, editBtn, deleteBtn);

        // Add all elements to the card
        card.getChildren().addAll(infoContainer, spacer, buttonBox);
        card.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        return card;
    }

    private void showMaintenanceDetails(Maintenance maintenance, Vehicule vehicule) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails de la maintenance");

            BorderPane detailsPane = new BorderPane();
            detailsPane.setPadding(new Insets(20));

            VBox detailsBox = new VBox(15);
            detailsBox.setAlignment(Pos.TOP_LEFT);

            // Format currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

            // Format dates
            String dateDebutStr = "N/A";
            String dateFinStr = "N/A";

            if (maintenance.getDateDebut() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateDebutStr = sdf.format(maintenance.getDateDebut());
            }

            if (maintenance.getDateFin() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateFinStr = sdf.format(maintenance.getDateFin());
            }

            // Status label with color
            HBox statusBox = new HBox(5);
            statusBox.setAlignment(Pos.CENTER_LEFT);

            Label statusLabel = new Label(maintenance.getStatus().toString());
            statusLabel.setPadding(new Insets(3, 10, 3, 10));
            statusLabel.setTextFill(Color.WHITE);
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            // Set background color based on status
            String statusStyle = "";
            switch (maintenance.getStatus()) {
                case EN_ATTENTE:
                    statusStyle = "-fx-background-color: #FF9800;";
                    break;
                case EN_COURS:
                    statusStyle = "-fx-background-color: #2196F3;";
                    break;
                case TERMINE:
                    statusStyle = "-fx-background-color: #4CAF50;";
                    break;
                case ANNULE:
                    statusStyle = "-fx-background-color: #F44336;";
                    break;
            }
            statusLabel.setStyle(statusStyle);

            // Title with vehicle info
            Label titleLabel = new Label("Maintenance pour " + 
                vehicule.getMarque() + " " + vehicule.getModele());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

            // Details
            Label immatLabel = new Label("Immatriculation du véhicule: " + vehicule.getImmatriculation());

            Label typeLabel = new Label("Type de maintenance: " + maintenance.getTypeMaintenance().toString());

            Label dateDebutLabel = new Label("Date de début: " + dateDebutStr);
            Label dateFinLabel = new Label("Date de fin: " + dateFinStr);

            Label coutLabel = new Label("Coût: " + currencyFormat.format(maintenance.getPrix()));
            coutLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

            // Description (full)
            Label descTitle = new Label("Description:");
            descTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

            TextArea descArea = new TextArea(maintenance.getDescription());
            descArea.setWrapText(true);
            descArea.setEditable(false);
            descArea.setPrefHeight(100);

            // Status display
            HBox statusTitleBox = new HBox(10);
            statusTitleBox.setAlignment(Pos.CENTER_LEFT);
            Label statusTitleLabel = new Label("Statut: ");
            statusTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            statusTitleBox.getChildren().addAll(statusTitleLabel, statusLabel);

            // Change status button
            Button changeStatusButton = new Button("Changer le statut");
            changeStatusButton.getStyleClass().add("button-primary");
            changeStatusButton.setOnAction(e -> showChangeStatusDialog(maintenance, detailStage));

            // Generate PDF button
            Button generatePdfButton = new Button("Générer PDF");
            generatePdfButton.getStyleClass().add("button-primary");
            generatePdfButton.setOnAction(e -> {
                try {
                    String pdfPath = pdfGenerationService.generateMaintenanceReport(maintenance, vehicule);
                    if (pdfPath != null) {
                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("PDF Généré");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Le rapport de maintenance a été généré avec succès: " + pdfPath);

                        // Add button to open the PDF
                        ButtonType openButton = new ButtonType("Ouvrir le PDF");
                        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                        successAlert.getButtonTypes().setAll(openButton, okButton);

                        Optional<ButtonType> result = successAlert.showAndWait();
                        if (result.isPresent() && result.get() == openButton) {
                            // Open the PDF file
                            try {
                                File pdfFile = new File(pdfPath);
                                if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                                    Desktop.getDesktop().open(pdfFile);
                                }
                            } catch (Exception ex) {
                                log.error("Error opening PDF file", ex);
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le fichier PDF.");
                            }
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de générer le rapport PDF.");
                    }
                } catch (Exception ex) {
                    log.error("Error generating PDF", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la génération du PDF.");
                }
            });

            // Close button
            Button closeButton = new Button("Fermer");
            closeButton.getStyleClass().add("button-secondary");
            closeButton.setPrefWidth(100);
            closeButton.setOnAction(e -> detailStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(generatePdfButton, changeStatusButton, closeButton);

            // Add all elements to the details box
            detailsBox.getChildren().addAll(
                titleLabel, 
                immatLabel,
                typeLabel,
                dateDebutLabel,
                dateFinLabel,
                statusTitleBox,
                coutLabel,
                descTitle,
                descArea,
                buttonBox
            );

            detailsPane.setCenter(detailsBox);

            detailsPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene scene = new Scene(detailsPane, 500, 550);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing maintenance details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails de la maintenance.");
        }
    }

    private void showChangeStatusDialog(Maintenance maintenance, Stage parentStage) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setTitle("Changer le statut");

            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));

            Label titleLabel = new Label("Sélectionner un nouveau statut");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

            // Create a combobox of statuses
            ComboBox<StatusMaintenance> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll(StatusMaintenance.values());
            statusCombo.setValue(maintenance.getStatus());

            // Action buttons
            Button updateButton = new Button("Mettre à jour");
            updateButton.getStyleClass().add("button-primary");
            updateButton.setOnAction(e -> {
                StatusMaintenance selectedStatus = statusCombo.getValue();
                if (selectedStatus != null) {
                    // Update the maintenance status
                    maintenance.setStatus(selectedStatus);

                    // If status is set to TERMINE, update the end date if not already set
                    if (selectedStatus == StatusMaintenance.TERMINE && maintenance.getDateFin() == null) {
                        maintenance.setDateFin(new Date());
                    }

                    maintenanceService.modifier(maintenance);

                    // Close dialog and refresh
                    dialogStage.close();
                    parentStage.close();

                    // Refresh the maintenance list
                    loadMaintenances();

                    // Find the vehicle and show details again
                    Vehicule vehicule = vehiculeService.afficher(maintenance.getVehiculeId());
                    if (vehicule != null) {
                        showMaintenanceDetails(maintenance, vehicule);
                    }
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> dialogStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, updateButton);

            dialogVbox.getChildren().addAll(titleLabel, statusCombo, buttonBox);
            dialogVbox.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene dialogScene = new Scene(dialogVbox, 350, 150);
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing status change dialog", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le statut.");
        }
    }

    private void showMaintenanceForm(Maintenance maintenanceToEdit) {
        try {
            if (organisation == null) {
                log.error("Organisation is null when trying to show maintenance form");
                showAlert(Alert.AlertType.ERROR, "Erreur", "Organisation non définie.");
                return;
            }

            // Ensure vehicles are loaded
            List<Vehicule> organisationVehicles = getOrganisationVehicles();
            if (organisationVehicles.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun véhicule disponible pour cette organisation.");
                return;
            }

            // Initialize form stage
            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.setTitle(maintenanceToEdit == null ? "Ajouter une maintenance" : "Modifier la maintenance");

            BorderPane mainPane = new BorderPane();
            mainPane.setPadding(new Insets(20));

            // Form layout
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(20));

            // Add form fields
            int row = 0;

            Label vehiculeLabel = new Label("Véhicule:");
            ComboBox<Vehicule> vehiculeCombo = new ComboBox<>();
            vehiculeCombo.getItems().addAll(organisationVehicles);

            // Custom cell factory to display vehicle info
            vehiculeCombo.setCellFactory(param -> new ListCell<Vehicule>() {
                @Override
                protected void updateItem(Vehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getMarque() + " " + item.getModele() + " (" + item.getImmatriculation() + ")");
                    }
                }
            });

            // Same for the button cell
            vehiculeCombo.setButtonCell(new ListCell<Vehicule>() {
                @Override
                protected void updateItem(Vehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getMarque() + " " + item.getModele() + " (" + item.getImmatriculation() + ")");
                    }
                }
            });

            Label typeLabel = new Label("Type:");
            ComboBox<TypeMaintenance> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(TypeMaintenance.values());

            Label statusLabel = new Label("Statut:");
            ComboBox<StatusMaintenance> statusCombo = new ComboBox<>();
            statusCombo.getItems().addAll(StatusMaintenance.values());

            Label dateDebutLabel = new Label("Date de début:");
            DatePicker dateDebutPicker = new DatePicker();

            Label dateFinLabel = new Label("Date de fin:");
            DatePicker dateFinPicker = new DatePicker();

            Label coutLabel = new Label("Coût:");
            TextField coutField = new TextField();
            coutField.setText("0.0");

            Label descLabel = new Label("Description:");
            TextArea descArea = new TextArea();
            descArea.setPrefRowCount(3);
            descArea.setWrapText(true);

            // Set default values for new maintenance
            if (maintenanceToEdit == null) {
                if (!vehiculeCombo.getItems().isEmpty()) {
                    vehiculeCombo.setValue(vehiculeCombo.getItems().get(0));
                }
                typeCombo.setValue(TypeMaintenance.PREVENTIFE);
                statusCombo.setValue(StatusMaintenance.EN_ATTENTE);
                dateDebutPicker.setValue(LocalDate.now());
            } else {
                // Set values for existing maintenance
                for (Vehicule v : vehiculeCombo.getItems()) {
                    if (v.getId() == maintenanceToEdit.getVehiculeId()) {
                        vehiculeCombo.setValue(v);
                        break;
                    }
                }

                typeCombo.setValue(maintenanceToEdit.getTypeMaintenance());
                statusCombo.setValue(maintenanceToEdit.getStatus());

                if (maintenanceToEdit.getDateDebut() != null) {
                    dateDebutPicker.setValue(maintenanceToEdit.getDateDebut().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
                }

                if (maintenanceToEdit.getDateFin() != null) {
                    dateFinPicker.setValue(maintenanceToEdit.getDateFin().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
                }

                coutField.setText(Double.toString(maintenanceToEdit.getPrix()));
                descArea.setText(maintenanceToEdit.getDescription());
            }

            // Add fields to grid
            formGrid.add(vehiculeLabel, 0, row);
            formGrid.add(vehiculeCombo, 1, row++);

            formGrid.add(typeLabel, 0, row);
            formGrid.add(typeCombo, 1, row++);

            formGrid.add(statusLabel, 0, row);
            formGrid.add(statusCombo, 1, row++);

            formGrid.add(dateDebutLabel, 0, row);
            formGrid.add(dateDebutPicker, 1, row++);

            formGrid.add(dateFinLabel, 0, row);
            formGrid.add(dateFinPicker, 1, row++);

            formGrid.add(coutLabel, 0, row);
            formGrid.add(coutField, 1, row++);

            formGrid.add(descLabel, 0, row);
            formGrid.add(descArea, 1, row++);

            // Buttons
            Button saveButton = new Button(maintenanceToEdit == null ? "Ajouter" : "Enregistrer");
            saveButton.getStyleClass().add("button-primary");

            Button cancelButton = new Button("Annuler");
            cancelButton.getStyleClass().add("button-secondary");
            cancelButton.setOnAction(e -> formStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);

            formGrid.add(buttonBox, 1, row);

            saveButton.setOnAction(e -> {
                // Validate form
                if (vehiculeCombo.getValue() == null || typeCombo.getValue() == null || 
                    statusCombo.getValue() == null || dateDebutPicker.getValue() == null ||
                    coutField.getText().trim().isEmpty()) {

                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                    return;
                }

                try {
                    // Parse cost
                    double cout;
                    try {
                        cout = Double.parseDouble(coutField.getText().trim());
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Le coût doit être un nombre valide.");
                        return;
                    }

                    // Create or update maintenance
                    Maintenance maintenance = maintenanceToEdit == null ? new Maintenance() : maintenanceToEdit;

                    maintenance.setVehiculeId(vehiculeCombo.getValue().getId());
                    maintenance.setTypeMaintenance(typeCombo.getValue());
                    maintenance.setStatus(statusCombo.getValue());
                    maintenance.setPrix(cout);
                    maintenance.setDescription(descArea.getText().trim());

                    // Convert DatePicker dates to java.util.Date
                    if (dateDebutPicker.getValue() != null) {
                        Date startDate = Date.from(dateDebutPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        maintenance.setDateDebut(startDate);
                    }

                    if (dateFinPicker.getValue() != null) {
                        Date endDate = Date.from(dateFinPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        maintenance.setDateFin(endDate);
                    } else {
                        maintenance.setDateFin(null);
                    }

                    if (maintenanceToEdit == null) {
                        maintenanceService.ajouter(maintenance);
                        // Notify conducteur if assigned
                        Vehicule vehicule = vehiculeService.afficher(maintenance.getVehiculeId());
                        if (vehicule != null && vehicule.getConducteurId() > 0) {
                            Conducteur conducteur = new services.ConducteurService().afficher(vehicule.getConducteurId());
                            if (conducteur != null && conducteur.getEmail() != null && !conducteur.getEmail().isEmpty()) {
                                services.EmailService emailService = new services.EmailService();
                                String subject = "Véhicule en maintenance - TuniTransport";
                                String html = emailService.buildMaintenanceEmailContent(
                                    conducteur.getPrenom(), conducteur.getNom(),
                                    vehicule.getMarque() + " " + vehicule.getModele(),
                                    vehicule.getImmatriculation()
                                );
                                emailService.sendGenericHtmlEmail(conducteur.getEmail(), subject, html);
                            }
                        }
                    } else {
                        maintenanceService.modifier(maintenance);
                    }

                    formStage.close();
                    loadMaintenances();
                } catch (Exception ex) {
                    log.error("Error saving maintenance", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement.");
                }
            });

            // Set scene and show
            mainPane.setCenter(formGrid);
            mainPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene scene = new Scene(mainPane, 500, 500);
            formStage.setScene(scene);
            formStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing maintenance form", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le formulaire.");
        }
    }

    private void deleteMaintenance(Maintenance maintenance) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la maintenance");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette maintenance ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maintenanceService.supprimer(maintenance);
                loadMaintenances();
            } catch (Exception e) {
                log.error("Error deleting maintenance", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression de la maintenance.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
