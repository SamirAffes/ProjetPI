package controllers;

import entities.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.*;
import utils.DateUtils;
import utils.UserContext;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final RouteService routeService = new RouteService();
    private final TransportService transportService = new TransportService();
    private double basePrice = 0.0;
    private static final Logger logger = LoggerFactory.getLogger(NewReservationController.class);

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 DT");

    // Lists for routes and transports
    private final ObservableList<Route> routes = FXCollections.observableArrayList();
    private final ObservableList<Transport> transports = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        user = UserContext.getInstance().getCurrentUser();
        if (user == null) {
            logger.warn("No user in session, reservation creation may fail");
        } else {
            logger.info("Creating reservation for user: {}", user.getUsername());
        }

        setupTimeOptions();
        loadRoutes();
        loadTransports();

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

    private void loadRoutes() {
        try {
            // Clear existing routes
            routes.clear();

            // Load routes from database
            List<Route> dbRoutes = routeService.afficher_tout();
            if (dbRoutes != null && !dbRoutes.isEmpty()) {
                routes.addAll(dbRoutes);
                logger.info("Loaded {} routes from database", dbRoutes.size());
            } else {
                logger.warn("No routes found in database, adding fallback routes");
                // Add fallback routes in case database is empty
                routes.add(new Route(1, "Downtown", "Airport", 25.0, 1));
                routes.add(new Route(2, "Airport", "Beach Resort", 30.0, 1));
            }

            routeComboBox.setItems(routes);
            routeComboBox.setPromptText("Sélectionner un itinéraire");

            // Select first route if available
            if (!routes.isEmpty()) {
                routeComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading routes from database", e);
            // Add fallback routes
            routes.add(new Route(1, "Centre ville", "Aéroport", 25.0, 1));
            routes.add(new Route(2, "Aéroport", "Plage", 30.0, 1));
            routeComboBox.setItems(routes);
        }
    }

    private void loadTransports() {
        try {
            // Clear existing transports
            transports.clear();

            // Load transports from database
            List<Transport> dbTransports = transportService.afficher_tout();
            if (dbTransports != null && !dbTransports.isEmpty()) {
                transports.addAll(dbTransports);
                logger.info("Loaded {} transports from database", dbTransports.size());
            } else {
                logger.warn("No transports found in database, adding fallback transports");
                // Add fallback transports in case database is empty
                transports.add(new Transport(1, "Bus Urbain 101", "BUS", 50, 1, "BUS-101", true));
                transports.add(new Transport(2, "Train Express", "TRAIN", 200, 1, "TRN-202", true));
            }

            transportComboBox.setItems(transports);
            transportComboBox.setPromptText("Sélectionner un transport");

            // Select first transport if available
            if (!transports.isEmpty()) {
                transportComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading transports from database", e);
            // Add fallback transports
            transports.add(new Transport(1, "Bus Urbain 101", "BUS", 50, 1, "BUS-101", true));
            transports.add(new Transport(2, "Train Express", "TRAIN", 200, 1, "TRN-202", true));
            transportComboBox.setItems(transports);
        }
    }

    private void updatePrice() {
        Route selectedRoute = routeComboBox.getValue();
        if (selectedRoute == null) {
            priceLabel.setText("0.00 DT");
            return;
        }

        basePrice = selectedRoute.getBasePrice();
        double totalPrice = basePrice;

        // If round trip, double the price with a 10% discount
        if (roundTripCheckBox.isSelected()) {
            totalPrice *= 1.8; // Apply a 10% discount for round trip (1.8 instead of 2.0)
        }

        priceLabel.setText(decimalFormat.format(totalPrice));
    }

    private void createReservation() {
        // Validate inputs
        if (routeComboBox.getValue() == null) {
            showError("Veuillez sélectionner un itinéraire");
            return;
        }

        if (transportComboBox.getValue() == null) {
            showError("Veuillez sélectionner un transport");
            return;
        }

        if (datePicker.getValue() == null) {
            showError("Veuillez sélectionner une date");
            return;
        }

        if (timeComboBox.getValue() == null) {
            showError("Veuillez sélectionner une heure");
            return;
        }

        if (roundTripCheckBox.isSelected()) {
            if (returnDatePicker.getValue() == null) {
                showError("Veuillez sélectionner une date de retour");
                return;
            }

            if (returnTimeComboBox.getValue() == null) {
                showError("Veuillez sélectionner une heure de retour");
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
            reservation.setIsPaid(payCardRadio.isSelected()); // Only paid if card payment is selected

            // Set status to PENDING by default
            reservation.setStatus(ReservationStatus.PENDING.name());

            // Save the reservation
            reservationService.ajouter(reservation);

            // Show success message and close window
            showSuccess("Réservation créée avec succès !");
            Stage stage = (Stage) reserveButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            logger.error("Error creating reservation", e);
            showError("Erreur lors de la création de la réservation: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText("Réservation");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
