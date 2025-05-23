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
import services.EmailService;
import services.VehiculeService;
import utils.OrganisationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ConducteurManagementController {

    @FXML
    private FlowPane conducteursContainer;

    @FXML
    private Button addButton;

    @FXML
    private TextField searchField;

    private final ConducteurService conducteurService = new ConducteurService();
    private final VehiculeService vehiculeService = new VehiculeService();
    private final EmailService emailService = new EmailService();
    private ObservableList<Conducteur> conducteursList = FXCollections.observableArrayList();
    private Organisation organisation;

    // Directory to store driver photos
    private static final String PHOTOS_DIRECTORY = "src/main/resources/Images/drivers/";

    @FXML
    public void initialize() {
        // Create photos directory if it doesn't exist
        createPhotosDirectory();

        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterDrivers(newValue);
        });

        // Try to get organization from global context
        if (OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            loadConducteurs();
            log.info("Organisation chargée depuis le contexte global dans ConducteurManagementController");
        }

        addButton.setOnAction(e -> showConducteurForm(null));
    }

    private void createPhotosDirectory() {
        File directory = new File(PHOTOS_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created driver photos directory: {}", PHOTOS_DIRECTORY);
            } else {
                log.error("Failed to create driver photos directory: {}", PHOTOS_DIRECTORY);
            }
        }
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        loadConducteurs();
        log.info("Organisation définie dans ConducteurManagementController: {}", 
                 organisation != null ? organisation.getNom() : "null");
    }

    @FXML
    public void onAddButtonClicked() {
        if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
        }

        showConducteurForm(null);
    }

    private void loadConducteurs() {
        conducteursContainer.getChildren().clear();

        // Use organization from global context if not already defined
        if (organisation == null && OrganisationContext.getInstance().hasCurrentOrganisation()) {
            this.organisation = OrganisationContext.getInstance().getCurrentOrganisation();
        }

        if (organisation != null) {
            List<Conducteur> allConducteurs = conducteurService.afficher_tout();

            // Filter by organisation
            List<Vehicule> organisationVehicles = vehiculeService.afficher_tout().stream()
                .filter(v -> v.getOrganisationId() == organisation.getId())
                .collect(Collectors.toList());

            // Get IDs of vehicles owned by this organization
            List<Integer> organisationVehicleIds = organisationVehicles.stream()
                .map(Vehicule::getId)
                .collect(Collectors.toList());

            // Filter conducteurs who are assigned to this organization's vehicles
            // or who belong to this organization directly
            List<Conducteur> filteredConducteurs = allConducteurs.stream()
                .filter(c -> c.getOrganisationId() == organisation.getId() || 
                             c.getVehiculeId() == 0 || 
                             organisationVehicleIds.contains(c.getVehiculeId()))
                .collect(Collectors.toList());

            boolean found = false;

            for (Conducteur conducteur : filteredConducteurs) {
                conducteursContainer.getChildren().add(createConducteurCard(conducteur));
                found = true;
            }

            if (!found) {
                Label emptyLabel = new Label("Aucun conducteur trouvé");
                emptyLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                conducteursContainer.getChildren().add(emptyLabel);
            }
        } else {
            Label errorLabel = new Label("Erreur: Organisation non définie");
            errorLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            conducteursContainer.getChildren().add(errorLabel);
        }
    }

    private void filterDrivers(String searchText) {
        conducteursContainer.getChildren().clear();

        if (searchText == null || searchText.isEmpty()) {
            loadConducteurs();
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            List<Conducteur> filteredList = conducteursList.stream()
                .filter(c -> 
                    (c.getNom() != null && c.getNom().toLowerCase().contains(lowerCaseSearch)) ||
                    (c.getPrenom() != null && c.getPrenom().toLowerCase().contains(lowerCaseSearch)) ||
                    (c.getCin() != null && c.getCin().toLowerCase().contains(lowerCaseSearch)) ||
                    (c.getNumeroPermis() != null && c.getNumeroPermis().toLowerCase().contains(lowerCaseSearch))
                )
                .collect(Collectors.toList());

            if (filteredList.isEmpty()) {
                Label noResultLabel = new Label("Aucun conducteur trouvé pour: " + searchText);
                noResultLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
                conducteursContainer.getChildren().add(noResultLabel);
            } else {
                for (Conducteur conducteur : filteredList) {
                    conducteursContainer.getChildren().add(createConducteurCard(conducteur));
                }
            }
        }
    }

    private VBox createConducteurCard(Conducteur conducteur) {
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
        photoContainer.getStyleClass().add("photo-container");

        ImageView photoView = new ImageView();
        photoView.setFitWidth(70);
        photoView.setFitHeight(70);
        photoView.setPreserveRatio(true);

        // Try to load driver's photo if available
        if (conducteur.getPhoto() != null && !conducteur.getPhoto().isEmpty()) {
            try {
                File photoFile = new File(conducteur.getPhoto());
                if (photoFile.exists()) {
                    Image photo = new Image(photoFile.toURI().toString());
                    photoView.setImage(photo);
                } else {
                    // Load default photo
                    Image defaultPhoto = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/Drivers/default_driver.png")));
                    photoView.setImage(defaultPhoto);
                }
            } catch (Exception e) {
                log.warn("Could not load driver photo", e);
                // Default photo as fallback
                Image defaultPhoto = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/Drivers/default_driver.png")));
                photoView.setImage(defaultPhoto);
            }
        } else {
            // Load default photo
            Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
            photoView.setImage(defaultPhoto);
        }

        photoContainer.getChildren().add(photoView);

        // Driver details
        VBox detailsContainer = new VBox(5);
        detailsContainer.setAlignment(Pos.TOP_LEFT);

        // Driver name
        Label nameLabel = new Label(conducteur.getNom() + " " + conducteur.getPrenom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);

        // Driver CIN
        Label cinLabel = new Label("CIN: " + conducteur.getCin());

        // Driver status
        Label statusLabel = new Label("Statut: " + (conducteur.getStatut() != null ? conducteur.getStatut() : "Actif"));

        // Vehicle info if assigned
        Label vehiculeLabel = new Label();
        if (conducteur.getVehiculeId() > 0) {
            Vehicule vehicule = vehiculeService.afficher(conducteur.getVehiculeId());
            if (vehicule != null) {
                vehiculeLabel.setText("Véhicule: " + vehicule.getMarque() + " " + vehicule.getModele());
            } else {
                vehiculeLabel.setText("Véhicule: Non trouvé");
            }
        } else {
            vehiculeLabel.setText("Véhicule: Non assigné");
        }

        detailsContainer.getChildren().addAll(nameLabel, cinLabel, statusLabel, vehiculeLabel);

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
        detailBtn.setOnAction(e -> showConducteurDetails(conducteur));

        // Edit button
        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("edit-button");
        editBtn.setOnAction(e -> showConducteurForm(conducteur));

        // Delete button
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnAction(e -> deleteConducteur(conducteur));

        buttonBox.getChildren().addAll(detailBtn, editBtn, deleteBtn);

        // Add all elements to the card
        card.getChildren().addAll(topContent, spacer, buttonBox);
        card.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        return card;
    }

    private void showConducteurDetails(Conducteur conducteur) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails du conducteur");

            BorderPane detailPane = new BorderPane();
            detailPane.setPadding(new Insets(20));

            // Header with driver info
            VBox headerBox = new VBox(10);
            headerBox.setAlignment(Pos.CENTER);

            // Photo
            ImageView photoView = new ImageView();
            photoView.setFitHeight(150);
            photoView.setFitWidth(150);
            photoView.setPreserveRatio(true);

            // Try to load driver's photo if available
            String photoPath = conducteur.getPhoto();
            if (photoPath != null && !photoPath.isEmpty()) {
                try {
                    File photoFile = new File(photoPath);
                    if (photoFile.exists()) {
                        Image photo = new Image(photoFile.toURI().toString());
                        photoView.setImage(photo);
                    } else {
                        // Load default photo
                        Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                        photoView.setImage(defaultPhoto);
                    }
                } catch (Exception e) {
                    log.warn("Could not load driver photo", e);
                    Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                    photoView.setImage(defaultPhoto);
                }
            } else {
                // Load default photo
                Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                photoView.setImage(defaultPhoto);
            }

            Label nameLabel = new Label(conducteur.getNom() + " " + conducteur.getPrenom());
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

            headerBox.getChildren().addAll(photoView, nameLabel);

            // Driver details grid
            GridPane detailsGrid = new GridPane();
            detailsGrid.setHgap(10);
            detailsGrid.setVgap(10);
            detailsGrid.setPadding(new Insets(20, 0, 20, 0));

            int row = 0;

            // CIN
            Label cinLbl = new Label("CIN:");
            cinLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label cinValue = new Label(conducteur.getCin());
            detailsGrid.add(cinLbl, 0, row);
            detailsGrid.add(cinValue, 1, row++);

            // Date de naissance
            Label dobLbl = new Label("Date de naissance:");
            dobLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label dobValue;
            if (conducteur.getDateNaissance() != null) {
                LocalDate dob = conducteur.getDateNaissance().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                dobValue = new Label(dob.toString());
            } else {
                dobValue = new Label("N/A");
            }
            detailsGrid.add(dobLbl, 0, row);
            detailsGrid.add(dobValue, 1, row++);

            // Date d'embauche
            Label embLbl = new Label("Date d'embauche:");
            embLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label embValue;
            if (conducteur.getDateEmbauche() != null) {
                LocalDate emb = conducteur.getDateEmbauche().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                embValue = new Label(emb.toString());
            } else {
                embValue = new Label("N/A");
            }
            detailsGrid.add(embLbl, 0, row);
            detailsGrid.add(embValue, 1, row++);

            // Numéro de permis
            Label permisLbl = new Label("Permis de conduire:");
            permisLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label permisValue = new Label(conducteur.getNumeroPermis());
            detailsGrid.add(permisLbl, 0, row);
            detailsGrid.add(permisValue, 1, row++);


            // Statut
            Label statutLbl = new Label("Statut:");
            statutLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label statutValue = new Label(conducteur.getStatut() != null ? conducteur.getStatut() : "Actif");
            detailsGrid.add(statutLbl, 0, row);
            detailsGrid.add(statutValue, 1, row++);

            // Véhicule assigné
            Label vehiculeLbl = new Label("Véhicule assigné:");
            vehiculeLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label vehiculeValue;
            if (conducteur.getVehiculeId() > 0) {
                Vehicule vehicule = vehiculeService.afficher(conducteur.getVehiculeId());
                if (vehicule != null) {
                    vehiculeValue = new Label(vehicule.getMarque() + " " + vehicule.getModele() + 
                                             " (" + vehicule.getImmatriculation() + ")");
                } else {
                    vehiculeValue = new Label("N/A");
                }
            } else {
                vehiculeValue = new Label("Non assigné");
            }
            detailsGrid.add(vehiculeLbl, 0, row);
            detailsGrid.add(vehiculeValue, 1, row++);

            // Contact info
            Label telLbl = new Label("Téléphone:");
            telLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label telValue = new Label(conducteur.getTelephone() != null ? conducteur.getTelephone() : "N/A");
            detailsGrid.add(telLbl, 0, row);
            detailsGrid.add(telValue, 1, row++);

            Label emailLbl = new Label("Email:");
            emailLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label emailValue = new Label(conducteur.getEmail() != null ? conducteur.getEmail() : "N/A");
            detailsGrid.add(emailLbl, 0, row);
            detailsGrid.add(emailValue, 1, row++);

            Label adresseLbl = new Label("Adresse:");
            adresseLbl.setFont(Font.font("System", FontWeight.BOLD, 14));
            Label adresseValue = new Label(conducteur.getAdresse() != null ? conducteur.getAdresse() : "N/A");
            adresseValue.setWrapText(true);
            detailsGrid.add(adresseLbl, 0, row);
            detailsGrid.add(adresseValue, 1, row++);

            // Buttons
            Button closeButton = new Button("Fermer");
            closeButton.setOnAction(e -> detailStage.close());

            Button assignButton = new Button("Assigner un véhicule");
            assignButton.getStyleClass().add("button-primary");
            assignButton.setOnAction(e -> {
                showVehiculeAssignmentDialog(conducteur);
                detailStage.close();
            });

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(assignButton, closeButton);

            // Layout
            VBox mainLayout = new VBox(20);
            mainLayout.getChildren().addAll(headerBox, detailsGrid, buttonBox);

            detailPane.setCenter(mainLayout);
            detailPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene scene = new Scene(detailPane, 500, 600);
            detailStage.setScene(scene);
            detailStage.showAndWait();
        } catch (Exception e) {
            log.error("Error showing conductor details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails du conducteur.");
        }
    }

    private void showVehiculeAssignmentDialog(Conducteur conducteur) {
        try {
            // Get vehicles for this organization
            List<Vehicule> organisationVehicles = vehiculeService.afficher_tout().stream()
                .filter(v -> v.getOrganisationId() == organisation.getId())
                .collect(Collectors.toList());

            if (organisationVehicles.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun véhicule disponible pour cette organisation.");
                return;
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Assigner un véhicule");

            VBox dialogVbox = new VBox(15);
            dialogVbox.setPadding(new Insets(20));

            Label titleLabel = new Label("Assigner un véhicule à " + conducteur.getNom() + " " + conducteur.getPrenom());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

            // Combobox to select vehicle
            ComboBox<Vehicule> vehiculeCombo = new ComboBox<>();

            // Add unassign option
            Vehicule unassignOption = new Vehicule();
            unassignOption.setId(-1);
            unassignOption.setMarque("-- Non assigné --");
            vehiculeCombo.getItems().add(unassignOption);

            // Add available vehicles
            for (Vehicule v : organisationVehicles) {
                // Check if vehicle is already assigned to another driver
                boolean alreadyAssigned = false;

                for (Conducteur c : conducteurService.afficher_tout()) {
                    if (c.getId() != conducteur.getId() && c.getVehiculeId() == v.getId()) {
                        alreadyAssigned = true;
                        break;
                    }
                }

                if (!alreadyAssigned) {
                    vehiculeCombo.getItems().add(v);
                }
            }

            // Custom cell factory to display vehicle info
            vehiculeCombo.setCellFactory(param -> new ListCell<Vehicule>() {
                @Override
                protected void updateItem(Vehicule item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        if (item.getId() == -1) {
                            setText("-- Non assigné --");
                        } else {
                            setText(item.getMarque() + " " + item.getModele() + " (" + item.getImmatriculation() + ")");
                        }
                    }
                }
            });

            // Set current vehicle if any
            if (conducteur.getVehiculeId() > 0) {
                for (Vehicule v : vehiculeCombo.getItems()) {
                    if (v.getId() == conducteur.getVehiculeId()) {
                        vehiculeCombo.setValue(v);
                        break;
                    }
                }
            } else {
                vehiculeCombo.setValue(unassignOption);
            }

            // Buttons
            Button saveButton = new Button("Enregistrer");
            saveButton.getStyleClass().add("button-primary");
            saveButton.setOnAction(e -> {
                try {
                    Vehicule selectedVehicle = vehiculeCombo.getValue();

                    if (selectedVehicle != null) {
                        if (selectedVehicle.getId() == -1) {
                            // Unassign
                            conducteur.setVehiculeId(0);
                        } else {
                            // Assign to new vehicle
                            conducteur.setVehiculeId(selectedVehicle.getId());
                        }

                        conducteurService.modifier(conducteur);
                        // Send vehicle assignment email if assigned
                        if (selectedVehicle.getId() != -1 && conducteur.getEmail() != null && !conducteur.getEmail().isEmpty()) {
                            String subject = "Véhicule assigné - TuniTransport";
                            String html = emailService.buildVehicleAssignmentEmailContent(
                                conducteur.getPrenom(), conducteur.getNom(),
                                selectedVehicle.getMarque() + " " + selectedVehicle.getModele(),
                                selectedVehicle.getImmatriculation()
                            );
                            emailService.sendGenericHtmlEmail(conducteur.getEmail(), subject, html);
                        }
                        dialogStage.close();
                        loadConducteurs();
                    }
                } catch (Exception ex) {
                    log.error("Error assigning vehicle", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'assigner le véhicule.");
                }
            });

            Button cancelButton = new Button("Annuler");
            cancelButton.setOnAction(e -> dialogStage.close());

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);

            dialogVbox.getChildren().addAll(titleLabel, vehiculeCombo, buttonBox);
            dialogVbox.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene dialogScene = new Scene(dialogVbox, 400, 150);
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing vehicle assignment dialog", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le dialogue d'assignation de véhicule.");
        }
    }

    private void showConducteurForm(Conducteur conducteurToEdit) {
        try {
            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);

            String title = conducteurToEdit == null ? "Ajouter un conducteur" : "Modifier le conducteur";
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
            photoBox.setAlignment(Pos.CENTER);

            ImageView photoView = new ImageView();
            photoView.setFitHeight(150);
            photoView.setFitWidth(150);
            photoView.setPreserveRatio(true);

            // Try to load driver's photo if available
            String initialPhotoPath;
            if (conducteurToEdit != null && conducteurToEdit.getPhoto() != null) {
                initialPhotoPath = conducteurToEdit.getPhoto();
                try {
                    File photoFile = new File(initialPhotoPath);
                    if (photoFile.exists()) {
                        Image photo = new Image(photoFile.toURI().toString());
                        photoView.setImage(photo);
                    } else {
                        // Load default photo
                        Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                        photoView.setImage(defaultPhoto);
                    }
                } catch (Exception e) {
                    log.warn("Could not load driver photo", e);
                    Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                    photoView.setImage(defaultPhoto);
                }
            } else {
                initialPhotoPath = null;
                // Load default photo
                Image defaultPhoto = new Image(getClass().getResourceAsStream("/Images/Drivers/default_driver.png"));
                photoView.setImage(defaultPhoto);
            }

            Button photoButton = new Button("Changer la photo");
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

            // Personal information
            Label nomLabel = new Label("Nom *");
            TextField nomField = new TextField();
            formGrid.add(nomLabel, 0, row);
            formGrid.add(nomField, 1, row++);

            Label prenomLabel = new Label("Prénom *");
            TextField prenomField = new TextField();
            formGrid.add(prenomLabel, 0, row);
            formGrid.add(prenomField, 1, row++);

            Label cinLabel = new Label("CIN *");
            TextField cinField = new TextField();
            formGrid.add(cinLabel, 0, row);
            formGrid.add(cinField, 1, row++);

            Label dateNaissanceLabel = new Label("Date de naissance *");
            DatePicker dateNaissancePicker = new DatePicker();
            formGrid.add(dateNaissanceLabel, 0, row);
            formGrid.add(dateNaissancePicker, 1, row++);

            Label dateEmbaucheLabel = new Label("Date d'embauche");
            DatePicker dateEmbauchePicker = new DatePicker();
            formGrid.add(dateEmbaucheLabel, 0, row);
            formGrid.add(dateEmbauchePicker, 1, row++);

            Label permisLabel = new Label("Numéro de permis *");
            TextField permisField = new TextField();
            formGrid.add(permisLabel, 0, row);
            formGrid.add(permisField, 1, row++);


            Label statutLabel = new Label("Statut");
            ComboBox<String> statutComboBox = new ComboBox<>();
            statutComboBox.getItems().addAll("Actif", "En congé", "Suspendu", "Retraité", "Autre");
            statutComboBox.setValue("Actif");
            formGrid.add(statutLabel, 0, row);
            formGrid.add(statutComboBox, 1, row++);

            // Contact information
            Label telephoneLabel = new Label("Téléphone");
            TextField telephoneField = new TextField();
            formGrid.add(telephoneLabel, 0, row);
            formGrid.add(telephoneField, 1, row++);

            Label emailLabel = new Label("Email");
            TextField emailField = new TextField();
            formGrid.add(emailLabel, 0, row);
            formGrid.add(emailField, 1, row++);

            Label adresseLabel = new Label("Adresse");
            TextArea adresseArea = new TextArea();
            adresseArea.setPrefRowCount(3);
            adresseArea.setWrapText(true);
            formGrid.add(adresseLabel, 0, row);
            formGrid.add(adresseArea, 1, row++);

            // Fill form with data if editing
            if (conducteurToEdit != null) {
                nomField.setText(conducteurToEdit.getNom());
                prenomField.setText(conducteurToEdit.getPrenom());
                cinField.setText(conducteurToEdit.getCin());
                permisField.setText(conducteurToEdit.getNumeroPermis());

                if (conducteurToEdit.getDateNaissance() != null) {
                    dateNaissancePicker.setValue(conducteurToEdit.getDateNaissance().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
                }

                if (conducteurToEdit.getDateEmbauche() != null) {
                    dateEmbauchePicker.setValue(conducteurToEdit.getDateEmbauche().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate());
                }


                if (conducteurToEdit.getStatut() != null) {
                    statutComboBox.setValue(conducteurToEdit.getStatut());
                }

                if (conducteurToEdit.getTelephone() != null) {
                    telephoneField.setText(conducteurToEdit.getTelephone());
                }

                if (conducteurToEdit.getEmail() != null) {
                    emailField.setText(conducteurToEdit.getEmail());
                }

                if (conducteurToEdit.getAdresse() != null) {
                    adresseArea.setText(conducteurToEdit.getAdresse());
                }
            }

            // Buttons
            Button saveButton = new Button(conducteurToEdit == null ? "Ajouter" : "Enregistrer");
            saveButton.getStyleClass().add("button-primary");

            Button cancelButton = new Button("Annuler");
            cancelButton.getStyleClass().add("button-secondary");

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);

            formGrid.add(buttonBox, 1, row);

            // Event handlers
            saveButton.setOnAction(e -> {
                if (nomField.getText().trim().isEmpty() ||
                    prenomField.getText().trim().isEmpty() ||
                    cinField.getText().trim().isEmpty() ||
                    permisField.getText().trim().isEmpty() ||
                    dateNaissancePicker.getValue() == null) {

                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                    return;
                }

                try {
                    // Create or update driver
                    Conducteur conducteur = conducteurToEdit == null ? new Conducteur() : conducteurToEdit;

                    conducteur.setNom(nomField.getText().trim());
                    conducteur.setPrenom(prenomField.getText().trim());
                    conducteur.setCin(cinField.getText().trim());
                    conducteur.setNumeroPermis(permisField.getText().trim());

                    // Convert DatePicker date to java.util.Date
                    if (dateNaissancePicker.getValue() != null) {
                        Date birthDate = Date.from(dateNaissancePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        conducteur.setDateNaissance(birthDate);
                    }

                    if (dateEmbauchePicker.getValue() != null) {
                        Date embaucheDate = Date.from(dateEmbauchePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                        conducteur.setDateEmbauche(embaucheDate);
                    }

                    conducteur.setTelephone(telephoneField.getText().trim());
                    conducteur.setEmail(emailField.getText().trim());
                    conducteur.setAdresse(adresseArea.getText().trim());
                    conducteur.setStatut(statutComboBox.getValue());


                    // Handle photo
                    String savedPhotoPath = null;
                    if (photoView.getUserData() != null) {
                        // New photo selected, save it
                        String sourcePhotoPath = (String) photoView.getUserData();
                        savedPhotoPath = saveDriverPhoto(sourcePhotoPath, conducteurToEdit);
                    } else if (initialPhotoPath != null) {
                        // Keep existing photo
                        savedPhotoPath = initialPhotoPath;
                    }

                    conducteur.setPhoto(savedPhotoPath);

                    if (conducteurToEdit == null) {
                        // For new driver, set vehiculeId to 0 (unassigned) and set the current organisation
                        conducteur.setVehiculeId(0);
                        conducteur.setOrganisationId(organisation.getId());
                        conducteurService.ajouter(conducteur);
                        // Send hiring email
                        if (conducteur.getEmail() != null && !conducteur.getEmail().isEmpty()) {
                            String subject = "Bienvenue chez TuniTransport !";
                            String html = emailService.buildHiringEmailContent(conducteur.getPrenom(), conducteur.getNom());
                            emailService.sendGenericHtmlEmail(conducteur.getEmail(), subject, html);
                        }
                    } else {
                        // Keep existing vehicle assignment
                        conducteurService.modifier(conducteur);
                    }

                    formStage.close();
                    loadConducteurs();
                } catch (Exception ex) {
                    log.error("Error saving driver", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement du conducteur.");
                }
            });

            cancelButton.setOnAction(e -> formStage.close());

            // Layout
            BorderPane photoAndFormPane = new BorderPane();
            photoAndFormPane.setLeft(photoBox);
            photoAndFormPane.setCenter(formGrid);

            mainPane.setCenter(photoAndFormPane);

            // Set scene and show
            mainPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            Scene scene = new Scene(mainPane, 600, 700);
            formStage.setScene(scene);
            formStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing driver form", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le formulaire.");
        }
    }

    private String saveDriverPhoto(String sourcePhotoPath, Conducteur existingDriver) {
        try {
            // Create photos directory if it doesn't exist
            Path photosDir = Paths.get("src/main/resources/Images/drivers");
            if (!Files.exists(photosDir)) {
                Files.createDirectories(photosDir);
            }

            // Generate a unique filename
            String fileName;
            if (existingDriver != null && existingDriver.getCin() != null) {
                // Use CIN for existing driver
                fileName = "driver_" + existingDriver.getCin() + "_" + UUID.randomUUID().toString().substring(0, 8)
                        + getFileExtension(sourcePhotoPath);
            } else {
                // Use UUID for new driver
                fileName = "driver_" + UUID.randomUUID().toString().substring(0, 8) + getFileExtension(sourcePhotoPath);
            }

            Path targetPath = photosDir.resolve(fileName);

            // Copy file to resources directory
            Files.copy(Paths.get(sourcePhotoPath), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            log.error("Error saving driver photo", e);
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

    private void deleteConducteur(Conducteur conducteur) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le conducteur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + 
            conducteur.getNom() + " " + conducteur.getPrenom() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Send email notification to the driver if email is available
                if (conducteur.getEmail() != null && !conducteur.getEmail().isEmpty()) {
                    try {
                        String subject = "Fin de contrat - TuniTransport";
                        String html = emailService.buildFiringEmailContent(
                            conducteur.getPrenom(), conducteur.getNom(),
                            organisation != null ? organisation.getNom() : "notre organisation"
                        );
                        emailService.sendGenericHtmlEmail(conducteur.getEmail(), subject, html);
                        log.info("Email de notification envoyé à {}", conducteur.getEmail());
                    } catch (Exception emailEx) {
                        log.error("Erreur lors de l'envoi de l'email de notification", emailEx);
                        // Continue with deletion even if email fails
                    }
                }

                // Delete the driver
                conducteurService.supprimer(conducteur);
                loadConducteurs();

                // Show success message including email notification status
                if (conducteur.getEmail() != null && !conducteur.getEmail().isEmpty()) {
                    showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", 
                        "Le conducteur a été supprimé et une notification a été envoyée à " + conducteur.getEmail());
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Suppression réussie", 
                        "Le conducteur a été supprimé. Aucune notification n'a été envoyée (email non disponible).");
                }
            } catch (Exception e) {
                log.error("Error deleting driver", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression du conducteur.");
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
