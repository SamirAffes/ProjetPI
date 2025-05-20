package controllers;

import entities.OrgType;
import entities.Organisation;
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
import org.mindrot.jbcrypt.BCrypt;
import services.EmailService;
import services.OrganisationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

@Slf4j
public class OrganisationManagementController {

    @FXML
    private FlowPane organisationsContainer;

    @FXML
    private Button addButton;

    private final OrganisationService organisationService = new OrganisationService();
    private final EmailService emailService = new EmailService();

    // Directory to store organization logos
    private static final String LOGOS_DIRECTORY = "src/main/resources/Images/Logos/";

    // Characters used for password generation
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";

    @FXML
    public void initialize() {
        // Create logos directory if it doesn't exist
        createLogosDirectory();
        loadOrganisations();
    }

    private void createLogosDirectory() {
        File directory = new File(LOGOS_DIRECTORY);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created logos directory: {}", LOGOS_DIRECTORY);
            } else {
                log.error("Failed to create logos directory: {}", LOGOS_DIRECTORY);
            }
        }
    }

    @FXML
    public void onAddButtonClicked() {
        showOrganisationForm(null);

    }

    private void loadOrganisations() {
        organisationsContainer.getChildren().clear();

        List<Organisation> organisations = organisationService.afficher_tout();

        if (organisations.isEmpty()) {
            Label emptyLabel = new Label("Aucune organisation trouvée");
            emptyLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
            organisationsContainer.getChildren().add(emptyLabel);
        } else {
            for (Organisation organisation : organisations) {
                organisationsContainer.getChildren().add(createOrganisationCard(organisation));
            }
        }
    }

    private VBox createOrganisationCard(Organisation organisation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMaxWidth(300);
        card.setMinWidth(300);
        card.setPrefWidth(300);
        card.setPrefHeight(220);

        // Create a layout with logo on the left and basic info on the right
        HBox topContent = new HBox(10);

        // Logo area
        StackPane logoContainer = new StackPane();
        logoContainer.setMinWidth(80);
        logoContainer.setMaxWidth(80);
        logoContainer.setPrefWidth(80);
        logoContainer.setMinHeight(80);
        logoContainer.setMaxHeight(80);
        logoContainer.setPrefHeight(80);
        logoContainer.getStyleClass().add("logo-container");

        ImageView logoView = new ImageView();
        logoView.setFitWidth(70);
        logoView.setFitHeight(70);
        logoView.setPreserveRatio(true);

        // Try to load the logo image
        if (organisation.getLogo() != null && !organisation.getLogo().isEmpty()) {
            try {
                File logoFile = new File(organisation.getLogo());
                if (logoFile.exists()) {
                    Image logoImage = new Image(logoFile.toURI().toString());
                    logoView.setImage(logoImage);
                } else {
                    // Use a default image if the logo file doesn't exist
                    logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
                }
            } catch (Exception e) {
                log.error("Error loading logo for organization: {}", organisation.getNom(), e);
                // Use a default image in case of error
                logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
            }
        } else {
            // Use a default image if no logo is set
            logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
        }

        logoContainer.getChildren().add(logoView);

        // Organisation details
        VBox detailsContainer = new VBox(5);
        detailsContainer.setAlignment(Pos.TOP_LEFT);

        // Organisation name
        Label nameLabel = new Label(organisation.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setWrapText(true);

        // Organisation type
        Label typeLabel = new Label("Type: " + organisation.getType());

        // Organisation address
        Label addressLabel = new Label(organisation.getAdresse());
        addressLabel.setWrapText(true);

        // Contact info
        Label contactLabel = new Label("Tel: " + organisation.getTelephone());

        detailsContainer.getChildren().addAll(nameLabel, typeLabel, addressLabel, contactLabel);

        topContent.getChildren().addAll(logoContainer, detailsContainer);

        // Add some spacing for better appearance
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

// Detail button with eye icon
        Button detailBtn = new Button();
        detailBtn.getStyleClass().add("detail-button");
        FontIcon eyeIcon = new FontIcon("fas-eye");
        eyeIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        detailBtn.setGraphic(eyeIcon);
        detailBtn.setTooltip(new Tooltip("Détails"));
        detailBtn.setOnAction(e -> showOrganisationDetails(organisation));

// Edit button with edit icon
        Button editBtn = new Button();
        editBtn.getStyleClass().add("edit-button");
        FontIcon editIcon = new FontIcon("fas-edit");
        editIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        editBtn.setGraphic(editIcon);
        editBtn.setTooltip(new Tooltip("Modifier"));
        editBtn.setOnAction(e -> showOrganisationForm(organisation));

// Delete button with trash icon
        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("delete-button");
        FontIcon trashIcon = new FontIcon("fas-trash-alt");
        trashIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setTooltip(new Tooltip("Supprimer"));
        deleteBtn.setOnAction(e -> deleteOrganisation(organisation));

        buttonBox.getChildren().addAll(detailBtn, editBtn, deleteBtn);

        // Add all elements to the card
        card.getChildren().addAll(topContent, spacer, buttonBox);
        card.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

        return card;
    }

    private void showOrganisationDetails(Organisation organisation) {
        try {
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Détails de l'organisation");

            BorderPane detailsPane = new BorderPane();
            detailsPane.setPadding(new Insets(20));

            VBox detailsBox = new VBox(15);
            detailsBox.setAlignment(Pos.TOP_LEFT);

            // Add logo at the top
            ImageView logoView = new ImageView();
            logoView.setFitWidth(100);
            logoView.setFitHeight(100);
            logoView.setPreserveRatio(true);

            // Try to load the logo image
            if (organisation.getLogo() != null && !organisation.getLogo().isEmpty()) {
                try {
                    File logoFile = new File(organisation.getLogo());
                    if (logoFile.exists()) {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoView.setImage(logoImage);
                    } else {
                        logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
                    }
                } catch (Exception e) {
                    log.error("Error loading logo for organization details: {}", organisation.getNom(), e);
                    logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
                }
            } else {
                logoView.setImage(new Image(getClass().getResourceAsStream("/Images/Logos/default_logo.svg")));
            }

            // Add organisation details
            Label titleLabel = new Label(organisation.getNom());
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

            // Format creation date
            String dateStr = "N/A";
            if (organisation.getDateCreation() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dateStr = sdf.format(organisation.getDateCreation());
            }

            // Create labels for all organisation details
            Label typeLabel = new Label("Type: " + organisation.getType());
            Label addressLabel = new Label("Adresse: " + organisation.getAdresse());
            Label phoneLabel = new Label("Téléphone: " + organisation.getTelephone());
            Label emailLabel = new Label("Email: " + organisation.getEmail());
            Label websiteLabel = new Label("Site Web: " + (organisation.getSiteWeb() != null ? organisation.getSiteWeb() : "N/A"));
            Label creationDateLabel = new Label("Date de création: " + dateStr);
            Label driverCountLabel = new Label("Nombre de conducteurs: " + organisation.getNombreConducteurs());
            Label fleetSizeLabel = new Label("Taille de la flotte: " + organisation.getTailleFlotte());

            Button closeButton = new Button("Fermer");
            closeButton.getStyleClass().add("button-primary");
            closeButton.setPrefWidth(100);
            closeButton.setOnAction(e -> detailStage.close());

            HBox buttonBox = new HBox();
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().add(closeButton);

            // Add logo to top center
            HBox logoBox = new HBox();
            logoBox.setAlignment(Pos.CENTER);
            logoBox.getChildren().add(logoView);

            // Add all elements to the details box
            detailsBox.getChildren().addAll(
                    titleLabel,
                    typeLabel,
                    addressLabel,
                    phoneLabel,
                    emailLabel,
                    websiteLabel,
                    creationDateLabel,
                    driverCountLabel,
                    fleetSizeLabel,
                    buttonBox
            );

            detailsPane.setTop(logoBox);
            detailsPane.setCenter(detailsBox);

            detailsPane.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());

            Scene scene = new Scene(detailsPane, 450, 500);
            detailStage.setScene(scene);
            detailStage.showAndWait();

        } catch (Exception e) {
            log.error("Error showing organisation details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails de l'organisation.");
        }
    }

    private void showOrganisationForm(Organisation organisationToEdit) {
        try {
            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);

            String title = organisationToEdit == null ? "Ajouter une organisation" : "Modifier l'organisation";
            formStage.setTitle(title);

            BorderPane mainPane = new BorderPane();

            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(20));

            // Add form fields
            Label nameLabel = new Label("Nom: *");
            nameLabel.setStyle("-fx-text-fill: #333333;");
            TextField nameField = new TextField();
            nameField.setPrefWidth(300);

            Label typeLabel = new Label("Type:");
            ComboBox<OrgType> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(OrgType.values());

            Label addressLabel = new Label("Adresse: *");
            addressLabel.setStyle("-fx-text-fill: #333333;");
            TextField addressField = new TextField();

            Label phoneLabel = new Label("Téléphone: *");
            phoneLabel.setStyle("-fx-text-fill: #333333;");
            TextField phoneField = new TextField();

            Label emailLabel = new Label("Email: *");
            emailLabel.setStyle("-fx-text-fill: #333333;");
            TextField emailField = new TextField();

            Label websiteLabel = new Label("Site Web:");
            TextField websiteField = new TextField();

            Label driversLabel = new Label("Nombre de conducteurs:");
            Spinner<Integer> driversSpinner = new Spinner<>(0, 1000, 0);

            Label fleetLabel = new Label("Taille de la flotte:");
            Spinner<Integer> fleetSpinner = new Spinner<>(0, 1000, 0);

            // Logo selection
            Label logoLabel = new Label("Logo:");

            HBox logoBox = new HBox(10);
            logoBox.setAlignment(Pos.CENTER_LEFT);

            TextField logoPathField = new TextField();
            logoPathField.setEditable(false);
            logoPathField.setPrefWidth(230);

            Button browseButton = new Button("Parcourir...");

            // Image preview
            ImageView logoPreview = new ImageView();
            logoPreview.setFitWidth(80);
            logoPreview.setFitHeight(80);
            logoPreview.setPreserveRatio(true);

            logoBox.getChildren().addAll(logoPathField, browseButton);

            VBox logoPreviewBox = new VBox(5);
            logoPreviewBox.setAlignment(Pos.CENTER);
            logoPreviewBox.getChildren().add(logoPreview);

            // Set current logo if available
            String logoPath = null;
            if (organisationToEdit != null && organisationToEdit.getLogo() != null) {
                logoPath = organisationToEdit.getLogo();
                logoPathField.setText(logoPath);

                try {
                    File logoFile = new File(logoPath);
                    if (logoFile.exists()) {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoPreview.setImage(logoImage);
                    }
                } catch (Exception e) {
                    log.error("Error loading existing logo: {}", e.getMessage());
                }
            }

            // Final logo path that will be saved
            final String[] selectedLogoPath = {logoPath};

            browseButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Logo Image");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );

                File selectedFile = fileChooser.showOpenDialog(formStage);
                if (selectedFile != null) {
                    try {
                        // Generate unique file name to avoid overwrites
                        String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                        Path targetPath = Paths.get(LOGOS_DIRECTORY + uniqueFileName);

                        // Copy the file to our logos directory
                        Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                        // Update the logo preview
                        Image logoImage = new Image(targetPath.toFile().toURI().toString());
                        logoPreview.setImage(logoImage);

                        // Store the path to save later
                        selectedLogoPath[0] = targetPath.toString();
                        logoPathField.setText(targetPath.toString());

                        log.info("Logo selected and copied to: {}", targetPath);
                    } catch (IOException ex) {
                        log.error("Error copying logo file", ex);
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Impossible de copier le fichier logo. " + ex.getMessage());
                    }
                }
            });

            // Set values if editing
            if (organisationToEdit != null) {
                nameField.setText(organisationToEdit.getNom());
                typeCombo.setValue(organisationToEdit.getType());
                addressField.setText(organisationToEdit.getAdresse());
                phoneField.setText(organisationToEdit.getTelephone());
                emailField.setText(organisationToEdit.getEmail());
                websiteField.setText(organisationToEdit.getSiteWeb());
                driversSpinner.getValueFactory().setValue(organisationToEdit.getNombreConducteurs());
                fleetSpinner.getValueFactory().setValue(organisationToEdit.getTailleFlotte());
            } else {
                typeCombo.setValue(OrgType.PRIVATE);
            }

            // Add elements to grid
            formGrid.add(nameLabel, 0, 0);
            formGrid.add(nameField, 1, 0);

            formGrid.add(typeLabel, 0, 1);
            formGrid.add(typeCombo, 1, 1);

            formGrid.add(addressLabel, 0, 2);
            formGrid.add(addressField, 1, 2);

            formGrid.add(phoneLabel, 0, 3);
            formGrid.add(phoneField, 1, 3);

            formGrid.add(emailLabel, 0, 4);
            formGrid.add(emailField, 1, 4);

            formGrid.add(websiteLabel, 0, 5);
            formGrid.add(websiteField, 1, 5);

            formGrid.add(driversLabel, 0, 6);
            formGrid.add(driversSpinner, 1, 6);

            formGrid.add(fleetLabel, 0, 7);
            formGrid.add(fleetSpinner, 1, 7);

            formGrid.add(logoLabel, 0, 8);
            formGrid.add(logoBox, 1, 8);

            // Add logo preview to the left side of the form
            VBox leftSide = new VBox(10);
            leftSide.setAlignment(Pos.TOP_CENTER);
            leftSide.setPadding(new Insets(20));
            leftSide.getChildren().add(logoPreviewBox);

            // Add required fields note
            Label requiredNote = new Label("* Champs obligatoires");
            requiredNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555; -fx-font-style: italic;");
            formGrid.add(requiredNote, 0, 9, 2, 1);

            // Buttons
            Button saveButton = new Button(organisationToEdit == null ? "Ajouter" : "Enregistrer");
            saveButton.getStyleClass().add("button-primary");

            Button cancelButton = new Button("Annuler");

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.getChildren().addAll(cancelButton, saveButton);

            formGrid.add(buttonBox, 1, 10);

            // Event handlers
            saveButton.setOnAction(e -> {
                if (nameField.getText().trim().isEmpty() ||
                        addressField.getText().trim().isEmpty() ||
                        phoneField.getText().trim().isEmpty() ||
                        emailField.getText().trim().isEmpty()) {

                    showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                    return;
                }

                try {
                    Organisation organisation = organisationToEdit == null ? new Organisation() : organisationToEdit;

                    organisation.setNom(nameField.getText().trim());
                    organisation.setType(typeCombo.getValue());
                    organisation.setAdresse(addressField.getText().trim());
                    organisation.setTelephone(phoneField.getText().trim());
                    organisation.setEmail(emailField.getText().trim());
                    organisation.setSiteWeb(websiteField.getText().trim());
                    organisation.setNombreConducteurs(driversSpinner.getValue());
                    organisation.setTailleFlotte(fleetSpinner.getValue());

                    // Set the logo path if a logo was selected
                    if (selectedLogoPath[0] != null) {
                        organisation.setLogo(selectedLogoPath[0]);
                    }

                    if (organisationToEdit == null) {
                        // Generate a random password for new organisations
                        String password = generateRandomPassword(10);
                        organisation.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
                        organisation.setDateCreation(new Date());
                        organisationService.ajouter(organisation);

                        // Send the password to the organisation's email
                        try {
                            String subject = "Bienvenue sur TuniTransport - Vos informations de connexion";
                            String message = "Bonjour " + organisation.getNom() + ",\n\n" +
                                    "Votre compte organisation a été créé avec succès sur la plateforme TuniTransport.\n\n" +
                                    "Voici vos informations de connexion :\n" +
                                    "Nom d'utilisateur : " + organisation.getNom() + "\n" +
                                    "Mot de passe : " + password + "\n\n" +
                                    "Veuillez conserver ces informations en lieu sûr.\n\n" +
                                    "Cordialement,\n" +
                                    "L'équipe TuniTransport";

                            emailService.sendEmail(organisation.getEmail(), subject, message);
                            log.info("Password sent to organisation email: {}", organisation.getEmail());

                            // Show success message
                            showAlert(Alert.AlertType.INFORMATION, "Succès",
                                    "L'organisation a été créée avec succès. Un email contenant les informations de connexion a été envoyé à " +
                                            organisation.getEmail());
                        } catch (Exception ex) {
                            log.error("Error sending email to organisation", ex);
                            showAlert(Alert.AlertType.WARNING, "Attention",
                                    "L'organisation a été créée, mais l'envoi de l'email a échoué. " +
                                            "Mot de passe généré : " + password);
                        }
                    } else {
                        organisationService.modifier(organisation);
                    }

                    formStage.close();
                    loadOrganisations();
                } catch (Exception ex) {
                    log.error("Error saving organisation", ex);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de l'organisation.");
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
            log.error("Error showing organisation form", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher le formulaire.");
        }
    }

    private void deleteOrganisation(Organisation organisation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer l'organisation");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'organisation '" + organisation.getNom() + "' ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete logo file if it exists
                if (organisation.getLogo() != null && !organisation.getLogo().isEmpty()) {
                    try {
                        File logoFile = new File(organisation.getLogo());
                        if (logoFile.exists() && logoFile.isFile()) {
                            boolean deleted = logoFile.delete();
                            if (deleted) {
                                log.info("Deleted logo file: {}", organisation.getLogo());
                            } else {
                                log.warn("Failed to delete logo file: {}", organisation.getLogo());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error deleting logo file", e);
                    }
                }

                organisationService.supprimer(organisation);
                loadOrganisations();
            } catch (Exception e) {
                log.error("Error deleting organisation", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression de l'organisation.");
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

    /**
     * Generates a random password of the specified length
     *
     * @param length the length of the password to generate
     * @return the generated password
     */
    private String generateRandomPassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(randomIndex));
        }

        return password.toString();
    }
}
