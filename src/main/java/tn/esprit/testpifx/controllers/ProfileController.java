package tn.esprit.testpifx.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.repositories.UserRepositoryFactory;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.CountryDataInitializer;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @FXML private StackPane profileImageView;
    @FXML private Button uploadPhotoButton;
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField countryPrefixField;
    @FXML private DatePicker birthdateField;
    @FXML private ComboBox<String> countryField;
    @FXML private ComboBox<String> regionField;
    @FXML private TextField addressField;
    @FXML private TextField zipCodeField;
    @FXML private ComboBox<String> genderField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button homeButton;
    @FXML private ListView<String> teamListView;
    @FXML private Button editButton;
    
    // Account details UI elements
    @FXML private ProgressBar profileCompletenessBar;
    @FXML private Label profileCompletenessValue;
    @FXML private ProgressBar accountStatusBar;
    @FXML private Label accountStatusValue;
    
    // Role management UI elements
    @FXML private VBox roleManagementContainer;
    @FXML private CheckBox adminRoleCheckbox;
    @FXML private CheckBox managerRoleCheckbox;
    @FXML private CheckBox userRoleCheckbox;
    
    // Teams table UI elements
    @FXML private TableView<TeamTableData> teamsTableView;
    @FXML private TableColumn<TeamTableData, String> teamNameColumn;
    @FXML private TableColumn<TeamTableData, Integer> memberCountColumn;
    @FXML private TableColumn<TeamTableData, String> teamRoleColumn;
    @FXML private TableColumn<TeamTableData, Void> teamActionColumn;
    @FXML private ComboBox<String> teamDisplayLimit;
    @FXML private TextField teamSearchField;
    @FXML private Label teamCountLabel;
    
    // Add a field to store the dashboard menu reference
    @FXML private HBox dashboardMenuLink;

    private boolean editMode = false;

    private User currentUser;
    private UserService userService;
    private TeamService teamService;
    private Stage stage;

    public ProfileController() {
        // Fallback mechanism to ensure UserService is initialized
        if (this.userService == null) {
            this.userService = new UserService(UserRepositoryFactory.createRepository(UserRepositoryFactory.RepositoryType.MYSQL));
            logger.warn("UserService was not injected. Initialized with default MySQL repository.");
        }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        // Load teams data if user is already set
        if (currentUser != null && teamListView != null) {
            loadUserTeams();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    public void initialize() {
        logger.info("Initializing ProfileController");
        if (userService == null) {
            logger.error("UserService is not injected. Profile features will not work.");
            Utils.showErrorAlert("Initialization Error", "UserService was not injected. Profile features will not work.");
            return;
        }
        
        // Set up gender options in the ComboBox
        if (genderField != null) {
            genderField.getItems().addAll("MALE", "FEMALE", "OTHER", "PREFER NOT TO SAY");
        }
        
        // Initialize country and region data
        initializeCountryData();
        
        // Disable fields by default - they should only be editable during edit mode
        setFieldsEditable(false);
        
        // Initialize team table if it exists - critical for showing teams data
        if (teamsTableView != null) {
            logger.info("Initializing teams table view");
            initializeTeamsTable();
        } else {
            logger.warn("Teams table view not found in FXML");
        }
        
        // Add JavaFX platform runLater for UI operations that should happen after everything is loaded
        javafx.application.Platform.runLater(() -> {
            if (currentUser != null && teamService != null) {
                // Load team data
                logger.info("Loading teams data in Platform.runLater");
                loadTeamsTableData();
            }
        });
        
        // Load current user data
        loadUserData();
    }

    private void initializeCountryData() {
        List<String> countries = CountryDataInitializer.getCountries();
        countryField.setItems(FXCollections.observableArrayList(countries));
        
        // Set listener for when a country is selected
        countryField.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.equals("Tunisia")) {
                // Load Tunisian governorates
                List<String> regions = CountryDataInitializer.getTunisianGovernorates();
                regionField.setItems(FXCollections.observableArrayList(regions));
                // Enable the region field
                regionField.setDisable(false);
                
                // If no region is selected yet, select the first one
                if (regionField.getValue() == null || regionField.getValue().isEmpty()) {
                    regionField.setValue(regions.get(0));
                }
                
                logger.info("Loaded {} Tunisian governorates into region dropdown", regions.size());
            } else {
                // For other countries, disable or clear the region dropdown
                regionField.setItems(FXCollections.observableArrayList());
                regionField.setValue(null);
                regionField.setDisable(true);
            }
        });
        
        // If Tunisia is already selected, load the regions immediately
        if (countryField.getValue() != null && countryField.getValue().equals("Tunisia")) {
            List<String> regions = CountryDataInitializer.getTunisianGovernorates();
            regionField.setItems(FXCollections.observableArrayList(regions));
            regionField.setDisable(false);
            logger.info("Pre-loaded Tunisian governorates on initialization");
        }
    }

    private void loadUserData() {
        if (currentUser == null) {
            logger.warn("Cannot load user data: current user is null");
            return;
        }

        logger.info("Loading user data for user: {}", currentUser.getUsername());
        
        // Basic profile information
        if (usernameField != null) usernameField.setText(currentUser.getUsername());
        if (emailField != null) emailField.setText(currentUser.getEmail());
        if (firstNameField != null) firstNameField.setText(currentUser.getFirstName());
        if (lastNameField != null) lastNameField.setText(currentUser.getLastName());
        
        // Contact information
        if (phoneNumberField != null) phoneNumberField.setText(currentUser.getPhoneNumber());
        if (countryPrefixField != null) countryPrefixField.setText(currentUser.getCountryPrefix());
        
        // Location information
        if (countryField != null) countryField.setValue(currentUser.getCountry());
        if (regionField != null) regionField.setValue(currentUser.getRegion());
        if (addressField != null) addressField.setText(currentUser.getAddress());
        if (zipCodeField != null) zipCodeField.setText(currentUser.getZipCode());
        
        // Personal information
        if (genderField != null && currentUser.getGender() != null && !currentUser.getGender().isEmpty()) {
            genderField.setValue(currentUser.getGender());
        }
        
        if (birthdateField != null && currentUser.getBirthdate() != null) {
            birthdateField.setValue(currentUser.getBirthdate());
        }
        
        // Update account details section with dynamic data
        updateAccountDetailsSection();
        
        // Reset edit mode
        editMode = false;
        if (editButton != null) editButton.setText("Edit");
        setFieldsEditable(false);
        
        // Hide dashboard for users with only USER role
        configureDashboardVisibility();
        
        // If profile picture URL is available, try to load it
        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) {
            // Code to load profile picture (not shown for brevity)
        }
        
        // If we get here, either there's no profile picture or loading failed - display initial
        displayUserInitial();
        
        // Initialize and load team data into the TableView
        if (teamsTableView != null) {
            // Make sure table is properly initialized
            if (teamNameColumn != null && memberCountColumn != null && teamRoleColumn != null) {
                initializeTeamsTable();
            } else {
                logger.warn("Team table columns not properly defined in FXML");
            }
        } else {
            logger.warn("Team table view not found in FXML");
            // Fallback to old method for backwards compatibility
            checkTeamMembershipsDirectly();
        }
    }

    private void setFieldsEditable(boolean editable) {
        // Basic fields
        if (usernameField != null) usernameField.setEditable(editable);
        if (firstNameField != null) firstNameField.setEditable(editable);
        if (lastNameField != null) lastNameField.setEditable(editable);
        if (emailField != null) emailField.setEditable(editable);
        if (phoneNumberField != null) phoneNumberField.setEditable(editable);
        
        // Additional fields
        if (countryPrefixField != null) countryPrefixField.setEditable(editable);
        if (addressField != null) addressField.setEditable(editable);
        if (zipCodeField != null) zipCodeField.setEditable(editable);
        
        // ComboBoxes
        if (genderField != null) genderField.setDisable(!editable);
        if (countryField != null) countryField.setDisable(!editable);
        if (regionField != null) regionField.setDisable(!editable);
        
        // DatePicker
        if (birthdateField != null) birthdateField.setEditable(editable);
        
        // Upload photo button should only be enabled during edit
        if (uploadPhotoButton != null) uploadPhotoButton.setDisable(!editable);
        
        // Toggle visibility of save button based on edit mode
        if (saveButton != null) saveButton.setVisible(editable);
        
        // Update role management checkboxes based on current user's permissions
        configureRoleManagement(editable);
    }

    @FXML
    private void handleUploadPhoto() {
        if (currentUser == null) {
            logger.error("Current user is null. Cannot upload photo.");
            Utils.showErrorAlert("Error", "No user logged in");
            return;
        }

        logger.info("Uploading photo for user: {}", currentUser.getUsername());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadPhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Save the image using ImageManager to get a consistent path
                String savedPath = tn.esprit.testpifx.utils.ImageManager.saveProfileImage(selectedFile.getAbsolutePath());
                
                // Store the path in the user model
                currentUser.setProfilePictureUrl(savedPath);
                
                // Update the user in the database
                if (userService != null) {
                    userService.updateUser(currentUser);
                    logger.info("Profile picture updated in database for user: {}", currentUser.getUsername());
                } else {
                    logger.error("UserService is null. Cannot update user in database.");
                }
                
                // Display the actual image in the UI
                profileImageView.getChildren().clear();
                
                // Create an ImageView to display the actual image
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
                
                // Get absolute path and create image
                String absolutePath = tn.esprit.testpifx.utils.ImageManager.getAbsolutePath(savedPath);
                if (absolutePath != null && new File(absolutePath).exists()) {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(
                            new File(absolutePath).toURI().toString(), 
                            150, 150, true, true);
                    
                    imageView.setImage(image);
                    imageView.setFitWidth(150);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);
                    
                    // Add image to the stack pane
                    profileImageView.getChildren().add(imageView);
                    profileImageView.setStyle("-fx-background-color: transparent; -fx-background-radius: 75;");
                    
                    logger.info("Profile image loaded successfully from path: {}", absolutePath);
                } else {
                    logger.error("Failed to load image from path: {}", absolutePath);
                    // Fall back to displaying first letter if image loading fails
                    displayUserInitial();
                }
                
                Utils.showSuccessAlert("Success", "Profile picture updated successfully");
            } catch (Exception e) {
                logger.error("Error uploading profile picture: {}", e.getMessage(), e);
                Utils.showErrorAlert("Error", "Failed to upload image: " + e.getMessage());
                // Fall back to displaying first letter
                displayUserInitial();
            }
        }
    }
    
    /**
     * Helper method to display the user's initial in the profile picture area
     */
    private void displayUserInitial() {
        if (currentUser == null || currentUser.getUsername() == null || currentUser.getUsername().isEmpty()) {
            return;
        }
        
        // Display first letter of username with custom background color
        String username = currentUser.getUsername();
        Text avatarText = new Text(username.substring(0, 1).toUpperCase());
        avatarText.setStyle("-fx-font-size: 80px; -fx-fill: white;");
        
        // Change the background color to indicate a custom profile
        profileImageView.getChildren().clear();
        profileImageView.getChildren().add(avatarText);
        profileImageView.setStyle("-fx-background-color: #3949ab; -fx-background-radius: 75; -fx-min-width: 150; -fx-min-height: 150; -fx-max-width: 150; -fx-max-height: 150; -fx-alignment: center;");
    }

    @FXML
    public void handleSave() {
        if (currentUser == null) {
            logger.error("Current user is null. Cannot save profile.");
            return;
        }

        logger.info("Saving profile for user: {}", currentUser.getUsername());
        // Validate password change if fields are filled
        if (!currentPasswordField.getText().isEmpty() || 
            !newPasswordField.getText().isEmpty() || 
            !confirmPasswordField.getText().isEmpty()) {
        
            if (!validatePasswordChange()) {
                return;
            }
        }
    
        // Update basic user data
        currentUser.setUsername(usernameField.getText());
        currentUser.setEmail(emailField.getText());
        
        // Explicitly log first/last name being set for debugging
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        logger.info("Setting first name: '{}', last name: '{}'", firstName, lastName);
        
        // Explicitly set first name and last name - this is critical for the update to work
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        
        // Update contact information
        currentUser.setPhoneNumber(phoneNumberField.getText());
        currentUser.setCountryPrefix(countryPrefixField.getText());
        
        // Update location information
        currentUser.setCountry(countryField.getValue());
        currentUser.setRegion(regionField.getValue());
        currentUser.setAddress(addressField.getText());
        currentUser.setZipCode(zipCodeField.getText());
        
        // Update personal information
        if (genderField.getValue() != null) {
            currentUser.setGender(genderField.getValue());
        }
        
        // Explicitly handle birthdate - this is critical for the update to work
        if (birthdateField.getValue() != null) {
            logger.info("Setting birthdate to: {}", birthdateField.getValue());
            currentUser.setBirthdate(birthdateField.getValue());
        } else {
            logger.info("Birthdate field is null, not updating this field");
        }

        // Update role information based on checkboxes
        try {
            // Get the user who is viewing/editing this profile (from session)
            User viewerUser = UserSessionManager.getCurrentUser();
            if (viewerUser == null) {
                viewerUser = currentUser; // Fallback to the profile user if no session user
            }

            // Check if viewer has permission to change roles
            boolean isAdmin = viewerUser.hasRole(tn.esprit.testpifx.models.Role.ADMIN);
            boolean isManager = viewerUser.hasRole(tn.esprit.testpifx.models.Role.MANAGER);
            
            // Create a new set to store updated roles
            Set<tn.esprit.testpifx.models.Role> updatedRoles = new HashSet<>();
            
            // Always add USER role
            updatedRoles.add(tn.esprit.testpifx.models.Role.USER);
            
            // Add MANAGER role if checked and viewer has permission
            if (managerRoleCheckbox.isSelected() && (isAdmin || isManager)) {
                updatedRoles.add(tn.esprit.testpifx.models.Role.MANAGER);
                logger.info("Adding MANAGER role to user {}", currentUser.getUsername());
            }
            
            // Add ADMIN role if checked and viewer is an admin
            if (adminRoleCheckbox.isSelected() && isAdmin) {
                updatedRoles.add(tn.esprit.testpifx.models.Role.ADMIN);
                logger.info("Adding ADMIN role to user {}", currentUser.getUsername());
            }
            
            // Update the user's roles
            currentUser.setRoles(updatedRoles);
            
        } catch (Exception e) {
            logger.error("Error updating user roles: {}", e.getMessage(), e);
        }
    
        try {
            if (userService == null) {
                logger.error("UserService is not initialized. Cannot save profile.");
                Utils.showErrorAlert("Error", "UserService is not initialized");
                return;
            }
            
            // Print the user details before saving for debugging
            logger.info("Saving user: {}. First name: '{}', Last name: '{}', Birthdate: '{}'", 
                        currentUser.getUsername(), 
                        currentUser.getFirstName(), 
                        currentUser.getLastName(),
                        currentUser.getBirthdate());
            
            // Save the user to the database
            userService.updateUser(currentUser);
            
            // Verify if saved correctly by re-fetching the user
            User refreshedUser = userService.getUserById(currentUser.getUserId()).orElse(null);
            if (refreshedUser != null) {
                logger.info("User refreshed from database. First name: '{}', Last name: '{}', Birthdate: '{}'", 
                            refreshedUser.getFirstName(), 
                            refreshedUser.getLastName(),
                            refreshedUser.getBirthdate());
                
                // Update current user with refreshed data
                currentUser = refreshedUser;
            }
            
            logger.info("Profile updated successfully for user: {}", currentUser.getUsername());
            Utils.showInfoAlert("Success", "Profile updated successfully");
            
            // Exit edit mode but stay on profile page
            editMode = false;
            if (editButton != null) {
                editButton.setText("Edit Profile");
            }
            setFieldsEditable(false);
            if (saveButton != null) {
                saveButton.setVisible(false);
            }
            
            // Reload the data to display the changes
            loadUserData();
            
        } catch (Exception e) {
            logger.error("Failed to update profile for user: {}. Error: {}", currentUser.getUsername(), e.getMessage(), e);
            Utils.showErrorAlert("Error", "Failed to update profile: " + e.getMessage());
        }
    }

    public boolean validatePasswordChange() {
        if (userService == null) {
            Utils.showErrorAlert("Error", "UserService is not initialized");
            return false;
        }
    
        if (currentPasswordField.getText().isEmpty()) {
            Utils.showErrorAlert("Error", "Current password is required");
            return false;
        }
    
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("Error", "New passwords do not match");
            return false;
        }
    
        if (!userService.validatePassword(currentUser, currentPasswordField.getText())) {
            Utils.showErrorAlert("Error", "Current password is incorrect");
            return false;
        }
    
        currentUser.setPassword(newPasswordField.getText());
        return true;
    }

    @FXML
    public void handleChangePassword() {
        if (currentUser == null) {
            logger.error("Current user is null. Cannot change password.");
            return;
        }

        if (currentPasswordField.getText().isEmpty()) {
            Utils.showErrorAlert("Error", "Current password is required");
            return;
        }

        if (newPasswordField.getText().isEmpty()) {
            Utils.showErrorAlert("Error", "New password is required");
            return;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            Utils.showErrorAlert("Error", "New passwords do not match");
            return;
        }

        if (!userService.validatePassword(currentUser, currentPasswordField.getText())) {
            Utils.showErrorAlert("Error", "Current password is incorrect");
            return;
        }        try {
            // Get the new password
            String newPassword = newPasswordField.getText();
            
            // Update the password in the user object
            currentUser.setPassword(newPassword);
            
            // Save to database
            userService.updateUser(currentUser);
            
            // Update the remembered credentials if this user is using "Remember Me"
            if (tn.esprit.testpifx.utils.UserPreferences.hasRememberedCredentials() && 
                tn.esprit.testpifx.utils.UserPreferences.getUsername().equals(currentUser.getUsername())) {
                tn.esprit.testpifx.utils.UserPreferences.saveLoginCredentials(
                    currentUser.getUsername(), 
                    newPassword, 
                    true // Keep the remember me setting enabled
                );
            }
            
            // Clear fields and show success message
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            
            Utils.showInfoAlert("Success", "Password changed successfully");
            logger.info("Password updated successfully for user: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Failed to update password: {}", e.getMessage(), e);
            Utils.showErrorAlert("Error", "Failed to update password: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        logger.info("Cancelling profile edit and returning to welcome screen.");
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            WelcomeController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/welcome.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to return to welcome screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHome() {
        handleCancel(); // For now, just redirect to welcome screen
    }    @FXML
    private void handleLogout() {
        logger.info("Logging out user: {}", currentUser != null ? currentUser.getUsername() : "Unknown");
        try {
            // Clear saved credentials to prevent auto re-login
            UserPreferences.clearLoginCredentials();
            System.out.println("Cleared saved login credentials during logout");
            
            UserSessionManager.clearSession();
            
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            AuthController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/login.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads the teams that the current user belongs to as a simple numbered list
     */
    private void loadUserTeams() {
        if (currentUser == null || teamListView == null) {
            logger.warn("Cannot load teams: currentUser={}, teamListView={}", 
                currentUser != null, teamListView != null);
            return;
        }
        
        logger.info("Loading teams for user: {}", currentUser.getUsername());
        
        try {
            // Get database connection details
            tn.esprit.testpifx.utils.DatabaseConfig dbConfig = new tn.esprit.testpifx.utils.DatabaseConfig();
            String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase());
            
            // Use direct SQL query to get team names only
            List<String> teamNames = new ArrayList<>();
            
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    url, dbConfig.getUsername(), dbConfig.getPassword())) {
                
                // First verify the team_members table exists
                boolean tableExists = false;
                try (java.sql.ResultSet tables = conn.getMetaData().getTables(null, null, "team_members", new String[]{"TABLE"})) {
                    tableExists = tables.next();
                }
                
                if (!tableExists) {
                    System.out.println("ERROR: team_members table does not exist!");
                    teamListView.setItems(FXCollections.observableArrayList("Database issue: team_members table not found"));
                    return;
                }
                
                // Simple query to get just team names for this user
                String sql = "SELECT t.name FROM teams t " +
                             "JOIN team_members tm ON t.team_id = tm.team_id " +
                             "WHERE tm.user_id = ? " +
                             "ORDER BY t.name";
                
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, currentUser.getUserId());
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            teamNames.add(name);
                            System.out.println("Found team: " + name);
                        }
                    }
                }
            }
            
            // Update the ListView with numbered team names
            if (!teamNames.isEmpty()) {
                List<String> numberedTeamsList = new ArrayList<>();
                for (int i = 0; i < teamNames.size(); i++) {
                    numberedTeamsList.add((i + 1) + ". " + teamNames.get(i));
                }
                
                teamListView.setItems(FXCollections.observableArrayList(numberedTeamsList));
                teamListView.setCellFactory(param -> new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("-fx-font-size: 14px; -fx-padding: 10 15; -fx-font-weight: normal;");
                        }
                    }
                });
                
                if (teamCountLabel != null) {
                    teamCountLabel.setText("Your Teams (" + teamNames.size() + "):");
                }
                
                logger.info("Loaded {} teams into numbered list view", teamNames.size());
            } else {
                teamListView.setItems(FXCollections.observableArrayList("You don't belong to any teams yet."));
                if (teamCountLabel != null) {
                    teamCountLabel.setText("Your Teams (0):");
                }
                logger.info("User doesn't belong to any teams");
            }
            
        } catch (Exception e) {
            logger.error("Error loading user teams: {}", e.getMessage(), e);
            teamListView.setItems(FXCollections.observableArrayList("Error loading teams: " + e.getMessage()));
        }
    }

    /**
     * Creates a test team with the current user as a member if the user 
     * doesn't belong to any teams yet. This is for demonstration purposes.
     */
    private void ensureUserHasTeam() {
        if (currentUser == null || teamService == null) {
            return;
        }
        
        try {
            // Check if user already belongs to any teams
            List<tn.esprit.testpifx.models.Team> userTeams = teamService.getTeamsByMember(currentUser.getUserId());
            
            if (userTeams == null || userTeams.isEmpty()) {
                logger.info("User doesn't belong to any teams. Creating a test team...");
                
                // Create a new test team
                tn.esprit.testpifx.models.Team testTeam = new tn.esprit.testpifx.models.Team(
                    "Test Team for " + currentUser.getUsername(),
                    "This is a test team created for profile view demonstration",
                    currentUser.getUserId()
                );
                
                // Create the team with the current user as creator and member
                teamService.createTeam(testTeam, currentUser);
                
                logger.info("Test team created successfully with ID: {}", testTeam.getTeamId());
                
                // Refresh team data
                loadUserTeams();
            }
        } catch (Exception e) {
            logger.error("Failed to create test team: {}", e.getMessage(), e);
        }
    }

    /**
     * A direct database query to check team memberships - for debugging purposes
     */
    private void checkTeamMembershipsDirectly() {
        if (currentUser == null) {
            logger.warn("Cannot check team memberships: currentUser is null");
            return;
        }
        
        logger.info("Checking team memberships directly from database for user: {}", currentUser.getUserId());
        
        String userId = currentUser.getUserId();
        List<String> teamNames = new ArrayList<>();
        
        // Use try-with-resources to ensure proper connection cleanup
        try {
            // Get database connection details from the database config
            tn.esprit.testpifx.utils.DatabaseConfig dbConfig = new tn.esprit.testpifx.utils.DatabaseConfig();
            String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase());
            
            logger.info("Connecting to database at: {}", url);
            
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    url, dbConfig.getUsername(), dbConfig.getPassword())) {
                
                // First, check if team_members table exists
                boolean tableExists = false;
                try (java.sql.ResultSet tables = conn.getMetaData().getTables(
                        null, null, "team_members", new String[]{"TABLE"})) {
                    tableExists = tables.next();
                }
                
                if (!tableExists) {
                    logger.error("team_members table does not exist in the database!");
                    return;
                }
                
                // Query the database directly
                String sql = "SELECT t.team_id, t.name, COUNT(tm2.user_id) AS member_count " +
                            "FROM teams t " +
                            "INNER JOIN team_members tm ON t.team_id = tm.team_id " +
                            "LEFT JOIN team_members tm2 ON t.team_id = tm2.team_id " +
                            "WHERE tm.user_id = ? " +
                            "GROUP BY t.team_id, t.name";
                
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, userId);
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String teamId = rs.getString("team_id");
                            String name = rs.getString("name");
                            int memberCount = rs.getInt("member_count");
                            
                            String teamInfo = name + " (" + memberCount + " members)";
                            logger.info("Found team: {} with ID: {}", teamInfo, teamId);
                            teamNames.add(teamInfo);
                        }
                    }
                }
            }
            
            // Update the teamsTableView with the results if it exists (ignoring the old ListView)
            if (!teamNames.isEmpty()) {
                logger.info("Found {} teams for user {}", teamNames.size(), userId);
                
                // We're now populating the TableView instead of ListView, so no need for this code anymore
                // The teams data will be loaded by loadTeamsTableData() method
            } else {
                logger.warn("No teams found for user {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("Error checking team memberships: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleEditProfile() {
        if (currentUser == null) {
            logger.error("Current user is null. Cannot edit profile.");
            Utils.showErrorAlert("Error", "No user logged in");
            return;
        }

        // Toggle edit mode instead of opening UserEditController
        editMode = !editMode;
        
        if (editMode) {
            // Enable editing
            if (editButton != null) {
                editButton.setText("Cancel Editing");
            }
            setFieldsEditable(true);
            if (saveButton != null) {
                saveButton.setVisible(true);
            }
            if (cancelButton != null) {
                cancelButton.setVisible(true);
            }
            logger.info("Profile edit mode enabled for user: {}", currentUser.getUsername());
        } else {
            // Disable editing and revert changes
            if (editButton != null) {
                editButton.setText("Edit Profile");
            }
            setFieldsEditable(false);
            if (saveButton != null) {
                saveButton.setVisible(false);
            }
            if (cancelButton != null) {
                cancelButton.setVisible(true);
            }
            
            // Reload user data to discard any unsaved changes
            loadUserData();
            logger.info("Profile edit mode disabled for user: {}", currentUser.getUsername());
        }
    }

    /**
     * Public method to enable edit mode when navigating to the profile from external controllers
     * This allows the user management table's edit button to directly open profile in edit mode
     */
    public void enableEditMode() {
        logger.info("Enabling edit mode from external controller");
        if (currentUser == null) {
            logger.error("Cannot enable edit mode: Current user is null");
            return;
        }
        
        // Enable editing
        editMode = true;
        if (editButton != null) {
            editButton.setText("Cancel Editing");
        }
        setFieldsEditable(true);
        if (saveButton != null) {
            saveButton.setVisible(true);
        }
        if (cancelButton != null) {
            cancelButton.setVisible(true);
        }
        logger.info("Profile edit mode enabled for user: {}", currentUser.getUsername());
    }
    
    /**
     * Calculate profile completeness as a percentage based on filled fields
     * @return double from 0.0 to 1.0 representing profile completeness percentage
     */
    private double calculateProfileCompleteness() {
        if (currentUser == null) {
            return 0.0;
        }
        
        int totalFields = 9; // Total number of profile fields we're checking
        int filledFields = 0;
        
        // Check which fields are filled
        if (currentUser.getProfilePictureUrl() != null && !currentUser.getProfilePictureUrl().isEmpty()) filledFields++;
        if (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty()) filledFields++;
        if (currentUser.getLastName() != null && !currentUser.getLastName().isEmpty()) filledFields++;
        if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) filledFields++;
        if (currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().isEmpty()) filledFields++;
        if (currentUser.getCountry() != null && !currentUser.getCountry().isEmpty()) filledFields++;
        if (currentUser.getAddress() != null && !currentUser.getAddress().isEmpty()) filledFields++;
        if (currentUser.getGender() != null && !currentUser.getGender().isEmpty()) filledFields++;
        if (currentUser.getBirthdate() != null) filledFields++;
        
        // Calculate percentage
        return (double) filledFields / totalFields;
    }
    
    /**
     * Update the account details section with profile completeness and account status
     */
    private void updateAccountDetailsSection() {
        if (currentUser == null) {
            return;
        }
        
        // Update profile completeness
        double completenessPercentage = calculateProfileCompleteness();
        int completenessValue = (int) Math.round(completenessPercentage * 100);
        
        if (profileCompletenessValue != null) {
            profileCompletenessValue.setText(completenessValue + "%");
        }
        
        if (profileCompletenessBar != null) {
            profileCompletenessBar.setProgress(completenessPercentage);
            
            // Set appropriate color based on completeness
            String barColor;
            if (completenessPercentage < 0.4) {
                barColor = "#e74a3b"; // Red for low completeness
            } else if (completenessPercentage < 0.7) {
                barColor = "#f6c23e"; // Yellow for medium completeness
            } else {
                barColor = "#1cc88a"; // Green for high completeness
            }
            profileCompletenessBar.setStyle("-fx-accent: " + barColor + "; -fx-pref-height: 8;");
        }
        
        // Update account status
        boolean isActive = currentUser.isActive();
        String statusText = isActive ? "Active" : "Inactive";
        
        if (accountStatusValue != null) {
            accountStatusValue.setText(statusText);
        }
        
        if (accountStatusBar != null) {
            accountStatusBar.setProgress(isActive ? 1.0 : 0.3);
            accountStatusBar.setStyle("-fx-accent: " + (isActive ? "#1cc88a" : "#e74a3b") + "; -fx-pref-height: 8;");
        }
        
        logger.info("Updated account details: completeness={}%, status={}", completenessValue, statusText);
    }
    
    /**
     * Data class for team table view
     */
    public static class TeamTableData {
        private final String teamId;
        private final String teamName;
        private final int memberCount;
        private final String userRole;
        
        public TeamTableData(String teamId, String teamName, int memberCount, String userRole) {
            this.teamId = teamId;
            this.teamName = teamName;
            this.memberCount = memberCount;
            this.userRole = userRole;
        }
        
        public String getTeamId() { return teamId; }
        public String getTeamName() { return teamName; }
        public int getMemberCount() { return memberCount; }
        public String getUserRole() { return userRole; }
    }
    
    /**
     * Initialize the teams table view with columns and data
     */
    private void initializeTeamsTable() {
        if (teamsTableView == null) {
            logger.warn("Cannot initialize teams table: teamsTableView is null");
            return;
        }
        
        // Debug output to check UI components
        System.out.println("Initializing teams table. teamsTableView: " + teamsTableView);
        System.out.println("Columns present: " + 
            (teamNameColumn != null ? "teamNameColumn, " : "") + 
            (memberCountColumn != null ? "memberCountColumn, " : "") + 
            (teamRoleColumn != null ? "teamRoleColumn, " : "") +
            (teamActionColumn != null ? "teamActionColumn" : ""));
        
        // Clear any existing cell factories and value factories to prevent conflicts
        if (teamNameColumn != null) {
            teamNameColumn.setCellValueFactory(null);
            teamNameColumn.setCellFactory(null);
        }
        if (memberCountColumn != null) {
            memberCountColumn.setCellValueFactory(null);
            memberCountColumn.setCellFactory(null);
        }
        if (teamRoleColumn != null) {
            teamRoleColumn.setCellValueFactory(null);
            teamRoleColumn.setCellFactory(null);
        }
        if (teamActionColumn != null) {
            teamActionColumn.setCellValueFactory(null);
            teamActionColumn.setCellFactory(null);
        }
        
        // Set up cell value factories with clear debug output
        if (teamNameColumn != null) {
            teamNameColumn.setCellValueFactory(cellData -> {
                try {
                    TeamTableData data = cellData.getValue();
                    String teamName = data.getTeamName();
                    System.out.println("Setting team name in column: " + teamName);
                    return new javafx.beans.property.SimpleStringProperty(teamName);
                } catch (Exception e) {
                    System.out.println("Error setting team name: " + e.getMessage());
                    return new javafx.beans.property.SimpleStringProperty("Error");
                }
            });
            
            // Add custom cell factory for better styling of team names
            teamNameColumn.setCellFactory(col -> new TableCell<TeamTableData, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #4e73df;");
                    }
                }
            });
        }
        
        if (memberCountColumn != null) {
            memberCountColumn.setCellValueFactory(cellData -> {
                try {
                    TeamTableData data = cellData.getValue();
                    int count = data.getMemberCount();
                    System.out.println("Setting member count in column: " + count);
                    return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
                } catch (Exception e) {
                    System.out.println("Error setting member count: " + e.getMessage());
                    return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
                }
            });
        }
        
        if (teamRoleColumn != null) {
            teamRoleColumn.setCellValueFactory(cellData -> {
                try {
                    TeamTableData data = cellData.getValue();
                    String role = data.getUserRole();
                    System.out.println("Setting team role in column: " + role);
                    return new javafx.beans.property.SimpleStringProperty(role);
                } catch (Exception e) {
                    System.out.println("Error setting team role: " + e.getMessage());
                    return new javafx.beans.property.SimpleStringProperty("Unknown");
                }
            });
        }
        
        // Set up column widths for better display
        if (teamNameColumn != null) teamNameColumn.setPrefWidth(150);
        if (memberCountColumn != null) memberCountColumn.setPrefWidth(100);
        if (teamRoleColumn != null) teamRoleColumn.setPrefWidth(100);
        
        // Add action column with View buttons
        if (teamActionColumn != null) {
            teamActionColumn.setPrefWidth(80);
            teamActionColumn.setCellFactory(col -> new TableCell<TeamTableData, Void>() {
                private final Button viewButton = new Button("View");
                {
                    viewButton.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
                    viewButton.setStyle("-fx-font-size: 10px; -fx-padding: 2 5;");
                    
                    viewButton.setOnAction(event -> {
                        if (getTableView().getItems() != null && !getTableView().getItems().isEmpty() 
                            && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            TeamTableData team = getTableView().getItems().get(getIndex());
                            viewTeamDetails(team.getTeamId());
                        }
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        setGraphic(viewButton);
                    }
                }
            });
        }
        
        // Initialize search functionality
        if (teamSearchField != null) {
            teamSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTeamTable(newValue);
            });
        }
        
        // Initialize display limit functionality
        if (teamDisplayLimit != null) {
            if (teamDisplayLimit.getItems() == null || teamDisplayLimit.getItems().isEmpty()) {
                teamDisplayLimit.getItems().addAll("All", "5", "10");
                teamDisplayLimit.setValue("All");
            }
            
            teamDisplayLimit.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateTeamTablePagination());
        }
        
        // Now load the team data after all the setup is done
        javafx.application.Platform.runLater(() -> {
            loadTeamsTableData();
            System.out.println("Teams data loading triggered from initializeTeamsTable");
        });
    }
    
    /**
     * Load teams data into the ListView with a numbered format
     */
    private void loadTeamsTableData() {
        if (currentUser == null) {
            logger.warn("Cannot load teams data: currentUser is null");
            return;
        }
        
        try {
            // Get database connection details
            tn.esprit.testpifx.utils.DatabaseConfig dbConfig = new tn.esprit.testpifx.utils.DatabaseConfig();
            String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase());
            
            List<String> teamsList = new ArrayList<>();
            
            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    url, dbConfig.getUsername(), dbConfig.getPassword())) {
                
                // Query to get team names
                String sql = "SELECT t.name " +
                             "FROM teams t " +
                             "INNER JOIN team_members tm ON t.team_id = tm.team_id " +
                             "WHERE tm.user_id = ?";
                
                try (java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, currentUser.getUserId());
                    
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            teamsList.add(name);
                            System.out.println("Added team to list: " + name);
                        }
                    }
                }
            }
            
            // Update the ListView with the numbered teams data
            if (!teamsList.isEmpty()) {
                List<String> numberedTeamsList = new ArrayList<>();
                
                // Create numbered list (1. Team Name, 2. Team Name, etc.)
                for (int i = 0; i < teamsList.size(); i++) {
                    numberedTeamsList.add((i + 1) + ". " + teamsList.get(i));
                }
                
                // Update the ListView
                if (teamListView != null) {
                    teamListView.setItems(FXCollections.observableArrayList(numberedTeamsList));
                    teamListView.setCellFactory(param -> new ListCell<String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            
                            if (empty || item == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(item);
                                setStyle("-fx-font-size: 14px; -fx-padding: 8 5 8 5;");
                            }
                        }
                    });
                    logger.info("Loaded {} teams into numbered list view", teamsList.size());
                } else {
                    logger.warn("teamListView is null, cannot update with team data");
                }
                
                // Display count in label if available
                if (teamCountLabel != null) {
                    teamCountLabel.setText("Total Teams: " + teamsList.size());
                }
                
            } else {
                // Show empty state
                if (teamListView != null) {
                    teamListView.setItems(FXCollections.observableArrayList("You don't belong to any teams yet."));
                }
                if (teamCountLabel != null) {
                    teamCountLabel.setText("No teams found");
                }
                logger.info("No teams found for current user");
            }
            
        } catch (Exception e) {
            logger.error("Error loading teams data: {}", e.getMessage(), e);
            if (teamListView != null) {
                teamListView.setItems(FXCollections.observableArrayList("Error loading teams: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Ensure that table columns are properly configured
     */
    private void ensureTableColumns() {
        if (teamsTableView == null) return;
        
        // First remove any existing cell factories to prevent duplicates
        if (teamNameColumn != null && teamNameColumn.getCellFactory() != null) {
            teamNameColumn.setCellFactory(null);
        }
        if (memberCountColumn != null && memberCountColumn.getCellFactory() != null) {
            memberCountColumn.setCellFactory(null);
        }
        if (teamRoleColumn != null && teamRoleColumn.getCellFactory() != null) {
            teamRoleColumn.setCellFactory(null);
        }
        if (teamActionColumn != null && teamActionColumn.getCellFactory() != null) {
            teamActionColumn.setCellFactory(null);
        }
        
        // Now set up the cell value factories
        if (teamNameColumn != null) {
            teamNameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTeamName()));
            
            // Add custom cell factory for better visual styling
            teamNameColumn.setCellFactory(tc -> {
                return new TableCell<TeamTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            setStyle("-fx-font-weight: bold;");
                        }
                    }
                };
            });
        }
        
        if (memberCountColumn != null) {
            memberCountColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMemberCount()).asObject());
            
            // Add custom cell factory for member counts
            memberCountColumn.setCellFactory(tc -> {
                return new TableCell<TeamTableData, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item.toString());
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                };
            });
        }
        
        if (teamRoleColumn != null) {
            teamRoleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserRole()));
            
            // Add custom cell factory for role with custom styling
            teamRoleColumn.setCellFactory(tc -> {
                return new TableCell<TeamTableData, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            if ("Creator".equals(item)) {
                                setStyle("-fx-text-fill: #4e73df; -fx-font-weight: bold;");
                            } else {
                                setStyle("-fx-text-fill: #858796;");
                            }
                        }
                    }
                };
            });
        }
        
        if (teamActionColumn != null) {
            teamActionColumn.setCellFactory(tc -> {
                return new TableCell<TeamTableData, Void>() {
                    private final Button viewBtn = new Button("View");
                    
                    {
                        viewBtn.getStyleClass().addAll("btn", "btn-primary", "btn-sm");
                        viewBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
                        
                        viewBtn.setOnAction(event -> {
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                TeamTableData team = (TeamTableData) getTableRow().getItem();
                                viewTeamDetails(team.getTeamId());
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(viewBtn);
                            setStyle("-fx-alignment: CENTER;");
                        }
                    }
                };
            });
        }
        
        // Ensure the columns have proper width
        if (teamNameColumn != null) teamNameColumn.setPrefWidth(150);
        if (memberCountColumn != null) memberCountColumn.setPrefWidth(80);
        if (teamRoleColumn != null) teamRoleColumn.setPrefWidth(100);
        if (teamActionColumn != null) teamActionColumn.setPrefWidth(80);
    }
    
    /**
     * Filter the teams table based on search text
     */
    private void filterTeamTable(String searchText) {
        if (teamsTableView.getItems() == null || teamsTableView.getItems().isEmpty()) {
            return;
        }
        
        // If search text is empty, show all teams
        if (searchText == null || searchText.trim().isEmpty()) {
            loadTeamsTableData();
            return;
        }
        
        // Filter the table based on the search text
        List<TeamTableData> filteredList = teamsTableView.getItems().stream()
            .filter(team -> 
                team.getTeamName().toLowerCase().contains(searchText.toLowerCase()) ||
                team.getUserRole().toLowerCase().contains(searchText.toLowerCase())
            )
            .collect(java.util.stream.Collectors.toList());
        
        // Update the table with filtered data
        teamsTableView.setItems(FXCollections.observableArrayList(filteredList));
        teamCountLabel.setText("Showing " + filteredList.size() + " matching teams");
    }
    
    /**
     * Update pagination based on display limit selection
     */
    private void updateTeamTablePagination() {
        if (teamDisplayLimit == null || teamsTableView == null) {
            return;
        }
        
        String limit = teamDisplayLimit.getValue();
        if ("All".equals(limit)) {
            // Show all teams without pagination
            loadTeamsTableData();
        } else {
            try {
                // Limit the number of teams shown
                int maxItems = Integer.parseInt(limit);
                List<TeamTableData> limitedList = new ArrayList<>();
                
                int count = 0;
                for (TeamTableData team : teamsTableView.getItems()) {
                    if (count < maxItems) {
                        limitedList.add(team);
                        count++;
                    } else {
                        break;
                    }
                }
                
                teamsTableView.setItems(FXCollections.observableArrayList(limitedList));
                teamCountLabel.setText("Showing 1 to " + limitedList.size() + " of " + teamsTableView.getItems().size() + " teams");
                
            } catch (NumberFormatException e) {
                logger.error("Invalid display limit: {}", limit);
            }
        }
    }
    
    /**
     * Navigate to team details view
     */
    private void viewTeamDetails(String teamId) {
        logger.info("Viewing team details for team: {}", teamId);
        Utils.showInfoAlert("Team View", "Navigating to team details page is not implemented yet.");
        // This would typically navigate to a team details page
    }
    
    /**
     * Configure role management UI based on current user and viewer permissions
     * @param editable true if fields should be editable
     */
    private void configureRoleManagement(boolean editable) {
        if (adminRoleCheckbox == null || managerRoleCheckbox == null || userRoleCheckbox == null || roleManagementContainer == null) {
            logger.warn("Role management UI components not initialized");
            return;
        }

        // Add null check for currentUser
        if (currentUser == null) {
            logger.warn("Cannot configure role management UI: currentUser is null");
            // Hide the entire role management section when no user is loaded
            roleManagementContainer.setVisible(false);
            roleManagementContainer.setManaged(false);
            return;
        }

        // Get the user who is viewing/editing this profile (from session)
        User viewerUser = UserSessionManager.getCurrentUser();
        if (viewerUser == null) {
            viewerUser = currentUser; // Fallback to the profile user if no session user
        }

        logger.info("Configuring role management UI. Viewer user: {}, Profile user: {}", 
            viewerUser.getUsername(), currentUser.getUsername());
        
        // Determine if the viewer has permission to see/edit roles
        boolean isAdmin = viewerUser.hasRole(tn.esprit.testpifx.models.Role.ADMIN);
        boolean isManager = viewerUser.hasRole(tn.esprit.testpifx.models.Role.MANAGER);
        boolean isViewingSelf = viewerUser.getUserId().equals(currentUser.getUserId());
        
        // Only show role management to admins editing other users 
        // or to users viewing their own roles (read-only)
        boolean shouldShowRoles = (isAdmin && !isViewingSelf) || isViewingSelf;
        
        // Show/hide the entire role management section
        roleManagementContainer.setVisible(shouldShowRoles);
        roleManagementContainer.setManaged(shouldShowRoles);
        
        // Apply styling to the role management container
        roleManagementContainer.getStyleClass().add("role-management-container");
        
        if (!shouldShowRoles) {
            // If we're hiding the section, we can return early
            return;
        }
        
        // Set current role checkboxes state based on user's roles
        adminRoleCheckbox.setSelected(currentUser.hasRole(tn.esprit.testpifx.models.Role.ADMIN));
        managerRoleCheckbox.setSelected(currentUser.hasRole(tn.esprit.testpifx.models.Role.MANAGER));
        userRoleCheckbox.setSelected(currentUser.hasRole(tn.esprit.testpifx.models.Role.USER));
        
        // Apply styling to checkbox elements
        adminRoleCheckbox.getStyleClass().add("role-checkbox");
        managerRoleCheckbox.getStyleClass().add("role-checkbox");
        userRoleCheckbox.getStyleClass().add("role-checkbox");
        
        // Add descriptive labels next to checkboxes
        if (adminRoleCheckbox.getTooltip() == null) {
            adminRoleCheckbox.setText("Administrator");
            adminRoleCheckbox.setTooltip(new Tooltip("Full access to all system features and user management"));
        }
        
        if (managerRoleCheckbox.getTooltip() == null) {
            managerRoleCheckbox.setText("Manager");
            managerRoleCheckbox.setTooltip(new Tooltip("Can manage teams, projects and view user information"));
        }
        
        if (userRoleCheckbox.getTooltip() == null) {
            userRoleCheckbox.setText("User");
            userRoleCheckbox.setTooltip(new Tooltip("Basic access to the system - all accounts have this role"));
        }
        
        // If the roleManagementContainer exists, add a header label if it doesn't have one
        if (roleManagementContainer != null && roleManagementContainer.getChildren().size() > 0) {
            boolean hasHeader = false;
            for (javafx.scene.Node node : roleManagementContainer.getChildren()) {
                if (node instanceof Label && ((Label)node).getText() != null && 
                    ((Label)node).getText().contains("Role Management")) {
                    hasHeader = true;
                    break;
                }
            }
            
            if (!hasHeader) {
                Label headerLabel = new Label("User Role Management");
                headerLabel.getStyleClass().add("section-header");
                headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5px 0;");
                roleManagementContainer.getChildren().add(0, headerLabel);
                
                // Add explanation text
                Label explanationLabel = new Label("Select the roles assigned to this user account:");
                explanationLabel.setStyle("-fx-font-size: 12px; -fx-padding: 0 0 10px 0;");
                roleManagementContainer.getChildren().add(1, explanationLabel);
            }
        }
        
        if (editable && isAdmin) {
            // Admins can edit all roles
            adminRoleCheckbox.setDisable(false);
            managerRoleCheckbox.setDisable(false);
            userRoleCheckbox.setDisable(true); // User role is always active
        } else if (editable && isManager) {
            // Managers can edit Manager and User roles but not Admin role
            adminRoleCheckbox.setDisable(true);
            managerRoleCheckbox.setDisable(false);
            userRoleCheckbox.setDisable(true); // User role is always active
        } else {
            // When not in edit mode or for regular users, all checkboxes are disabled but show correct state
            adminRoleCheckbox.setDisable(true);
            managerRoleCheckbox.setDisable(true);
            userRoleCheckbox.setDisable(true);
        }
        
        // Make at least User role always checked and disabled to prevent users from having no roles
        userRoleCheckbox.setSelected(true);
        userRoleCheckbox.setDisable(true);
        
        // Add a tooltip to explain why admin role can't be changed by non-admin users
        if (!isAdmin) {
            adminRoleCheckbox.setTooltip(new Tooltip("Only administrators can assign the Admin role"));
        }
    }

    /**
     * Configure dashboard visibility based on user roles
     */
    private void configureDashboardVisibility() {
        if (dashboardMenuLink == null) {
            logger.warn("Dashboard menu link is not initialized");
            return;
        }

        if (currentUser == null) {
            logger.warn("Cannot configure dashboard visibility: currentUser is null");
            return;
        }

        // Hide the dashboard menu item for users with only the USER role
        boolean isAdmin = currentUser.hasRole(tn.esprit.testpifx.models.Role.ADMIN);
        boolean isManager = currentUser.hasRole(tn.esprit.testpifx.models.Role.MANAGER);

        if (!isAdmin && !isManager) {
            dashboardMenuLink.setVisible(false);
            dashboardMenuLink.setManaged(false);
            logger.info("Dashboard menu hidden for user: {}", currentUser.getUsername());
        } else {
            dashboardMenuLink.setVisible(true);
            dashboardMenuLink.setManaged(true);
            logger.info("Dashboard menu visible for user: {}", currentUser.getUsername());
        }
    }
}