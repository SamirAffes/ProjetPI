package tn.esprit.testpifx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.testpifx.models.Team;
import tn.esprit.testpifx.models.User;
import tn.esprit.testpifx.services.TeamService;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.utils.Utils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class TeamFormController {
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;

    private TeamService teamService;
    private UserService userService;
    private User currentUser;
    private Team teamToEdit;
    private Stage stage;

    public void setTeamService(TeamService teamService) {
        this.teamService = teamService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setTeamToEdit(Team team) {
        this.teamToEdit = team;
        if (team != null) {
            titleLabel.setText("Edit Team");
            nameField.setText(team.getName());
            descriptionField.setText(team.getDescription());
            saveButton.setText("Update Team");
        } else {
            titleLabel.setText("Add Team");
            saveButton.setText("Create Team");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleSave() {
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();

        if (name.isEmpty()) {
            Utils.showErrorAlert("Error", "Team name cannot be empty");
            return;
        }

        try {
            if (teamToEdit == null) {
                // Creating a new team
                if (teamService.getTeamByName(name).isPresent()) {
                    Utils.showErrorAlert("Error", "A team with this name already exists");
                    return;
                }

                Team newTeam = new Team(name, description, currentUser.getUserId());
                teamService.createTeam(newTeam, currentUser);
            } else {
                // Updating existing team
                Optional<Team> existingTeam = teamService.getTeamByName(name);
                if (existingTeam.isPresent() && !existingTeam.get().getTeamId().equals(teamToEdit.getTeamId())) {
                    Utils.showErrorAlert("Error", "A team with this name already exists");
                    return;
                }

                // Preserve the original data but update the editable fields
                Team updatedTeam = new Team(name, description, teamToEdit.getCreatedBy());
                updatedTeam.setTeamId(teamToEdit.getTeamId());
                updatedTeam.setMemberIds(teamToEdit.getMemberIds());
                teamService.updateTeam(updatedTeam, currentUser);
            }

            // Success message
            Utils.showSuccessAlert("Success", "Team saved successfully");
            
            // Navigate back to team management view instead of closing the stage
            navigateToTeamManagement();
        } catch (Throwable e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            Utils.showErrorAlert("Error", "Failed to save team: " + e.getMessage());
        }
    }
    
    /**
     * Navigate back to the team management view
     */
    private void navigateToTeamManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/testpifx/views/team_management.fxml"));
            Parent root = loader.load();
            
            TeamManagementController controller = loader.getController();
            controller.setUserService(userService);
            controller.setTeamService(teamService);
            controller.setCurrentUser(currentUser);
            
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/tn/esprit/testpifx/styles/modern.css")).toExternalForm());
            
            stage.setScene(scene);
            Utils.setFullScreenMode(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.showErrorAlert("Error", "Failed to return to team management view: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Navigate back to team management instead of just closing
        navigateToTeamManagement();
    }
}