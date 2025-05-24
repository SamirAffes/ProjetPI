package tn.esprit.testpifx.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.UserPreferences;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;

import java.io.IOException;
import java.util.Objects;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckBox;

    private UserService userService;
    private TeamService teamService;
    
    /**
     * Sets the TeamService and attempts auto-login if credentials are available.
     * This is typically called after the controller is created via dependency injection.
     * 
     * @param teamService The team service to set
     */
    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        System.out.println("TeamService set in AuthController: " + (teamService != null));
        
        // Only auto-login if we have both services available
        if (this.userService != null && this.teamService != null) {
            System.out.println("Both services available, attempting auto-login");
            attemptAutoLogin();
        } else {
            System.out.println("Delaying auto-login until all services are available");
        }
    }
    
    /**
     * Sets the UserService and attempts auto-login if credentials are available.
     * This is typically called after the controller is created via dependency injection.
     * 
     * @param userService The user service to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
        System.out.println("UserService set in AuthController: " + (userService != null));
        
        // Only auto-login if we have both services available
        if (this.userService != null && this.teamService != null) {
            System.out.println("Both services available, attempting auto-login");
            attemptAutoLogin();
        } else {
            System.out.println("Delaying auto-login until all services are available");
        }
    }
    
    @FXML
    public void initialize() {
        System.out.println("AuthController.initialize called");
        
        // Load saved credentials if they exist
        if (UserPreferences.hasRememberedCredentials()) {
            usernameField.setText(UserPreferences.getUsername());
            passwordField.setText(UserPreferences.getPassword());
            rememberMeCheckBox.setSelected(true);
            
            System.out.println("Credentials loaded from preferences to text fields");
        }
        
        // Set up the keyboard event handler for login
        passwordField.setOnKeyPressed(this::handleEnterKeyPressed);
        usernameField.setOnKeyPressed(this::handleEnterKeyPressed);
        
        // Add a delayed auto-login attempt that will only execute if the services are available
        // This ensures the UI is fully loaded before attempting auto-login
        Platform.runLater(() -> {
            // First check if we already have a valid user session
            if (UserSessionManager.isSessionValid()) {
                System.out.println("Found existing valid session during initialize");
                User sessionUser = UserSessionManager.getCurrentUser();
                if (sessionUser != null) {
                    System.out.println("Using existing session user: " + sessionUser.getUsername());
                    // Wait for services to be available
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (userService != null && teamService != null) {
                                    showWelcomeScreen(sessionUser);
                                }
                            });
                        }
                    }, 300);
                    return;
                }
            }
            
            // No valid session, try auto-login with credentials if available
            if (userService != null && teamService != null && UserPreferences.hasRememberedCredentials()) {
                System.out.println("Attempting delayed auto-login with services");
                // Use a small delay to make sure the UI is properly initialized
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // Ignore
                }
                attemptAutoLogin();
            }
        });
    }
    
    /**
     * Attempts to automatically log in the user if credentials are saved
     * and the userService is available
     */    
    private void attemptAutoLogin() {
        System.out.println("Attempting auto-login...");
        if (userService != null && UserPreferences.hasRememberedCredentials()) {
            String username = UserPreferences.getUsername();
            String password = UserPreferences.getPassword();
            
            System.out.println("Found saved credentials for user: " + username);
            
            if (!username.isEmpty() && !password.isEmpty()) {
                System.out.println("Authenticating with saved credentials...");
                
                // Using try-catch to ensure any exceptions during auto-login don't crash the app
                try {
                    userService.authenticate(username, password).ifPresentOrElse(user -> {
                        // Add user to connected users list
                        UserSessionManager.addConnectedUser(user);
                        System.out.println("Auto-login successful for user: " + user.getUsername());
                        
                        // Ensure we have the team service before showing welcome screen
                        if (teamService == null) {
                            System.out.println("WARNING: TeamService is null during auto-login, trying to initialize it");
                            // This is a fallback, ideally teamService should be already set
                        }
                        
                        // Use a single Platform.runLater to show welcome screen without nested delays
                        Platform.runLater(() -> {
                            // Simply show the welcome screen - no additional delays or timers
                            System.out.println("Auto-login successful, showing welcome screen for user: " + user.getUsername());
                            showWelcomeScreen(user);
                        });
                    }, () -> {
                        System.out.println("Auto-login failed: Invalid credentials for user " + username);
                        // Clear invalid credentials
                        UserPreferences.clearLoginCredentials();
                    });
                } catch (Exception e) {
                    System.err.println("Error during auto-login: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Auto-login skipped: Empty username or password");
            }
        } else {
            String reason = userService == null ? "UserService not initialized" : "No remembered credentials";
            System.out.println("Auto-login skipped: " + reason);
        }
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        // Verify services are available
        if (userService == null) {
            System.err.println("UserService is null in handleLogin!");
            showAlert("Error", "Application error: User service not initialized");
            return;
        }

        System.out.println("Attempting manual login for user: " + username);
        
        // Show a loading indicator or disable login button here if needed
        
        try {
            userService.authenticate(username, password).ifPresentOrElse(
                user -> {                
                    // Save credentials if "Remember Me" is checked
                    boolean rememberMe = rememberMeCheckBox.isSelected();
                    if (rememberMe) {
                        System.out.println("Saving login credentials for user: " + username);
                        UserPreferences.saveLoginCredentials(username, password, true);
                    } else {
                        // Clear any saved credentials if "Remember Me" is unchecked
                        System.out.println("Clearing saved credentials since Remember Me is unchecked");
                        UserPreferences.clearLoginCredentials();
                    }
                    
                    // Add user to connected users list and ensure it's the current user
                    UserSessionManager.addConnectedUser(user);
                    System.out.println("User logged in: " + user.getUsername());
                    
                    // Introduce a small delay to ensure the session is properly set
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    
                    // Now navigate to welcome screen
                    showWelcomeScreen(user);
                },
                () -> {
                    System.out.println("Login failed for user: " + username);
                    showAlert("Error", "Invalid credentials");
                }
            );
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "An error occurred during login: " + e.getMessage());
        }
    }    
    
    private void showWelcomeScreen(User user) {
        System.out.println("ShowWelcomeScreen called for user: " + user.getUsername());
        
        // Double-check the session is valid before proceeding
        if (!UserSessionManager.isSessionValid() || !user.equals(UserSessionManager.getCurrentUser())) {
            UserSessionManager.addConnectedUser(user);
            System.out.println("Added user to session before showing welcome screen: " + user.getUsername());
            
            // Wait a moment to ensure session is properly set
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
        
        try {
            // Get stage from the usernameField if available
            Stage stage = null;
            if (usernameField != null && usernameField.getScene() != null && usernameField.getScene().getWindow() != null) {
                stage = (Stage) usernameField.getScene().getWindow();
                System.out.println("Using stage from usernameField");
            } else {
                // Find the current focused window as a fallback
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isFocused()) {
                        stage = (Stage) window;
                        System.out.println("Using focused window as stage");
                        break;
                    }
                }
                
                // If still null, create a new stage as final fallback
                if (stage == null) {
                    stage = new Stage();
                    System.out.println("Created new stage as fallback");
                }
            }
            
            // Verify the stage is not null
            if (stage == null) {
                System.err.println("Failed to get or create a valid stage");
                return;
            }
            
            System.out.println("Loading welcome.fxml for user: " + user.getUsername());
            
            // Use SceneManager to handle the scene transition with consistent behavior
            WelcomeController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/welcome.fxml");
            
            // Configure the controller after loading
            System.out.println("Configuring WelcomeController...");
            if (controller != null) {
                // Important: Set the services first, then the user last
                controller.setUserService(userService);
                controller.setTeamService(teamService);
                
                // Determine if this is an auto-login
                boolean isAutoLogin = UserPreferences.hasRememberedCredentials();
                
                if (isAutoLogin) {
                    System.out.println("Auto-login detected, using direct configuration to avoid flickering");
                    // For auto-login, bypass the Platform.runLater calls to reduce screen flickering
                    controller.setCurrentUser(user);
                } else {
                    // For normal login, use the standard approach
                    controller.setCurrentUser(user);
                }
                
                // Additional confirmation to ensure session persistence
                System.out.println("WelcomeController configured successfully. Session user: " + 
                                  (UserSessionManager.isSessionValid() ? 
                                   UserSessionManager.getCurrentUser().getUsername() : "null"));
                
                // Output confirmation of successful navigation
                System.out.println(user.hasRole(Role.ADMIN) ? 
                    "Admin logged in: " + user.getUsername() : 
                    "Regular user logged in: " + user.getUsername());
            } else {
                System.err.println("Failed to get WelcomeController instance from SceneManager");
            }
        } catch (IOException e) {
            System.err.println("IOException in showWelcomeScreen: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load welcome screen: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception in showWelcomeScreen: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Internal application error: " + e.getMessage());
        }
    }

    @FXML
    private void navigateToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/signup.fxml"));
            Parent root = loader.load();

            SignupController controller = loader.getController();
            controller.setUserService(userService);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage); // Ensure consistent full-screen behavior
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load signup screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle the "Forgot Password" link click event.
     * Opens a dialog to collect the user's email and initiates the password reset process.
     */
    @FXML
    private void handleForgotPassword() {
        try {
            // Load the forgot password dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/forgot-password.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the user service
            ForgotPasswordController controller = loader.getController();
            controller.setUserService(userService);
            
            // Create a new stage for the dialog
            Stage stage = new Stage();
            stage.setTitle("Reset Password");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(usernameField.getScene().getWindow());
            stage.setResizable(false);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());
            stage.setScene(scene);
            
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to load forgot password dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // clearFields method is marked as unused, but keeping it for potential future use
    // with a suppressed warning
    @SuppressWarnings("unused")
    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }    
    
    @FXML
    private void handleEnterKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
    
    /**
     * Clears any saved login credentials
     * This could be exposed through a button or menu option
     */
    public void clearSavedCredentials() {
        UserPreferences.clearLoginCredentials();
        showAlert("Success", "Saved login credentials have been cleared");
    }
}
