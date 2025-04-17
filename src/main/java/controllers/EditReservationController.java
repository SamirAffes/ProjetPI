package controllers;

import entities.Reservation;
import entities.Route;
import entities.Transport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import services.ReservationService;
import services.RouteService;
import services.RouteServiceImpl;
import services.TransportService;
import services.TransportServiceImpl;
import utils.AlertUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditReservationController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(EditReservationController.class.getName());

    @FXML
    private ComboBox<Route> routeComboBox;
    @FXML
    private ComboBox<Transport> transportComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> timeComboBox;
    @FXML
    private CheckBox roundTripCheckBox;
    @FXML
    private DatePicker returnDatePicker;
    @FXML
    private ComboBox<String> returnTimeComboBox;
    @FXML
    private Label priceLabel;
    @FXML
    private RadioButton payWalletRadio;
    @FXML
    private RadioButton payCardRadio;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Reservation reservation;
    private ReservationService reservationService;
    private RouteService routeService;
    private TransportService transportService;
    private Runnable onReservationUpdated;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        routeService = new RouteServiceImpl();
        transportService = new TransportServiceImpl();
        
        setupTimeComboBoxes();
        setupReturnDateControls();
        setupStatusComboBox();
        setupButtons();

        // Set up validation
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        timeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        roundTripCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
        returnDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        returnTimeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        ToggleGroup paymentGroup = new ToggleGroup();
        payWalletRadio.setToggleGroup(paymentGroup);
        payCardRadio.setToggleGroup(paymentGroup);
        payWalletRadio.setSelected(true);
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        populateFormWithReservationData();
    }
    
    public void setReservationService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    
    public void setOnReservationUpdated(Runnable callback) {
        this.onReservationUpdated = callback;
    }
    
    private void setupTimeComboBoxes() {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        for (int hour = 6; hour < 23; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        
        timeComboBox.setItems(timeOptions);
        returnTimeComboBox.setItems(timeOptions);
    }
    
    private void setupReturnDateControls() {
        roundTripCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            returnDatePicker.setDisable(!newVal);
            returnTimeComboBox.setDisable(!newVal);
        });
        
        returnDatePicker.setDisable(true);
        returnTimeComboBox.setDisable(true);
    }
    
    private void setupStatusComboBox() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "En attente", "Confirmé", "Terminé", "Annulé"
        );
        statusComboBox.setItems(statusOptions);
    }
    
    private void setupButtons() {
        cancelButton.setOnAction(this::handleCancel);
        saveButton.setOnAction(this::handleSave);
    }
    
    private void populateFormWithReservationData() {
        loadRoutes();
        
        if (reservation.getRoute() != null) {
            routeComboBox.getSelectionModel().select(reservation.getRoute());
            loadTransportsForRoute(reservation.getRoute());
        }
        
        if (reservation.getTransport() != null) {
            transportComboBox.getSelectionModel().select(reservation.getTransport());
        }
        
        if (reservation.getDateTime() != null) {
            datePicker.setValue(reservation.getDateTime().toLocalDate());
            timeComboBox.setValue(reservation.getDateTime().format(timeFormatter));
        }
        
        roundTripCheckBox.setSelected(reservation.isRoundTrip());
        
        if (reservation.getReturnDateTime() != null) {
            returnDatePicker.setValue(reservation.getReturnDateTime().toLocalDate());
            returnTimeComboBox.setValue(reservation.getReturnDateTime().format(timeFormatter));
        }
        
        priceLabel.setText(String.format("%.2f DT", reservation.getPrice()));
        
        payWalletRadio.setSelected(true); // Assume wallet by default
        
        statusComboBox.setValue(translateStatus(reservation.getStatus()));
    }
    
    private void loadRoutes() {
        routeComboBox.getItems().clear();
        routeComboBox.getItems().addAll(routeService.afficher_tout());
        
        routeComboBox.setConverter(new StringConverter<Route>() {
            @Override
            public String toString(Route route) {
                return route == null ? "" : route.getOrigin() + " → " + route.getDestination();
            }

            @Override
            public Route fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        routeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadTransportsForRoute(newVal);
                updatePrice();
            }
        });
    }
    
    private void loadTransportsForRoute(Route route) {
        try {
            List<Transport> transports = transportService.getTransportsByRouteId(route.getId());
            transportComboBox.setItems(FXCollections.observableArrayList(transports));
            
            transportComboBox.setConverter(new StringConverter<Transport>() {
                @Override
                public String toString(Transport transport) {
                    return transport == null ? "" : transport.getName() + " (" + transport.getType() + ")";
                }

                @Override
                public Transport fromString(String string) {
                    return null; // Not needed for ComboBox
                }
            });
            
            transportComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updatePrice();
                }
            });
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading transports", e);
            AlertUtils.showError("Erreur", "Impossible de charger les transports", 
                    "Une erreur est survenue lors du chargement des transports: " + e.getMessage());
        }
    }
    
    private void updatePrice() {
        Route selectedRoute = routeComboBox.getValue();
        if (selectedRoute != null) {
            double basePrice = selectedRoute.getBasePrice();
            double finalPrice = basePrice;
            
            if (roundTripCheckBox.isSelected()) {
                finalPrice = basePrice * 1.8; // 10% discount for round trip
            }
            
            priceLabel.setText(String.format("%.2f DT", finalPrice));
        }
    }
    
    private String translateStatus(String statusEnglish) {
        switch (statusEnglish.toLowerCase()) {
            case "pending":
                return "En attente";
            case "confirmed":
                return "Confirmé";
            case "completed":
                return "Terminé";
            case "cancelled":
                return "Annulé";
            default:
                return statusEnglish;
        }
    }
    
    private String translateStatusToEnglish(String statusFrench) {
        switch (statusFrench) {
            case "En attente":
                return "pending";
            case "Confirmé":
                return "confirmed";
            case "Terminé":
                return "completed";
            case "Annulé":
                return "cancelled";
            default:
                return statusFrench;
        }
    }
    
    private void validateForm() {
        boolean isValid = true;
        
        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            isValid = false;
        }
        
        if (roundTripCheckBox.isSelected() && 
            (returnDatePicker.getValue() == null || returnTimeComboBox.getValue() == null)) {
            isValid = false;
        }
        
        saveButton.setDisable(!isValid);
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Update reservation fields
            if (routeComboBox.getValue() != null) {
                reservation.setRouteId(routeComboBox.getValue().getId());
                reservation.setRoute(routeComboBox.getValue());
            }
            
            if (transportComboBox.getValue() != null) {
                reservation.setTransportId(transportComboBox.getValue().getId());
                reservation.setTransport(transportComboBox.getValue());
            }
            
            // Set date and time
            LocalDate date = datePicker.getValue();
            LocalTime time = LocalTime.parse(timeComboBox.getValue(), timeFormatter);
            reservation.setDateTime(LocalDateTime.of(date, time));
            
            // Handle round trip
            reservation.setRoundTrip(roundTripCheckBox.isSelected());
            if (roundTripCheckBox.isSelected()) {
                LocalDate returnDate = returnDatePicker.getValue();
                LocalTime returnTime = LocalTime.parse(returnTimeComboBox.getValue(), timeFormatter);
                reservation.setReturnDateTime(LocalDateTime.of(returnDate, returnTime));
            } else {
                reservation.setReturnDateTime(null);
            }
            
            // Update status
            String newStatus = translateStatusToEnglish(statusComboBox.getValue());
            reservation.setStatus(newStatus);
            
            // Set payment method (for demo)
            reservation.setIsPaid(true);
            
            // Update price (already calculated)
            try {
                String priceText = priceLabel.getText().replace(" DT", "");
                double price = Double.parseDouble(priceText);
                reservation.setPrice(price);
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Could not parse price", e);
            }
            
            // Save reservation
            reservationService.modifier(reservation);
            
            AlertUtils.showInformation("Succès", "Réservation modifiée", 
                    "La réservation a été modifiée avec succès.");
            
            if (onReservationUpdated != null) {
                onReservationUpdated.run();
            }
            
            closeWindow();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating reservation", e);
            AlertUtils.showError("Erreur", "Erreur de mise à jour", 
                    "Une erreur est survenue lors de la mise à jour de la réservation: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 