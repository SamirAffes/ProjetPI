package tn.esprit.testpifx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.testpifx.controllers.AuthController;
import tn.esprit.testpifx.controllers.SignupController;
import tn.esprit.testpifx.controllers.WelcomeController;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.repositories.TeamRepository;
import tn.esprit.testpifx.repositories.TeamRepositoryFactory;
import tn.esprit.testpifx.repositories.UserRepository;
import tn.esprit.testpifx.repositories.UserRepositoryFactory;
import tn.esprit.testpifx.repositories.UserRepositoryFactory.RepositoryType;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.DatabaseConfig;
import tn.esprit.testpifx.utils.TeamDataInitializer;
import tn.esprit.testpifx.utils.UserDataInitializer;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    private UserService userService;
    private TeamService teamService;

    // CSS file paths
    public static final String BASE_CSS = "/tn/esprit/testpifx/styles/base.css";
    public static final String MODERN_CSS = "/tn/esprit/testpifx/styles/modern.css";
    public static final String MODERN2_CSS = "/tn/esprit/testpifx/styles/modern2.css";

    // Current CSS file
    private static String currentCssFile = MODERN_CSS;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Initialize services
        initializeServices();

        // Configure primary stage to be full-screen
        configurePrimaryStage(primaryStage);

        // Load and show login screen
        showLoginScreen(primaryStage);
    }

    private void configurePrimaryStage(Stage stage) {
        // Use the SceneManager for consistent window settings
        SceneManager.applyStandardWindowSettings(stage);
    }

    private void showWelcomeScreen(Stage primaryStage, User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/welcome.fxml"));
        Parent root = loader.load();

        WelcomeController controller = loader.getController();
        controller.setUserService(userService);
        controller.setTeamService(teamService);
        controller.setCurrentUser(user);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(getCurrentCssFile())).toExternalForm());

        primaryStage.setScene(scene);
        // Use SceneManager for consistent window settings
        SceneManager.applyStandardWindowSettings(primaryStage);
    }

    private void initializeServices() {
        // Use DatabaseConfig to get MySQL connection details
        DatabaseConfig dbConfig = new DatabaseConfig();
        
        // Create MySQL user repository based on database configuration
        UserRepository userRepository;
        if ("MYSQL".equalsIgnoreCase(dbConfig.getDatabaseType())) {
            userRepository = UserRepositoryFactory.createMySQLRepository(
                dbConfig.getHost(),
                dbConfig.getPort(),
                dbConfig.getDatabase(),
                dbConfig.getUsername(),
                dbConfig.getPassword()
            );
            System.out.println("Using MySQL User Repository with database: " + dbConfig.getDatabase());
        } else {
            // Fallback to SQLite repository if not MySQL
            userRepository = UserRepositoryFactory.createRepository(RepositoryType.SQLITE);
            System.out.println("Using SQLite User Repository (MySQL not configured)");
        }
        
        userService = new UserService(userRepository);

        // Initialize users using the UserDataInitializer
        UserDataInitializer userDataInitializer = new UserDataInitializer(userService);
        userDataInitializer.initializeBasicUsers();
        userDataInitializer.initializeExtendedUsers();  // Uncommented to create more users
        userDataInitializer.initializeTestUsers();      // Uncommented to create more users

        // Print summary of initialized users
        System.out.println("Initialized sample users count:"+ userService.getAllUsers().size());
        
        // Create MySQL team repository
        TeamRepository teamRepository;
        if ("MYSQL".equalsIgnoreCase(dbConfig.getDatabaseType())) {
            teamRepository = TeamRepositoryFactory.createMySQLRepository(
                dbConfig.getHost(),
                dbConfig.getPort(),
                dbConfig.getDatabase(),
                dbConfig.getUsername(),
                dbConfig.getPassword()
            );
            System.out.println("Using MySQL Team Repository with database: " + dbConfig.getDatabase());
        } else {
            // Fallback to default repository if not MySQL
            teamRepository = TeamRepositoryFactory.createRepository(TeamRepositoryFactory.RepositoryType.IN_MEMORY);
            System.out.println("Using In-Memory Team Repository (MySQL not configured)");
        }
        
        teamService = new TeamService(teamRepository, userRepository);

        // Initialize teams using the TeamDataInitializer
        TeamDataInitializer teamDataInitializer = new TeamDataInitializer(teamService, userService);
        teamDataInitializer.initializeBasicTeams();
        teamDataInitializer.initializeDepartmentTeams();
        teamDataInitializer.initializeProjectTeams();

        System.out.println("Team service initialized (team initialization complete)");
        
        // Add this check to ensure services are set
        if (teamService == null || userService == null ) {
            System.err.println("Error: Services or current user not initialized!");
            Platform.runLater(() -> {
                Utils.showErrorAlert("Initialization Error",
                        "Team management services not properly initialized. Please contact admin.");
            });
            return;
        }
    }

    private void showLoginScreen(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));

        // Set controller factory
        loader.setControllerFactory(param -> {
            try {
                Object controller = param.getDeclaredConstructor().newInstance();

                // Inject UserService if the controller has a setUserService method
                try {
                    param.getMethod("setUserService", UserService.class).invoke(controller, userService);
                } catch (NoSuchMethodException e) {
                    // Controller doesn't need UserService
                }

                // Inject TeamService if the controller has a setTeamService method
                try {
                    param.getMethod("setTeamService", TeamService.class).invoke(controller, teamService);
                } catch (NoSuchMethodException e) {
                    // Controller doesn't need TeamService
                }

                return controller;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create controller: " + param.getName(), e);
            }
        });

        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Apply CSS
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(currentCssFile)).toExternalForm());

        primaryStage.setTitle("User Management System");
        primaryStage.setScene(scene);
        // Use SceneManager for consistent window settings
        SceneManager.applyStandardWindowSettings(primaryStage);
    }


    /**
     * Sets the CSS file to use for the application.
     * 
     * @param cssFile The path to the CSS file
     */
    public static void setCssFile(String cssFile) {
        currentCssFile = cssFile;
    }

    /**
     * Gets the current CSS file being used.
     * 
     * @return The path to the current CSS file
     */
    public static String getCurrentCssFile() {
        return currentCssFile;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
