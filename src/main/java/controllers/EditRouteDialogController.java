package controllers;

import entities.OrganisationRoute;
import entities.Route;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationRouteService;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class EditRouteDialogController implements Initializable {

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
    
    private OrganisationRoute organisationRoute;
    private Route route;
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
        
        // Setup button actions
        saveButton.setOnAction(event -> saveRoute());
        cancelButton.setOnAction(event -> closeDialog());
    }
    
    public void setOrganisationRoute(OrganisationRoute organisationRoute) {
        this.organisationRoute = organisationRoute;
        populateFields();
    }
    
    public void setRoute(Route route) {
        this.route = route;
        updateRouteInfo();
    }
    
    public void setOrganisationRouteService(OrganisationRouteService service) {
        this.organisationRouteService = service;
    }
    
    private void updateRouteInfo() {
        if (route != null) {
            String routeInfo = route.getOrigin() + " → " + route.getDestination();
            routeInfoLabel.setText(routeInfo);
        }
    }
    
    private void populateFields() {
        if (organisationRoute != null) {
            internalCodeField.setText(organisationRoute.getInternalRouteCode());
            vehicleTypeComboBox.setValue(organisationRoute.getAssignedVehicleType());
            
            if (organisationRoute.getFrequencyPerDay() > 0) {
                SpinnerValueFactory<Integer> valueFactory = 
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, organisationRoute.getFrequencyPerDay());
                frequencySpinner.setValueFactory(valueFactory);
            }
            
            departureTimeField.setText(organisationRoute.getDepartureTime());
            arrivalTimeField.setText(organisationRoute.getArrivalTime());
            notesArea.setText(organisationRoute.getNotes());
            activeCheckbox.setSelected(organisationRoute.isActive());
        }
    }
    
    @FXML
    private void saveRoute() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            organisationRoute.setInternalRouteCode(internalCodeField.getText());
            organisationRoute.setAssignedVehicleType(vehicleTypeComboBox.getValue());
            organisationRoute.setFrequencyPerDay(frequencySpinner.getValue());
            organisationRoute.setDepartureTime(departureTimeField.getText());
            organisationRoute.setArrivalTime(arrivalTimeField.getText());
            organisationRoute.setNotes(notesArea.getText());
            organisationRoute.setActive(activeCheckbox.isSelected());
                
            organisationRouteService.modifier(organisationRoute);
            
            log.info("Updated organisation route {}", organisationRoute.getId());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'itinéraire a été modifié avec succès");
            closeDialog();
            
        } catch (Exception e) {
            log.error("Error updating organisation route", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification de l'itinéraire");
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