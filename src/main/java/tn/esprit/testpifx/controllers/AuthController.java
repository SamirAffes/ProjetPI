package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.models.Role;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.UserSessionManager;
import tn.esprit.testpifx.utils.Utils;
import tn.esprit.testpifx.utils.SceneManager;

import java.io.IOException;
import java.util.Objects;

public class AuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private UserService userService;
    private TeamService teamService;  // Add this field
    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
        System.out.println("TeamService set in AuthController: " + (teamService != null));
    }
    
    public void setUserService(UserService userService) {
        this.userService = userService;
        System.out.println("UserService set in AuthController: " + (userService != null));
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
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

        userService.authenticate(username, password).ifPresentOrElse(
            user -> {
                // Add user to connected users list
                UserSessionManager.addConnectedUser(user);
                System.out.println("User logged in: " + user.getUsername());
                showWelcomeScreen(user);
            },
            () -> showAlert("Error", "Invalid credentials")
        );
    }

    private void showWelcomeScreen(User user) {
        try {
            // Get stage from the usernameField if available
            Stage stage = null;
            if (usernameField != null && usernameField.getScene() != null && usernameField.getScene().getWindow() != null) {
                stage = (Stage) usernameField.getScene().getWindow();
            } else {
                // Find the current focused window as a fallback
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage && window.isFocused()) {
                        stage = (Stage) window;
                        break;
                    }
                }
                
                // If still null, create a new stage as final fallback
                if (stage == null) {
                    stage = new Stage();
                }
            }
            
            // Use SceneManager to handle the scene transition with consistent behavior
            WelcomeController controller = SceneManager.changeScene(stage, "/tn/esprit/testpifx/views/welcome.fxml");
            
            // Configure the controller after loading
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(user);
            
            // Output confirmation of successful navigation
            System.out.println(user.hasRole(Role.ADMIN) ? 
                "Admin logged in: " + user.getUsername() : 
                "Regular user logged in: " + user.getUsername());
        } catch (IOException e) {
            showAlert("Error", "Failed to load welcome screen: " + e.getMessage());
            e.printStackTrace();
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
}
