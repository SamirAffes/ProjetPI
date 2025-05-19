package controllers;

import entities.Reclamation;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.ReclamationService;
import utils.UserContext;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class CreateReclamationController implements Initializable {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Reclamation.ReclamationType> typeComboBox;

    private final ReclamationService reclamationService = new ReclamationService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the type combo box with all ReclamationType values
        typeComboBox.getItems().addAll(Reclamation.ReclamationType.values());
    }

    @FXML
    private void handleSubmit() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        Reclamation.ReclamationType type = typeComboBox.getValue();

        if (title.isEmpty() || description.isEmpty() || type == null) {
            showAlert("Error", "Please fill in all fields and select a type.");
            return;
        }

        User currentUser = UserContext.getInstance().getCurrentUser();
        Reclamation reclamation = Reclamation.builder()
            .user(currentUser)
                .status(Reclamation.ReclamationStatus.PENDING)

            .title(title)
            .description(description)
            .type(type)
                .creationDate(LocalDateTime.now())
            .build();

        try {
            reclamationService.createReclamation(reclamation);
            closeWindow();
        } catch (Exception e) {
            showAlert("Error", "Failed to create complaint: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) titleField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
