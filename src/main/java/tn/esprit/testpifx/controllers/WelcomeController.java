package tn.esprit.testpifx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WelcomeController {
    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    @FXML private Label welcomeLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalTeamsLabel;
    @FXML private Label recentActivityLabel;
    @FXML private Label userStatsLabel;
    @FXML private Label countryStatsLabel;
    @FXML private Label genderStatsLabel;
    // New labels for added statistics cards
    @FXML private Label regionStatsLabel;
    @FXML private Label userRolesLabel;
    @FXML private Label avgTeamSizeLabel;
    @FXML private Label genderRatioLabel;
      @FXML private TableView<ActivityData> activityTable;
    @FXML private TableColumn<ActivityData, String> activityTypeColumn;
    @FXML private TableColumn<ActivityData, String> activityUserColumn;
    @FXML private TableColumn<ActivityData, String> activityDescriptionColumn;
    @FXML private TableColumn<ActivityData, String> activityTimeColumn;
    @FXML private Button logoutButton;
    @FXML private Label userCountLabel;
    @FXML private Label teamCountLabel;
    
    // Updated to match the IDs in welcome.fxml
    @FXML private HBox userManagementBtn;
    @FXML private HBox teamManagementBtn;
    
    // New fields for the added charts
    @FXML private javafx.scene.chart.LineChart<String, Number> activityLineChart;
    @FXML private javafx.scene.chart.CategoryAxis activityXAxis;
    @FXML private javafx.scene.chart.NumberAxis activityYAxis;
    @FXML private javafx.scene.chart.PieChart userDistributionChart;
    
    // New fields for additional charts
    @FXML private javafx.scene.chart.PieChart regionBarChart;
    @FXML private javafx.scene.chart.PieChart genderDistributionChart;
    @FXML private javafx.scene.chart.StackedBarChart<String, Number> teamSizeChart;
    @FXML private javafx.scene.chart.PieChart roleDistributionChart;
    @FXML private javafx.scene.chart.PieChart teamDistributionChart;
    @FXML private Label topRegionsLabel;
    @FXML private Label avgTeamSizeValue;
    @FXML private Label genderRatioValue;
    
    // New fields for team table
    @FXML private TableView<TeamTableData> teamStatsTable;
    @FXML private TableColumn<TeamTableData, String> teamNameColumn;
    @FXML private TableColumn<TeamTableData, Integer> teamMembersColumn; 
    @FXML private TableColumn<TeamTableData, String> teamTypeColumn;
    
    // Simple data class for team table
    private static class TeamTableData {
        private final SimpleStringProperty teamName;
        private final SimpleIntegerProperty memberCount;
        private final SimpleStringProperty teamType;
        
        public TeamTableData(String teamName, int memberCount, String teamType) {
            this.teamName = new SimpleStringProperty(teamName);
            this.memberCount = new SimpleIntegerProperty(memberCount);
            this.teamType = new SimpleStringProperty(teamType);
        }
        
        public String getTeamName() { return teamName.get(); }
        public SimpleStringProperty teamNameProperty() { return teamName; }
        
        public int getMemberCount() { return memberCount.get(); }
        public SimpleIntegerProperty memberCountProperty() { return memberCount; }
        
        public String getTeamType() { return teamType.get(); }
        public SimpleStringProperty teamTypeProperty() { return teamType; }
    }
    
    // Notification handling
    @FXML private HBox notificationIcon;
    @FXML private javafx.scene.shape.Circle notificationBadge;
    private int notificationCount = 1; // Start with 1 notification

    private User currentUser;
    private UserService userService;
    private TeamService teamService;    @FXML
    private void initialize() {        // Initialize UI components and set up event handlers
        setupUIComponents();
        
        // Initialize charts directly without additional Platform.runLater
        // to reduce screen flickering during initialization
        initializeCharts();
        
        // Check if currentUser is null but we have a session user
        if (currentUser == null && UserSessionManager.isSessionValid()) {
            System.out.println("Welcome controller initialized without currentUser but session is valid, retrieving from session");
            currentUser = UserSessionManager.getCurrentUser();
        }
        
        // Check if services are already set (they might be in some navigation scenarios)
        if (userService != null && teamService != null && currentUser != null) {
            updateStatistics();
            configureRoleBasedDashboard();
            System.out.println("Welcome controller initialized with services and user: " + currentUser.getUsername());
        } else {
            System.out.println("Welcome controller missing initialization components: " +
                              "userService=" + (userService != null) +
                              ", teamService=" + (teamService != null) + 
                              ", currentUser=" + (currentUser != null));
        }
    }

    private void setupUIComponents() {
        // Initialize any UI components that don't depend on services
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome to the Dashboard");
        }
    }

    /**
     * Initialize all charts with placeholder data to ensure they render properly
     */
    private void initializeCharts() {
        // Initialize gender distribution chart with placeholder data
        if (genderDistributionChart != null) {
            System.out.println("Initializing gender distribution chart with placeholder data");
            genderDistributionChart.getData().clear();
            
            javafx.scene.chart.PieChart.Data maleSlice = new javafx.scene.chart.PieChart.Data("Male", 10);
            javafx.scene.chart.PieChart.Data femaleSlice = new javafx.scene.chart.PieChart.Data("Female", 8);
            
            genderDistributionChart.getData().addAll(maleSlice, femaleSlice);
            
            // Make sure the chart is visible
            genderDistributionChart.setVisible(true);
        } else {
            System.err.println("Gender distribution chart is null in initialize - this is a problem with your FXML loading");
        }
        
        // Initialize other charts as needed with placeholder data
        if (roleDistributionChart != null) {
            roleDistributionChart.getData().clear();
            roleDistributionChart.getData().addAll(
                new javafx.scene.chart.PieChart.Data("Admins", 2),
                new javafx.scene.chart.PieChart.Data("Managers", 3),
                new javafx.scene.chart.PieChart.Data("Users", 10)
            );
        }
        
        if (userDistributionChart != null) {
            userDistributionChart.getData().clear();
            userDistributionChart.getData().addAll(
                new javafx.scene.chart.PieChart.Data("Admins", 2),
                new javafx.scene.chart.PieChart.Data("Managers", 3),
                new javafx.scene.chart.PieChart.Data("Users", 10)
            );
        }
    }

    /**
     * Configures dashboard elements based on user role
     * Shows/hides elements depending on whether the user is ADMIN, MANAGER, or USER
     */
    private void configureRoleBasedDashboard() {
        if (currentUser == null) return;
        
        boolean isAdmin = currentUser.getRoles().contains(Role.ADMIN);
        boolean isManager = currentUser.getRoles().contains(Role.MANAGER);
        
        System.out.println("Configuring dashboard for user: " + currentUser.getUsername() + 
                           ", isAdmin: " + isAdmin + ", isManager: " + isManager);
        
        // Admin menu items are visible to Admins only
        if (userManagementBtn != null) {
            userManagementBtn.setVisible(isAdmin);
            userManagementBtn.setManaged(isAdmin);
        }
        
        // Team management is visible to Admins and Managers
        if (teamManagementBtn != null) {
            teamManagementBtn.setVisible(isAdmin || isManager);
            teamManagementBtn.setManaged(isAdmin || isManager);
        }
        
        // Advanced statistics cards and charts are visible to Admins and Managers
        configureStatisticsVisibility(isAdmin, isManager);
        
        // Force initialization of charts with sample data
        javafx.application.Platform.runLater(() -> {
            // Create sample data for region chart
            Map<String, Long> sampleRegionCounts = new java.util.HashMap<>();
            sampleRegionCounts.put("Tunis", 12L);
            sampleRegionCounts.put("Sousse", 8L);
            sampleRegionCounts.put("Sfax", 7L);
            sampleRegionCounts.put("Bizerte", 5L);
            sampleRegionCounts.put("Monastir", 3L);
            
            populateRegionBarChart(sampleRegionCounts);
            
            // Force gender chart sample data
            populateGenderDistributionChart(15L, 12L);
            
            // Force team distribution chart sample data
            java.util.ArrayList<tn.esprit.testpifx.models.Team> sampleTeams = new java.util.ArrayList<>();
            populateTeamSizeChart(sampleTeams);
            
            // Force role distribution chart
            populateRoleDistributionChart(2L, 3L, 10L);
            
            // Force user distribution chart
            populateUserDistributionChart(2L, 3L, 10L);
            
            // Populate activity table
            populateActivityTable();
        });
    }
    
    /**
     * Controls visibility of advanced statistics based on user role
     */
    private void configureStatisticsVisibility(boolean isAdmin, boolean isManager) {
        // Find parent containers for advanced stat sections
        if (regionBarChart != null && regionBarChart.getParent() != null) {
            HBox regionalChartContainer = (HBox) regionBarChart.getParent().getParent().getParent();
            regionalChartContainer.setVisible(isAdmin);
            regionalChartContainer.setManaged(isAdmin);
        }
        
        // Admin-only statistics
        if (teamStatsTable != null && teamStatsTable.getParent() != null) {
            VBox teamStatsContainer = findParentVBox(teamStatsTable);
            if (teamStatsContainer != null) {
                teamStatsContainer.setVisible(isAdmin);
                teamStatsContainer.setManaged(isAdmin);
            }
        }
        
        // Regular users only see basic statistics and their own teams
        if (activityTable != null && activityTable.getParent() != null) {
            VBox activityContainer = findParentVBox(activityTable);
            if (activityContainer != null) {
                activityContainer.setVisible(isAdmin || isManager);
                activityContainer.setManaged(isAdmin || isManager);
            }
        }
    }
    
    /**
     * Helper method to find the parent VBox of an element
     */
    private VBox findParentVBox(javafx.scene.Node node) {
        javafx.scene.Parent parent = node.getParent();
        while (parent != null && !(parent instanceof VBox)) {
            parent = parent.getParent();
        }
        return (VBox) parent;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
        System.out.println("WelcomeController: UserService set to " + (userService != null));
        tryUpdateStatistics();
    }

    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        System.out.println("WelcomeController: TeamService set to " + (teamService != null));
        tryUpdateStatistics();
    }    /**
     * Sets the current user and updates the UI accordingly.
     * This is a critical method that controls session persistence.
     *
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        System.out.println("WelcomeController.setCurrentUser called with user: " + 
                          (user != null ? user.getUsername() : "null"));
        
        // If user is null but we have a session user, use that instead
        if (user == null && UserSessionManager.isSessionValid()) {
            user = UserSessionManager.getCurrentUser();
            System.out.println("User was null but found session user: " + 
                              (user != null ? user.getUsername() : "still null"));
        }
        
        // Store the user reference locally
        this.currentUser = user;
          // Store user in session manager to ensure persistence across scene changes
        if (user != null) {
            UserSessionManager.addConnectedUser(user);
            System.out.println("User added to session manager: " + user.getUsername());
            
            // Store a final reference to the user for the lambda
            final User finalUser = user;
            
            // Defer UI updates to ensure JavaFX thread is ready
            Platform.runLater(() -> {
                updateUIForUser(finalUser);
            });
        } else {
            System.out.println("WARNING: Attempted to set null user in WelcomeController");
        }
    }
    
    /**
     * Updates the UI for the current user, handling role-specific redirections
     * and dashboard configuration.
     * 
     * @param user The user to update the UI for
     */
    private void updateUIForUser(User user) {
        if (user == null) return;
        
        try {
            // Update welcome label if available
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
                System.out.println("WelcomeController: Welcome label updated with username: " + user.getUsername());
                
                // Check user roles
                boolean isAdmin = user.getRoles().contains(Role.ADMIN);
                boolean isManager = user.getRoles().contains(Role.MANAGER);
                
                // If the user has only USER role, redirect them to their profile page
                if (!isAdmin && !isManager) {
                    System.out.println("User with only USER role detected. Redirecting to profile page...");
                    
                    // Add a small delay to ensure the scene is fully loaded before redirecting
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                try {
                                    logger.info("Redirecting user with only USER role to profile view");
                                    
                                    // Use SceneManager to handle the scene transition
                                    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                                    ProfileController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/profile.fxml");
                                    
                                    // Configure the controller after loading
                                    controller.setUserService(userService);
                                    controller.setTeamService(teamService);
                                    controller.setCurrentUser(currentUser);
                                } catch (IOException e) {
                                    logger.error("Failed to redirect to profile view: {}", e.getMessage(), e);
                                    Utils.showErrorAlert("Error", "Failed to load profile view: " + e.getMessage());
                                }
                            });
                        }
                    }, 300);  // Use a slightly longer delay for redirection
                    return;
                }
                
                // Configure dashboard based on user role
                configureRoleBasedDashboard();
                
                // Use a short delay to ensure charts are properly rendered
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            System.out.println("Delayed tryUpdateStatistics call executing");
                            tryUpdateStatistics();
                        });
                    }
                }, 200);  // Delay for statistics update
            } else {
                System.out.println("WelcomeController: Welcome label not initialized for user: " + user.getUsername());
            }
        } catch (Exception ex) {
            System.err.println("Error updating UI for user: " + ex.getMessage());
            ex.printStackTrace();
        }
    }    private void tryUpdateStatistics() {
        if (userService != null && teamService != null && currentUser != null) {
            System.out.println("WelcomeController: All requirements met, updating statistics");
            
            // During auto-login, update statistics directly to reduce flickering
            if (UserPreferences.hasRememberedCredentials()) {
                // Skip Platform.runLater during auto-login to reduce extra UI cycles
                updateStatistics();
            } else {
                // For normal navigation, use Platform.runLater
                javafx.application.Platform.runLater(() -> {
                    updateStatistics();
                });
            }
        } else {
            System.out.println("WelcomeController: Cannot update statistics, missing dependencies: " + 
                               "userService=" + (userService != null) + 
                               ", teamService=" + (teamService != null) + 
                               ", currentUser=" + (currentUser != null));
        }
    }

    /**
     * After initializing team services, fetch the most populated teams and update the UI
     */
    private void updateStatistics() { 
        try { 
            if (userService == null) {
                System.err.println("UserService is null in updateStatistics");
                return;
            }
            if (teamService == null) {
                System.err.println("TeamService is null in updateStatistics");
                return;
            }
            
            // Verify that required labels are initialized
            if (totalUsersLabel == null || activeUsersLabel == null || 
                totalTeamsLabel == null || recentActivityLabel == null) {
                System.err.println("One or more critical labels are not initialized");
                return;
            }

            // Fetch users data
            List<User> allUsers = userService.getAllUsers(); 
            
            // Update total users 
            int totalUsers = allUsers.size(); 
            totalUsersLabel.setText(String.valueOf(totalUsers)); 
         
            // Update active users (currently logged in) 
            int activeUsers = UserSessionManager.getConnectedUsers().size(); 
            activeUsersLabel.setText(String.valueOf(activeUsers)); 
         
            // Fetch teams and sort by member count
            List<tn.esprit.testpifx.models.Team> allTeams = teamService.getAllTeams();
            
            // Sort teams by member count (descending)
            allTeams.sort((t1, t2) -> Integer.compare(t2.getMemberIds().size(), t1.getMemberIds().size()));
            
            // Update total teams 
            int totalTeams = allTeams.size(); 
            totalTeamsLabel.setText(String.valueOf(totalTeams)); 
            
            // Update Team Size Distribution - populate the progress bars with real team data
            updateTeamSizeDistribution(allTeams);
         
            // Update recent activity count with at least a placeholder value
            // In a real application, this would come from an activity log
            recentActivityLabel.setText(String.valueOf(allUsers.size() > 0 ? allUsers.size() / 2 : 0));
            
            // Count users by role
            long adminCount = allUsers.stream()
                .filter(u -> u.getRoles().contains(Role.ADMIN))
                .count();
            long managerCount = allUsers.stream()
                .filter(u -> u.getRoles().contains(Role.MANAGER))
                .count();
            long userCount = allUsers.stream()
                .filter(u -> u.getRoles().contains(Role.USER))
                .count();
            
            // Example of updating user stats by role if userStatsLabel exists
            if (userStatsLabel != null) {
                userStatsLabel.setText(String.format("Admins: %d, Managers: %d, Users: %d", 
                    adminCount, managerCount, userCount));
                
                // Update user distribution pie chart
                populateUserDistributionChart(adminCount, managerCount, userCount);
            }
            
            // Update country stats if the label exists
            if (countryStatsLabel != null) {
                Map<String, Long> countryCounts = allUsers.stream()
                    .filter(u -> u.getCountry() != null && !u.getCountry().isEmpty())
                    .collect(Collectors.groupingBy(User::getCountry, Collectors.counting()));
                
                String countryStats = countryCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
                
                countryStatsLabel.setText(countryStats.isEmpty() ? "No country data" : countryStats);
            }
            
            // Update gender stats if the label exists
            if (genderStatsLabel != null) {
                long maleCount = allUsers.stream()
                    .filter(u -> "MALE".equalsIgnoreCase(u.getGender()))
                    .count();
                long femaleCount = allUsers.stream()
                    .filter(u -> "FEMALE".equalsIgnoreCase(u.getGender()))
                    .count();
                long otherCount = allUsers.stream()
                    .filter(u -> u.getGender() != null && 
                           !u.getGender().equalsIgnoreCase("MALE") && 
                           !u.getGender().equalsIgnoreCase("FEMALE"))
                    .count();
                
                genderStatsLabel.setText(String.format("Male: %d, Female: %d, Other: %d", 
                    maleCount, femaleCount, otherCount));
            }
            
            // Update activity line chart
            if (activityLineChart != null) {
                populateActivityChart();
            }
            
            // Populate activity table if it exists
            if (activityTable != null) {
                System.out.println("Activity table exists, would populate with real data in a full implementation");
                populateActivityTable();
            }
            
            // Update new statistic cards
            
            // Regional Distribution Card
            if (regionStatsLabel != null) {
                Map<String, Long> regionCounts = allUsers.stream()
                    .filter(u -> u.getRegion() != null && !u.getRegion().isEmpty())
                    .collect(Collectors.groupingBy(User::getRegion, Collectors.counting()));
                
                String topRegion = regionCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
                
                long topRegionCount = regionCounts.getOrDefault(topRegion, 0L);
                regionStatsLabel.setText(topRegion + ": " + topRegionCount);
                
                // Also update the region bar chart
                populateRegionBarChart(regionCounts);
                
                // Update top regions label if available
                if (topRegionsLabel != null) {
                    String topRegions = regionCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.joining(", "));
                    
                    topRegionsLabel.setText(topRegions.isEmpty() ? "None" : topRegions);
                }
            }
            
            // Role Distribution Card and Chart
            if (userRolesLabel != null) {
                userRolesLabel.setText(String.format("Admin: %d, Mgr: %d", adminCount, managerCount));
                
                // Update role distribution pie chart
                if (roleDistributionChart != null) {
                    populateRoleDistributionChart(adminCount, managerCount, userCount);
                }
            }
            
            // Team Size Card
            if (avgTeamSizeLabel != null && teamService != null) {
                double avgSize = allTeams.stream()
                    .mapToInt(t -> t.getMemberIds().size())
                    .average()
                    .orElse(0.0);
                
                avgTeamSizeLabel.setText(String.format("Avg: %.1f members", avgSize));
                
                // Update team size value label if available
                if (avgTeamSizeValue != null) {
                    avgTeamSizeValue.setText(String.format("%.1f members", avgSize));
                }
                
                // Call these methods with a short delay to ensure UI is ready
                javafx.application.Platform.runLater(() -> {
                    // Use a timer to ensure charts get populated after UI is fully loaded
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> {
                                System.out.println("Populating team stats table and charts");
                                // Populate team stats table
                                populateTeamStatsTable(allTeams);
                            });
                        }
                    }, 200);
                });
            }
            
            // Gender Ratio Card
            if (genderRatioLabel != null) {
                long maleCount = allUsers.stream()
                    .filter(u -> "Male".equalsIgnoreCase(u.getGender()))
                    .count();
                long femaleCount = allUsers.stream()
                    .filter(u -> "Female".equalsIgnoreCase(u.getGender()))
                    .count();
                
                long total = maleCount + femaleCount;
                int malePercent = total > 0 ? (int)((maleCount * 100) / total) : 0;
                int femalePercent = total > 0 ? (int)((femaleCount * 100) / total) : 0;
                
                genderRatioLabel.setText(String.format("M: %d%% F: %d%%", malePercent, femalePercent));
                
                // Update gender ratio value label if available
                if (genderRatioValue != null) {
                    genderRatioValue.setText(String.format("Male: %d%% Female: %d%%", malePercent, femalePercent));
                }
                
                // Also update the gender distribution pie chart
                populateGenderDistributionChart(maleCount, femaleCount);
            }
            
            // Update team distribution chart
            populateTeamDistributionChart();
            
        } catch (Exception e) { 
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
            Utils.showErrorAlert("Error", "Failed to update statistics: " + e.getMessage()); 
        } 
    }
    
    /**
     * Populates the user distribution pie chart
     */
    private void populateUserDistributionChart(long adminCount, long managerCount, long userCount) {
        if (userDistributionChart == null) return;
        
        userDistributionChart.getData().clear();
        
        if (adminCount > 0) {
            javafx.scene.chart.PieChart.Data adminSlice = 
                new javafx.scene.chart.PieChart.Data("Admins", adminCount);
            userDistributionChart.getData().add(adminSlice);
        }
        
        if (managerCount > 0) {
            javafx.scene.chart.PieChart.Data managerSlice = 
                new javafx.scene.chart.PieChart.Data("Managers", managerCount);
            userDistributionChart.getData().add(managerSlice);
        }
        
        if (userCount > 0) {
            javafx.scene.chart.PieChart.Data userSlice = 
                new javafx.scene.chart.PieChart.Data("Users", userCount);
            userDistributionChart.getData().add(userSlice);
        }
        
        // Apply custom colors to pie slices later when nodes are available
        javafx.application.Platform.runLater(() -> {
            String[] colors = {"#4e73df", "#1cc88a", "#36b9cc"};
            int colorIndex = 0;
            for (javafx.scene.chart.PieChart.Data data : userDistributionChart.getData()) {
                if (data.getNode() != null) {
                    String color = colors[colorIndex % colors.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                    colorIndex++;
                }
            }
        });
    }
    
    /**
     * Populates the regional distribution bar chart
     */
    private void populateRegionBarChart(Map<String, Long> regionCounts) {
        if (regionBarChart == null) {
            System.out.println("Region chart is null");
            return;
        }
        
        // Clear existing data
        regionBarChart.getData().clear();
        
        // Colors for the pie slices
        String[] colors = {"#e74a3b", "#fd7e14", "#f6c23e", "#1cc88a", "#36b9cc", "#4e73df", "#6f42c1", "#5a5c69"};
        
        // If there are no regions with data, add some sample data
        if (regionCounts.isEmpty() || regionCounts.size() < 2) {
            // Add sample data
            regionBarChart.getData().addAll(
                new javafx.scene.chart.PieChart.Data("Tunis", 12),
                new javafx.scene.chart.PieChart.Data("Sousse", 8),
                new javafx.scene.chart.PieChart.Data("Sfax", 7),
                new javafx.scene.chart.PieChart.Data("Bizerte", 5),
                new javafx.scene.chart.PieChart.Data("Monastir", 3)
            );
        } else {
            // Use real data
            // Sort regions by user count in descending order
            regionCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(8) // Show top 8 regions
                    .forEach(entry -> 
                        regionBarChart.getData().add(
                            new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue())
                        )
                    );
        }
        
        // Apply custom colors to pie slices
        javafx.application.Platform.runLater(() -> {
            int colorIndex = 0;
            for (javafx.scene.chart.PieChart.Data data : regionBarChart.getData()) {
                if (data.getNode() != null) {
                    // Apply color and add percentage to the label
                    String color = colors[colorIndex % colors.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                    
                    // Calculate percentage
                    double total = regionBarChart.getData().stream()
                        .mapToDouble(javafx.scene.chart.PieChart.Data::getPieValue)
                        .sum();
                    int percentage = (int)((data.getPieValue() * 100) / total);
                    
                    // Update the name with percentage
                    data.setName(data.getName() + " (" + percentage + "%)");
                    
                    colorIndex++;
                }
            }
        });
    }
    
    /**
     * Populates the gender distribution pie chart
     */
    private void populateGenderDistributionChart(long maleCount, long femaleCount) {
        if (genderDistributionChart == null) {
            System.out.println("Gender distribution chart is null");
            return;
        }
        
        System.out.println("Populating gender distribution chart with male: " + maleCount + ", female: " + femaleCount);
        
        // Clear any existing data
        genderDistributionChart.getData().clear();
        
        // Always add data, even if counts are 0, to ensure chart renders
        javafx.scene.chart.PieChart.Data maleSlice = 
            new javafx.scene.chart.PieChart.Data("Male", maleCount > 0 ? maleCount : 10);
        genderDistributionChart.getData().add(maleSlice);
        
        javafx.scene.chart.PieChart.Data femaleSlice = 
            new javafx.scene.chart.PieChart.Data("Female", femaleCount > 0 ? femaleCount : 8);
        genderDistributionChart.getData().add(femaleSlice);
        
        // Create duplicate of the data to make sure the chart is being populated
        System.out.println("Added " + genderDistributionChart.getData().size() + " slices to gender chart");
        
        // Apply custom colors using a delayed approach
        javafx.application.Platform.runLater(() -> {
            try {
                // Add a short delay to ensure chart nodes are created
                Thread.sleep(100);
                
                // Apply styling to male slice
                if (genderDistributionChart.getData().size() > 0) {
                    javafx.scene.chart.PieChart.Data slice = genderDistributionChart.getData().get(0);
                    if (slice.getNode() != null) {
                        slice.getNode().setStyle("-fx-pie-color: #e74a3b;"); // Male - red
                        System.out.println("Applied style to male slice");
                    } else {
                        System.out.println("Male slice node is null");
                    }
                }
                
                // Apply styling to female slice
                if (genderDistributionChart.getData().size() > 1) {
                    javafx.scene.chart.PieChart.Data slice = genderDistributionChart.getData().get(1);
                    if (slice.getNode() != null) {
                        slice.getNode().setStyle("-fx-pie-color: #4e73df;"); // Female - blue
                        System.out.println("Applied style to female slice");
                    } else {
                        System.out.println("Female slice node is null");
                    }
                }
            } catch (Exception e) {
                System.err.println("Error styling gender chart: " + e.getMessage());
            }
        });
    }
    
    /**
     * Populates the team statistics table
     */
    private void populateTeamStatsTable(List<tn.esprit.testpifx.models.Team> teams) {
        if (teamStatsTable == null || teamNameColumn == null || 
            teamMembersColumn == null || teamTypeColumn == null) {
            System.out.println("Team stats table or columns are null, cannot populate table");
            return;
        }
        
        System.out.println("Populating team stats table with " + teams.size() + " teams");
        
        try {
            // Configure table columns if not already configured
            if (teamNameColumn.getCellValueFactory() == null) {
                teamNameColumn.setCellValueFactory(cellData -> cellData.getValue().teamNameProperty());
            }
            if (teamMembersColumn.getCellValueFactory() == null) {
                teamMembersColumn.setCellValueFactory(cellData -> cellData.getValue().memberCountProperty().asObject());
            }
            if (teamTypeColumn.getCellValueFactory() == null) {
                teamTypeColumn.setCellValueFactory(cellData -> cellData.getValue().teamTypeProperty());
            }
            
            // Clear previous data
            javafx.application.Platform.runLater(() -> {
                teamStatsTable.getItems().clear();
                
                // Create data for the table
                javafx.collections.ObservableList<TeamTableData> teamTableData = 
                    javafx.collections.FXCollections.observableArrayList();
                    
                // If no teams exist, add sample data
                if (teams.isEmpty()) {
                    teamTableData.add(new TeamTableData("Development Department", 8, "Department Team"));
                    teamTableData.add(new TeamTableData("Marketing Team", 6, "Project Team"));
                    teamTableData.add(new TeamTableData("IT Support", 4, "Department Team"));
                    teamTableData.add(new TeamTableData("Mobile App Project", 5, "Project Team"));
                    teamTableData.add(new TeamTableData("Website Redesign", 3, "Project Team"));
                } else {
                    // Classify real teams as department or project based on their name
                    teams.forEach(team -> {
                        String teamType = team.getName().toLowerCase().contains("department") || 
                                         team.getName().toLowerCase().contains("dept") ? 
                                         "Department Team" : "Project Team";
                                         
                        teamTableData.add(new TeamTableData(
                            team.getName(), 
                            team.getMemberIds().size(),
                            teamType
                        ));
                    });
                }
                
                // Sort by number of members (descending)
                javafx.collections.FXCollections.sort(teamTableData, 
                    (t1, t2) -> Integer.compare(t2.getMemberCount(), t1.getMemberCount()));
                    
                // Add to table
                teamStatsTable.setItems(teamTableData);
                
                System.out.println("Team stats table populated with " + teamTableData.size() + " entries");
            });
        } catch (Exception e) {
            System.err.println("Error populating team stats table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Populates the team distribution chart
     */
    private void populateTeamDistributionChart() {
        if (teamDistributionChart == null) return;
        
        teamDistributionChart.getData().clear();
        
        var teams = teamService.getAllTeams();
        
        // Count department teams vs. project teams
        long deptTeams = teams.stream()
            .filter(t -> t.getName().toLowerCase().contains("department") || 
                         t.getName().toLowerCase().contains("dept"))
            .count();
            
        long projectTeams = teams.size() - deptTeams;
        
        if (deptTeams > 0) {
            javafx.scene.chart.PieChart.Data deptSlice = 
                new javafx.scene.chart.PieChart.Data("Department Teams", deptTeams);
            teamDistributionChart.getData().add(deptSlice);
        }
        
        if (projectTeams > 0) {
            javafx.scene.chart.PieChart.Data projectSlice = 
                new javafx.scene.chart.PieChart.Data("Project Teams", projectTeams);
            teamDistributionChart.getData().add(projectSlice);
        }
        
        // Apply custom colors
        if (teamDistributionChart.getData().size() > 0) {
            teamDistributionChart.getData().get(0).getNode().setStyle("-fx-pie-color: #6f42c1;"); // Purple for dept
        }
        
        if (teamDistributionChart.getData().size() > 1) {
            teamDistributionChart.getData().get(1).getNode().setStyle("-fx-pie-color: #1cc88a;"); // Green for projects
        }
    }
    
    /**
     * Populates the role distribution pie chart
     */
    private void populateRoleDistributionChart(long adminCount, long managerCount, long userCount) {
        if (roleDistributionChart == null) return;
        
        roleDistributionChart.getData().clear();
        
        if (adminCount > 0) {
            javafx.scene.chart.PieChart.Data adminSlice = 
                new javafx.scene.chart.PieChart.Data("Admins", adminCount);
            roleDistributionChart.getData().add(adminSlice);
        } else {
            // Add sample data if we don't have any real data
            roleDistributionChart.getData().add(
                new javafx.scene.chart.PieChart.Data("Admins", 1));
        }
        
        if (managerCount > 0) {
            javafx.scene.chart.PieChart.Data managerSlice = 
                new javafx.scene.chart.PieChart.Data("Managers", managerCount);
            roleDistributionChart.getData().add(managerSlice);
        } else {
            // Add sample data if we don't have any real data
            roleDistributionChart.getData().add(
                new javafx.scene.chart.PieChart.Data("Managers", 2));
        }
        
        if (userCount > 0) {
            javafx.scene.chart.PieChart.Data userSlice = 
                new javafx.scene.chart.PieChart.Data("Users", userCount);
            roleDistributionChart.getData().add(userSlice);
        } else {
            // Add sample data if we don't have any real data
            roleDistributionChart.getData().add(
                new javafx.scene.chart.PieChart.Data("Users", 5));
        }
        
        // Apply custom colors to pie slices using Platform.runLater
        javafx.application.Platform.runLater(() -> {
            String[] colors = {"#4e73df", "#1cc88a", "#36b9cc"};
            int colorIndex = 0;
            for (javafx.scene.chart.PieChart.Data data : roleDistributionChart.getData()) {
                if (data.getNode() != null) {
                    String color = colors[colorIndex % colors.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                    colorIndex++;
                }
            }
        });
    }
    
    /**
     * Populates the team size stacked bar chart
     */
    private void populateTeamSizeChart(List<tn.esprit.testpifx.models.Team> teams) {
        if (teamSizeChart == null) return;
        
        teamSizeChart.getData().clear();
        
        // Create series for department teams and project teams
        javafx.scene.chart.XYChart.Series<String, Number> deptSeries = new javafx.scene.chart.XYChart.Series<>();
        deptSeries.setName("Department Teams");
        
        javafx.scene.chart.XYChart.Series<String, Number> projectSeries = new javafx.scene.chart.XYChart.Series<>();
        projectSeries.setName("Project Teams");
        
        // If we have no teams or very few, add some sample data
        if (teams.size() < 3) {
            deptSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("HR Department", 7));
            deptSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("IT Department", 12));
            deptSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("Finance Dept", 5));
            
            projectSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("Mobile App", 6));
            projectSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("Website Redesign", 4));
            projectSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("Database Migration", 3));
        } else {
            // Classify teams and add data to series
            teams.forEach(team -> {
                String teamType = team.getName().toLowerCase().contains("department") || 
                                 team.getName().toLowerCase().contains("dept") ? 
                                 "Department Team" : "Project Team";
                                 
                if ("Department Team".equals(teamType)) {
                    deptSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(team.getName(), team.getMemberIds().size()));
                } else {
                    projectSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(team.getName(), team.getMemberIds().size()));
                }
            });
        }
          // Add series to chart one by one to avoid varargs warning
        teamSizeChart.getData().add(deptSeries);
        teamSizeChart.getData().add(projectSeries);
        
        // Apply custom colors to bars using Platform.runLater to ensure nodes are created
        javafx.application.Platform.runLater(() -> {
            for (javafx.scene.chart.XYChart.Series<String, Number> s : teamSizeChart.getData()) {
                if (s.getName().equals("Department Teams")) {
                    for (javafx.scene.chart.XYChart.Data<String, Number> data : s.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-bar-fill: #6f42c1;"); // Purple for dept
                        }
                    }
                } else {
                    for (javafx.scene.chart.XYChart.Data<String, Number> data : s.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-bar-fill: #1cc88a;"); // Green for projects
                        }
                    }
                }
            }
        });
    }

    @FXML
    private void handleViewProfile() {
        try {
            logger.info("Navigating to profile view");
            
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            ProfileController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/profile.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
            
        } catch (IOException e) {
            logger.error("Failed to load profile view: {}", e.getMessage(), e);
            Utils.showErrorAlert("Error", "Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewStatistics() {
        updateStatistics();
    }

    @FXML
    private void handleManageUsers() {
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            UserManagementController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/user_management.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load user management: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageTeams() {
        try {
            if (teamService == null) {
                Utils.showErrorAlert("Error", "Team service is not initialized");
                return;
            }
            
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            TeamManagementController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/team_management.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load team management: " + e.getMessage());
        }
    }    @FXML
    private void handleLogout() {
        // First, clear the user's saved preferences to prevent auto re-login
        UserPreferences.clearLoginCredentials();
        System.out.println("Cleared saved login credentials during logout");
        
        if (currentUser != null) {
            UserSessionManager.removeConnectedUser(currentUser);
        }
        
        try {
            // Use SceneManager to handle the scene transition
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            AuthController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/login.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            
        } catch (IOException e) {
            Utils.showErrorAlert("Error", "Failed to load login screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNotifications() {
        // Show notification popup or dropdown
        if (notificationCount > 0) {
            Utils.showInfoAlert("Notifications", "You have " + notificationCount + " unread system notification.\n\nNew user has joined the system.");
            
            // Mark as read by hiding the red badge
            notificationBadge.setVisible(false);
            notificationCount = 0;
        } else {
            Utils.showInfoAlert("Notifications", "You have no new notifications.");
        }
    }
    
    private void populateActivityChart() {
        if (activityLineChart == null) {
            return;
        }
        
        // Clear existing data
        activityLineChart.getData().clear();
        
        // Create a series for user logins
        javafx.scene.chart.XYChart.Series<String, Number> loginSeries = new javafx.scene.chart.XYChart.Series<>();
        loginSeries.setName("Logins");
        
        // Add sample data (in a real app, this would come from a database)
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 1", 5));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 2", 7));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 3", 2));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 4", 0));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 5", 8));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 6", 12));
        loginSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 7", 10));
        
        // Create a series for user registrations
        javafx.scene.chart.XYChart.Series<String, Number> registrationSeries = new javafx.scene.chart.XYChart.Series<>();
        registrationSeries.setName("New Users");
        
        // Add sample data (in a real app, this would come from a database)
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 1", 1));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 2", 3));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 3", 2));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 4", 0));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 5", 2));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 6", 1));
        registrationSeries.getData().add(new javafx.scene.chart.XYChart.Data<>("May 7", 4));
          // Add both series to the chart one by one to avoid varargs warning
        activityLineChart.getData().add(loginSeries);
        activityLineChart.getData().add(registrationSeries);
        
        // Apply custom colors to the lines
        loginSeries.getNode().setStyle("-fx-stroke: #4e73df;");
        registrationSeries.getNode().setStyle("-fx-stroke: #1cc88a;");
    }
    
    /**
     * Populates the recent activity table with sample data
     */
    private void populateActivityTable() {
        if (activityTable == null || 
            activityTypeColumn == null || 
            activityUserColumn == null || 
            activityDescriptionColumn == null || 
            activityTimeColumn == null) {
            System.out.println("Activity table or columns are null, cannot populate");
            return;
        }
          try {
            // No need to cast since we defined the proper types in the field declarations
            
            // Set up cell value factories for each column
            activityTypeColumn.setCellValueFactory(data -> data.getValue().activityTypeProperty());
            activityUserColumn.setCellValueFactory(data -> data.getValue().usernameProperty());
            activityDescriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());
            activityTimeColumn.setCellValueFactory(data -> data.getValue().timestampProperty());
            
            // Create sample data
            javafx.collections.ObservableList<ActivityData> activityData = 
                javafx.collections.FXCollections.observableArrayList(
                    new ActivityData("Login", "admin", "User logged in to the system", "Today 08:30"),
                    new ActivityData("Update", "manager1", "Updated team 'Marketing'", "Today 09:15"),
                    new ActivityData("Create", "admin", "Created new user 'testuser'", "Today 10:05"),
                    new ActivityData("Delete", "admin", "Removed user 'olduser'", "Yesterday 14:20"),
                    new ActivityData("Login", "user1", "User logged in to the system", "Yesterday 15:30"),
                    new ActivityData("Update", "user2", "Updated profile information", "Yesterday 16:45"),
                    new ActivityData("Login", "manager2", "User logged in to the system", "2 days ago"),
                    new ActivityData("Create", "admin", "Created new team 'Project X'", "3 days ago")
                );
                  // Set items to the table
            activityTable.setItems(activityData);
            
            System.out.println("Activity table populated with " + activityData.size() + " entries");
            
        } catch (Exception e) {
            System.err.println("Error populating activity table: " + e.getMessage());
            e.printStackTrace();
        }
    }    /**
     * Helper class for activity table data
     */
    @SuppressWarnings("unused") // Suppress warnings about unused getter methods that are required by JavaFX
    private static class ActivityData {
        private final SimpleStringProperty activityType;
        private final SimpleStringProperty username;
        private final SimpleStringProperty description;
        private final SimpleStringProperty timestamp;
        
        public ActivityData(String activityType, String username, String description, String timestamp) {
            this.activityType = new SimpleStringProperty(activityType);
            this.username = new SimpleStringProperty(username);
            this.description = new SimpleStringProperty(description);
            this.timestamp = new SimpleStringProperty(timestamp);
        }
        
        public String getActivityType() { return activityType.get(); }
        public SimpleStringProperty activityTypeProperty() { return activityType; }
        
        public String getUsername() { return username.get(); }
        public SimpleStringProperty usernameProperty() { return username; }
        
        public String getDescription() { return description.get(); }
        public SimpleStringProperty descriptionProperty() { return description; }
        
        public String getTimestamp() { return timestamp.get(); }
        public SimpleStringProperty timestampProperty() { return timestamp; }
    }
    
    /**
     * Updates the Team Size Distribution section with real team data
     * This populates the progress bars dynamically with the top 5 teams sorted by member count
     */
    private void updateTeamSizeDistribution(List<tn.esprit.testpifx.models.Team> allTeams) {
        // Verify that we're already in the JavaFX application thread
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(() -> updateTeamSizeDistribution(allTeams));
            return;
        }
        
        try {
            // First, find all the team size distribution UI components in the scene graph
            // We need to find the VBox that contains all the team progress bars
            if (welcomeLabel == null || welcomeLabel.getScene() == null) {
                System.out.println("Scene not ready for team size distribution update");
                return;
            }
            
            // Search for the parent VBox containing all progress bars
            javafx.scene.Parent root = welcomeLabel.getScene().getRoot();
            VBox teamSizeContainer = findTeamSizeContainer(root);
            
            if (teamSizeContainer == null) {
                System.out.println("Could not find team size distribution container");
                return;
            }
            
            // Clear existing progress bars
            VBox progressBarsContainer = null;
            
            // Find the VBox containing the progress bars
            for (javafx.scene.Node node : teamSizeContainer.getChildren()) {
                if (node instanceof VBox) {
                    for (javafx.scene.Node childNode : ((VBox) node).getChildren()) {
                        if (childNode instanceof VBox) {
                            progressBarsContainer = (VBox) childNode;
                            break;
                        }
                    }
                    if (progressBarsContainer != null) {
                        break;
                    }
                }
            }
            
            if (progressBarsContainer == null) {
                System.out.println("Could not find progress bars container");
                return;
            }
            
            // Clear existing progress bars
            progressBarsContainer.getChildren().clear();
            
            // Sort teams by member count (already done in updateStatistics, but just to be sure)
            allTeams.sort((t1, t2) -> Integer.compare(t2.getMemberIds().size(), t1.getMemberIds().size()));
            
            // Find the maximum member count for scaling the progress bars
            int maxMemberCount = 1; // Default to 1 to avoid division by zero
            if (!allTeams.isEmpty()) {
                maxMemberCount = allTeams.get(0).getMemberIds().size();
                if (maxMemberCount < 1) maxMemberCount = 1;
            }
            
            // If we have no teams or teams with no members, add sample data
            if (allTeams.isEmpty() || maxMemberCount <= 1) {
                addSampleTeamData(progressBarsContainer);
                return;
            }
            
            // Choose different colors for variety
            String[] colors = {
                "#6f42c1", // Purple
                "#1cc88a", // Green
                "#36b9cc", // Cyan
                "#4e73df", // Blue
                "#f6c23e"  // Yellow
            };
            
            // Add up to 5 teams with most members
            int teamsToShow = Math.min(5, allTeams.size());
            for (int i = 0; i < teamsToShow; i++) {
                tn.esprit.testpifx.models.Team team = allTeams.get(i);
                int memberCount = team.getMemberIds().size();
                String teamName = team.getName();
                
                // Calculate progress (0.0 to 1.0)
                double progress = (double) memberCount / maxMemberCount;
                
                // Choose a color based on the team's index
                String color = colors[i % colors.length];
                
                // Create a progress bar container
                VBox teamProgressContainer = createTeamProgressBar(teamName, memberCount, progress, color);
                progressBarsContainer.getChildren().add(teamProgressContainer);
            }
            
        } catch (Exception e) {
            System.err.println("Error updating team size distribution: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to find the team size container in the scene graph
     */
    private VBox findTeamSizeContainer(javafx.scene.Parent root) {
        // Look for the VBox that contains the team size distribution
        if (root instanceof BorderPane) {
            BorderPane borderPane = (BorderPane) root;
            javafx.scene.Node center = borderPane.getCenter();
            
            if (center instanceof VBox) {
                for (javafx.scene.Node scrollPane : ((VBox) center).getChildren()) {
                    if (scrollPane instanceof javafx.scene.control.ScrollPane) {
                        javafx.scene.Node content = ((javafx.scene.control.ScrollPane) scrollPane).getContent();
                        if (content instanceof VBox) {
                            VBox contentVBox = (VBox) content;
                            
                            // Look through the rows to find the one with team size chart
                            for (javafx.scene.Node row : contentVBox.getChildren()) {
                                if (row instanceof HBox) {
                                    HBox rowHBox = (HBox) row;
                                    
                                    // Look through the cards in this row
                                    for (javafx.scene.Node card : rowHBox.getChildren()) {
                                        if (card instanceof VBox) {
                                            VBox cardVBox = (VBox) card;
                                            
                                            // Look for the header with the title
                                            for (javafx.scene.Node header : cardVBox.getChildren()) {
                                                if (header instanceof HBox) {
                                                    HBox headerHBox = (HBox) header;
                                                    
                                                    // Check if this is the team size header
                                                    for (javafx.scene.Node titleContainer : headerHBox.getChildren()) {
                                                        if (titleContainer instanceof HBox) {
                                                            for (javafx.scene.Node titleNode : ((HBox) titleContainer).getChildren()) {
                                                                if (titleNode instanceof Label) {
                                                                    Label titleLabel = (Label) titleNode;
                                                                    if ("Team Size Distribution".equals(titleLabel.getText())) {
                                                                        // Found the right card!
                                                                        return cardVBox;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Creates a progress bar for a team with its name, member count, and progress value
     */
    private VBox createTeamProgressBar(String teamName, int memberCount, double progress, String color) {
        VBox container = new VBox();
        container.setSpacing(5);
        
        // Create the header with team name and member count
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setSpacing(5);
        
        Label nameLabel = new Label(teamName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label countLabel = new Label(memberCount + " members");
        countLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #858796;");
        
        header.getChildren().addAll(nameLabel, spacer, countLabel);
        
        // Create the progress bar
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setStyle("-fx-accent: " + color + "; -fx-pref-height: 10;");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        
        // Add everything to the container
        container.getChildren().addAll(header, progressBar);
        
        return container;
    }
    
    /**
     * Adds sample team data when no real data is available
     */
    private void addSampleTeamData(VBox container) {
        // Sample data to show when there are no teams or no members
        createAndAddProgressBar(container, "IT Department", 12, 1.0, "#6f42c1");
        createAndAddProgressBar(container, "Marketing Team", 8, 0.6, "#1cc88a");
        createAndAddProgressBar(container, "Finance Department", 6, 0.4, "#36b9cc");
        createAndAddProgressBar(container, "Mobile App Project", 5, 0.35, "#4e73df");
        createAndAddProgressBar(container, "Website Redesign", 3, 0.2, "#f6c23e");
    }
    
    /**
     * Helper method to create and add a progress bar
     */
    private void createAndAddProgressBar(VBox container, String teamName, int memberCount, 
                                      double progress, String color) {
        VBox teamContainer = createTeamProgressBar(teamName, memberCount, progress, color);
        container.getChildren().add(teamContainer);
    }
}