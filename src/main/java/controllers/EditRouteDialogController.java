package controllers;

import entities.OrganisationRoute;
import entities.Route;
import entities.Station;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.EmailService;
import services.OrganisationRouteService;
import services.StationService;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class EditRouteDialogController implements Initializable {

    @FXML private Label routeInfoLabel;
    @FXML private Label originDetailsLabel;
    @FXML private Label destinationDetailsLabel;
    @FXML private Label originCoordinatesLabel;
    @FXML private Label destinationCoordinatesLabel;
    @FXML private TextField internalCodeField;
    @FXML private ComboBox<String> vehicleTypeComboBox;
    @FXML private Spinner<Integer> frequencySpinner;
    @FXML private TextField departureTimeField;
    @FXML private TextField arrivalTimeField;
    @FXML private TextArea notesArea;
    @FXML private CheckBox activeCheckbox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    // New fields
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private CheckBox wifiServiceCheckbox;
    @FXML private CheckBox accessibilityCheckbox;
    
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
        if (route != null) {
            // Mise à jour immédiate des informations de la route
            Platform.runLater(() -> {
                updateRouteInfo();
                // Debug logging
                System.out.println("Route origin: " + route.getOrigin());
                System.out.println("Route destination: " + route.getDestination());

                // Vérification des coordonnées
                double[] originCoords = Station.getCoordinates(route.getOrigin());
                double[] destCoords = Station.getCoordinates(route.getDestination());

                System.out.println("Origin coordinates: " + (originCoords != null ? originCoords[0] + "," + originCoords[1] : "null"));
                System.out.println("Destination coordinates: " + (destCoords != null ? destCoords[0] + "," + destCoords[1] : "null"));
            });
        }
    }
    
    public void setOrganisationRouteService(OrganisationRouteService service) {
        this.organisationRouteService = service;
    }
    
    private void updateRouteInfo() {
        if (route != null) {
            System.out.println("Mise à jour des informations de la route...");
            System.out.println("Origin: " + route.getOrigin());
            System.out.println("Destination: " + route.getDestination());

            // Affichage du texte basique de la route
            String routeInfo = route.getOrigin() + " → " + route.getDestination();
            Platform.runLater(() -> {
                routeInfoLabel.setText(routeInfo);
                System.out.println("Route info label mis à jour: " + routeInfo);

                // Mise à jour des informations détaillées des stations
                String origin = route.getOrigin();
                String destination = route.getDestination();

                // Récupération et affichage des coordonnées
                double[] originCoords = Station.getCoordinates(origin);
                double[] destCoords = Station.getCoordinates(destination);

                System.out.println("Coordonnées origine trouvées: " + (originCoords != null));
                System.out.println("Coordonnées destination trouvées: " + (destCoords != null));

                if (originCoords != null) {
                    originDetailsLabel.setText("Origine: " + origin);
                    originCoordinatesLabel.setText(String.format("Coordonnées: %.4f, %.4f", originCoords[0], originCoords[1]));
                    System.out.println("Labels origine mis à jour");
                } else {
                    System.out.println("Coordonnées non trouvées pour: " + origin);
                    originDetailsLabel.setText("Origine: " + origin);
                    originCoordinatesLabel.setText("Coordonnées non disponibles");
                }

                if (destCoords != null) {
                    destinationDetailsLabel.setText("Destination: " + destination);
                    destinationCoordinatesLabel.setText(String.format("Coordonnées: %.4f, %.4f", destCoords[0], destCoords[1]));
                    System.out.println("Labels destination mis à jour");
                } else {
                    System.out.println("Coordonnées non trouvées pour: " + destination);
                    destinationDetailsLabel.setText("Destination: " + destination);
                    destinationCoordinatesLabel.setText("Coordonnées non disponibles");
                }

                // Mise à jour des autres champs
                if (priceField != null) {
                    priceField.setText(String.valueOf(route.getBasePrice()));
                }

                if (durationField != null) {
                    durationField.setText(String.valueOf(route.getEstimatedDuration()));
                }
            });
        } else {
            System.out.println("Route est null!");
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
            
            // Set additional service features
            if (wifiServiceCheckbox != null) {
                wifiServiceCheckbox.setSelected(organisationRoute.getWifiAvailable() != null ? 
                    organisationRoute.getWifiAvailable() : false);
            }
            
            if (accessibilityCheckbox != null) {
                accessibilityCheckbox.setSelected(organisationRoute.getAccessible() != null ? 
                    organisationRoute.getAccessible() : false);
            }
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
            
            // Save additional service features
            if (wifiServiceCheckbox != null) {
                organisationRoute.setWifiAvailable(wifiServiceCheckbox.isSelected());
            }
            
            if (accessibilityCheckbox != null) {
                organisationRoute.setAccessible(accessibilityCheckbox.isSelected());
            }
            
            // Save custom price if provided
            if (priceField != null && !priceField.getText().isEmpty()) {
                try {
                    double customPrice = Double.parseDouble(priceField.getText());
                    organisationRoute.setCustomPrice(customPrice);
                } catch (NumberFormatException e) {
                    log.warn("Invalid price format, using default route price");
                }
            }
            
            // Save custom duration if provided
            if (durationField != null && !durationField.getText().isEmpty()) {
                try {
                    int customDuration = Integer.parseInt(durationField.getText());
                    organisationRoute.setCustomDuration(customDuration);
                    // Also set routeDuration to match to avoid database constraints issues
                    organisationRoute.setRouteDuration(customDuration);
                } catch (NumberFormatException e) {
                    log.warn("Invalid duration format, using default route duration");
                    // Always ensure routeDuration has a value to satisfy NOT NULL constraint
                    organisationRoute.setRouteDuration(0);
                }
            } else {
                // Always ensure routeDuration has a value to satisfy NOT NULL constraint
                organisationRoute.setRouteDuration(0);
            }
            
            organisationRouteService.modifier(organisationRoute);
            
            log.info("Updated organisation route {}", organisationRoute.getId());
            utils.NotificationManager.showSuccess("Succès", "L'itinéraire a été modifié avec succès");
            closeDialog();



            EmailService es = new EmailService();
            es.sendEmail(organisationRoute.getOrganisation().getEmail(),"Une ligne modifié","Votre ligne a été modifié avec succès");
            
        } catch (Exception e) {
            log.error("Error updating organisation route", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification de l'itinéraire");
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        if (internalCodeField.getText().isEmpty()) {
            errors.append("- Le code de ligne est requis\n");
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
        
        // Validate price if provided
        if (priceField != null && !priceField.getText().isEmpty()) {
            try {
                double price = Double.parseDouble(priceField.getText());
                if (price < 0) {
                    errors.append("- Le prix ne peut pas être négatif\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Format de prix invalide (doit être un nombre)\n");
            }
        }
        
        // Validate duration if provided
        if (durationField != null && !durationField.getText().isEmpty()) {
            try {
                int duration = Integer.parseInt(durationField.getText());
                if (duration <= 0) {
                    errors.append("- La durée doit être supérieure à 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Format de durée invalide (doit être un nombre entier)\n");
            }
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
