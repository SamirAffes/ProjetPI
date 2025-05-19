package controllers;

import entities.Reservation;
import entities.Route;
import entities.Transport;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import services.ReservationService;
import services.ReservationServiceImpl;
import services.TransportService;
import utils.UserContext;
import javafx.scene.Node;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;

/**
 * Controller for the create reservation form
 */
@Slf4j
public class CreateReservationController implements Initializable {

    @FXML private Label routeLabel;
    @FXML private ComboBox<Transport> transportComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private CheckBox roundTripCheckBox;
    @FXML private VBox returnDateContainer;
    @FXML private DatePicker returnDatePicker;
    @FXML private ComboBox<String> returnTimeComboBox;
    @FXML private Label priceLabel;
    @FXML private RadioButton payWalletRadio;
    @FXML private RadioButton payCardRadio;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final ObservableList<Transport> transports = FXCollections.observableArrayList();
    private final TransportService transportService = new TransportService();
    private final ReservationService reservationService = new ReservationServiceImpl();

    private Route selectedRoute;
    private String selectedTransportType;
    private User currentUser;
    private double totalPrice = 0.0;
    private boolean paymentMode = false;

    // New payment related fields
    @FXML private VBox paymentContainer;
    @FXML private RadioButton creditCardRadio;
    @FXML private RadioButton debitCardRadio;
    @FXML private RadioButton paypalRadio;
    @FXML private RadioButton bankTransferRadio;
    @FXML private TextField cardNumberTextField;
    @FXML private TextField cardHolderTextField;
    @FXML private TextField expiryDateTextField;
    @FXML private TextField cvvTextField;
    @FXML private Button processPaymentButton;
    @FXML private ProgressIndicator paymentProgressIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing CreateReservationController");

        // Get current user from context
        currentUser = UserContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            log.error("No user found in CreateReservationController");
            return;
        }

        // Setup time options
        setupTimeOptions();

        // Setup round trip checkbox action
        setupRoundTripCheckbox();

        // Setup payment options
        setupPaymentOptions();

        // Setup save and cancel buttons
        setupButtons();

        // Setup payment container if exists
        setupPaymentContainer();
    }

    /**
     * Set the selected route and transport type from the search view
     */
    public void setRouteAndTransportType(Route route, String transportType) {
        log.info("Setting route and transport type: {} - {}", route, transportType);

        this.selectedRoute = route;
        this.selectedTransportType = transportType;

        // Update route label
        if (routeLabel != null && route != null) {
            routeLabel.setText(route.getOrigin() + " → " + route.getDestination());

            // Update weather widget with origin city
            if (route.getOrigin() != null && !route.getOrigin().isEmpty()) {
                Platform.runLater(() -> updateWeatherWidget(route.getOrigin()));
            }
        }

        // Load matching transports
        loadTransports();

        // Set default date to today
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }

        // Update price
        updatePrice();
    }

    /**
     * Set the departure time and date from the search view
     */
    public void setDepartureTimeAndDate(String time, LocalDate date) {
        log.info("Setting departure time and date: {} - {}", time, date);

        if (datePicker != null && date != null) {
            datePicker.setValue(date);
        }

        if (timeComboBox != null && time != null) {
            timeComboBox.setValue(time);
        }
    }

    /**
     * Set payment mode - if true, UI is configured for payment flow
     */
    public void setPaymentMode(boolean isPaymentMode) {
        log.info("Setting payment mode: {}", isPaymentMode);
        this.paymentMode = isPaymentMode;

        if (isPaymentMode) {
            // Update UI for payment mode
            if (saveButton != null) {
                saveButton.setText("Confirmer et payer");
            }

            // Default to card payment
            if (payCardRadio != null) {
                payCardRadio.setSelected(true);
            }

            // Show payment container if available
            if (paymentContainer != null) {
                paymentContainer.setVisible(true);
                paymentContainer.setManaged(true);
            }
        }
    }

    private void setupTimeOptions() {
        // Populate time options at 30-minute intervals
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        for (int hour = 6; hour < 22; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }

        if (timeComboBox != null) {
            timeComboBox.setItems(timeOptions);
            timeComboBox.setValue("08:00");
        }

        if (returnTimeComboBox != null) {
            returnTimeComboBox.setItems(timeOptions);
            returnTimeComboBox.setValue("18:00");
        }
    }

    private void setupRoundTripCheckbox() {
        if (roundTripCheckBox != null && returnDateContainer != null) {
            // Initially hide return date container
            returnDateContainer.setVisible(false);
            returnDateContainer.setManaged(false);

            // Show/hide return date options based on checkbox
            roundTripCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                returnDateContainer.setVisible(newVal);
                returnDateContainer.setManaged(newVal);
                updatePrice();
            });
        }
    }

    private void setupPaymentOptions() {
        if (payWalletRadio != null && payCardRadio != null) {
            // Group radio buttons
            ToggleGroup paymentGroup = new ToggleGroup();
            payWalletRadio.setToggleGroup(paymentGroup);
            payCardRadio.setToggleGroup(paymentGroup);

            // Select wallet payment by default
            payWalletRadio.setSelected(true);
        }
    }

    private void setupPaymentContainer() {
        if (paymentContainer != null) {
            // Initially hide payment container unless in payment mode
            paymentContainer.setVisible(paymentMode);
            paymentContainer.setManaged(paymentMode);

            // Configure payment method radios if they exist
            if (creditCardRadio != null && debitCardRadio != null && 
                paypalRadio != null && bankTransferRadio != null) {

                ToggleGroup paymentMethodGroup = new ToggleGroup();
                creditCardRadio.setToggleGroup(paymentMethodGroup);
                debitCardRadio.setToggleGroup(paymentMethodGroup);
                paypalRadio.setToggleGroup(paymentMethodGroup);
                bankTransferRadio.setToggleGroup(paymentMethodGroup);

                // Select credit card by default
                creditCardRadio.setSelected(true);

                // Setup listeners to show/hide appropriate fields
                paymentMethodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                    updatePaymentFields();
                });

                // Initial call to set up fields
                updatePaymentFields();
            }

            // Setup payment processing button
            if (processPaymentButton != null) {
                processPaymentButton.setOnAction(event -> processPayment());
            }

            // Hide progress indicator initially
            if (paymentProgressIndicator != null) {
                paymentProgressIndicator.setVisible(false);
            }
        }
    }

    private void updatePaymentFields() {
        if (cardNumberTextField == null || cardHolderTextField == null || 
            expiryDateTextField == null || cvvTextField == null) {
            return;
        }

        boolean isCardPayment = creditCardRadio.isSelected() || debitCardRadio.isSelected();

        // Show/hide card fields based on selection
        cardNumberTextField.setVisible(isCardPayment);
        cardNumberTextField.setManaged(isCardPayment);
        cardHolderTextField.setVisible(isCardPayment);
        cardHolderTextField.setManaged(isCardPayment);
        expiryDateTextField.setVisible(isCardPayment);
        expiryDateTextField.setManaged(isCardPayment);
        cvvTextField.setVisible(isCardPayment);
        cvvTextField.setManaged(isCardPayment);
    }

    private void processPayment() {
        // Validate payment information
        if (creditCardRadio.isSelected() || debitCardRadio.isSelected()) {
            if (cardNumberTextField.getText().isEmpty() ||
                cardHolderTextField.getText().isEmpty() ||
                expiryDateTextField.getText().isEmpty() ||
                cvvTextField.getText().isEmpty()) {
                showError("Validation", "Veuillez remplir tous les champs de paiement.");
                return;
            }

            // Validate card number (basic check)
            String cardNumber = cardNumberTextField.getText().replaceAll("\\s", "");
            if (!cardNumber.matches("\\d{16}")) {
                showError("Validation", "Numéro de carte invalide. Veuillez entrer 16 chiffres.");
                return;
            }

            // Validate expiry date (MM/YY format)
            if (!expiryDateTextField.getText().matches("\\d{2}/\\d{2}")) {
                showError("Validation", "Format de date d'expiration invalide. Utilisez MM/YY.");
                return;
            }

            // Validate CVV (3 digits)
            if (!cvvTextField.getText().matches("\\d{3}")) {
                showError("Validation", "CVV invalide. Veuillez entrer 3 chiffres.");
                return;
            }
        }

        // Show progress indicator
        if (paymentProgressIndicator != null) {
            paymentProgressIndicator.setVisible(true);
        }

        // Disable payment button
        if (processPaymentButton != null) {
            processPaymentButton.setDisable(true);
        }

        // Simulate payment processing
        new Thread(() -> {
            try {
                // Simulate payment processing delay
                Thread.sleep(2000);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Hide progress indicator
                    if (paymentProgressIndicator != null) {
                        paymentProgressIndicator.setVisible(false);
                    }

                    // Enable payment button
                    if (processPaymentButton != null) {
                        processPaymentButton.setDisable(false);
                    }

                    // Save reservation with payment
                    saveReservationWithPayment();
                });
            } catch (InterruptedException e) {
                log.error("Payment processing interrupted", e);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Hide progress indicator
                    if (paymentProgressIndicator != null) {
                        paymentProgressIndicator.setVisible(false);
                    }

                    // Enable payment button
                    if (processPaymentButton != null) {
                        processPaymentButton.setDisable(false);
                    }

                    showError("Erreur", "Le traitement du paiement a été interrompu.");
                });
            }
        }).start();
    }

    private void saveReservationWithPayment() {
        try {
            if (selectedRoute == null) {
                showError("Erreur", "Aucun itinéraire sélectionné.");
                return;
            }

            if (transportComboBox.getValue() == null) {
                showError("Erreur", "Aucun transport sélectionné.");
                return;
            }

            // Create reservation object
            Reservation reservation = new Reservation();
            reservation.setUserId(currentUser.getId());
            reservation.setRouteId(selectedRoute.getId());
            reservation.setTransportId(transportComboBox.getValue().getId());

            // Set the route company ID from the selected route's company ID
            // This associates the reservation with the specific organization
            if (selectedRoute.getCompanyId() > 0) {
                reservation.setRouteCompanyId(selectedRoute.getCompanyId());
                log.info("Setting reservation company ID to: {}", selectedRoute.getCompanyId());
            }

            // Set date and time
            LocalTime time = LocalTime.parse(timeComboBox.getValue());
            LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), time);
            reservation.setDateTime(dateTime);

            // Set round trip info if selected
            reservation.setRoundTrip(roundTripCheckBox.isSelected());
            if (roundTripCheckBox.isSelected() && returnTimeComboBox.getValue() != null && returnDatePicker.getValue() != null) {
                LocalTime returnTime = LocalTime.parse(returnTimeComboBox.getValue());
                LocalDateTime returnDateTime = LocalDateTime.of(returnDatePicker.getValue(), returnTime);
                reservation.setReturnDateTime(returnDateTime);
            }

            // Set payment info
            boolean isPaid = paymentMode || payCardRadio.isSelected() || 
                            (paymentContainer != null && (creditCardRadio.isSelected() || 
                            debitCardRadio.isSelected() || paypalRadio.isSelected()));

            reservation.setIsPaid(isPaid);
            reservation.setPrice(totalPrice);

            // Set initial status (if paid, set to CONFIRMED)
            reservation.setStatus(isPaid ? "CONFIRMED" : "PENDING");

            // Save reservation
            reservationService.ajouter(reservation);
            log.info("Reservation created successfully with ID: {}", reservation.getId());

            // Show success message and close form
            if (isPaid) {
                showSuccessPayment("Paiement réussi", "Votre paiement a été traité avec succès et votre réservation est confirmée.");
            } else {
                showInfo("Succès", "Réservation créée avec succès !");
            }
            closeForm();

        } catch (Exception e) {
            log.error("Error creating reservation: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de créer la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void saveReservation() {
        // If in payment mode or we have a payment container visible, process payment
        if (paymentMode || (paymentContainer != null && paymentContainer.isVisible())) {
            processPayment();
        } else {
            // Otherwise, save reservation directly
            saveReservationWithPayment();
        }
    }

    private void closeForm() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessPayment(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Add custom styling to make it look like a success message
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("success-dialog");
        dialogPane.setStyle("-fx-background-color: #dff0d8; -fx-border-color: #d6e9c6;");

        alert.showAndWait();
    }

    private void loadTransports() {
        try {
            // Clear existing list
            transports.clear();

            if (selectedTransportType == null || selectedTransportType.isEmpty()) {
                // Load all transports if no specific type selected
                transports.addAll(transportService.afficher_tout());
            } else {
                // Filter by transport type
                List<Transport> allTransports = transportService.afficher_tout();
                allTransports.stream()
                    .filter(t -> t.getType().equalsIgnoreCase(selectedTransportType) && t.isAvailable())
                    .forEach(transports::add);
            }

            // If no transports found, add a sample one for testing
            if (transports.isEmpty()) {
                Transport testTransport = new Transport();
                testTransport.setId(1);
                testTransport.setName("Transport Test");
                testTransport.setType(selectedTransportType != null ? selectedTransportType : "Bus");
                testTransport.setCapacity(50);
                testTransport.setAvailable(true);
                testTransport.setCompanyId(1);
                transports.add(testTransport);
            }

            // Set up cell factory for transport ComboBox
            if (transportComboBox != null) {
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

                // Set string converter for the selected value
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
                transportComboBox.getSelectionModel().selectFirst();

                // Listen for changes to update price
                transportComboBox.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldVal, newVal) -> updatePrice());
            }

        } catch (Exception e) {
            log.error("Error loading transports: {}", e.getMessage(), e);
            showError("Erreur", "Impossible de charger les transports: " + e.getMessage());
        }
    }

    private void updatePrice() {
        if (selectedRoute == null || priceLabel == null) {
            return;
        }

        // Base price from route
        double basePrice = selectedRoute.getBasePrice();
        totalPrice = basePrice;

        // Apply additional fee for premium transport (10%)
        Transport selectedTransport = transportComboBox.getValue();
        if (selectedTransport != null && "Avion".equalsIgnoreCase(selectedTransport.getType())) {
            totalPrice += basePrice * 0.1;
        }

        // Double price for round trip
        if (roundTripCheckBox.isSelected()) {
            totalPrice *= 2;
        }

        // Update price label
        priceLabel.setText(String.format("%.2f DT", totalPrice));
    }

    private void setupButtons() {
        if (saveButton != null) {
            saveButton.setOnAction(event -> saveReservation());
        }

        if (cancelButton != null) {
            cancelButton.setOnAction(event -> closeForm());
        }
    }

    /**
     * Updates the weather widget with the specified city
     * 
     * @param city The city name
     */
    private void updateWeatherWidget(String city) {
        try {
            // Extract the main city name if it's a detailed location
            String mainCity = city;
            if (city.contains(" - ")) {
                mainCity = city.substring(0, city.indexOf(" - "));
            } else if (city.contains("(")) {
                mainCity = city.substring(0, city.indexOf("(")).trim();
            }

            log.info("Updating weather widget with city: {}", mainCity);

            // Use a simpler approach - find all WeatherWidgetController instances in the scene
            if (routeLabel.getScene() != null) {
                // Find the weather widget by its ID
                VBox weatherWidget = (VBox) routeLabel.getScene().lookup("#weatherWidget");
                if (weatherWidget != null) {
                    // Get the controller through reflection
                    for (Node node : weatherWidget.getChildren()) {
                        if (node.getId() != null && node.getId().equals("cityLabel")) {
                            // Found the city label, update it directly as a workaround
                            if (node instanceof Label) {
                                ((Label) node).setText(mainCity);
                                log.info("Updated city label directly");
                            }
                        }
                    }
                } else {
                    log.warn("Weather widget not found in the scene");
                }
            } else {
                log.warn("Scene not available yet");
            }
        } catch (Exception e) {
            log.error("Error updating weather widget: {}", e.getMessage(), e);
        }
    }
} 
