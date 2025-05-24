package tn.esprit.testpifx.controllers;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.testpifx.models.Team;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;
import tn.esprit.testpifx.Main;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the team management view.
 * Allows admins and managers to create, edit, and delete teams, as well as manage team members.
 */
public class TeamManagementController {
    @FXML private Button homeButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ListView<Team> teamListView;
    @FXML private Label teamNameLabel;
    @FXML private Label teamDescriptionLabel;
    @FXML private Label teamCreatorLabel;
    @FXML private Label teamMemberCountLabel;
    @FXML private TableView<User> teamMembersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Button editTeamButton;
    @FXML private Button deleteTeamButton;
    @FXML private Button addMemberButton;
    private TeamService teamService;
    private UserService userService;
    private User currentUser;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Initialize table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Apply modern table styling
        teamMembersTable.getStyleClass().add("modern-table");
        
        // Customize team list view cell display to show team name and member count
        teamListView.setCellFactory(param -> new ListCell<Team>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                
                if (empty || team == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // For initial rendering, show just the name
                    // Member count will be updated when teams are loaded
                    setText(team.getName());
                    
                    // Apply styling
                    getStyleClass().add("team-list-item");
                }
            }
        });

        // Set up the actions column
        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button removeButton = new Button("Remove");
            private final HBox pane = new HBox(removeButton);

            {
                pane.setSpacing(5);
                removeButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleRemoveMember(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        // Set up selection listener for the team list
        teamListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayTeamDetails(newVal);
            } else {
                clearTeamDetails();
            }
        });

        // Bind button disable properties to selection state
        BooleanBinding noSelection = Bindings.isNull(teamListView.getSelectionModel().selectedItemProperty());
        editTeamButton.disableProperty().bind(noSelection);
        deleteTeamButton.disableProperty().bind(noSelection);
        addMemberButton.disableProperty().bind(noSelection);

        // Set button actions
        editTeamButton.setOnAction(event -> handleEditTeam());
        deleteTeamButton.setOnAction(event -> handleDeleteTeam());
        addMemberButton.setOnAction(event -> handleAddMember());
    }
    /**
     * Sets the team service.
     * 
     * @param teamService The team service to use
     */
    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        
        // Once services are fully set, call refreshTeams
        if (userService != null && currentUser != null) {
            // First ensure all teams have at least 2 members
            new Thread(() -> {
                ensureMinimumTeamMembers();
                Platform.runLater(this::refreshTeams);
            }).start();
        } else {
            System.out.println("UserService or CurrentUser not set yet, deferring refresh");
        }
    }

    /**
     * Sets the user service.
     * 
     * @param userService The user service to use
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
        
        // If team service was previously set but couldn't refresh due to missing userService
        if (teamService != null && currentUser != null) {
            // First ensure all teams have at least 2 members
            new Thread(() -> {
                ensureMinimumTeamMembers();
                Platform.runLater(this::refreshTeams);
            }).start();
        }
    }

    /**
     * Sets the current user.
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        // If both services were previously set but couldn't refresh due to missing currentUser
        if (teamService != null && userService != null) {
            // First ensure all teams have at least 2 members
            new Thread(() -> {
                ensureMinimumTeamMembers();
                Platform.runLater(this::refreshTeams);
            }).start();
        }
    }

    /**
     * Refreshes the list of teams.
     */
    @FXML
    private void refreshTeams() {
        // Show loading indicator
        loadingIndicator.setVisible(true);
        teamListView.setVisible(false);

        // Use a background thread to avoid freezing the UI
        new Thread(() -> {
            try {
                // Get teams based on user role
                List<Team> teams;
                if (currentUser.hasRole(tn.esprit.testpifx.models.Role.ADMIN)) {
                    // Admins can see all teams
                    teams = teamService.getAllTeams();
                } else if (currentUser.hasRole(tn.esprit.testpifx.models.Role.MANAGER)) {
                    // Managers can see teams they created and teams they're a member of
                    Set<Team> managerTeams = new HashSet<>();
                    managerTeams.addAll(teamService.getTeamsByCreator(currentUser.getUserId()));
                    managerTeams.addAll(teamService.getTeamsByMember(currentUser.getUserId()));
                    teams = new ArrayList<>(managerTeams);
                } else {
                    // Regular users can only see teams they're a member of
                    teams = teamService.getTeamsByMember(currentUser.getUserId());
                }
                
                // Update the UI on the JavaFX application thread
                Platform.runLater(() -> {
                    // Create a custom ListCell that shows team name and member count
                    teamListView.setCellFactory(param -> new ListCell<Team>() {
                        @Override
                        protected void updateItem(Team team, boolean empty) {
                            super.updateItem(team, empty);
                            
                            if (empty || team == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                // Get member count for each team
                                Set<String> memberIds = teamService.getTeamMembers(team.getTeamId());
                                int memberCount = memberIds.size();
                                
                                // Format display string with name and member count
                                setText(String.format("%s (%d members)", team.getName(), memberCount));
                                
                                // Apply styling
                                getStyleClass().add("team-list-item");
                            }
                        }
                    });
                    
                    teamListView.setItems(FXCollections.observableArrayList(teams));
                    loadingIndicator.setVisible(false);
                    teamListView.setVisible(true);

                    // Select the first team if available
                    if (!teams.isEmpty()) {
                        teamListView.getSelectionModel().selectFirst();
                    } else {
                        clearTeamDetails();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Utils.showErrorAlert("Error", "Failed to load teams: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                    teamListView.setVisible(true);
                });
            }
        }).start();
    }

    /**
     * Displays the details of the selected team.
     * 
     * @param team The team to display
     */
    private void displayTeamDetails(Team team) {
        teamNameLabel.setText(team.getName());
        teamDescriptionLabel.setText(team.getDescription());

        // Get the creator's username
        userService.getUserById(team.getCreatedBy())
                .ifPresentOrElse(
                        creator -> teamCreatorLabel.setText(creator.getUsername()),
                        () -> teamCreatorLabel.setText("[Unknown]")
                );

        // Get the member count
        Set<String> memberIds = teamService.getTeamMembers(team.getTeamId());
        teamMemberCountLabel.setText(String.valueOf(memberIds.size()));

        // Load team members
        loadTeamMembers(team);
    }

    /**
     * Loads the members of the selected team.
     * 
     * @param team The team to load members for
     */
    private void loadTeamMembers(Team team) {
        // Show loading indicator
        loadingIndicator.setVisible(true);
        teamMembersTable.setVisible(false);

        // Use a background thread to avoid freezing the UI
        new Thread(() -> {
            try {
                // Debug the team member ids that we're starting with
                Set<String> memberIds = team.getMemberIds();
                System.out.println("Initial team member IDs in team object: " + memberIds);
                
                // Get database connection details
                tn.esprit.testpifx.utils.DatabaseConfig dbConfig = new tn.esprit.testpifx.utils.DatabaseConfig();
                String url = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC", 
                    dbConfig.getHost(), dbConfig.getPort(), dbConfig.getDatabase());
                
                // Direct SQL query to get ALL members
                List<User> members = new ArrayList<>();
                System.out.println("Querying database directly for team members of team: " + team.getName() + " (ID: " + team.getTeamId() + ")");
                
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, dbConfig.getUsername(), dbConfig.getPassword())) {
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM team_members WHERE team_id = ?")) {
                        stmt.setString(1, team.getTeamId());
                        
                        // Print the SQL for debugging
                        System.out.println("Executing SQL: " + stmt.toString());
                        
                        try (java.sql.ResultSet rs = stmt.executeQuery()) {
                            // Create a new set for member IDs from DB
                            Set<String> memberIdsFromDb = new HashSet<>();
                            
                            while (rs.next()) {
                                String memberId = rs.getString("user_id");
                                memberIdsFromDb.add(memberId);
                                System.out.println("Found member ID in database: " + memberId);
                            }
                            
                            System.out.println("Total members found in DB: " + memberIdsFromDb.size());
                            
                            // For each member ID from DB, fetch the full user object
                            for (String memberId : memberIdsFromDb) {
                                Optional<User> userOpt = userService.getUserById(memberId);
                                if (userOpt.isPresent()) {
                                    User member = userOpt.get();
                                    members.add(member);
                                    System.out.println("Added member to list: " + member.getUsername());
                                } else {
                                    System.out.println("WARNING: User not found for ID: " + memberId);
                                }
                            }
                        }
                    }
                }

                // Log for debugging purposes
                System.out.println("Found " + members.size() + " members for team " + team.getName());
                for (User member : members) {
                    System.out.println("  - Member: " + member.getUsername() + " (ID: " + member.getUserId() + ")");
                }

                // Update the UI on the JavaFX application thread
                Platform.runLater(() -> {
                    if (members.isEmpty()) {
                        // If no members, show appropriate message
                        teamMembersTable.setPlaceholder(new Label("No members found for this team"));
                    } else {
                        // Display members in table
                        teamMembersTable.setItems(FXCollections.observableArrayList(members));
                    }
                    
                    // Update member count label
                    teamMemberCountLabel.setText(String.valueOf(members.size()));
                    
                    // Hide loading indicator and show table
                    loadingIndicator.setVisible(false);
                    teamMembersTable.setVisible(true);
                });
            } catch (Exception e) {
                e.printStackTrace();  // More detailed error logging
                Platform.runLater(() -> {
                    Utils.showErrorAlert("Error", "Failed to load team members: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                    teamMembersTable.setVisible(true);
                    
                    // Set placeholder with error message
                    teamMembersTable.setPlaceholder(new Label("Error loading team members: " + e.getMessage()));
                });
            }
        }).start();
    }

    /**
     * Clears the team details.
     */
    private void clearTeamDetails() {
        teamNameLabel.setText("[No team selected]");
        teamDescriptionLabel.setText("");
        teamCreatorLabel.setText("");
        teamMemberCountLabel.setText("0");
        teamMembersTable.setItems(FXCollections.observableArrayList());
    }

    /**
     * Handles the add team button click.
     */
    @FXML
    private void handleAddTeam() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) teamListView.getScene().getWindow();
            TeamFormController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/team_form.fxml");
            
            // Configure the controller after loading
            controller.setTeamService(teamService);
            controller.setUserService(userService);
            controller.setCurrentUser(currentUser);
            controller.setStage(stage);
        } catch (IOException e) {
            System.err.println("Error loading team form: " + e.getMessage());
            Utils.showErrorAlert("Error", "Failed to load team form");
        }
    }

    /**
     * Handles the edit team button click.
     */
    @FXML
    private void handleEditTeam() {
        Team selectedTeam = teamListView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            Utils.showErrorAlert("Error", "No team selected for editing");
            return;
        }

        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) teamListView.getScene().getWindow();
            TeamFormController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/team_form.fxml");
            
            // Configure the controller after loading
            controller.setTeamService(teamService);
            controller.setUserService(userService);
            controller.setCurrentUser(currentUser);
            controller.setTeamToEdit(selectedTeam);
            controller.setStage(stage);
        } catch (IOException e) {
            System.err.println("Error loading team form: " + e.getMessage());
            Utils.showErrorAlert("Error", "Failed to load team form");
        }
    }

    /**
     * Handles the delete team button click.
     */
    @FXML
    private void handleDeleteTeam() {
        Team selectedTeam = teamListView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            Utils.showErrorAlert("Error", "No team selected for deletion");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete this team?");
        confirmation.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            teamService.deleteTeam(selectedTeam.getTeamId(), currentUser);  // Added currentUser parameter
            refreshTeams();
        }
    }

    /**
     * Handles the add member button click.
     */
    @FXML
    private void handleAddMember() {
        Team selectedTeam = teamListView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            Utils.showErrorAlert("Error", "No team selected");
            return;
        }

        try {
            // Create a dialog for adding members with search functionality
            Dialog<User> dialog = new Dialog<>();
            dialog.setTitle("Add Team Member");
            dialog.setHeaderText("Select a user to add to the team");
            
            // Apply custom styling to match application theme
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());
            
            // Create the content layout
            VBox content = new VBox();
            content.setSpacing(10);
            content.setPadding(new Insets(10));
            
            // Add search field with instructions
            Label searchLabel = new Label("Search by name, email or username:");
            TextField searchField = new TextField();
            searchField.setPromptText("Type to search...");
            searchField.setPrefWidth(400);
            
            // Create list view for displaying users
            ListView<User> usersListView = new ListView<>();
            usersListView.setPrefHeight(300);
            usersListView.setPrefWidth(400);
            
            // Set custom cell factory to display user info properly
            usersListView.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        String displayText = user.getUsername();
                        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                            displayText += " • " + user.getEmail();
                        }
                        setText(displayText);
                    }
                }
            });
            
            // Add components to content layout
            content.getChildren().addAll(searchLabel, searchField, usersListView);
            
            // Set up initial data
            List<User> availableUsers = loadAvailableUsersForTeam(selectedTeam);
            usersListView.setItems(FXCollections.observableArrayList(availableUsers));
            
            // Add search functionality
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.isEmpty()) {
                    // If search is empty, show all available users
                    usersListView.setItems(FXCollections.observableArrayList(availableUsers));
                } else {
                    // Filter users based on search term (case-insensitive)
                    String searchTerm = newValue.toLowerCase();
                    List<User> filteredUsers = availableUsers.stream()
                        .filter(user -> 
                            (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchTerm)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchTerm))
                        )
                        .collect(Collectors.toList());
                    
                    usersListView.setItems(FXCollections.observableArrayList(filteredUsers));
                }
            });
            
            // Set dialog content and add buttons
            dialogPane.setContent(content);
            dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            // Disable OK button until user selects someone
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            okButton.setDisable(true);
            
            usersListView.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> okButton.setDisable(newValue == null));
            
            // Convert dialog result to User object
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return usersListView.getSelectionModel().getSelectedItem();
                }
                return null;
            });
            
            // Show dialog and handle adding member
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(user -> {
                try {
                    teamService.addMember(selectedTeam.getTeamId(), user.getUserId(), currentUser);
                    // Refresh the team details and member list
                    loadTeamMembers(selectedTeam);
                    refreshTeams(); // Refresh to update member count
                } catch (Exception e) {
                    Utils.showErrorAlert("Error", "Failed to add member: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Utils.showErrorAlert("Error", "An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to load available users for a team (users who aren't already members)
     * 
     * @param team The team to check against
     * @return List of available users
     */
    private List<User> loadAvailableUsersForTeam(Team team) {
        try {
            List<User> allUsers = userService.getAllUsers();
            Set<String> teamMemberIds = teamService.getTeamMembers(team.getTeamId());
            
            return allUsers.stream()
                .filter(user -> !teamMemberIds.contains(user.getUserId()))
                .sorted(Comparator.comparing(User::getUsername))
                .collect(Collectors.toList());
        } catch (Exception e) {
            Utils.showErrorAlert("Error", "Failed to load available users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Handles removing a member from the team.
     * 
     * @param user The user to remove
     */
    private void handleRemoveMember(User user) {
        Team selectedTeam = teamListView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            Utils.showErrorAlert("Error", "No team selected");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Remove");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to remove this member from the team?");
        confirmation.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                teamService.removeMember(selectedTeam.getTeamId(), user.getUserId(), currentUser);
                loadTeamMembers(selectedTeam);
                
                // Update the member count in team list and details
                refreshTeams();
                
                // Update the member count label immediately for better UX
                Set<String> memberIds = teamService.getTeamMembers(selectedTeam.getTeamId());
                teamMemberCountLabel.setText(String.valueOf(memberIds.size()));
            } catch (Exception e) {
                Utils.showErrorAlert("Error", "Failed to remove member: " + e.getMessage());
            }
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        refreshTeams();
    }

    /**
     * Handles the home button click.
     */
    @FXML
    private void handleHome() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) homeButton.getScene().getWindow();
            WelcomeController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/welcome.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load welcome screen: " + e.getMessage());
        }
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        try {
            // First, clear the user's saved preferences to prevent auto re-login
            UserPreferences.clearLoginCredentials();
            System.out.println("Cleared saved login credentials during logout");
            
            // Remove user from connected users list
            if (currentUser != null) {
                UserSessionManager.removeConnectedUser(currentUser);
                System.out.println("User logged out: " + currentUser.getUsername());
            }

            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) homeButton.getScene().getWindow();
            AuthController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/login.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for navigating to User Management screen from sidebar
     */
    @FXML
    private void handleUserManagement() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) homeButton.getScene().getWindow();
            UserManagementController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/user_management.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load user management screen: " + e.getMessage());
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
            Stage stage = (Stage) homeButton.getScene().getWindow();
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

    /**
     * Ensures all teams have at least 2 members
     * This method will check each team and add additional members if needed
     */
    private void ensureMinimumTeamMembers() {
        if (teamService == null || userService == null) {
            System.err.println("Cannot ensure minimum team members: services not initialized");
            return;
        }
        
        System.out.println("Verifying all teams have at least 2 members...");
        
        try {
            // Get all teams
            List<Team> allTeams = teamService.getAllTeams();
            System.out.println("Found " + allTeams.size() + " teams total");
            
            // Get all available users who can be added to teams
            List<User> allUsers = userService.getAllUsers();
            System.out.println("Found " + allUsers.size() + " users available for team assignment");
            
            // Keep track of teams that were modified
            List<String> modifiedTeams = new ArrayList<>();
            
            for (Team team : allTeams) {
                // Get current team member count from database
                Set<String> memberIds = teamService.getTeamMembers(team.getTeamId());
                
                if (memberIds.size() < 2) {
                    System.out.println("Team '" + team.getName() + "' has only " + memberIds.size() + " members - adding more members");
                    
                    // How many members do we need to add?
                    int membersToAdd = 2 - memberIds.size();
                    int added = 0;
                    
                    // Try to add 'admin', 'manager', and 'user' first since they definitely exist
                    for (String defaultUsername : Arrays.asList("admin", "manager", "user")) {
                        Optional<User> defaultUserOpt = userService.findByUsername(defaultUsername);
                        
                        if (defaultUserOpt.isPresent()) {
                            User defaultUser = defaultUserOpt.get();
                            
                            // Skip if user is already a member
                            if (memberIds.contains(defaultUser.getUserId())) {
                                continue;
                            }
                            
                            // Add user to team
                            try {
                                teamService.addMember(team.getTeamId(), defaultUser.getUserId(), currentUser);
                                System.out.println("Added default user '" + defaultUsername + "' to team '" + team.getName() + "'");
                                added++;
                                
                                if (added >= membersToAdd) {
                                    break; // We've added enough members
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to add " + defaultUsername + " to team: " + e.getMessage());
                            }
                        }
                    }
                    
                    // If we still need more members, try adding any other available users
                    if (added < membersToAdd) {
                        for (User user : allUsers) {
                            // Skip if user is already a member
                            if (memberIds.contains(user.getUserId())) {
                                continue;
                            }
                            
                            // Add user to team
                            try {
                                teamService.addMember(team.getTeamId(), user.getUserId(), currentUser);
                                System.out.println("Added user '" + user.getUsername() + "' to team '" + team.getName() + "'");
                                added++;
                                
                                if (added >= membersToAdd) {
                                    break; // We've added enough members
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to add " + user.getUsername() + " to team: " + e.getMessage());
                            }
                        }
                    }
                    
                    // Record this team was modified
                    modifiedTeams.add(team.getName());
                } else {
                    System.out.println("Team '" + team.getName() + "' already has " + memberIds.size() + " members - no action needed");
                }
            }
            
            // Log summary of actions
            if (!modifiedTeams.isEmpty()) {
                System.out.println("Fixed " + modifiedTeams.size() + " teams to ensure minimum membership: " + modifiedTeams);
            } else {
                System.out.println("All teams already have at least 2 members - no changes needed");
            }
            
        } catch (Exception e) {
            System.err.println("Error ensuring minimum team members: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
