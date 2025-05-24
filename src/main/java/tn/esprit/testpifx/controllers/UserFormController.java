package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.CountryDataInitializer;
import tn.esprit.testpifx.utils.ImageManager;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UserFormController {
    @FXML private Label titleLabel;
    @FXML private ImageView profileImageView;
    @FXML private Button uploadButton;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthdatePicker;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private Label regionLabel;
    @FXML private ComboBox<String> regionComboBox;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private UserService userService;
    private TeamService teamService;
    private User userToEdit;
    private User currentUser; // Add current user field
    private Stage stage;
    private String profilePicturePath;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        if (user != null) {
            titleLabel.setText("Edit User");
            usernameField.setText(user.getUsername());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            birthdatePicker.setValue(user.getBirthdate());

            // Handle roles from Set
            if (!user.getRoles().isEmpty()) {
                roleComboBox.setValue(user.getRoles().iterator().next().name());
            }

            // Set country and handle governorate if applicable
            if (user.getCountry() != null && !user.getCountry().isEmpty()) {
                countryComboBox.setValue(user.getCountry());
                
                // Check if Tunisia is selected and handle governorate region
                if ("Tunisia".equals(user.getCountry())) {
                    regionLabel.setVisible(true);
                    regionComboBox.setVisible(true);
                    if (user.getRegion() != null && !user.getRegion().isEmpty()) {
                        regionComboBox.setValue(user.getRegion());
                    }
                }
            }
            
            genderComboBox.setValue(user.getGender());
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                profileImageView.setImage(new Image(user.getProfilePictureUrl()));
            }
            
            // Make sure the save button is labeled correctly for editing
            saveButton.setText("Update User");
        } else {
            titleLabel.setText("Add User");
            saveButton.setText("Register");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        // Initialize dropdown options
        roleComboBox.getItems().addAll(Arrays.asList("ADMIN", "MANAGER", "USER"));
        genderComboBox.getItems().addAll(Arrays.asList("MALE", "FEMALE", "OTHER"));
        
        // Initialize country dropdown using CountryDataInitializer
        List<String> countries = CountryDataInitializer.getCountries();
        countryComboBox.getItems().addAll(countries);
        
        // Initialize region/governorate dropdown for Tunisia
        List<String> tunisianGovernorates = CountryDataInitializer.getTunisianGovernorates();
        regionComboBox.getItems().addAll(tunisianGovernorates);
        
        // Set default role if not editing
        if (roleComboBox.getValue() == null) {
            roleComboBox.setValue("USER");
        }
        
        // Set default gender if not set
        if (genderComboBox.getValue() == null) {
            genderComboBox.setValue("MALE");
        }
        
        // Initialize any empty ComboBoxes with default values for clarity
        if (countryComboBox.getValue() == null) {
            // Just set the prompt text, don't select a default value
            countryComboBox.setPromptText("Select Country");
        }
        
        // Ensure proper visibility of all form elements
        regionLabel.setVisible(false);
        regionComboBox.setVisible(false);
        
        // Set a reasonable default max date for birthdate (today)
        birthdatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.isAfter(today));
            }
        });
        
        // Ensure buttons are visible and properly styled
        if (saveButton != null) {
            saveButton.setVisible(true);
            saveButton.getStyleClass().add("primary-button");
        }
        
        if (cancelButton != null) {
            cancelButton.setVisible(true);
            cancelButton.getStyleClass().add("secondary-button");
        }
        
        // Set default image if none provided
        if (profileImageView != null && profileImageView.getImage() == null) {
            profileImageView.setImage(new Image(Utils.DEFAULT_PROFILE_PICTURE_URL));
        }
        
        // Apply CSS for ComboBox to ensure text is visible
        roleComboBox.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        genderComboBox.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        countryComboBox.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
        regionComboBox.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray;");
    }
    
    @FXML
    private void handleCountryChange() {
        String selectedCountry = countryComboBox.getValue();
        if ("Tunisia".equals(selectedCountry)) {
            // Show governorate selection for Tunisia
            regionLabel.setVisible(true);
            regionComboBox.setVisible(true);
            regionLabel.setText("Governorate:");
        } else {
            // Hide governorate selection for other countries
            regionLabel.setVisible(false);
            regionComboBox.setVisible(false);
            regionComboBox.setValue(null);
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // Use ImageManager to save the image to the application directory
                profilePicturePath = ImageManager.saveProfileImage(selectedFile.getAbsolutePath());
                
                // Create an image object from the absolute path for display
                String absolutePath = ImageManager.getAbsolutePath(profilePicturePath);
                profileImageView.setImage(new Image(new File(absolutePath).toURI().toString()));
                
            } catch (IOException e) {
                Utils.showErrorAlert("Error", "Failed to save profile image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            User user = userToEdit != null ? userToEdit : new User();
            user.setUsername(usernameField.getText());
            user.setFirstName(firstNameField.getText());
            user.setLastName(lastNameField.getText());
            user.setEmail(emailField.getText());
            user.setBirthdate(birthdatePicker.getValue());
              boolean passwordChanged = false;
            if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
                String newPassword = passwordField.getText();
                user.setPassword(newPassword);
                passwordChanged = true;
            }

            // Updated line - use the correct enum reference
            user.setRoles(Set.of(tn.esprit.testpifx.models.Role.valueOf(roleComboBox.getValue())));

            user.setCountry(countryComboBox.getValue());
            user.setGender(genderComboBox.getValue());
            
            // Save region/governorate if applicable
            if ("Tunisia".equals(countryComboBox.getValue()) && regionComboBox.getValue() != null) {
                user.setRegion(regionComboBox.getValue());
            } else {
                user.setRegion("");
            }
            
            // Set country prefix based on selected country
            if (countryComboBox.getValue() != null) {
                user.setCountryPrefix(CountryDataInitializer.getCountryPrefix(countryComboBox.getValue()));
            }
            
            if (profilePicturePath != null) {
                user.setProfilePictureUrl(profilePicturePath);
            }

            if (userToEdit != null) {
                userService.updateUser(user);
            } else {                userService.createUser(user);
            }
            
            // Update remembered credentials if this is the current logged-in user and the username or password changed
            if (currentUser != null && currentUser.getUserId() != null && 
                currentUser.getUserId().equals(user.getUserId()) && 
                UserPreferences.hasRememberedCredentials()) {
                
                String rememberedUsername = UserPreferences.getUsername();
                
                // If this is the current remembered user, update the credentials
                if (rememberedUsername.equals(user.getUsername()) || rememberedUsername.equals(userToEdit.getUsername())) {
                    UserPreferences.saveLoginCredentials(
                        user.getUsername(), 
                        passwordChanged ? passwordField.getText() : UserPreferences.getPassword(),
                        true
                    );
                }
            }

            handleBack();
        } catch (Exception e) {
            Utils.showErrorAlert("Error", "Failed to save user: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        navigateBack();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/user_management.fxml"));
            Parent root = loader.load();

            UserManagementController controller = loader.getController();
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

            Stage stage = this.stage != null ? this.stage : (Stage) cancelButton.getScene().getWindow();
            stage.setScene(scene);
            
            // Use Platform.runLater to ensure window state changes are applied after scene transition
            javafx.application.Platform.runLater(() -> {
                Utils.setFullScreenMode(stage);
                stage.setIconified(false); // Ensure window is not minimized
                stage.requestFocus(); // Request focus to prevent minimizing
                stage.show();
            });
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to navigate back to user management: " + e.getMessage());
        }
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/user_management.fxml"));
            Parent root = loader.load();

            UserManagementController controller = loader.getController();
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            
            // Pass the current user to ensure UI is properly initialized
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

            stage.setScene(scene);
            Utils.setFullScreenMode(stage); // Ensure consistent full-screen behavior
            stage.show();
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to return to user management");
        }
    }

    private boolean validateForm() {
        // Always require username and email
        if (usernameField.getText() == null || usernameField.getText().isEmpty()) {
            Utils.showErrorAlert("Validation Error", "Username is required");
            return false;
        }
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            Utils.showErrorAlert("Validation Error", "Email is required");
            return false;
        }
        
        // Password is only required for new users
        if (userToEdit == null && (passwordField.getText() == null || passwordField.getText().isEmpty())) {
            Utils.showErrorAlert("Validation Error", "Password is required for new users");
            return false;
        }
        
        // If password is provided, make sure it matches confirmation
        if (passwordField.getText() != null && !passwordField.getText().isEmpty() && 
            !passwordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("Validation Error", "Passwords do not match");
            return false;
        }
        
        // Role is always required
        if (roleComboBox.getValue() == null) {
            Utils.showErrorAlert("Validation Error", "Role is required");
            return false;
        }
        
        // For existing users, country and gender can be left as they were
        if (userToEdit == null) {
            // Country is required only for new users
            if (countryComboBox.getValue() == null || countryComboBox.getValue().isEmpty() 
                    || countryComboBox.getValue().equals("Select a country")) {
                Utils.showErrorAlert("Validation Error", "Country is required for new users");
                return false;
            }
            
            // Governorate is required for Tunisia
            if ("Tunisia".equals(countryComboBox.getValue()) && 
                (regionComboBox.getValue() == null || regionComboBox.getValue().equals("Select a governorate"))) {
                Utils.showErrorAlert("Validation Error", "Governorate is required for Tunisian users");
                return false;
            }
            
            // Gender is required only for new users
            if (genderComboBox.getValue() == null) {
                Utils.showErrorAlert("Validation Error", "Gender is required for new users");
                return false;
            }
        }
        
        return true;
    }
}