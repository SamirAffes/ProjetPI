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
import tn.esprit.testpifx.services.ServiceProvider;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.DatabaseConfig;
import tn.esprit.testpifx.utils.TeamDataInitializer;
import tn.esprit.testpifx.utils.TokenUrlHandler;
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
    private static String currentCssFile = MODERN_CSS;    @Override
    public void start(Stage primaryStage) throws IOException {
        System.out.println("Application starting...");
        
        // Initialize services
        initializeServices();
        

        
        // Configure primary stage to be full-screen
        configurePrimaryStage(primaryStage);
        
        // Load and show login screen
        System.out.println("Preparing to show login screen with userService: " + (userService != null) + 
                           " and teamService: " + (teamService != null));
        
        // Make sure the application UI is properly initialized before proceeding
        Platform.runLater(() -> {
            try {
                showLoginScreen(primaryStage);
            } catch (IOException e) {
                e.printStackTrace();
                Utils.showErrorAlert("Application Error", "Failed to load login screen: " + e.getMessage());
            }
        });
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
    }    private void showLoginScreen(Stage primaryStage) throws IOException {
        System.out.println("Loading login screen...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));

        // Set controller factory with enhanced error handling and service injection
        loader.setControllerFactory(param -> {
            try {
                System.out.println("Creating controller: " + param.getName());
                Object controller = param.getDeclaredConstructor().newInstance();

                // First set TeamService if needed (so it's available before UserService is set)
                try {
                    param.getMethod("setTeamService", TeamService.class).invoke(controller, teamService);
                    System.out.println("Injected TeamService into " + param.getSimpleName());
                } catch (NoSuchMethodException e) {
                    // Controller doesn't need TeamService
                    System.out.println("Controller " + param.getSimpleName() + " doesn't have setTeamService method");
                }
                
                // IMPORTANT: Set UserService last for AuthController, since it might trigger auto-login
                try {
                    param.getMethod("setUserService", UserService.class).invoke(controller, userService);
                    System.out.println("Injected UserService into " + param.getSimpleName());
                } catch (NoSuchMethodException e) {
                    // Controller doesn't need UserService
                    System.out.println("Controller " + param.getSimpleName() + " doesn't have setUserService method");
                }

                return controller;
            } catch (Exception e) {
                System.err.println("Error creating controller: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to create controller: " + param.getName(), e);
            }
        });

        // Load FXML with controller factory configured
        Parent root = loader.load();
        Scene scene = new Scene(root);

        // Apply CSS
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(currentCssFile)).toExternalForm());

        // Set up the stage
        primaryStage.setTitle("User Management System");
        primaryStage.setScene(scene);
        
        // Get the controller after loading
        AuthController authController = loader.getController();
        System.out.println("AuthController loaded: " + (authController != null));
        
        // Ensure services are properly set (redundant safety check)
        if (authController != null) {
            // Set both services again to ensure they're properly initialized
            // Order matters: first TeamService, then UserService (might trigger auto-login)
            authController.setTeamService(teamService);
            
            // Introduce a small delay before setting UserService to ensure UI is ready
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // Ignore
                }
                authController.setUserService(userService);
            });
        }
        
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
