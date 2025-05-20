package controllers;

import entities.Reservation;
import entities.ReservationStatus;
import entities.Route;
import entities.Transport;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.ReclamationService;
import org.kordamp.ikonli.javafx.FontIcon;
import services.ReservationService;
import services.ReservationServiceImpl;
import utils.UserContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class UserDashboardController {

    @FXML
    private Label dashboardTitle;

    @FXML
    private Label welcomeLabel;

    // These labels are now in homeContent.fxml
    private Label reservationsCountLabel;
    private Label completedReservationsLabel;
    private Label pendingReservationsLabel;

    @FXML
    private VBox contentArea;

    @FXML
    private VBox recentActivityContainer;

    @FXML
    private HBox popularRoutesContainer;

    @FXML
    private Button createReservationButton;

    // Buttons for visual indication of current page
    @FXML
    private Button dashboardButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button reservationsButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button reclamationsButton;

    private User currentUser;
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final ReclamationService reclamationService = new ReclamationService();

    // Store reservation statistics
    private int totalReservations = 0;
    private int completedReservations = 0;
    private int pendingReservations = 0;

    @FXML
    public void initialize() {
        // Get current user from context if not already set
        if (currentUser == null) {
            currentUser = UserContext.getInstance().getCurrentUser();
        }

        if (currentUser != null) {
            // Update welcome message
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue, " + currentUser.getFullName());
            }

            // Load reservation statistics
            loadReservationStats();

            // Load the home content by default
            loadHomeContent();

            // Set dashboard button as active
            setActiveButton(dashboardButton);
        } else {
            log.error("No user found in dashboard controller");
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getFullName());
            loadReservationStats();
            loadHomeContent();
        }
    }

    private void loadReservationStats() {
        try {
            // Load user's reservation data
            totalReservations = reservationService.getReservationsByUserId(currentUser.getId()).size();
            pendingReservations = reservationService.getReservationsByUserIdAndStatus(
                    currentUser.getId(), "PENDING").size();
            completedReservations = reservationService.getReservationsByUserIdAndStatus(
                    currentUser.getId(), "COMPLETED").size();
        } catch (Exception e) {
            log.error("Error loading reservation data", e);
            totalReservations = 0;
            completedReservations = 0;
            pendingReservations = 0;
        }
    }

    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardButton.getStyleClass().remove("active");
        searchButton.getStyleClass().remove("active");
        reservationsButton.getStyleClass().remove("active");
        profileButton.getStyleClass().remove("active");
        reclamationsButton.getStyleClass().remove("active");

        // Add active class to the selected button
        activeButton.getStyleClass().add("active");
    }

    private void loadContent(String fxmlFile) {
        try {
            // Clear existing content
            contentArea.getChildren().clear();

            // Load the new content
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();

            // Add the new content to the content area
            contentArea.getChildren().add(content);

            // Initialize specific content based on the loaded file
            if (fxmlFile.contains("search.fxml")) {
                initializeSearchView(content);
            } else if (fxmlFile.contains("reservations.fxml")) {
                initializeReservationsView(content);
            } else if (fxmlFile.contains("profile.fxml")) {
                initializeProfileView(content);
            }

        } catch (IOException e) {
            log.error("Error loading content: {}", e.getMessage(), e);
        }
    }

    private void loadHomeContent() {
        try {
            // Clear existing content
            contentArea.getChildren().clear();

            // Load the home content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/homeContent.fxml"));
            Parent content = loader.load();

            // Add the new content to the content area
            contentArea.getChildren().add(content);

            // Update the reservation statistics in the loaded content
            updateReservationStats(content);

            // Set current date in the welcome banner
            Label dateLabel = (Label) content.lookup("#currentDateLabel");
            if (dateLabel != null) {
                // Format current date as "Day, DD Month YYYY"
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.FRENCH);
                String formattedDate = java.time.LocalDate.now().format(formatter);
                // Capitalize first letter
                formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                dateLabel.setText(formattedDate);
            }

            // Add action to the create reservation button in the loaded content
            Button createButton = (Button) content.lookup("#createReservationButton");
            if (createButton != null) {
                createButton.setOnAction(event -> onCreateReservationButtonClick());
            }

            // Connect quick action buttons to their respective functions
            setupQuickActionButtons(content);

            // Populate upcoming reservations if container exists
            populateUpcomingReservations(content);

        } catch (IOException e) {
            log.error("Error loading home content: {}", e.getMessage(), e);
        }
    }

    /**
     * Sets up the quick action buttons in the dashboard
     */
    private void setupQuickActionButtons(Parent content) {
        // Find all action buttons by their parent container
        HBox actionButtonsRow1 = (HBox) content.lookup(".dashboard-panel .action-button").getParent();
        if (actionButtonsRow1 != null) {
            // Get the first row of buttons
            for (Node node : actionButtonsRow1.getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button)node;
                    // Find the label inside the button's graphic
                    HBox hbox = (HBox) button.getGraphic();
                    if (hbox != null) {
                        for (Node child : hbox.getChildren()) {
                            if (child instanceof Label) {
                                Label label = (Label) child;
                                String text = label.getText();

                                // Set actions based on button text
                                if ("Rechercher".equals(text)) {
                                    button.setOnAction(event -> onSearchButtonClick());
                                } else if ("Réserver".equals(text)) {
                                    button.setOnAction(event -> onCreateReservationButtonClick());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Find the second row of buttons
        HBox actionButtonsRow2 = (HBox) content.lookup(".dashboard-panel .action-button").getParent().getParent().getChildrenUnmodifiable().get(2);
        if (actionButtonsRow2 instanceof HBox) {
            for (Node node : ((HBox) actionButtonsRow2).getChildren()) {
                if (node instanceof Button) {
                    Button button = (Button) node;
                    // Find the label inside the button's graphic
                    HBox hbox = (HBox) button.getGraphic();
                    if (hbox != null) {
                        for (Node child : hbox.getChildren()) {
                            if (child instanceof Label) {
                                Label label = (Label) child;
                                String text = label.getText();

                                // Set actions based on button text
                                if ("Historique".equals(text)) {
                                    button.setOnAction(event -> onReservationsButtonClick());
                                } else if ("Profil".equals(text)) {
                                    button.setOnAction(event -> onProfileButtonClick());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Populates the upcoming reservations section with real data if available
     */
    private void populateUpcomingReservations(Parent content) {
        VBox upcomingContainer = (VBox) content.lookup("#upcomingReservationsContainer");
        if (upcomingContainer != null) {
            try {
                // Get user's upcoming reservations
                List<Reservation> upcomingReservations = reservationService.getReservationsByUserIdAndStatus(
                        currentUser.getId(), "PENDING");

                // If we have real reservations, clear the sample ones
                if (!upcomingReservations.isEmpty()) {
                    upcomingContainer.getChildren().clear();

                    // Add real reservations (limit to 3 for space)
                    int count = 0;
                    for (Reservation reservation : upcomingReservations) {
                        upcomingContainer.getChildren().add(createUpcomingReservationItem(reservation));
                        count++;
                        if (count >= 3) break;
                    }
                }

                // Add "See all" button action
                Button seeAllButton = (Button) content.lookup(".dashboard-panel .transparent-button");
                if (seeAllButton != null) {
                    seeAllButton.setOnAction(event -> onReservationsButtonClick());
                }

            } catch (Exception e) {
                log.error("Error loading upcoming reservations: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Creates a styled upcoming reservation item for the dashboard
     */
    private HBox createUpcomingReservationItem(Reservation reservation) {
        HBox item = new HBox();
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.getStyleClass().add("upcoming-reservation-item");
        item.setPadding(new Insets(10));
        item.setSpacing(15);

        // Date container
        VBox dateContainer = new VBox();
        dateContainer.setAlignment(javafx.geometry.Pos.CENTER);
        dateContainer.getStyleClass().add("reservation-date-container");

        // Format date
        LocalDateTime dateTime = reservation.getDateTime();
        String month = dateTime.getMonth().toString().substring(0, 3);
        String day = String.valueOf(dateTime.getDayOfMonth());

        Label monthLabel = new Label(month);
        monthLabel.getStyleClass().add("reservation-date-month");

        Label dayLabel = new Label(day);
        dayLabel.getStyleClass().add("reservation-date-day");

        dateContainer.getChildren().addAll(monthLabel, dayLabel);

        // Details container
        VBox detailsContainer = new VBox();
        HBox.setHgrow(detailsContainer, javafx.scene.layout.Priority.ALWAYS);

        // Route info
        String origin = reservation.getRoute() != null ? reservation.getRoute().getOrigin() : "Départ";
        String destination = reservation.getRoute() != null ? reservation.getRoute().getDestination() : "Arrivée";
        Label routeLabel = new Label(origin + " → " + destination);
        routeLabel.getStyleClass().add("reservation-route");

        // Time and transport info
        HBox infoContainer = new HBox();
        infoContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        infoContainer.setSpacing(15);

        // Time info
        HBox timeInfo = new HBox();
        timeInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        timeInfo.setSpacing(5);

        FontIcon clockIcon = new FontIcon();
        clockIcon.setIconLiteral("fas-clock");
        clockIcon.setIconSize(12);
        clockIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));

        String time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(time);
        timeLabel.getStyleClass().add("reservation-detail");

        timeInfo.getChildren().addAll(clockIcon, timeLabel);

        // Transport info
        HBox transportInfo = new HBox();
        transportInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        transportInfo.setSpacing(5);

        FontIcon transportIcon = new FontIcon();
        String transportType = reservation.getTransport() != null ?
                reservation.getTransport().getType() : "Bus";

        // Set appropriate icon based on transport type
        switch (transportType.toLowerCase()) {
            case "train":
                transportIcon.setIconLiteral("fas-train");
                break;
            case "métro":
            case "metro":
                transportIcon.setIconLiteral("fas-subway");
                break;
            case "tgm":
                transportIcon.setIconLiteral("fas-tram");
                break;
            case "avion":
            case "plane":
                transportIcon.setIconLiteral("fas-plane");
                break;
            default:
                transportIcon.setIconLiteral("fas-bus");
        }

        transportIcon.setIconSize(12);
        transportIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));

        Label transportLabel = new Label(transportType);
        transportLabel.getStyleClass().add("reservation-detail");

        transportInfo.getChildren().addAll(transportIcon, transportLabel);

        // Status label
        Label statusLabel = new Label(reservation.getStatus());
        if ("PENDING".equalsIgnoreCase(reservation.getStatus())) {
            statusLabel.setText("En attente");
            statusLabel.getStyleClass().add("reservation-status-pending");
        } else if ("CONFIRMED".equalsIgnoreCase(reservation.getStatus())) {
            statusLabel.setText("Confirmé");
            statusLabel.getStyleClass().add("reservation-status-confirmed");
        }

        infoContainer.getChildren().addAll(timeInfo, transportInfo, statusLabel);

        detailsContainer.getChildren().addAll(routeLabel, infoContainer);

        // Details button
        Button detailsButton = new Button("Détails");
        detailsButton.getStyleClass().add("detail-button");

        // Add all components to the item
        item.getChildren().addAll(dateContainer, detailsContainer, detailsButton);

        return item;
    }

    private void updateReservationStats(Parent content) {
        // Find the labels in the loaded content
        Label reservationsCount = (Label) content.lookup("#reservationsCountLabel");
        Label completedCount = (Label) content.lookup("#completedReservationsLabel");
        Label pendingCount = (Label) content.lookup("#pendingReservationsLabel");

        // Update the labels with the reservation statistics
        if (reservationsCount != null) reservationsCount.setText(String.valueOf(totalReservations));
        if (completedCount != null) completedCount.setText(String.valueOf(completedReservations));
        if (pendingCount != null) pendingCount.setText(String.valueOf(pendingReservations));
    }

    @FXML
    public void onDashboardButtonClick() {
        // Update UI for dashboard
        dashboardTitle.setText("Tableau de bord");
        setActiveButton(dashboardButton);
        loadHomeContent();
    }

    @FXML
    public void onSearchButtonClick() {
        dashboardTitle.setText("Rechercher");
        setActiveButton(searchButton);
        loadContent("/fxml/user/search.fxml");
    }

    @FXML
    public void onReservationsButtonClick() {
        dashboardTitle.setText("Mes réservations");
        setActiveButton(reservationsButton);
        loadContent("/fxml/user/reservations.fxml");
    }

    @FXML
    public void onProfileButtonClick() {
        dashboardTitle.setText("Mon compte");
        setActiveButton(profileButton);
        loadContent("/fxml/user/profile.fxml");
    }

    @FXML
    public void onCreateReservationButtonClick() {
        // Redirect to search view for reservation creation
        log.info("Redirecting to search view for new reservation");
        dashboardTitle.setText("Rechercher");
        setActiveButton(searchButton);
        loadContent("/fxml/user/search.fxml");
    }

    @FXML
    public void onLogoutButtonClick(ActionEvent event) {
        try {
            // Clear user from context
            UserContext.getInstance().clearCurrentUser();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Remember fullscreen state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();

            // Return to home page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setTitle("TunTransport");
            stage.setScene(scene);

            // Restore fullscreen state
            stage.setMaximized(wasMaximized);
            stage.setFullScreen(wasFullScreen);

            stage.show();

            log.info("User logged out");
        } catch (IOException e) {
            log.error("Error returning to home view", e);
        }
    }

    private void initializeSearchView(Parent searchView) {
        // No need to manually initialize the search view anymore
        // The dedicated SearchViewController now handles everything automatically
        log.info("Search view loaded with dedicated controller");
    }

    private void initializeReservationsView(Parent reservationsView) {
        ComboBox<String> sortComboBox = (ComboBox<String>) reservationsView.lookup("#sortComboBox");
        VBox allReservationsContainer = (VBox) reservationsView.lookup("#allReservationsContainer");
        VBox upcomingReservationsContainer = (VBox) reservationsView.lookup("#upcomingReservationsContainer");
        VBox completedReservationsContainer = (VBox) reservationsView.lookup("#completedReservationsContainer");
        VBox cancelledReservationsContainer = (VBox) reservationsView.lookup("#cancelledReservationsContainer");

        // Check if containers exist
        if (allReservationsContainer == null) {
            log.error("allReservationsContainer not found in the FXML. The FXML might be using a different controller.");
            return; // Exit early to avoid NullPointerException
        }

        // Add sort options
        if (sortComboBox != null) {
            sortComboBox.getItems().addAll(
                "Date (plus récent)", 
                "Date (plus ancien)", 
                "Prix (croissant)", 
                "Prix (décroissant)"
            );
            sortComboBox.setValue("Date (plus récent)");
        }

        // Clear containers
        allReservationsContainer.getChildren().clear();
        if (upcomingReservationsContainer != null) upcomingReservationsContainer.getChildren().clear();
        if (completedReservationsContainer != null) completedReservationsContainer.getChildren().clear();
        if (cancelledReservationsContainer != null) cancelledReservationsContainer.getChildren().clear();

        try {
            // Debug logging
            log.info("Current user ID: {}", currentUser.getId());

            // Get user's reservations from service
            List<Reservation> userReservations = reservationService.getReservationsByUserId(currentUser.getId());
            log.info("Retrieved {} reservations for user", userReservations.size());

            // Debug output for reservations
            for (Reservation res : userReservations) {
                log.info("Reservation ID: {}, Status: {}, Date: {}", 
                        res.getId(), res.getStatus(), res.getDateTime());
            }

            if (userReservations.isEmpty()) {
                log.info("No reservations found for user, showing empty message");

                // Add some dummy data for testing if no reservations found
                log.info("Adding sample reservations for testing");
                allReservationsContainer.getChildren().add(createSampleReservationCard("Tunis", "Sousse", "PENDING"));
                allReservationsContainer.getChildren().add(createSampleReservationCard("Monastir", "Sfax", "COMPLETED"));

                if (upcomingReservationsContainer != null) {
                    upcomingReservationsContainer.getChildren().add(createSampleReservationCard("Tunis", "Sousse", "PENDING"));
                }
                if (completedReservationsContainer != null) {
                    completedReservationsContainer.getChildren().add(createSampleReservationCard("Monastir", "Sfax", "COMPLETED"));
                }

                return;
            }

            // Categorize reservations
            List<Reservation> upcoming = new ArrayList<>();
            List<Reservation> completed = new ArrayList<>();
            List<Reservation> cancelled = new ArrayList<>();

            for (Reservation reservation : userReservations) {
                // Add to all reservations tab
                if (allReservationsContainer != null) {
                    allReservationsContainer.getChildren().add(createReservationCard(reservation));
                }

                // Categorize by status
                String status = reservation.getStatus().toString().toUpperCase();
                if (status.equals("PENDING") || status.equals("CONFIRMED")) {
                    upcoming.add(reservation);
                } else if (status.equals("COMPLETED")) {
                    completed.add(reservation);
                } else if (status.equals("CANCELLED")) {
                    cancelled.add(reservation);
                }
            }

            // Populate each tab
            if (upcomingReservationsContainer != null) {
                if (upcoming.isEmpty()) {
                    upcomingReservationsContainer.getChildren().add(new Label("Aucune réservation à venir"));
                } else {
                    for (Reservation res : upcoming) {
                        upcomingReservationsContainer.getChildren().add(createReservationCard(res));
                    }
                }
            }

            if (completedReservationsContainer != null) {
                if (completed.isEmpty()) {
                    completedReservationsContainer.getChildren().add(new Label("Aucune réservation terminée"));
                } else {
                    for (Reservation res : completed) {
                        completedReservationsContainer.getChildren().add(createReservationCard(res));
                    }
                }
            }

            if (cancelledReservationsContainer != null) {
                if (cancelled.isEmpty()) {
                    cancelledReservationsContainer.getChildren().add(new Label("Aucune réservation annulée"));
                } else {
                    for (Reservation res : cancelled) {
                        cancelledReservationsContainer.getChildren().add(createReservationCard(res));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error loading reservations: {}", e.getMessage(), e);

            // Show error message in tabs
            Label errorLabel = new Label("Erreur lors du chargement des réservations");
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");

            if (allReservationsContainer != null) 
                allReservationsContainer.getChildren().add(errorLabel);
        }
    }

    private VBox createReservationCard(Reservation reservation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(Double.MAX_VALUE);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Get route information
        String departure = "Ville de départ";
        String arrival = "Ville d'arrivée";

        if (reservation.getRoute() != null) {
            departure = reservation.getRoute().getOrigin();
            arrival = reservation.getRoute().getDestination();
        }

        Label routeLabel = new Label(departure + " → " + arrival);
        routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Status with color coding
        String status = reservation.getStatus();
        String statusStyle;
        switch (status.toUpperCase()) {
            case "PENDING":
                statusStyle = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "CONFIRMED":
                statusStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "COMPLETED":
                statusStyle = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "CANCELED":
                statusStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            default:
                statusStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
        }

        Label statusLabel = new Label(status);
        statusLabel.setStyle(statusStyle);
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        HBox.setHgrow(statusLabel, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(routeLabel, statusLabel);

        // Build details
        VBox details = new VBox(5);

        // Date information
        String departureDate = "Date non spécifiée";
        if (reservation.getDateTime() != null) {
            departureDate = reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        Label departureDateLabel = new Label("Départ: " + departureDate);

        // Return date if round trip
        String returnInfo = "";
        if (reservation.isRoundTrip() && reservation.getReturnDateTime() != null) {
            returnInfo = "Retour: " + reservation.getReturnDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        Label returnDateLabel = new Label(returnInfo);

        // Transport information
        String transportInfo = "Transport: Non spécifié";
        if (reservation.getTransport() != null) {
            transportInfo = "Transport: " + reservation.getTransport().getType() + " (" + reservation.getTransport().getName() + ")";
        }

        Label transportLabel = new Label(transportInfo);

        // Payment information
        String paymentInfo = "Prix: " + reservation.getPrice() + " DT";
        if (reservation.isPaid()) {
            paymentInfo += " (Payé)";
        } else {
            paymentInfo += " (Non payé)";
        }

        Label paymentLabel = new Label(paymentInfo);

        details.getChildren().addAll(departureDateLabel);
        if (!returnInfo.isEmpty()) {
            details.getChildren().add(returnDateLabel);
        }
        details.getChildren().addAll(transportLabel, paymentLabel);

        // Action buttons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        // Only show buttons for non-completed reservations
        if (!status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELED")) {
            Button cancelButton = new Button("Annuler");
            cancelButton.getStyleClass().add("delete-button");
            cancelButton.setOnAction(event -> {
                // Handle cancellation logic
                cancelReservation(reservation.getId());
            });

            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("detail-button");

            actionButtons.getChildren().addAll(detailsButton, cancelButton);
        } else {
            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("detail-button");

            actionButtons.getChildren().add(detailsButton);
        }

        card.getChildren().addAll(header, details, actionButtons);
        return card;
    }

    private void cancelReservation(int reservationId) {
        try {
            boolean success = reservationService.cancelReservation(reservationId);
            if (success) {
                // Refresh reservations view
                onReservationsButtonClick();
            } else {
                showError("Erreur lors de l'annulation de la réservation. Veuillez réessayer.");
            }
        } catch (Exception e) {
            log.error("Error cancelling reservation: {}", e.getMessage(), e);
            showError("Erreur lors de l'annulation de la réservation: " + e.getMessage());
        }
    }

    private void initializeProfileView(Parent profileView) {
        // Get references to fields
        TextField lastNameField = (TextField) profileView.lookup("#lastNameField");
        TextField firstNameField = (TextField) profileView.lookup("#firstNameField");
        TextField emailField = (TextField) profileView.lookup("#emailField");
        TextField phoneField = (TextField) profileView.lookup("#phoneField");
        Button updateProfileButton = (Button) profileView.lookup("#updateProfileButton");
        Button changePasswordButton = (Button) profileView.lookup("#changePasswordButton");

        // Fill fields with user data
        if (lastNameField != null && currentUser.getFullName() != null) {
            String fullName = currentUser.getFullName();
            String[] nameParts = fullName.split(" ", 2);

            if (nameParts.length > 1) {
                lastNameField.setText(nameParts[1]);
                firstNameField.setText(nameParts[0]);
            } else {
                lastNameField.setText(fullName);
                firstNameField.setText("");
            }
        }

        if (emailField != null && currentUser.getEmail() != null) {
            emailField.setText(currentUser.getEmail());
        }

        if (phoneField != null && currentUser.getPhoneNumber() != null) {
            phoneField.setText(currentUser.getPhoneNumber());
        }

        // Set up update profile button
        if (updateProfileButton != null) {
            updateProfileButton.setOnAction(event -> updateUserProfile(profileView));
        }

        // Set up change password button
        if (changePasswordButton != null) {
            changePasswordButton.setOnAction(event -> changeUserPassword(profileView));
        }
    }

    private void updateUserProfile(Parent profileView) {
        TextField lastNameField = (TextField) profileView.lookup("#lastNameField");
        TextField firstNameField = (TextField) profileView.lookup("#firstNameField");
        TextField emailField = (TextField) profileView.lookup("#emailField");
        TextField phoneField = (TextField) profileView.lookup("#phoneField");

        // Update user object
        if (firstNameField != null && lastNameField != null) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String fullName = firstName;

            if (!lastName.isEmpty()) {
                fullName += " " + lastName;
            }

            currentUser.setFullName(fullName);
        }

        if (emailField != null && !emailField.getText().isBlank()) {
            currentUser.setEmail(emailField.getText());
        }

        if (phoneField != null && !phoneField.getText().isBlank()) {
            currentUser.setPhoneNumber(phoneField.getText());
        }

        // In a real app, you would call a service to update the user in the database
        // For now, just show a success message using an alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profil mis à jour");
        alert.setHeaderText(null);
        alert.setContentText("Votre profil a été mis à jour avec succès.");
        alert.showAndWait();
    }

    private void changeUserPassword(Parent profileView) {
        PasswordField currentPasswordField = (PasswordField) profileView.lookup("#currentPasswordField");
        PasswordField newPasswordField = (PasswordField) profileView.lookup("#newPasswordField");
        PasswordField confirmPasswordField = (PasswordField) profileView.lookup("#confirmPasswordField");

        // Validate inputs
        if (currentPasswordField.getText().isBlank()) {
            showError("Veuillez entrer votre mot de passe actuel.");
            return;
        }

        if (newPasswordField.getText().isBlank()) {
            showError("Veuillez entrer un nouveau mot de passe.");
            return;
        }

        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // In a real app, validate current password against database
        // For now, show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe changé");
        alert.setHeaderText(null);
        alert.setContentText("Votre mot de passe a été changé avec succès.");
        alert.showAndWait();

        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to create sample reservation cards for testing
    private VBox createSampleReservationCard(String departure, String arrival, String status) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(Double.MAX_VALUE);

        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label routeLabel = new Label(departure + " → " + arrival);
        routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        String statusStyle;
        switch (status.toUpperCase()) {
            case "PENDING":
                statusStyle = "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "CONFIRMED":
                statusStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "COMPLETED":
                statusStyle = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            case "CANCELED":
                statusStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
            default:
                statusStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
                break;
        }

        Label statusLabel = new Label(status);
        statusLabel.setStyle(statusStyle);
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        HBox.setHgrow(statusLabel, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(routeLabel, statusLabel);

        VBox details = new VBox(5);

        LocalDateTime sampleDate = LocalDateTime.now().plusDays(status.equals("COMPLETED") ? -5 : 3);
        Label departureDateLabel = new Label("Départ: " + sampleDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Label transportLabel = new Label("Transport: Bus (Standard)");
        Label paymentLabel = new Label("Prix: 35.0 DT (Non payé)");

        details.getChildren().addAll(departureDateLabel, transportLabel, paymentLabel);

        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        if (!status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELED")) {
            Button cancelButton = new Button("Annuler");
            cancelButton.getStyleClass().add("delete-button");

            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("detail-button");

            actionButtons.getChildren().addAll(detailsButton, cancelButton);
        } else {
            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().add("detail-button");

            actionButtons.getChildren().add(detailsButton);
        }

        card.getChildren().addAll(header, details, actionButtons);
        return card;
    }

    @FXML
    public void onReclamationsButtonClick() {
        dashboardTitle.setText("Mes réclamations");
        setActiveButton(reclamationsButton);
        loadContent("/fxml/user/reclamations.fxml");
    }
}
