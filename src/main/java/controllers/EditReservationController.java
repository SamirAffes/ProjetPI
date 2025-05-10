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

public class EditReservationController implements Initializable {

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
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private ComboBox<String> statusComboBox;

    private User user;
    private Reservation reservation;
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final RouteService routeService = new RouteService();
    private final TransportService transportService = new TransportService();
    private double basePrice = 0.0;
    private static final Logger logger = LoggerFactory.getLogger(EditReservationController.class);

    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 DT");

    // Lists for routes and transports
    private final ObservableList<Route> routes = FXCollections.observableArrayList();
    private final ObservableList<Transport> transports = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user
        user = UserContext.getInstance().getCurrentUser();
        if (user == null) {
            logger.warn("No user in session, reservation edit may fail");
        } else {
            logger.info("Editing reservation for user: {}", user.getUsername());
        }

        setupTimeOptions();
        setupStatusOptions();
        
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
        saveButton.setOnAction(event -> saveReservation());
        cancelButton.setOnAction(event -> ((Stage) cancelButton.getScene().getWindow()).close());
        
        // Load data after initializing UI components
        loadRoutes();
        loadTransports();
    }
    
    private void setupTimeOptions() {
        // Generate time slots from 6:00 to 23:00
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        for (int hour = 6; hour <= 23; hour++) {
            timeSlots.add(String.format("%02d:00", hour));
            timeSlots.add(String.format("%02d:30", hour));
        }
        
        timeComboBox.setItems(timeSlots);
        returnTimeComboBox.setItems(timeSlots);
        
        timeComboBox.getSelectionModel().select(0);
        returnTimeComboBox.getSelectionModel().select(0);
    }
    
    private void setupStatusOptions() {
        // Set up status options
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
            ReservationStatus.PENDING.getDisplayName(),
            ReservationStatus.CONFIRMED.getDisplayName(),
            ReservationStatus.COMPLETED.getDisplayName(),
            ReservationStatus.CANCELED.getDisplayName()
        );
        
        if (statusComboBox != null) {
            statusComboBox.setItems(statusOptions);
        }
    }
    
    private void loadRoutes() {
        try {
            // Clear existing routes
            routes.clear();

            // Load routes from database
            List<Route> dbRoutes = routeService.findAll();
            if (dbRoutes != null && !dbRoutes.isEmpty()) {
                routes.addAll(dbRoutes);
                logger.info("Loaded {} routes from database", dbRoutes.size());
            } else {
                logger.warn("No routes found in database, adding fallback routes");
                // Add fallback routes in case database is empty
                routes.add(new Route(1, "Tunis", "Sousse", 25.0, 1));
                routes.add(new Route(2, "Tunis", "Sfax", 30.0, 1));
            }

            // Setup the combo box cell factory to display the route properly
            routeComboBox.setCellFactory(param -> new ListCell<Route>() {
                @Override
                protected void updateItem(Route item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getOrigin() + " → " + item.getDestination());
                    }
                }
            });

            // Set a custom string converter for the combo box
            routeComboBox.setConverter(new javafx.util.StringConverter<Route>() {
                @Override
                public String toString(Route route) {
                    if (route == null) {
                        return null;
                    }
                    return route.getOrigin() + " → " + route.getDestination();
                }

                @Override
                public Route fromString(String string) {
                    return null; // Not used in this context
                }
            });

            routeComboBox.setItems(routes);
            routeComboBox.setPromptText("Sélectionner un itinéraire");

            // Select first route if available
            if (!routes.isEmpty()) {
                routeComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading routes: {}", e.getMessage(), e);
            showError("Erreur lors du chargement des itinéraires: " + e.getMessage());
        }
    }
    
    private void loadTransports() {
        try {
            // Clear existing transports
            transports.clear();

            // Load transports from database
            List<Transport> dbTransports = transportService.findAll();
            if (dbTransports != null && !dbTransports.isEmpty()) {
                transports.addAll(dbTransports);
                logger.info("Loaded {} transports from database", dbTransports.size());
            } else {
                logger.warn("No transports found in database, adding fallback transports");
                // Add fallback transports in case database is empty
                transports.add(new Transport(1, "Bus Urbain 101", "BUS", 50, 1, "BUS-101", true));
                transports.add(new Transport(2, "Train Express", "TRAIN", 200, 1, "TRN-202", true));
            }

            // Setup the combo box cell factory to display the transport properly
            transportComboBox.setCellFactory(param -> new ListCell<Transport>() {
                @Override
                protected void updateItem(Transport item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName() + " (" + item.getType() + ")");
                    }
                }
            });

            // Set a custom string converter for the combo box
            transportComboBox.setConverter(new javafx.util.StringConverter<Transport>() {
                @Override
                public String toString(Transport transport) {
                    if (transport == null) {
                        return null;
                    }
                    return transport.getName() + " (" + transport.getType() + ")";
                }

                @Override
                public Transport fromString(String string) {
                    return null; // Not used in this context
                }
            });

            transportComboBox.setItems(transports);
            transportComboBox.setPromptText("Sélectionner un transport");

            // Select first transport if available
            if (!transports.isEmpty()) {
                transportComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading transports: {}", e.getMessage(), e);
            showError("Erreur lors du chargement des transports: " + e.getMessage());
        }
    }
    
    private void updatePrice() {
        Route selectedRoute = routeComboBox.getValue();
        Transport selectedTransport = transportComboBox.getValue();
        
        if (selectedRoute != null && selectedTransport != null) {
            // Base price calculation
            basePrice = selectedRoute.getBasePrice();
            
            // Apply transport factors
            switch (selectedTransport.getType().toUpperCase()) {
                case "BUS" -> basePrice *= 1.0;
                case "TRAIN" -> basePrice *= 1.3;
                case "TAXI" -> basePrice *= 2.5;
                case "PREMIUM" -> basePrice *= 3.0;
                default -> basePrice *= 1.0;
            }
            
            // Calculate round trip price
            double totalPrice = basePrice;
            if (roundTripCheckBox.isSelected()) {
                totalPrice *= 1.8; // 10% discount for round trip
            }
            
            priceLabel.setText(decimalFormat.format(totalPrice));
        } else {
            priceLabel.setText("0.00 DT");
        }
    }
    
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        this.user = UserContext.getInstance().getCurrentUser();
        
        // Populate form with reservation data
        if (reservation != null) {
            logger.info("Loading reservation data for editing. ID: {}", reservation.getId());
            
            // Wait a moment to ensure routes and transports are loaded
            javafx.application.Platform.runLater(() -> {
                try {
                    // If we have a route and transport entities from database, use them directly
                    if (reservation.getRoute() != null) {
                        // Find the route in our loaded routes
                        boolean routeFound = false;
                        for (Route route : routes) {
                            if (route.getId() == reservation.getRouteId()) {
                                routeComboBox.setValue(route);
                                routeFound = true;
                                break;
                            }
                        }
                        
                        // If not found, add it directly
                        if (!routeFound && reservation.getRoute() != null) {
                            routes.add(reservation.getRoute());
                            routeComboBox.setValue(reservation.getRoute());
                        }
                    } else {
                        // Try to find by ID only
                        Route selectedRoute = routeService.afficher(reservation.getRouteId());
                        if (selectedRoute != null) {
                            if (!routes.contains(selectedRoute)) {
                                routes.add(selectedRoute);
                            }
                            routeComboBox.setValue(selectedRoute);
                        }
                    }
                    
                    // Same for transport
                    if (reservation.getTransport() != null) {
                        // Find the transport in our loaded transports
                        boolean transportFound = false;
                        for (Transport transport : transports) {
                            if (transport.getId() == reservation.getTransportId()) {
                                transportComboBox.setValue(transport);
                                transportFound = true;
                                break;
                            }
                        }
                        
                        // If not found, add it directly
                        if (!transportFound && reservation.getTransport() != null) {
                            transports.add(reservation.getTransport());
                            transportComboBox.setValue(reservation.getTransport());
                        }
                    } else {
                        // Try to find by ID only
                        Transport selectedTransport = transportService.afficher(reservation.getTransportId());
                        if (selectedTransport != null) {
                            if (!transports.contains(selectedTransport)) {
                                transports.add(selectedTransport);
                            }
                            transportComboBox.setValue(selectedTransport);
                        }
                    }
                    
                    // Set date and time
                    if (reservation.getDateTime() != null) {
                        datePicker.setValue(reservation.getDateTime().toLocalDate());
                        String time = reservation.getDateTime().toLocalTime().toString();
                        for (String t : timeComboBox.getItems()) {
                            if (t.equals(time)) {
                                timeComboBox.setValue(t);
                                break;
                            }
                        }
                    }
                    
                    // Set round trip info
                    roundTripCheckBox.setSelected(reservation.isRoundTrip());
                    if (reservation.isRoundTrip() && reservation.getReturnDateTime() != null) {
                        returnDatePicker.setValue(reservation.getReturnDateTime().toLocalDate());
                        String returnTime = reservation.getReturnDateTime().toLocalTime().toString();
                        for (String t : returnTimeComboBox.getItems()) {
                            if (t.equals(returnTime)) {
                                returnTimeComboBox.setValue(t);
                                break;
                            }
                        }
                    }
                    
                    // Set payment info
                    if (reservation.isPaid()) {
                        payCardRadio.setSelected(true);
                    } else {
                        payWalletRadio.setSelected(true);
                    }
                    
                    // Set status
                    if (statusComboBox != null) {
                        String status = "";
                        switch(reservation.getStatus().toUpperCase()) {
                            case "PENDING":
                                status = ReservationStatus.PENDING.getDisplayName();
                                break;
                            case "CONFIRMED":
                                status = ReservationStatus.CONFIRMED.getDisplayName();
                                break;
                            case "COMPLETED":
                                status = ReservationStatus.COMPLETED.getDisplayName();
                                break;
                            case "CANCELED":
                            case "CANCELLED":
                                status = ReservationStatus.CANCELED.getDisplayName();
                                break;
                            default:
                                status = ReservationStatus.PENDING.getDisplayName();
                        }
                        statusComboBox.setValue(status);
                    }
                    
                    // Update price
                    updatePrice();
                } catch (Exception e) {
                    logger.error("Error setting reservation data: {}", e.getMessage(), e);
                    showError("Erreur lors du chargement des données de réservation: " + e.getMessage());
                }
            });
        }
    }
    
    private void saveReservation() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            // Update reservation object
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
            } else {
                reservation.setReturnDateTime(null);
            }
            
            // Set price and payment info
            double price = basePrice;
            if (roundTripCheckBox.isSelected()) {
                price *= 1.8; // Apply a 10% discount for round trip
            }
            reservation.setPrice(price);
            reservation.setIsPaid(payCardRadio.isSelected()); // Only paid if card payment is selected
            
            // Set status
            if (statusComboBox != null && statusComboBox.getValue() != null) {
                String statusValue = statusComboBox.getValue();
                String statusCode = "";
                
                if (statusValue.equals(ReservationStatus.PENDING.getDisplayName())) {
                    statusCode = ReservationStatus.PENDING.name();
                } else if (statusValue.equals(ReservationStatus.CONFIRMED.getDisplayName())) {
                    statusCode = ReservationStatus.CONFIRMED.name();
                } else if (statusValue.equals(ReservationStatus.COMPLETED.getDisplayName())) {
                    statusCode = ReservationStatus.COMPLETED.name();
                } else if (statusValue.equals(ReservationStatus.CANCELED.getDisplayName())) {
                    statusCode = ReservationStatus.CANCELED.name();
                }
                
                reservation.setStatus(statusCode);
            }
            
            // Update the reservation
            if (reservationService.update(reservation)) {
                showSuccess("Réservation mise à jour avec succès !");
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            } else {
                showError("Erreur lors de la mise à jour de la réservation.");
            }
        } catch (Exception e) {
            logger.error("Error saving reservation: {}", e.getMessage(), e);
            showError("Erreur lors de la mise à jour de la réservation: " + e.getMessage());
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        if (routeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un itinéraire.\n");
        }
        
        if (transportComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un transport.\n");
        }
        
        if (datePicker.getValue() == null) {
            errors.append("- Veuillez sélectionner une date de départ.\n");
        }
        
        if (timeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner une heure de départ.\n");
        }
        
        if (roundTripCheckBox.isSelected()) {
            if (returnDatePicker.getValue() == null) {
                errors.append("- Veuillez sélectionner une date de retour.\n");
            } else if (returnDatePicker.getValue().isBefore(datePicker.getValue())) {
                errors.append("- La date de retour ne peut pas être avant la date de départ.\n");
            }
            
            if (returnTimeComboBox.getValue() == null) {
                errors.append("- Veuillez sélectionner une heure de retour.\n");
            }
        }
        
        if (statusComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un statut.\n");
        }
        
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de Validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 