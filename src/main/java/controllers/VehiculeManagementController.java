package controllers;

import entities.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.ConducteurService;
import services.VehiculeService;
import utils.OrganisationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class VehiculeManagementController {

    @FXML
    private FlowPane vehiculesContainer;

    @FXML
    private Button addButton;

    private final VehiculeService vehiculeService = new VehiculeService();
    private final ConducteurService conducteurService = new ConducteurService();
    
    private Organisation organisation;
    
    // Directory to store vehicle photos
    private static final String PHOTOS_DIRECTORY = "src/main/resources/Images/Vehicles/";

    @FXML
    public void initialize() {
        // Create photos directory if it doesn't exist
        createPhotosDirectory();
        
        // Essayer de charger l'organisation depuis le contexte global
        if (OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            loadVehicules();
            log.info("Organisation chargée depuis le contexte global dans VehiculeManagementController");
        }
    }
    
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        // Load vehicles for this organization
        loadVehicules();
        log.info("Organisation définie dans VehiculeManagementController: {}", 
                 organisation != null ? organisation.getNom() : "null");
    }
    
    private void createPhotosDirectory() {
        File directory = new File(PHOTOS_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created vehicle photos directory: {}", PHOTOS_DIRECTORY);
            } else {
                log.error("Failed to create vehicle photos directory: {}", PHOTOS_DIRECTORY);
            }
        }
    }

    @FXML
    public void onAddButtonClicked() {
        if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
        }
        
        showVehiculeForm(null);
    }

    private void loadVehicules() {
        vehiculesContainer.getChildren().clear();

        // Utiliser l'organisation du contexte global si elle n'est pas déjà définie
        if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
        }

        if (organisation != null) {
            // Get all vehicles and filter by organization
            List<Vehicule> allVehicules = vehiculeService.afficher_tout();
            boolean found = false;
            
            for (Vehicule vehicule : allVehicules) {
                if (vehicule.getOrganisationId() == organisation.getId()) {
                    vehiculesContainer.getChildren().add(createVehiculeCard(vehicule));
                    found = true;
                }
            }
            
            if (!found) {
                Label emptyLabel = new Label("Aucun véhicule trouvé");
                emptyLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                vehiculesContainer.getChildren().add(emptyLabel);
            }
        } else {
            Label errorLabel = new Label("Erreur: Organisation non définie");
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            vehiculesContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createVehiculeCard(Vehicule vehicule) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(300);
        card.setMinWidth(300);
        card.setPrefWidth(300);
        card.setPrefHeight(220);
        
        // Create a layout with photo on the left and basic info on the right
        HBox topContent = new HBox(10);
        
        // Photo area
        StackPane photoContainer = new StackPane();
        photoContainer.setMinWidth(80);
        photoContainer.setMaxWidth(80);
        photoContainer.setPrefWidth(80);
        photoContainer.setMinHeight(80);
        photoContainer.setMaxHeight(80);
        photoContainer.setPrefHeight(80);
        photoContainer.getStyleClass().add("logo-container");
        
        ImageView photoView = new ImageView();
        photoView.setFitWidth(70);
        photoView.setFitHeight(70);
        photoView.setPreserveRatio(true);
        
        // Try to load the vehicle photo
        if (vehicule.getPhoto() != null && !vehicule.getPhoto().isEmpty()) {
            try {
                File photoFile = new File(vehicule.getPhoto());
                if (photoFile.exists()) {
                    Image photoImage = new Image(photoFile.toURI().toString());
                    photoView.setImage(photoImage);
                } else {
                    // Use a default image if the photo file doesn't exist
                    photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
                }
            } catch (Exception e) {
                log.error("Error loading photo for vehicle: {}", vehicule.getImmatriculation(), e);
                // Use a default image in case of error
                photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
            }
        } else {
            // Use a default image if no photo is set
            photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
        }
        
        photoContainer.getChildren().add(photoView);
        
        // Vehicle details
        VBox detailsContainer = new VBox(5);
        detailsContainer.setAlignment(Pos.TOP_LEFT);
        
        // Vehicle marca and model
        Label nameLabel = new Label(vehicule.getMarque() + " " + vehicule.getModele());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);
        
        // Vehicle immatriculation
        Label immatLabel = new Label("Immat: " + vehicule.getImmatriculation());
        
        // Vehicle status
        Label statusLabel = new Label("Statut: " + vehicule.getStatut());
        
        // Conducteur info if assigned
        Label conducteurLabel = new Label();
        if (vehicule.getConducteurId() > 0) {
            Conducteur conducteur = conducteurService.afficher(vehicule.getConducteurId());
            if (conducteur != null) {
                conducteurLabel.setText("Conducteur: " + conducteur.getPrenom() + " " + conducteur.getNom());
            } else {
                conducteurLabel.setText("Conducteur: Non trouvé");
            }
        } else {
            conducteurLabel.setText("Conducteur: Non assigné");
        }
        
        detailsContainer.getChildren().addAll(nameLabel, immatLabel, statusLabel, conducteurLabel);
        
        topContent.getChildren().addAll(photoContainer, detailsContainer);
        
        // Add some spacing for better appearance
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Detail button
        Button detailBtn = new Button("Détails");
        detailBtn.getStyleClass().add("detail-button");
        detailBtn.setOnAction(e -> showVehiculeDetails(vehicule));
        
        // Edit button
        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> showVehiculeForm(vehicule));
        
        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> deleteVehicule(vehicule));
        
        buttonBox.getChildren().addAll(detailBtn, editBtn, deleteBtn);
        
        // Add all elements to the card
        card.getChildren().addAll(topContent, spacer, buttonBox);
        card.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
        
        return card;
    }

    private void showVehiculeDetails(Vehicule vehicule) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails du véhicule");
            
            BorderPane detailsPane = new BorderPane();
            detailsPane.setPadding(new Insets(20));
            
            VBox detailsBox = new VBox(15);
            detailsBox.setAlignment(Pos.TOP_LEFT);
            
            // Add photo at the top
            ImageView photoView = new ImageView();
            photoView.setFitWidth(150);
            photoView.setFitHeight(150);
            photoView.setPreserveRatio(true);
            
            // Try to load the vehicle photo
            if (vehicule.getPhoto() != null && !vehicule.getPhoto().isEmpty()) {
                try {
                    File photoFile = new File(vehicule.getPhoto());
                    if (photoFile.exists()) {
                        Image photoImage = new Image(photoFile.toURI().toString());
                        photoView.setImage(photoImage);
                    } else {
                        photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
                    }
                } catch (Exception e) {
                    log.error("Error loading photo for vehicle details: {}", vehicule.getImmatriculation(), e);
                    photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
                }
            } else {
                photoView.setImage(new Image(getClass().getResourceAsStream("/Images/Vehicles/default_vehicle.png")));
            }
            
            // Add vehicle details
            Label titleLabel = new Label(vehicule.getMarque() + " " + vehicule.getModele());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            
            // Format dates
            String dateAjoutStr = "N/A";
            String dateFabStr = "N/A";
            
            if (vehicule.getDateAjout() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateAjoutStr = sdf.format(vehicule.getDateAjout());
            }
            
            if (vehicule.getDateFabrication() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateFabStr = sdf.format(vehicule.getDateFabrication());
            }
            
            // Create labels for all vehicle details
            Label immatLabel = new Label("Immatriculation: " + vehicule.getImmatriculation());
            Label typeLabel = new Label("Type: " + vehicule.getType());
            Label statusLabel = new Label("Statut: " + vehicule.getStatut());
            Label capaciteLabel = new Label("Capacité: " + vehicule.getCapacite() + " personnes");
            Label dateAjoutLabel = new Label("Date d'ajout: " + dateAjoutStr);
            Label dateFabLabel = new Label("Date de fabrication: " + dateFabStr);
            
            // Conducteur info
            Label conducteurLabel = new Label();
            if (vehicule.getConducteurId() > 0) {
                Conducteur conducteur = conducteurService.afficher(vehicule.getConducteurId());
                if (conducteur != null) {
                    conducteurLabel.setText("Conducteur: " + conducteur.getPrenom() + " " + conducteur.getNom() + " (Tél: " + conducteur.getTelephone() + ")");
                } else {
                    conducteurLabel.setText("Conducteur: Non trouvé");
                }
            } else {
                conducteurLabel.setText("Conducteur: Non assigné");
            }
            
            // Buttons for assigning/changing conducteur
            Button assignConducteurBtn = new Button(vehicule.getConducteurId() > 0 ? "Changer conducteur" : "Assigner un conducteur");
            assignConducteurBtn.getStyleClass().add("button-primary");
            assignConducteurBtn.setOnAction(e -> assignConducteur(vehicule, detailStage));
            
            Button closeButton = new Button("Fermer");
            closeButton.getStyleClass().add("button-secondary");
            closeButton.setPrefWidth(100);
            closeButton.setOnAction(e -> detailStage.close());
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(assignConducteurBtn, closeButton);
            
            // Add photo to top center
            HBox photoBox = new HBox();
            photoBox.setAlignment(Pos.CENTER);
            photoBox.getChildren().add(photoView);
            
            // Add all elements to the details box
            detailsBox.getChildren().addAll(
                titleLabel, 
                immatLabel,
                typeLabel,
                statusLabel,
                capaciteLabel,
                dateAjoutLabel,
                dateFabLabel,
                conducteurLabel,
                buttonBox
            );
            
            detailsPane.setTop(photoBox);
            detailsPane.setCenter(detailsBox);
            
            detailsPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            Scene scene = new Scene(detailsPane, 450, 550);
            detailStage.setScene(scene);
            detailStage.showAndWait();
            
        } catch (Exception e) {
            log.error("Error showing vehicle details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails du véhicule.");
        }
    }
    
    private void assignConducteur(Vehicule vehicule, Stage parentStage) {
        try {
            // Get all conducteurs from the current organization
            List<Conducteur> conducteurs = conducteurService.afficher_tout();
            
            if (conducteurs.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun conducteur disponible. Veuillez ajouter des conducteurs d'abord.");
                return;
            }
            
            // Create a dialog to select a conducteur
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setTitle("Assigner un conducteur");
            
            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));
            
            Label titleLabel = new Label("Sélectionner un conducteur");
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            
            // Create a combobox of conducteurs
            ComboBox<Conducteur> conducteurCombo = new ComboBox<>();
            for (Conducteur c : conducteurs) {
                // Only show conducteurs from this organization
                if (c.getOrganisationId() == organisation.getId()) {
                    conducteurCombo.getItems().add(c);
                }
            }
            
            // Custom cell factory to display conducteur info
            conducteurCombo.setCellFactory(param -> new ListCell<Conducteur>() {
                @Override
                protected void updateItem(Conducteur item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getPrenom() + " " + item.getNom() + " (" + item.getTelephone() + ")");
                    }
                }
            });
            
            // Custom string converter for the selected item
            conducteurCombo.setButtonCell(new ListCell<Conducteur>() {
                @Override
                protected void updateItem(Conducteur item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getPrenom() + " " + item.getNom() + " (" + item.getTelephone() + ")");
                    }
                }
            });
            
            // Set current conducteur if any
            if (vehicule.getConducteurId() > 0) {
                for (Conducteur c : conducteurCombo.getItems()) {
                    if (c.getId() == vehicule.getConducteurId()) {
                        conducteurCombo.setValue(c);
                        break;
                    }
                }
            }
            
            // Action buttons
            Button assignButton = new Button("Assigner");
            assignButton.getStyleClass().add("button-primary");
            assignButton.setOnAction(e -> {
                Conducteur selectedConducteur = conducteurCombo.getValue();
                if (selectedConducteur != null) {
                    // Update the vehicle
                    vehicule.setConducteurId(selectedConducteur.getId());
                    vehiculeService.modifier(vehicule);
                    
                    // Close dialog and refresh details
                    dialogStage.close();
                    parentStage.close();
                    showVehiculeDetails(vehicule);
                    
                    // Also update the cards view
                    loadVehicules();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner un conducteur.");
                }
            });
            
            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> dialogStage.close());
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, assignButton);
            
            dialogVbox.getChildren().addAll(titleLabel, conducteurCombo, buttonBox);
            dialogVbox.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            Scene dialogScene = new Scene(dialogVbox, 400, 150);
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            log.error("Error showing conductor assignment dialog", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher la boîte de dialogue d'assignation.");
        }
    }

    private void showVehiculeForm(Vehicule vehiculeToEdit) {
        try {
            // Vérifier si l'organisation est disponible
            if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
                this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            }
            
            if (organisation == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de déterminer l'organisation actuelle.");
                return;
            }
            
            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);
            
            String title = vehiculeToEdit == null ? "Ajouter un véhicule" : "Modifier le véhicule";
            formStage.setTitle(title);
            
            BorderPane mainPane = new BorderPane();
            
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(20));
            
            // Add form fields
            Label marqueLabel = new Label("Marque:");
            TextField marqueField = new TextField();
            marqueField.setPrefWidth(300);
            
            Label modelLabel = new Label("Modèle:");
            TextField modelField = new TextField();
            
            Label immatLabel = new Label("Immatriculation:");
            TextField immatField = new TextField();
            
            Label typeLabel = new Label("Type:");
            ComboBox<VehiculeType> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(VehiculeType.values());
            
            Label statutLabel = new Label("Statut:");
            ComboBox<VehiculeStatut> statutCombo = new ComboBox<>();
            statutCombo.getItems().addAll(VehiculeStatut.values());
            
            Label capaciteLabel = new Label("Capacité:");
            Spinner<Integer> capaciteSpinner = new Spinner<>(1, 100, 5);
            
            Label dateFabLabel = new Label("Date de fabrication:");
            DatePicker dateFabPicker = new DatePicker();
            
            // Photo selection
            Label photoLabel = new Label("Photo:");
            
            HBox photoBox = new HBox(10);
            photoBox.setAlignment(Pos.CENTER_LEFT);
            
            TextField photoPathField = new TextField();
            photoPathField.setEditable(false);
            photoPathField.setPrefWidth(230);
            
            Button browseButton = new Button("Parcourir...");
            
            // Image preview
            ImageView photoPreview = new ImageView();
            photoPreview.setFitWidth(120);
            photoPreview.setFitHeight(120);
            photoPreview.setPreserveRatio(true);
            
            photoBox.getChildren().addAll(photoPathField, browseButton);
            
            VBox photoPreviewBox = new VBox(5);
            photoPreviewBox.setAlignment(Pos.CENTER);
            photoPreviewBox.getChildren().add(photoPreview);
            
            // Set current photo if available
            String photoPath = null;
            if (vehiculeToEdit != null && vehiculeToEdit.getPhoto() != null) {
                photoPath = vehiculeToEdit.getPhoto();
                photoPathField.setText(photoPath);
                
                try {
                    File photoFile = new File(photoPath);
                    if (photoFile.exists()) {
                        Image photoImage = new Image(photoFile.toURI().toString());
                        photoPreview.setImage(photoImage);
                    }
                } catch (Exception e) {
                    log.error("Error loading existing photo: {}", e.getMessage());
                }
            }
            
            // Final photo path that will be saved
            final String[] selectedPhotoPath = {photoPath};
            
            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Vehicle Photo");
                fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                
                File selectedFile = fileChooser.showOpenDialog(formStage);
                if (selectedFile != null) {
                    try {
                        // Generate unique file name to avoid overwrites
                        String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                        Path targetPath = Paths.get(PHOTOS_DIRECTORY + uniqueFileName);
                        
                        // Create directory if it doesn't exist
                        Files.createDirectories(targetPath.getParent());
                        
                        // Copy the file to our photos directory
                        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                        
                        // Update the photo preview
                        Image photoImage = new Image(targetPath.toFile().toURI().toString());
                        photoPreview.setImage(photoImage);
                        
                        // Store the path to save later
                        selectedPhotoPath[0] = targetPath.toString();
                        photoPathField.setText(targetPath.toString());
                        
                        log.info("Photo selected and copied to: {}", targetPath);
                    } catch (IOException ex) {
                        log.error("Error copying photo file", ex);
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Impossible de copier le fichier photo. " + ex.getMessage());
                    }
                }
            });
            
            // Set values if editing
            if (vehiculeToEdit != null) {
                marqueField.setText(vehiculeToEdit.getMarque());
                modelField.setText(vehiculeToEdit.getModele());
                immatField.setText(vehiculeToEdit.getImmatriculation());
                typeCombo.setValue(vehiculeToEdit.getType());
                statutCombo.setValue(vehiculeToEdit.getStatut());
                capaciteSpinner.getValueFactory().setValue(vehiculeToEdit.getCapacite());
                if (vehiculeToEdit.getDateFabrication() != null) {
                    dateFabPicker.setValue(vehiculeToEdit.getDateFabrication().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate());
                }
            } else {
                typeCombo.setValue(VehiculeType.VOITURE);
                statutCombo.setValue(VehiculeStatut.DISPONIBLE);
            }
            
            // Add elements to grid
            formGrid.add(marqueLabel, 0, 0);
            formGrid.add(marqueField, 1, 0);
            
            formGrid.add(modelLabel, 0, 1);
            formGrid.add(modelField, 1, 1);
            
            formGrid.add(immatLabel, 0, 2);
            formGrid.add(immatField, 1, 2);
            
            formGrid.add(typeLabel, 0, 3);
            formGrid.add(typeCombo, 1, 3);
            
            formGrid.add(statutLabel, 0, 4);
            formGrid.add(statutCombo, 1, 4);
            
            formGrid.add(capaciteLabel, 0, 5);
            formGrid.add(capaciteSpinner, 1, 5);
            
            formGrid.add(dateFabLabel, 0, 6);
            formGrid.add(dateFabPicker, 1, 6);
            
            formGrid.add(photoLabel, 0, 7);
            formGrid.add(photoBox, 1, 7);
            
            // Add photo preview to the left side of the form
            VBox leftSide = new VBox(10);
            leftSide.setAlignment(Pos.TOP_CENTER);
            leftSide.setPadding(new Insets(20));
            leftSide.getChildren().add(photoPreviewBox);
            
            // Buttons
            Button saveButton = new Button(vehiculeToEdit == null ? "Ajouter" : "Enregistrer");
            saveButton.getStyleClass().add("button-primary");
            
            Button cancelButton = new Button("Annuler");
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);
            
            formGrid.add(buttonBox, 1, 8);
            
            // Event handlers
            saveButton.setOnAction(e -> {
                if (marqueField.getText().trim().isEmpty() || 
                    modelField.getText().trim().isEmpty() ||
                    immatField.getText().trim().isEmpty()) {
                    
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                    return;
                }
                
                try {
                    Vehicule vehicule = vehiculeToEdit == null ? new Vehicule() : vehiculeToEdit;
                    
                    vehicule.setMarque(marqueField.getText().trim());
                    vehicule.setModele(modelField.getText().trim());
                    vehicule.setImmatriculation(immatField.getText().trim());
                    vehicule.setType(typeCombo.getValue());
                    vehicule.setStatut(statutCombo.getValue());
                    vehicule.setCapacite(capaciteSpinner.getValue());
                    
                    // Set the organisation ID - FIX: Utiliser l'ID de l'organisation courante
                    vehicule.setOrganisationId(organisation.getId());
                    log.info("ID de l'organisation utilisé pour le véhicule: {}", organisation.getId());
                    
                    // Set the date of fabrication if provided
                    if (dateFabPicker.getValue() != null) {
                        vehicule.setDateFabrication(java.sql.Date.valueOf(dateFabPicker.getValue()));
                    }
                    
                    // Set the photo path if a photo was selected
                    if (selectedPhotoPath[0] != null) {
                        vehicule.setPhoto(selectedPhotoPath[0]);
                    }
                    
                    if (vehiculeToEdit == null) {
                        vehicule.setDateAjout(new Date());
                        vehiculeService.ajouter(vehicule);
                    } else {
                        vehiculeService.modifier(vehicule);
                    }
                    
                    formStage.close();
                    loadVehicules();
                } catch (Exception ex) {
                    log.error("Error saving vehicle", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement du véhicule.");
                }
            });
            
            cancelButton.setOnAction(e -> formStage.close());
            
            // Set up the main layout
            mainPane.setCenter(formGrid);
            mainPane.setLeft(leftSide);
            
            // Set scene and show
            mainPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Scene scene = new Scene(mainPane, 650, 500);
            formStage.setScene(scene);
            formStage.showAndWait();
            
        } catch (Exception e) {
            log.error("Error showing vehicle form", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le formulaire.");
        }
    }

    private void deleteVehicule(Vehicule vehicule) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le véhicule");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer le véhicule '" + vehicule.getMarque() + " " + vehicule.getModele() + "' ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete photo file if it exists
                if (vehicule.getPhoto() != null && !vehicule.getPhoto().isEmpty()) {
                    try {
                        File photoFile = new File(vehicule.getPhoto());
                        if (photoFile.exists() && photoFile.isFile()) {
                            boolean deleted = photoFile.delete();
                            if (deleted) {
                                log.info("Deleted photo file: {}", vehicule.getPhoto());
                            } else {
                                log.warn("Failed to delete photo file: {}", vehicule.getPhoto());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error deleting photo file", e);
                    }
                }
                
                vehiculeService.supprimer(vehicule);
                loadVehicules();
            } catch (Exception e) {
                log.error("Error deleting vehicle", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression du véhicule.");
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