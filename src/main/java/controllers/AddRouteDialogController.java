package controllers;

import entities.Organisation;
import entities.OrganisationRoute;
import entities.Route;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationRouteService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

@Slf4j
public class AddRouteDialogController implements Initializable {

    @FXML private Label routeInfoLabel;
    @FXML private TextField internalCodeField;
    @FXML private ComboBox<String> vehicleTypeComboBox;
    @FXML private Spinner<Integer> frequencySpinner;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextArea notesArea;
    @FXML private CheckBox activeCheckbox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Route route;
    private Organisation organisation;
    private OrganisationRouteService organisationRouteService;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup vehicle type options
        vehicleTypeComboBox.getItems().addAll(
            "Bus standard", "Bus articulé", "Minibus", "Train", "Taxi", "Voiture", "Rame de métro", 
            "Avion", "Ferry", "Autre"
        );
        
        // Setup frequency spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        frequencySpinner.setValueFactory(valueFactory);
        
        // Set default values
        activeCheckbox.setSelected(true);
        
        // Setup button actions
        saveButton.setOnAction(event -> saveRoute());
        cancelButton.setOnAction(event -> closeDialog());
    }
    
    public void setRoute(Route route) {
        this.route = route;
        updateRouteInfo();
    }
    
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
    
    public void setOrganisationRouteService(OrganisationRouteService service) {
        this.organisationRouteService = service;
    }
    
    private void updateRouteInfo() {
        if (route != null) {
            String routeInfo = route.getOrigin() + " → " + route.getDestination();
            routeInfoLabel.setText(routeInfo);
            
            // Set suggested vehicle type based on transport mode
            switch (route.getTransportMode()) {
                case "Bus":
                    vehicleTypeComboBox.setValue("Bus standard");
                    break;
                case "Train":
                    vehicleTypeComboBox.setValue("Train");
                    break;
                case "Taxi":
                    vehicleTypeComboBox.setValue("Taxi");
                    break;
                case "Métro":
                    vehicleTypeComboBox.setValue("Rame de métro");
                    break;
                case "TGM":
                    vehicleTypeComboBox.setValue("Train");
                    break;
                case "Avion":
                    vehicleTypeComboBox.setValue("Avion");
                    break;
                case "Ferry":
                    vehicleTypeComboBox.setValue("Ferry");
                    break;
                default:
                    vehicleTypeComboBox.setValue("Autre");
            }
            
            // Set suggested departure and arrival times
            departureTimeField.setText("08:00");
            
            // Calculate estimated arrival time based on duration
            int durationMinutes = route.getEstimatedDuration();
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            
            int arrivalHour = (8 + hours) % 24;
            int arrivalMinute = minutes;
            
            String arrivalTime = String.format("%02d:%02d", arrivalHour, arrivalMinute);
            arrivalTimeField.setText(arrivalTime);
            
            // Set suggested internal code
            String code = route.getTransportMode().substring(0, 1) + "-" + 
                         route.getOrigin().substring(0, 3).toUpperCase() + "-" +
                         route.getDestination().substring(0, 3).toUpperCase();
            internalCodeField.setText(code);
        }
    }
    
    @FXML
    private void saveRoute() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            OrganisationRoute orgRoute = OrganisationRoute.builder()
                .organisationId(organisation.getId())
                .routeId(route.getId())
                .internalRouteCode(internalCodeField.getText())
                .assignedVehicleType(vehicleTypeComboBox.getValue())
                .frequencyPerDay(frequencySpinner.getValue())
                .departureTime(departureTimeField.getText())
                .arrivalTime(arrivalTimeField.getText())
                .notes(notesArea.getText())
                .isActive(activeCheckbox.isSelected())
                .build();
                
            organisationRouteService.ajouter(orgRoute);
            
            log.info("Added route {} to organisation {}", route.getId(), organisation.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'itinéraire a été ajouté avec succès");
            closeDialog();
            
        } catch (Exception e) {
            log.error("Error adding route to organisation", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'ajout de l'itinéraire");
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        if (internalCodeField.getText().isEmpty()) {
            errors.append("- Le code interne est requis\n");
        }
        
        if (vehicleTypeComboBox.getValue() == null) {
            errors.append("- Le type de véhicule est requis\n");
        }
        
        if (departureTimeField.getText().isEmpty()) {
            errors.append("- L'heure de départ est requise\n");
        } else if (!departureTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("- Format d'heure de départ invalide (HH:MM)\n");
        }
        
        if (arrivalTimeField.getText().isEmpty()) {
            errors.append("- L'heure d'arrivée est requise\n");
        } else if (!arrivalTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("- Format d'heure d'arrivée invalide (HH:MM)\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation", "Veuillez corriger les erreurs suivantes:\n" + errors);
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 