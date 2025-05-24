package tn.esprit.testpifx.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.ImageManager;
import tn.esprit.testpifx.utils.SceneManager;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserManagementController {
    // Default profile picture URL to use when none is provided
    private static final String DEFAULT_PROFILE_PICTURE_URL = "https://via.placeholder.com/40";
    // Cache for profile images to avoid reloading them
    private static final java.util.Map<String, Image> imageCache = new java.util.HashMap<>();
    private TeamService teamService;
    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        // Initialize team filter when team service is set
        if (teamFilterComboBox != null) {
            initializeTeamFilter();
        }
    }
    public ProgressIndicator loadingIndicator;
    @FXML private Button homeButton;
    private UserService userService;

    @FXML
    private TableView<User> usersTable;

    private User currentUser;

    // Add these fields to the existing field declarations
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label adminUsersLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<Integer> entriesComboBox;
    @FXML private ComboBox<String> roleFilterComboBox; // Role filter dropdown
    @FXML private ComboBox<String> regionFilterComboBox; // Region filter dropdown
    @FXML private Label pageInfoLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private HBox pageButtonsContainer;
    @FXML private TableColumn<User, String> countryColumn;
    @FXML private ComboBox<TeamItem> teamFilterComboBox; // Team filter dropdown

    // Pagination and search state
    private List<User> allUsers = new ArrayList<>();
    private List<User> filteredUsers = new ArrayList<>();
    private int currentPage = 1;
    private int entriesPerPage = 10;
    private String currentSearchTerm = "";
    private String currentRoleFilter = "All"; // Default role filter
    private String currentRegionFilter = "All"; // Default region filter
    private String currentTeamFilter = "All"; // Default team filter

    public void setUserService(UserService userService) {
        this.userService = userService;
        refreshUsers();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Admin logged in: " + user.getUsername());
    }

    public void initialize() {
        setupTableColumns();
        setupComboBox();
        setupPagination();
        
        // Apply modern table styling
        usersTable.getStyleClass().add("table-modern");
    }

    private void setupTableColumns() {
        usersTable.getColumns().clear();

        // Profile Picture column
        TableColumn<User, String> profilePictureCol = new TableColumn<>("Picture");
        profilePictureCol.setCellValueFactory(new PropertyValueFactory<>("profilePictureUrl"));
        profilePictureCol.setCellFactory(col -> new TableCell<User, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(40);
                imageView.setFitWidth(40);
                imageView.setPreserveRatio(true);
                setGraphic(imageView);
                // Center the image
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String profilePictureUrl, boolean empty) {
                super.updateItem(profilePictureUrl, empty);

                if (empty || profilePictureUrl == null || profilePictureUrl.isEmpty()) {
                    // Use the default image from cache or create it if not cached
                    imageView.setImage(getOrCreateImage(DEFAULT_PROFILE_PICTURE_URL));
                } else {
                    try {
                        // Use the image from cache or create it if not cached
                        imageView.setImage(getOrCreateImage(profilePictureUrl));
                    } catch (Exception e) {
                        // Use the default image if there's an error
                        imageView.setImage(getOrCreateImage(DEFAULT_PROFILE_PICTURE_URL));
                    }
                }
            }
        });
        profilePictureCol.setPrefWidth(60);
        profilePictureCol.setMinWidth(60);
        profilePictureCol.setMaxWidth(80);

        // Username column
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setCellFactory(col -> new TableCell<User, String>() {
            private final Hyperlink link = new Hyperlink();
            
            {
                // Style the hyperlink
                link.getStyleClass().add("username-link");
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
            
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    link.setText(username);
                    link.setOnAction(event -> viewUserProfile(getTableView().getItems().get(getIndex())));
                    setGraphic(link);
                }
            }
        });
        usernameCol.setPrefWidth(110);
        usernameCol.setMinWidth(100);
        
        // First Name column
        TableColumn<User, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(110);
        firstNameCol.setMinWidth(100);
        
        // Last Name column
        TableColumn<User, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(110);
        lastNameCol.setMinWidth(100);

        // Email column
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);
        emailCol.setMinWidth(150);
        
        // Birthdate column
        TableColumn<User, LocalDate> birthdateCol = new TableColumn<>("Birthdate");
        birthdateCol.setCellValueFactory(new PropertyValueFactory<>("birthdate"));
        birthdateCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate birthdate, boolean empty) {
                super.updateItem(birthdate, empty);
                if (empty || birthdate == null) {
                    setText("");
                } else {
                    setText(formatter.format(birthdate));
                }
            }
        });
        birthdateCol.setPrefWidth(100);

        // Status column
        TableColumn<User, Boolean> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                setText(empty ? null : active ? "Active" : "Inactive");
            }
        });
        statusCol.setPrefWidth(80);
        statusCol.setMinWidth(60);

        // Roles column
        TableColumn<User, String> rolesCol = new TableColumn<>("Roles");
        rolesCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            String rolesString = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(rolesString);
        });
        rolesCol.setPrefWidth(100);
        rolesCol.setMinWidth(80);

        // Country column
        TableColumn<User, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        countryCol.setPrefWidth(100);
        countryCol.setMinWidth(80);
        
        // Region/Governorate column
        TableColumn<User, String> regionCol = new TableColumn<>("Region");
        regionCol.setCellValueFactory(new PropertyValueFactory<>("region"));
        regionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String region, boolean empty) {
                super.updateItem(region, empty);
                if (empty || region == null || region.isEmpty()) {
                    setText("");
                } else {
                    setText(region);
                }
            }
        });
        regionCol.setPrefWidth(100);

        // In the setupTableColumns() method, update the actionsCol cell factory:
        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(220);
        actionsCol.setMinWidth(200);
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(editBtn, toggleBtn, deleteBtn);

            {
                // Improve button styling and layout
                pane.setSpacing(5);
                pane.setAlignment(Pos.CENTER);
                
                // Set button sizes to prevent truncation
                editBtn.setMinWidth(60);
                toggleBtn.setMinWidth(80);
                deleteBtn.setMinWidth(60);
                
                // Add CSS classes for better styling
                editBtn.getStyleClass().add("edit-btn");
                toggleBtn.getStyleClass().add("toggle-btn"); 
                deleteBtn.getStyleClass().add("delete-btn");

                // Initialize button text based on initial user status
                updateToggleButtonText();

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                toggleBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleToggleUserStatus(user);
                    // Update button text immediately after action
                    updateToggleButtonText();
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            private void updateToggleButtonText() {
                if (getTableRow() != null && getTableRow().getItem() != null) {
                    User user = getTableRow().getItem();
                    toggleBtn.setText(user.isActive() ? "Deactivate" : "Activate");
                    // Optional: Update button style as well
                    toggleBtn.getStyleClass().removeAll("activate-btn", "deactivate-btn");
                    toggleBtn.getStyleClass().add(user.isActive() ? "deactivate-btn" : "activate-btn");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    updateToggleButtonText();
                    setGraphic(pane);
                }
            }
        });
        
        // Add only the columns that should be visible in the UI
        // Remove countryCol since all users are from Tunisia
        usersTable.getColumns().addAll(
            profilePictureCol, 
            usernameCol,
            emailCol,
            regionCol,
            statusCol,
            actionsCol
        );
    }

    private void setupComboBox() {
        // Initialize entries per page combo box
        entriesComboBox.setItems(FXCollections.observableArrayList(5, 10, 25, 50, 100));
        entriesComboBox.getSelectionModel().select(Integer.valueOf(entriesPerPage));
        entriesComboBox.setOnAction(event -> {
            entriesPerPage = entriesComboBox.getSelectionModel().getSelectedItem();
            currentPage = 1; // Reset to first page when changing entries per page
            refreshData();
        });

        // Initialize role filter combo box with all available roles
        roleFilterComboBox.setItems(FXCollections.observableArrayList("All", "ADMIN", "MANAGER", "USER"));
        roleFilterComboBox.getSelectionModel().select(currentRoleFilter);
        roleFilterComboBox.setOnAction(event -> {
            currentRoleFilter = roleFilterComboBox.getSelectionModel().getSelectedItem();
            currentPage = 1; // Reset to first page when changing role filter
            filterAndDisplayUsers();
        });
        
        // Initialize region filter with Tunisian governorates
        List<String> regions = new ArrayList<>();
        regions.add("All");
        regions.addAll(List.of(
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", 
            "Bizerte", "Béja", "Jendouba", "Kef", "Siliana", "Sousse", 
            "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine", "Sidi Bouzid", 
            "Gabès", "Medenine", "Tataouine", "Gafsa", "Tozeur", "Kebili"
        ));
        regionFilterComboBox.setItems(FXCollections.observableArrayList(regions));
        regionFilterComboBox.getSelectionModel().select(currentRegionFilter);
        regionFilterComboBox.setOnAction(event -> {
            currentRegionFilter = regionFilterComboBox.getSelectionModel().getSelectedItem();
            currentPage = 1; // Reset to first page when changing region filter
            filterAndDisplayUsers();
        });

        // Initialize team filter dropdown
        if (teamFilterComboBox != null) {
            initializeTeamFilter();
        }
    }

    private void initializeTeamFilter() {
        // Create a placeholder list with "All" option
        List<TeamItem> teamItems = new ArrayList<>();
        teamItems.add(new TeamItem("", "All"));
        
        // Only populate teams if teamService is available
        if (teamService != null) {
            // Get all teams and add them to the list
            teamService.getAllTeams().forEach(team -> 
                teamItems.add(new TeamItem(team.getTeamId(), team.getName()))
            );
        }
        
        teamFilterComboBox.setItems(FXCollections.observableArrayList(teamItems));
        teamFilterComboBox.getSelectionModel().selectFirst();  // Select "All" by default
        teamFilterComboBox.setOnAction(event -> {
            TeamItem selectedTeam = teamFilterComboBox.getSelectionModel().getSelectedItem();
            currentTeamFilter = (selectedTeam != null) ? selectedTeam.getTeamId() : "";
            currentPage = 1; // Reset to first page when changing team filter
            filterAndDisplayUsers();
        });
    }
    
    private void setupPagination() {
        prevButton.setOnAction(event -> handlePreviousPage());
        nextButton.setOnAction(event -> handleNextPage());
    }
    
    @FXML
    public void handleSearch() {
        currentSearchTerm = searchField.getText().toLowerCase();
        currentPage = 1; // Reset to first page when searching
        filterAndDisplayUsers();
    }
    
    @FXML
    public void handlePreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayCurrentPage();
        }
    }
    
    @FXML
    public void handleNextPage() {
        int totalPages = calculateTotalPages();
        if (currentPage < totalPages) {
            currentPage++;
            displayCurrentPage();
        }
    }
    
    private void refreshData() {
        if (userService == null) {
            System.err.println("ERROR: UserService is null in UserManagementController!");
            return;
        }

        loadingIndicator.setVisible(true);
        new Thread(() -> {
            try {
                allUsers = userService.getAllUsers();
                Platform.runLater(() -> {
                    updateStatistics();
                    filterAndDisplayUsers();
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Utils.showErrorAlert("Error", "Failed to load users: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void updateStatistics() {
        if (totalUsersLabel != null) {
            int total = allUsers.size();
            totalUsersLabel.setText(String.valueOf(total));
        }
        
        if (activeUsersLabel != null) {
            long active = allUsers.stream().filter(User::isActive).count();
            activeUsersLabel.setText(String.valueOf(active));
        }
        
        if (adminUsersLabel != null) {
            long admins = allUsers.stream()
                .filter(user -> user.getRoles().stream()
                    .anyMatch(role -> role.name().equals("ADMIN")))
                .count();
            adminUsersLabel.setText(String.valueOf(admins));
        }
    }
    
    private void filterAndDisplayUsers() {
        // Filter users based on search term, role filter, region filter, and team filter
        filteredUsers = allUsers.stream()
            .filter(user -> {
                // Text search filter
                boolean matchesSearch = currentSearchTerm.isEmpty() 
                    || user.getUsername().toLowerCase().contains(currentSearchTerm)
                    || (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(currentSearchTerm))
                    || (user.getLastName() != null && user.getLastName().toLowerCase().contains(currentSearchTerm))
                    || user.getEmail().toLowerCase().contains(currentSearchTerm)
                    || (user.getRegion() != null && user.getRegion().toLowerCase().contains(currentSearchTerm));
                
                // Role filter
                boolean matchesRole = currentRoleFilter.equals("All") 
                    || user.getRoles().stream().anyMatch(role -> role.name().equals(currentRoleFilter));
                
                // Region filter
                boolean matchesRegion = currentRegionFilter.equals("All")
                    || (user.getRegion() != null && user.getRegion().equals(currentRegionFilter));
                
                // Team filter (we'll handle this separately due to its complexity)
                boolean matchesTeam = true;
                
                return matchesSearch && matchesRole && matchesRegion && matchesTeam;
            })
            .collect(Collectors.toList());

        // Apply team filter separately if necessary (it requires a database query)
        if (teamFilterComboBox != null && teamService != null) {
            TeamItem selectedTeam = teamFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedTeam != null && !selectedTeam.getTeamId().isEmpty()) {
                // Get users for the selected team and filter our current list
                List<String> teamUserIds = userService.getUsersByTeam(selectedTeam.getTeamId())
                    .stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
                
                filteredUsers = filteredUsers.stream()
                    .filter(user -> teamUserIds.contains(user.getUserId()))
                    .collect(Collectors.toList());
            }
        }
        
        displayCurrentPage();
        updatePageButtons();
    }
    
    private void displayCurrentPage() {
        int startIndex = (currentPage - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, filteredUsers.size());
        
        if (startIndex >= filteredUsers.size()) {
            currentPage = 1; // Reset to first page if current page is out of bounds
            startIndex = 0;
            endIndex = Math.min(entriesPerPage, filteredUsers.size());
        }
        
        List<User> currentPageUsers = filteredUsers.isEmpty() ? 
            List.of() : filteredUsers.subList(startIndex, endIndex);
            
        usersTable.setItems(FXCollections.observableArrayList(currentPageUsers));
        
        // Update page info label
        if (pageInfoLabel != null) {
            int start = filteredUsers.isEmpty() ? 0 : startIndex + 1;
            int end = filteredUsers.isEmpty() ? 0 : endIndex;
            pageInfoLabel.setText(String.format("Showing %d to %d of %d entries", 
                start, end, filteredUsers.size()));
        }
        
        // Update button states
        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= calculateTotalPages());
    }
    
    private void updatePageButtons() {
        pageButtonsContainer.getChildren().clear();
        
        int totalPages = calculateTotalPages();
        if (totalPages <= 1) {
            return;
        }
        
        // Calculate range of page buttons to show
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, startPage + 4);
        
        // Adjust start if we're near the end
        if (endPage - startPage < 4) {
            startPage = Math.max(1, endPage - 4);
        }
        
        for (int i = startPage; i <= endPage; i++) {
            final int pageNum = i;
            Button pageButton = new Button(String.valueOf(pageNum));
            pageButton.getStyleClass().add("pagination-button-modern");
            
            if (pageNum == currentPage) {
                pageButton.getStyleClass().add("current-page");
                pageButton.setStyle("-fx-background-color: #4e73df; -fx-text-fill: white;");
            }
            
            pageButton.setOnAction(e -> {
                currentPage = pageNum;
                displayCurrentPage();
                updatePageButtons();
            });
            
            pageButtonsContainer.getChildren().add(pageButton);
        }
    }
    
    private int calculateTotalPages() {
        return (int) Math.ceil((double) filteredUsers.size() / entriesPerPage);
    }
    
    public void refreshUsers() {
        refreshData();
    }

    /**
     * Gets an image from the cache or creates it if it doesn't exist.
     * This improves performance by avoiding loading the same image multiple times.
     * 
     * @param url The URL or path of the image
     * @return The image from the cache or a newly created image
     */
    private Image getOrCreateImage(String url) {
        return imageCache.computeIfAbsent(url, imageUrl -> {
            try {
                // Check if it's a URL or a path
                if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    return new Image(imageUrl, true); // true for background loading
                } else {
                    // Use ImageManager to get the absolute path
                    String absolutePath = ImageManager.getAbsolutePath(imageUrl);
                    if (absolutePath != null && ImageManager.imageExists(absolutePath)) {
                        return new Image(new File(absolutePath).toURI().toString(), true);
                    } else {
                        // If the file doesn't exist, use the default image
                        return getOrCreateImage(DEFAULT_PROFILE_PICTURE_URL);
                    }
                }
            } catch (Exception e) {
                // If there's an error loading the image, use the default image
                if (!imageUrl.equals(DEFAULT_PROFILE_PICTURE_URL)) {
                    return getOrCreateImage(DEFAULT_PROFILE_PICTURE_URL);
                }
                // If there's an error loading the default image, create a new one
                return new Image(DEFAULT_PROFILE_PICTURE_URL);
            }
        });
    }

    private void handleToggleUserStatus(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Action");
        confirmation.setHeaderText(null);
        confirmation.setContentText(user.isActive()
                ? "Are you sure you want to deactivate this user?"
                : "Are you sure you want to activate this user?");
        confirmation.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (user.isActive()) {
                userService.disableUser(user.getUserId());
            } else {
                userService.enableUser(user.getUserId());
            }
            refreshUsers();
        }
    }

    private void handleDeleteUser(User user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this user?");
        confirmation.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.deleteUser(user.getUserId());
            refreshUsers();
        }
    }

    @FXML
    private void handleHome() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) homeButton.getScene().getWindow();
            
            // Ensure stage is maximized before the transition
            stage.setMaximized(true);
            
            WelcomeController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/welcome.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load welcome screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            // First, clear the user's saved preferences to prevent auto re-login
            UserPreferences.clearLoginCredentials();
            System.out.println("Cleared saved login credentials during logout");
            
            // Remove user from connected users list
            if (currentUser != null) {
                UserSessionManager.removeConnectedUser(currentUser);
                System.out.println("User logged out from management: " + currentUser.getUsername());
            }

            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            AuthController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/login.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
        } catch (IOException e) {
            Utils.showErrorAlert("Failed to load login screen", "Failed to load login screen");
            e.printStackTrace();
        }
    }

    public void handleAddUser() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            UserFormController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/user_form.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setUserToEdit(null);
            controller.setCurrentUser(currentUser);
            controller.setStage(stage);
        } catch (IOException e) {
            System.err.println("Error loading user form: " + e.getMessage());
            Utils.showErrorAlert("Error", "Failed to load user form");
        }
    }

    private void showUserForm(User user) {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            UserFormController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/user_form.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setUserToEdit(user);
            controller.setCurrentUser(currentUser);
            controller.setStage(stage);
        } catch (IOException e) {
            System.err.println("Error loading user form: " + e.getMessage());
            Utils.showErrorAlert("Error", "Failed to load user form");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshUsers();
        System.out.println("refreshed");
    }

    public UserService getUserService() {
        return userService;
    }

    public TeamService getTeamService() {
        return teamService;
    }

    public TableView<User> getUsersTable() {
        return usersTable;
    }

    /**
     * Handles editing a user from the table.
     * Redirects to the user profile page in edit mode.
     * 
     * @param user The user to edit
     */
    private void handleEditUser(User user) {
        if (user == null) {
            Utils.showErrorAlert("Error", "No user selected for editing");
            return;
        }
        
        try {
            // Use SceneManager to navigate to the profile page
            Stage stage = (Stage) usersTable.getScene().getWindow();
            ProfileController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/profile.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(user);
            
            // Enable edit mode directly
            Platform.runLater(() -> {
                controller.enableEditMode();
            });
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load profile view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the user profile view when a username is clicked
     * 
     * @param user The selected user to view
     */
    private void viewUserProfile(User user) {
        if (user == null) {
            Utils.showErrorAlert("Error", "No user selected to view");
            return;
        }
        
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            ProfileController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/profile.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(user);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load profile view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for navigating to Team Management screen from sidebar
     */
    @FXML
    private void handleTeamManagement() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            
            // Ensure stage is maximized before the transition
            stage.setMaximized(true);
            
            TeamManagementController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/team_management.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load team management screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for navigating to Profile screen from sidebar
     */
    @FXML
    private void handleProfile() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) usersTable.getScene().getWindow();
            ProfileController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/profile.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load profile screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Inner class to represent a team in the ComboBox
    private class TeamItem {
        private final String teamId;
        private final String teamName;

        public TeamItem(String teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }

        public String getTeamId() { return teamId; }
        public String getTeamName() { return teamName; }

        @Override
        public String toString() {
            return teamName;
        }
    }
}
