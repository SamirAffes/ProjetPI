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
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;

public class SignupController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;

    private UserService userService;
    private TeamService teamService;  // Add this field
    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
    }
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill all fields");
            return;
        }

        if (userService.userExists(username)) {
            showAlert("Error", "Username already exists");
            return;
        }

        if (userService.emailExists(email)) {
            showAlert("Error", "Email already registered");
            return;
        }

        if (password.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters");
            return;
        }

        User newUser = new User(username, password, email);
        userService.createUser(newUser);

        showAlert("Success", "Account created successfully!");
        clearFields();

        // Optionally navigate to login after successful signup
        navigateToLogin();
    }

    @FXML
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/login.fxml"));
            Parent root = loader.load();

            AuthController controller = loader.getController();
            controller.setUserService(userService);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(Main.getCurrentCssFile())).toExternalForm());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            Utils.setFullScreenMode(stage); // Ensure consistent full-screen behavior
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
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
            handleSignUp();
        }
    }
}
