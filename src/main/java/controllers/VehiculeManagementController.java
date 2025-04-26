package controllers;

import entities.Conducteur;
import entities.Organisation;
import entities.Vehicule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class VehiculeManagementController {

    @FXML
    private TableView<Vehicule> vehiculeTableView;
    
    @FXML
    private TableColumn<Vehicule, Integer> idColumn;
    
    @FXML
    private TableColumn<Vehicule, String> immatriculationColumn;
    
    @FXML
    private TableColumn<Vehicule, String> marqueColumn;
    
    @FXML
    private TableColumn<Vehicule, String> modeleColumn;
    
    @FXML
    private TableColumn<Vehicule, Integer> anneeColumn;
    
    @FXML
    private TableColumn<Vehicule, String> statusColumn;
    
    @FXML
    private TableColumn<Vehicule, String> typeColumn;
    
    @FXML
    private TableColumn<Vehicule, Vehicule> actionsColumn;
    
    @FXML
    private Button addVehiculeButton;

    @FXML
    private TextField searchField;
    
    private VehiculeService vehiculeService = new VehiculeService();
    private ConducteurService conducteurService = new ConducteurService();
    private ObservableList<Vehicule> vehiculesList = FXCollections.observableArrayList();
    private Organisation organisation;
    
    // Dossier pour stocker les photos des véhicules
    private static final String PHOTOS_DIRECTORY = "src/main/resources/Images/vehicles/";

    @FXML
    public void initialize() {
        // Créer le dossier de photos si nécessaire
        createPhotosDirectory();
        
        setupTableColumns();
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterVehicles(newValue);
        });
        
        // Essayer de charger l'organisation depuis le contexte global
        if (OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            loadVehicules();
            log.info("Organisation chargée depuis le contexte global dans VehiculeManagementController");
        }
        
        addVehiculeButton.setOnAction(e -> showVehiculeForm(null));
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
    
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        loadVehicules();
        log.info("Organisation définie dans VehiculeManagementController: {}", 
                 organisation != null ? organisation.getNom() : "null");
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        immatriculationColumn.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        marqueColumn.setCellValueFactory(new PropertyValueFactory<>("marque"));
        modeleColumn.setCellValueFactory(new PropertyValueFactory<>("modele"));
        anneeColumn.setCellValueFactory(new PropertyValueFactory<>("annee"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        
        // Setup action column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<Vehicule, Vehicule>() {
            private final Button viewBtn = new Button("Voir");
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            
            {
                viewBtn.getStyleClass().add("button-small");
                editBtn.getStyleClass().add("button-small");
                deleteBtn.getStyleClass().add("button-small");
                
                viewBtn.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    showVehiculeDetails(vehicule);
                });
                
                editBtn.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    showVehiculeForm(vehicule);
                });
                
                deleteBtn.setOnAction(event -> {
                    Vehicule vehicule = getTableView().getItems().get(getIndex());
                    deleteVehicule(vehicule);
                });
            }
            
            @Override
            protected void updateItem(Vehicule vehicule, boolean empty) {
                super.updateItem(vehicule, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadVehicules() {
        try {
            // Utiliser l'organisation du contexte global si elle n'est pas déjà définie
            if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
                this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            }
            
            List<Vehicule> allVehicules = vehiculeService.afficher_tout();
            
            // Filter by organisation
            if (organisation != null) {
                List<Vehicule> filteredVehicules = allVehicules.stream()
                    .filter(v -> v.getOrganisationId() == organisation.getId())
                    .collect(Collectors.toList());
                
                vehiculesList = FXCollections.observableArrayList(filteredVehicules);
            } else {
                vehiculesList = FXCollections.observableArrayList(allVehicules);
            }
            
            vehiculeTableView.setItems(vehiculesList);
        } catch (Exception e) {
            log.error("Error loading vehicles", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des véhicules.");
        }
    }
    
    private void filterVehicles(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            loadVehicules();
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            List<Vehicule> filteredList = vehiculesList.stream()
                .filter(v -> 
                    (v.getImmatriculation() != null && v.getImmatriculation().toLowerCase().contains(lowerCaseSearch)) ||
                    (v.getMarque() != null && v.getMarque().toLowerCase().contains(lowerCaseSearch)) ||
                    (v.getModele() != null && v.getModele().toLowerCase().contains(lowerCaseSearch)) ||
                    (v.getType() != null && v.getType().toLowerCase().contains(lowerCaseSearch))
                )
                .collect(Collectors.toList());
            
            vehiculeTableView.setItems(FXCollections.observableArrayList(filteredList));
        }
    }
    
    private void showVehiculeDetails(Vehicule vehicule) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails du véhicule");
            
            BorderPane detailPane = new BorderPane();
            detailPane.setPadding(new Insets(20));
            
            // Header with vehicle info
            VBox headerBox = new VBox(10);
            headerBox.setAlignment(Pos.CENTER);
            
            // Photo
            ImageView photoView = new ImageView();
            photoView.setFitHeight(150);
            photoView.setFitWidth(180);
            photoView.setPreserveRatio(true);
            
            // Try to load vehicle's photo if available
            if (vehicule.getPhoto() != null && !vehicule.getPhoto().isEmpty()) {
                try {
                    File photoFile = new File(vehicule.getPhoto());
                    if (photoFile.exists()) {
                        Image photo = new Image(photoFile.toURI().toString());
                        photoView.setImage(photo);
                    } else {
                        // Load default photo
                        photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
                    }
                } catch (Exception e) {
                    log.warn("Could not load vehicle photo", e);
                    photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
                }
            } else {
                // Load default photo
                photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
            }
            
            Label nameLabel = new Label(vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getAnnee() + ")");
            nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            headerBox.getChildren().addAll(photoView, nameLabel);
            
            // Vehicle details grid
            GridPane detailsGrid = new GridPane();
            detailsGrid.setHgap(10);
            detailsGrid.setVgap(10);
            detailsGrid.setPadding(new Insets(20, 0, 20, 0));
            
            int row = 0;
            
            // Immatriculation
            Label immatriculationLbl = new Label("Immatriculation:");
            immatriculationLbl.setStyle("-fx-font-weight: bold;");
            Label immatriculationValue = new Label(vehicule.getImmatriculation());
            detailsGrid.add(immatriculationLbl, 0, row);
            detailsGrid.add(immatriculationValue, 1, row++);
            
            // Type
            Label typeLbl = new Label("Type:");
            typeLbl.setStyle("-fx-font-weight: bold;");
            Label typeValue = new Label(vehicule.getType());
            detailsGrid.add(typeLbl, 0, row);
            detailsGrid.add(typeValue, 1, row++);
            
            // Status
            Label statusLbl = new Label("Status:");
            statusLbl.setStyle("-fx-font-weight: bold;");
            Label statusValue = new Label(vehicule.getStatus());
            detailsGrid.add(statusLbl, 0, row);
            detailsGrid.add(statusValue, 1, row++);
            
            // Date d'acquisition
            Label acquisitionLbl = new Label("Date d'acquisition:");
            acquisitionLbl.setStyle("-fx-font-weight: bold;");
            Label acquisitionValue;
            if (vehicule.getDateAcquisition() != null) {
                acquisitionValue = new Label(vehicule.getDateAcquisition().toString());
            } else {
                acquisitionValue = new Label("Non spécifiée");
            }
            detailsGrid.add(acquisitionLbl, 0, row);
            detailsGrid.add(acquisitionValue, 1, row++);
            
            // Kilométrage
            Label kmLbl = new Label("Kilométrage:");
            kmLbl.setStyle("-fx-font-weight: bold;");
            Label kmValue = new Label(vehicule.getKilometrage() + " km");
            detailsGrid.add(kmLbl, 0, row);
            detailsGrid.add(kmValue, 1, row++);
            
            // Conducteur assigné
            Label conducteurLbl = new Label("Conducteur assigné:");
            conducteurLbl.setStyle("-fx-font-weight: bold;");
            Label conducteurValue;
            
            // Find driver assigned to this vehicle
            List<Conducteur> conducteurs = conducteurService.afficher_tout();
            Optional<Conducteur> assignedConducteur = conducteurs.stream()
                .filter(c -> c.getVehiculeId() == vehicule.getId())
                .findFirst();
            
            if (assignedConducteur.isPresent()) {
                Conducteur conducteur = assignedConducteur.get();
                conducteurValue = new Label(conducteur.getNom() + " " + conducteur.getPrenom());
            } else {
                conducteurValue = new Label("Non assigné");
            }
            detailsGrid.add(conducteurLbl, 0, row);
            detailsGrid.add(conducteurValue, 1, row++);
            
            // Description
            if (vehicule.getDescription() != null && !vehicule.getDescription().isEmpty()) {
                Label descriptionLbl = new Label("Description:");
                descriptionLbl.setStyle("-fx-font-weight: bold;");
                TextArea descriptionValue = new TextArea(vehicule.getDescription());
                descriptionValue.setEditable(false);
                descriptionValue.setWrapText(true);
                descriptionValue.setPrefRowCount(3);
                descriptionValue.setPrefColumnCount(20);
                detailsGrid.add(descriptionLbl, 0, row);
                detailsGrid.add(descriptionValue, 1, row++);
            }
            
            // Buttons
            Button closeButton = new Button("Fermer");
            closeButton.setOnAction(e -> detailStage.close());
            
            // Layout
            VBox mainLayout = new VBox(20);
            mainLayout.getChildren().addAll(headerBox, detailsGrid, closeButton);
            
            detailPane.setCenter(mainLayout);
            
            Scene scene = new Scene(detailPane, 500, 600);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            detailStage.setScene(scene);
            detailStage.showAndWait();
        } catch (Exception e) {
            log.error("Error showing vehicle details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails du véhicule.");
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
            mainPane.setPadding(new Insets(20));
            
            // Create form layout
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(15);
            formGrid.setPadding(new Insets(20, 10, 10, 10));
            
            // Photo section
            VBox photoBox = new VBox(10);
            photoBox.setAlignment(Pos.TOP_CENTER);
            photoBox.setPadding(new Insets(0, 20, 0, 0));
            
            ImageView photoView = new ImageView();
            photoView.setFitHeight(150);
            photoView.setFitWidth(180);
            photoView.setPreserveRatio(true);
            
            // Try to load vehicle's photo if editing
            String initialPhotoPath;
            if (vehiculeToEdit != null && vehiculeToEdit.getPhoto() != null) {
                initialPhotoPath = vehiculeToEdit.getPhoto();
                try {
                    File photoFile = new File(initialPhotoPath);
                    if (photoFile.exists()) {
                        Image photo = new Image(photoFile.toURI().toString());
                        photoView.setImage(photo);
                    } else {
                        // Load default photo
                        photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
                    }
                } catch (Exception e) {
                    log.warn("Could not load vehicle photo", e);
                    photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
                }
            } else {
                initialPhotoPath = null;
                // Load default photo
                photoView.setImage(new Image(getClass().getResourceAsStream("/Images/default_vehicle.png")));
            }
            
            Button photoButton = new Button("Choisir une photo");
            photoButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Sélectionner une photo");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                File selectedFile = fileChooser.showOpenDialog(formStage);
                if (selectedFile != null) {
                    try {
                        Image photo = new Image(selectedFile.toURI().toString());
                        photoView.setImage(photo);
                        photoView.setUserData(selectedFile.getAbsolutePath());
                    } catch (Exception ex) {
                        log.warn("Could not load selected photo", ex);
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la photo sélectionnée.");
                    }
                }
            });
            
            photoBox.getChildren().addAll(photoView, photoButton);
            
            // Form fields
            int row = 0;
            
            // Immatriculation
            Label immatriculationLabel = new Label("Immatriculation *");
            TextField immatriculationField = new TextField();
            formGrid.add(immatriculationLabel, 0, row);
            formGrid.add(immatriculationField, 1, row++);
            
            // Marque
            Label marqueLabel = new Label("Marque *");
            TextField marqueField = new TextField();
            formGrid.add(marqueLabel, 0, row);
            formGrid.add(marqueField, 1, row++);
            
            // Modèle
            Label modeleLabel = new Label("Modèle *");
            TextField modeleField = new TextField();
            formGrid.add(modeleLabel, 0, row);
            formGrid.add(modeleField, 1, row++);
            
            // Année
            Label anneeLabel = new Label("Année *");
            TextField anneeField = new TextField();
            formGrid.add(anneeLabel, 0, row);
            formGrid.add(anneeField, 1, row++);
            
            // Type de véhicule
            Label typeLabel = new Label("Type *");
            ComboBox<String> typeComboBox = new ComboBox<>();
            typeComboBox.getItems().addAll(
                "Voiture", "Bus", "Camion", "Utilitaire", "Autre"
            );
            formGrid.add(typeLabel, 0, row);
            formGrid.add(typeComboBox, 1, row++);
            
            // Status
            Label statusLabel = new Label("Status *");
            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll(
                "En service", "En maintenance", "Hors service", "Réservé"
            );
            formGrid.add(statusLabel, 0, row);
            formGrid.add(statusComboBox, 1, row++);
            
            // Kilométrage
            Label kilometrageLabel = new Label("Kilométrage");
            TextField kilometrageField = new TextField();
            formGrid.add(kilometrageLabel, 0, row);
            formGrid.add(kilometrageField, 1, row++);
            
            // Description
            Label descriptionLabel = new Label("Description");
            TextArea descriptionArea = new TextArea();
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setWrapText(true);
            formGrid.add(descriptionLabel, 0, row);
            formGrid.add(descriptionArea, 1, row++);
            
            // Fill form with data if editing
            if (vehiculeToEdit != null) {
                immatriculationField.setText(vehiculeToEdit.getImmatriculation());
                marqueField.setText(vehiculeToEdit.getMarque());
                modeleField.setText(vehiculeToEdit.getModele());
                anneeField.setText(String.valueOf(vehiculeToEdit.getAnnee()));
                typeComboBox.setValue(vehiculeToEdit.getType());
                statusComboBox.setValue(vehiculeToEdit.getStatus());
                kilometrageField.setText(String.valueOf(vehiculeToEdit.getKilometrage()));
                if (vehiculeToEdit.getDescription() != null) {
                    descriptionArea.setText(vehiculeToEdit.getDescription());
                }
            } else {
                // Set default values for new vehicle
                typeComboBox.setValue("Voiture");
                statusComboBox.setValue("En service");
                kilometrageField.setText("0");
            }
            
            // Buttons
            Button saveButton = new Button(vehiculeToEdit == null ? "Ajouter" : "Enregistrer");
            saveButton.getStyleClass().add("button-primary");
            
            Button cancelButton = new Button("Annuler");
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);
            
            formGrid.add(buttonBox, 1, row);
            
            // Event handlers
            saveButton.setOnAction(e -> {
                if (immatriculationField.getText().trim().isEmpty() ||
                    marqueField.getText().trim().isEmpty() ||
                    modeleField.getText().trim().isEmpty() ||
                    anneeField.getText().trim().isEmpty() ||
                    typeComboBox.getValue() == null ||
                    statusComboBox.getValue() == null) {
                    
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                    return;
                }
                
                try {
                    int annee;
                    int kilometrage = 0;
                    
                    try {
                        annee = Integer.parseInt(anneeField.getText().trim());
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "L'année doit être un nombre entier.");
                        return;
                    }
                    
                    if (!kilometrageField.getText().trim().isEmpty()) {
                        try {
                            kilometrage = Integer.parseInt(kilometrageField.getText().trim());
                        } catch (NumberFormatException ex) {
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Le kilométrage doit être un nombre entier.");
                            return;
                        }
                    }
                    
                    // Create or update vehicle
                    Vehicule vehicule = vehiculeToEdit == null ? new Vehicule() : vehiculeToEdit;
                    
                    vehicule.setImmatriculation(immatriculationField.getText().trim());
                    vehicule.setMarque(marqueField.getText().trim());
                    vehicule.setModele(modeleField.getText().trim());
                    vehicule.setAnnee(annee);
                    vehicule.setType(typeComboBox.getValue());
                    vehicule.setStatus(statusComboBox.getValue());
                    vehicule.setKilometrage(kilometrage);
                    vehicule.setDescription(descriptionArea.getText().trim());
                    
                    // Handle photo
                    String savedPhotoPath = null;
                    if (photoView.getUserData() != null) {
                        // New photo selected, save it
                        String sourcePhotoPath = (String) photoView.getUserData();
                        savedPhotoPath = saveVehiclePhoto(sourcePhotoPath, vehiculeToEdit);
                    } else if (initialPhotoPath != null) {
                        // Keep existing photo
                        savedPhotoPath = initialPhotoPath;
                    }
                    
                    vehicule.setPhoto(savedPhotoPath);
                    
                    // Set acquisition date if not already set
                    if (vehicule.getDateAcquisition() == null) {
                        vehicule.setDateAcquisition(new Date());
                    }
                    
                    // Définir l'ID de l'organisation actuelle
                    if (vehiculeToEdit == null) {
                        vehicule.setOrganisationId(organisation.getId());
                        log.info("ID de l'organisation utilisé pour le véhicule: {}", organisation.getId());
                    }
                    
                    if (vehiculeToEdit == null) {
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
            
            // Layout
            BorderPane formPane = new BorderPane();
            formPane.setLeft(photoBox);
            formPane.setCenter(formGrid);
            
            mainPane.setCenter(formPane);
            
            // Set scene and show
            Scene scene = new Scene(mainPane, 600, 550);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            formStage.setScene(scene);
            formStage.showAndWait();
            
        } catch (Exception e) {
            log.error("Error showing vehicle form", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le formulaire.");
        }
    }
    
    private String saveVehiclePhoto(String sourcePhotoPath, Vehicule existingVehicle) {
        try {
            // Create photos directory if it doesn't exist
            Path photosDir = Paths.get(PHOTOS_DIRECTORY);
            if (!Files.exists(photosDir)) {
                Files.createDirectories(photosDir);
            }
            
            // Generate a unique filename based on vehicle info
            String fileName;
            if (existingVehicle != null) {
                // Use existing vehicle info
                fileName = "vehicle_" + existingVehicle.getImmatriculation().replaceAll("[^a-zA-Z0-9]", "_")
                        + "_" + UUID.randomUUID().toString().substring(0, 8) 
                        + getFileExtension(sourcePhotoPath);
            } else {
                // Use UUID for new vehicle
                fileName = "vehicle_" + UUID.randomUUID().toString().substring(0, 8) 
                        + getFileExtension(sourcePhotoPath);
            }
            
            Path targetPath = photosDir.resolve(fileName);
            
            // Copy file
            Files.copy(Paths.get(sourcePhotoPath), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error saving vehicle photo", e);
            return null;
        }
    }
    
    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return path.substring(lastDotIndex);
        }
        return "";
    }
    
    private void deleteVehicule(Vehicule vehicule) {
        // Vérifier si ce véhicule est assigné à un conducteur
        List<Conducteur> conducteurs = conducteurService.afficher_tout();
        boolean isAssigned = conducteurs.stream().anyMatch(c -> c.getVehiculeId() == vehicule.getId());
        
        if (isAssigned) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Ce véhicule est assigné à un conducteur. Veuillez désassigner le conducteur avant de supprimer le véhicule.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le véhicule");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + 
                                    vehicule.getMarque() + " " + vehicule.getModele() + 
                                    " (" + vehicule.getImmatriculation() + ") ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
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