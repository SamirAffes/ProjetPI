package controllers;

import entities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;
import services.ReservationServiceImpl;
import utils.AlertUtils;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NewReservationController implements Initializable {

    @FXML private ComboBox<Route> routeComboBox;
    @FXML private ComboBox<Transport> transportComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private CheckBox roundTripCheckBox;
    @FXML private DatePicker returnDatePicker;
    @FXML private ComboBox<String> returnTimeComboBox;
    @FXML private Label priceLabel;
    @FXML private RadioButton payWalletRadio;
    @FXML private RadioButton payCardRadio;
    @FXML private Button reserveButton;
    @FXML private Button cancelButton;
    
    private User user;
    private ReservationService reservationService;
    private double basePrice = 0.0;
    private static final Logger LOGGER = Logger.getLogger(NewReservationController.class.getName());
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 DT");
    
    // Dummy data for routes and transports (would be replaced with actual data from services)
    private final ObservableList<Route> routes = FXCollections.observableArrayList();
    private final ObservableList<Transport> transports = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reservationService = new ReservationServiceImpl();
        
        // Initialize user with a dummy ID for demo purposes
        // In a real app, this would be set properly via setUser()
        user = new User();
        user.setId(1);
        
        setupTimeOptions();
        setupRoutes();
        setupTransports();
        
        // Initialize UI components
        datePicker.setValue(LocalDate.now());
        timeComboBox.getSelectionModel().selectFirst();
        
        returnDatePicker.setDisable(true);
        returnTimeComboBox.setDisable(true);
        
        roundTripCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            returnDatePicker.setDisable(!newVal);
            returnTimeComboBox.setDisable(!newVal);
            updatePrice();
        });
        
        ToggleGroup paymentToggle = new ToggleGroup();
        payWalletRadio.setToggleGroup(paymentToggle);
        payCardRadio.setToggleGroup(paymentToggle);
        payCardRadio.setSelected(true);
        
        // Add listeners for price calculation
        routeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updatePrice());
        
        // Set button actions
        reserveButton.setOnAction(event -> createReservation());
        cancelButton.setOnAction(event -> ((Stage) cancelButton.getScene().getWindow()).close());
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    private void setupTimeOptions() {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        
        // Add time options every 30 minutes
        for (int hour = 6; hour < 22; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        
        timeComboBox.setItems(timeOptions);
        returnTimeComboBox.setItems(timeOptions);
        
        // Select default time
        timeComboBox.getSelectionModel().select("09:00");
        returnTimeComboBox.getSelectionModel().select("18:00");
    }
    
    private void setupRoutes() {
        // In a real implementation, this would fetch routes from a RouteService
        // For now, we'll add some dummy data
        routes.add(new Route(1, "Downtown", "Airport", 25.0, 1));
        routes.add(new Route(2, "Airport", "Beach Resort", 30.0, 1));
        routes.add(new Route(3, "Downtown", "Shopping Mall", 15.0, 2));
        routes.add(new Route(4, "University", "Downtown", 10.0, 2));
        
        routeComboBox.setItems(routes);
        routeComboBox.setPromptText("Select a route");
    }
    
    private void setupTransports() {
        // In a real implementation, this would fetch transports from a TransportService
        // For now, we'll add some dummy data
        transports.add(new Transport(1, "City Bus 101", "BUS", 50, 1, "BUS-101", true));
        transports.add(new Transport(2, "Express Train", "TRAIN", 200, 1, "TRN-202", true));
        transports.add(new Transport(3, "Yellow Cab", "TAXI", 4, 2, "TAXI-303", true));
        
        transportComboBox.setItems(transports);
        transportComboBox.setPromptText("Select a transport");
    }
    
    private void updatePrice() {
        Route selectedRoute = routeComboBox.getValue();
        if (selectedRoute == null) {
            priceLabel.setText("0.00 DT");
            return;
        }
        
        basePrice = selectedRoute.getPrice();
        double totalPrice = basePrice;
        
        // If round trip, double the price
        if (roundTripCheckBox.isSelected()) {
            totalPrice *= 1.8; // Apply a 10% discount for round trip (1.8 instead of 2.0)
        }
        
        priceLabel.setText(decimalFormat.format(totalPrice));
    }
    
    private void createReservation() {
        // Validate inputs
        if (routeComboBox.getValue() == null) {
            showError("Please select a route");
            return;
        }
        
        if (transportComboBox.getValue() == null) {
            showError("Please select a transport");
            return;
        }
        
        if (datePicker.getValue() == null) {
            showError("Please select a date");
            return;
        }
        
        if (timeComboBox.getValue() == null) {
            showError("Please select a time");
            return;
        }
        
        if (roundTripCheckBox.isSelected()) {
            if (returnDatePicker.getValue() == null) {
                showError("Please select a return date");
                return;
            }
            
            if (returnTimeComboBox.getValue() == null) {
                showError("Please select a return time");
                return;
            }
        }
        
        try {
            // Create reservation object
            Reservation reservation = new Reservation();
            
            // Set basic properties
            reservation.setUserId(user.getId());
            reservation.setRouteId(routeComboBox.getValue().getId());
            reservation.setTransportId(transportComboBox.getValue().getId());
            
            // Set date and time
            LocalTime time = LocalTime.parse(timeComboBox.getValue());
            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), time);
            reservation.setDateTime(dateTime);
            
            // Set round trip info
            reservation.setRoundTrip(roundTripCheckBox.isSelected());
            if (roundTripCheckBox.isSelected()) {
                LocalTime returnTime = LocalTime.parse(returnTimeComboBox.getValue());
                LocalDateTime returnDateTime = LocalDateTime.of(returnDatePicker.getValue(), returnTime);
                reservation.setReturnDateTime(returnDateTime);
            }
            
            // Set price and payment info
            double price = basePrice;
            if (roundTripCheckBox.isSelected()) {
                price *= 1.8; // Apply a 10% discount for round trip
            }
            reservation.setPrice(price);
            reservation.setIsPaid(true); // Assuming payment is processed immediately
            
            // Set status to PENDING by default
            reservation.setStatus(ReservationStatus.PENDING.name());
            
            // Save the reservation
            reservationService.ajouter(reservation);
            
            // Close the window
            Stage stage = (Stage) reserveButton.getScene().getWindow();
            AlertUtils.showInformation("Réservation", "Réservation créée", "Votre réservation a été créée avec succès!");
            stage.close();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating reservation", e);
            showError("Failed to create reservation: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        AlertUtils.showError("Erreur", "Une erreur est survenue", message);
    }
} 