package controllers;

import entities.Organisation;
import entities.OrganisationRoute;
import entities.Route;
import entities.Station;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationRouteService;
import services.StationService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
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
    
    // New fields for enhanced route management
    @FXML private ComboBox<Station> departureStationComboBox;
    @FXML private ComboBox<Station> arrivalStationComboBox;
    @FXML private TextField weekdayScheduleField;
    @FXML private TextField saturdayScheduleField;
    @FXML private TextField sundayScheduleField;
    @FXML private TextField holidayScheduleField;
    @FXML private TextField firstDepartureField;
    @FXML private TextField lastDepartureField;
    @FXML private TextField routePriceField;
    @FXML private TextField routeDurationField;
    @FXML private TextField platformInfoField;
    @FXML private TextField reducedPriceField;
    
    // Service feature checkboxes
    @FXML private CheckBox wifiServiceCheckbox;
    @FXML private CheckBox acServiceCheckbox;
    @FXML private CheckBox foodServiceCheckbox;
    @FXML private CheckBox accessibilityCheckbox;
    
    @FXML private CheckBox mondayCheckbox;
    @FXML private CheckBox tuesdayCheckbox;
    @FXML private CheckBox wednesdayCheckbox;
    @FXML private CheckBox thursdayCheckbox;
    @FXML private CheckBox fridayCheckbox;
    @FXML private CheckBox saturdayCheckbox;
    @FXML private CheckBox sundayCheckbox;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Route route;
    private Organisation organisation;
    private OrganisationRouteService organisationRouteService;
    private StationService stationService = new StationService();
    private List<Station> availableStations;
    
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
        
        // Set all weekday checkboxes to true by default
        mondayCheckbox.setSelected(true);
        tuesdayCheckbox.setSelected(true);
        wednesdayCheckbox.setSelected(true);
        thursdayCheckbox.setSelected(true);
        fridayCheckbox.setSelected(true);
        saturdayCheckbox.setSelected(true);
        sundayCheckbox.setSelected(true);
        
        // Setup default schedules
        weekdayScheduleField.setText("06:00-22:00");
        saturdayScheduleField.setText("08:00-20:00");
        sundayScheduleField.setText("09:00-18:00");
        
        // Load available stations
        loadStations();
        
        // Setup button actions
        saveButton.setOnAction(event -> saveRoute());
        cancelButton.setOnAction(event -> closeDialog());
    }
    
    private void loadStations() {
        try {
            // Load all stations
            availableStations = stationService.afficher_tout();
            
            if (availableStations.isEmpty()) {
                log.warn("No stations found in the database");
                
                // Create some default stations if none exist
                createDefaultStations();
                availableStations = stationService.afficher_tout();
            }
            
            // Populate the station combo boxes
            departureStationComboBox.getItems().clear();
            arrivalStationComboBox.getItems().clear();
            
            departureStationComboBox.getItems().addAll(availableStations);
            arrivalStationComboBox.getItems().addAll(availableStations);
            
            log.info("Loaded {} stations for route assignment", availableStations.size());
        } catch (Exception e) {
            log.error("Error loading stations", e);
        }
    }
    
    private void createDefaultStations() {
        try {
            // Create a basic set of stations for each transport type if none exist
            log.info("Creating default stations as none were found");
            
            // Bus stations
            Station tunisBusStation = Station.builder()
                .name("Gare Routière de Tunis")
                .city("Tunis")
                .stationType("Bus")
                .build();
            stationService.ajouter(tunisBusStation);
            
            Station sousseStation = Station.builder()
                .name("Gare Routière de Sousse")
                .city("Sousse")
                .stationType("Bus")
                .build();
            stationService.ajouter(sousseStation);
            
            // Train stations
            Station tunisTrainStation = Station.builder()
                .name("Gare de Tunis")
                .city("Tunis")
                .stationType("Train")
                .build();
            stationService.ajouter(tunisTrainStation);
            
            // Airport
            Station tunisAirport = Station.builder()
                .name("Aéroport Tunis-Carthage")
                .city("Tunis")
                .stationType("Avion")
                .stationCode("TUN")
                .build();
            stationService.ajouter(tunisAirport);
            
            log.info("Created default stations successfully");
        } catch (Exception e) {
            log.error("Error creating default stations", e);
        }
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
            
            // Set suggested first and last departure times
            firstDepartureField.setText("06:00");
            lastDepartureField.setText("22:00");
            
            // Set suggested price and duration from route
            routePriceField.setText(String.valueOf(route.getBasePrice()));
            routeDurationField.setText(String.valueOf(route.getEstimatedDuration()));
            
            // Try to find matching stations
            setInitialStations(route.getOrigin(), route.getDestination(), route.getTransportMode());
        }
    }
    
    private void setInitialStations(String origin, String destination, String transportMode) {
        // Find stations that match the origin and destination cities and transport mode
        for (Station station : availableStations) {
            if (station.getCity().equals(origin) && 
                (station.getStationType() == null || station.getStationType().equals(transportMode))) {
                departureStationComboBox.setValue(station);
            }
            
            if (station.getCity().equals(destination) && 
                (station.getStationType() == null || station.getStationType().equals(transportMode))) {
                arrivalStationComboBox.setValue(station);
            }
        }
    }
    
    @FXML
    private void saveRoute() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            // Calculate operational days based on checkboxes
            int operationalDays = 0;
            if (mondayCheckbox.isSelected()) operationalDays |= 1;
            if (tuesdayCheckbox.isSelected()) operationalDays |= 2;
            if (wednesdayCheckbox.isSelected()) operationalDays |= 4;
            if (thursdayCheckbox.isSelected()) operationalDays |= 8;
            if (fridayCheckbox.isSelected()) operationalDays |= 16;
            if (saturdayCheckbox.isSelected()) operationalDays |= 32;
            if (sundayCheckbox.isSelected()) operationalDays |= 64;
            
            // Parse price and duration if provided
            Double routePrice = null;
            Integer routeDuration = null;
            
            if (routePriceField.getText() != null && !routePriceField.getText().isEmpty()) {
                try {
                    routePrice = Double.parseDouble(routePriceField.getText());
                } catch (NumberFormatException e) {
                    log.warn("Invalid price format: {}", routePriceField.getText());
                }
            }
            
            if (routeDurationField.getText() != null && !routeDurationField.getText().isEmpty()) {
                try {
                    routeDuration = Integer.parseInt(routeDurationField.getText());
                } catch (NumberFormatException e) {
                    log.warn("Invalid duration format: {}", routeDurationField.getText());
                }
            }
            
            // Build the organization route object
            OrganisationRoute.OrganisationRouteBuilder builder = OrganisationRoute.builder()
                .organisationId(organisation.getId())
                .routeId(route.getId())
                .internalRouteCode(internalCodeField.getText())
                .assignedVehicleType(vehicleTypeComboBox.getValue())
                .frequencyPerDay(frequencySpinner.getValue())
                .departureTime(departureTimeField.getText())
                .arrivalTime(arrivalTimeField.getText())
                .notes(notesArea.getText())
                .isActive(activeCheckbox.isSelected())
                .operationalDays(operationalDays)
                .weekdaySchedule(weekdayScheduleField.getText())
                .saturdaySchedule(saturdayScheduleField.getText())
                .sundaySchedule(sundayScheduleField.getText())
                .holidaySchedule(holidayScheduleField.getText())
                .firstDepartureTime(firstDepartureField.getText())
                .lastDepartureTime(lastDepartureField.getText())
                .platformInfo(platformInfoField.getText())
                .wifiAvailable(wifiServiceCheckbox != null && wifiServiceCheckbox.isSelected())
                .accessible(accessibilityCheckbox != null && accessibilityCheckbox.isSelected())
                .airConditioned(acServiceCheckbox != null && acServiceCheckbox.isSelected())
                .foodService(foodServiceCheckbox != null && foodServiceCheckbox.isSelected());
                
            // Add optional fields if they have values
            if (departureStationComboBox.getValue() != null) {
                builder.departureStationId(departureStationComboBox.getValue().getId());
            }
            
            if (arrivalStationComboBox.getValue() != null) {
                builder.arrivalStationId(arrivalStationComboBox.getValue().getId());
            }
            
            if (routePrice != null) {
                builder.customPrice(routePrice);
                // Also set routePrice field to satisfy database constraint
                builder.routePrice(routePrice);
            } else {
                // Always set a default value for routePrice to avoid NOT NULL constraint errors
                builder.routePrice(0.0);
            }
            
            if (routeDuration != null) {
                builder.customDuration(routeDuration);
                builder.routeDuration(routeDuration); // Set legacy field as well
            } else {
                // Always set routeDuration to avoid NOT NULL constraint errors
                builder.routeDuration(0);
            }
            
            OrganisationRoute orgRoute = builder.build();
            organisationRouteService.ajouter(orgRoute);
            
            log.info("Added route {} to organisation {}", route.getId(), organisation.getId());
            utils.NotificationManager.showSuccess("Succès", "L'itinéraire a été ajouté avec succès");
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
        
        // Validate schedule format (optional fields)
        String timeRangeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        
        if (!weekdayScheduleField.getText().isEmpty() && 
            !weekdayScheduleField.getText().matches(timeRangeRegex)) {
            errors.append("- Format d'horaire de semaine invalide (HH:MM-HH:MM)\n");
        }
        
        if (!saturdayScheduleField.getText().isEmpty() && 
            !saturdayScheduleField.getText().matches(timeRangeRegex)) {
            errors.append("- Format d'horaire du samedi invalide (HH:MM-HH:MM)\n");
        }
        
        if (!sundayScheduleField.getText().isEmpty() && 
            !sundayScheduleField.getText().matches(timeRangeRegex)) {
            errors.append("- Format d'horaire du dimanche invalide (HH:MM-HH:MM)\n");
        }
        
        if (!holidayScheduleField.getText().isEmpty() && 
            !holidayScheduleField.getText().matches(timeRangeRegex)) {
            errors.append("- Format d'horaire des jours fériés invalide (HH:MM-HH:MM)\n");
        }
        
        // Validate first and last departure times
        String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
        
        if (!firstDepartureField.getText().isEmpty() && 
            !firstDepartureField.getText().matches(timeRegex)) {
            errors.append("- Format d'heure du premier départ invalide (HH:MM)\n");
        }
        
        if (!lastDepartureField.getText().isEmpty() && 
            !lastDepartureField.getText().matches(timeRegex)) {
            errors.append("- Format d'heure du dernier départ invalide (HH:MM)\n");
        }
        
        // Validate price and duration if provided
        if (!routePriceField.getText().isEmpty()) {
            try {
                double price = Double.parseDouble(routePriceField.getText());
                if (price < 0) {
                    errors.append("- Le prix doit être positif\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide\n");
            }
        }
        
        if (!routeDurationField.getText().isEmpty()) {
            try {
                int duration = Integer.parseInt(routeDurationField.getText());
                if (duration <= 0) {
                    errors.append("- La durée doit être positive\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- La durée doit être un nombre entier valide\n");
            }
        }
        
        // Make sure at least one day is selected
        if (!mondayCheckbox.isSelected() && !tuesdayCheckbox.isSelected() && 
            !wednesdayCheckbox.isSelected() && !thursdayCheckbox.isSelected() && 
            !fridayCheckbox.isSelected() && !saturdayCheckbox.isSelected() && 
            !sundayCheckbox.isSelected()) {
            errors.append("- Au moins un jour de la semaine doit être sélectionné\n");
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