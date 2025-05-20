package controllers;

import entities.Reservation;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.ReservationService;
import services.ReservationServiceImpl;
import utils.UserContext;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dedicated controller for the reservations view
 */
@Slf4j
public class ReservationsViewController implements Initializable {

    @FXML
    private ComboBox<String> sortComboBox;
    
    @FXML
    private VBox allReservationsContainer;
    
    @FXML
    private VBox upcomingReservationsContainer;
    
    @FXML
    private VBox completedReservationsContainer;
    
    @FXML
    private VBox cancelledReservationsContainer;
    
    @FXML
    private Label noAllReservationsLabel;
    
    @FXML
    private Label noUpcomingReservationsLabel;
    
    @FXML
    private Label noCompletedReservationsLabel;
    
    @FXML
    private Label noCancelledReservationsLabel;
    
    @FXML
    private Button createReservationButton;
    
    private User currentUser;
    private final ReservationService reservationService = new ReservationServiceImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("*** RESERVATIONS VIEW CONTROLLER INITIALIZING ***");
        
        // Get current user from context
        currentUser = UserContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            log.error("No user found in ReservationsViewController");
            return;
        }
        
        log.info("Initializing reservations view for user: {}", currentUser.getUsername());
        
        // Add sort options dropdown
        if (sortComboBox != null) {
            sortComboBox.getItems().clear();
            sortComboBox.getItems().addAll(
                "Date (plus récent)", 
                "Date (plus ancien)", 
                "Prix (croissant)", 
                "Prix (décroissant)"
            );
            sortComboBox.setValue("Date (plus récent)");
            
            // Add sort functionality
            sortComboBox.setOnAction(event -> loadReservations());
        }
        
        // Setup create reservation button
        if (createReservationButton != null) {
            createReservationButton.setOnAction(event -> createNewReservation());
        }
        
        // Load reservations
        loadReservations();
    }
    
    private void loadReservations() {
        try {
            log.info("Loading reservations for user ID: {}", currentUser.getId());
            
            // Get all reservations for the user
            List<Reservation> allReservations = reservationService.getReservationsByUserId(currentUser.getId());
            log.info("Found {} reservations", allReservations.size());
            
            // Clear containers
            clearContainers();
            
            if (allReservations.isEmpty()) {
                log.info("No reservations found");
                return;
            }
            
            // Hide "no reservations" labels
            hideNoReservationsLabels();
            
            // Apply sorting if selected
            if (sortComboBox != null && sortComboBox.getValue() != null) {
                String sortOption = sortComboBox.getValue();
                sortReservations(allReservations, sortOption);
                log.info("Applied sorting: {}", sortOption);
            }
            
            // For each reservation, create a card and add it to the appropriate container
            for (Reservation reservation : allReservations) {
                log.info("Processing reservation: {} ({})", reservation.getId(), reservation.getStatus());
                
                // Create card
                VBox card = createReservationCard(reservation);
                
                // Add to all reservations container
                if (allReservationsContainer != null) {
                    allReservationsContainer.getChildren().add(card);
                }
                
                // Add to specific tab based on status
                if (reservation.getStatus().equalsIgnoreCase("PENDING") || 
                    reservation.getStatus().equalsIgnoreCase("CONFIRMED")) {
                    if (upcomingReservationsContainer != null) {
                        upcomingReservationsContainer.getChildren().add(createReservationCard(reservation));
                    }
                } else if (reservation.getStatus().equalsIgnoreCase("COMPLETED")) {
                    if (completedReservationsContainer != null) {
                        completedReservationsContainer.getChildren().add(createReservationCard(reservation));
                    }
                } else if (reservation.getStatus().equalsIgnoreCase("CANCELLED") || 
                           reservation.getStatus().equalsIgnoreCase("CANCELED")) {
                    if (cancelledReservationsContainer != null) {
                        cancelledReservationsContainer.getChildren().add(createReservationCard(reservation));
                    }
                }
            }
            
            // Check if any tab is empty, show the "no reservations" label if needed
            if (upcomingReservationsContainer != null && upcomingReservationsContainer.getChildren().isEmpty()) {
                upcomingReservationsContainer.getChildren().add(noUpcomingReservationsLabel);
            }
            
            if (completedReservationsContainer != null && completedReservationsContainer.getChildren().isEmpty()) {
                completedReservationsContainer.getChildren().add(noCompletedReservationsLabel);
            }
            
            if (cancelledReservationsContainer != null && cancelledReservationsContainer.getChildren().isEmpty()) {
                cancelledReservationsContainer.getChildren().add(noCancelledReservationsLabel);
            }
            
        } catch (Exception e) {
            log.error("Error loading reservations: {}", e.getMessage(), e);
            clearContainers();
            showNoReservationsLabels();
            
            // Fallback to emergency display
            createEmergencyDisplay();
        }
    }
    
    /**
     * Sort the reservation list based on user selection
     */
    private void sortReservations(List<Reservation> reservations, String sortOption) {
        if (reservations == null || reservations.isEmpty() || sortOption == null) {
            return;
        }
        
        switch (sortOption) {
            case "Date (plus récent)":
                reservations.sort((r1, r2) -> {
                    if (r1.getDateTime() == null || r2.getDateTime() == null) {
                        return 0;
                    }
                    return r2.getDateTime().compareTo(r1.getDateTime());
                });
                break;
                
            case "Date (plus ancien)":
                reservations.sort((r1, r2) -> {
                    if (r1.getDateTime() == null || r2.getDateTime() == null) {
                        return 0;
                    }
                    return r1.getDateTime().compareTo(r2.getDateTime());
                });
                break;
                
            case "Prix (croissant)":
                reservations.sort((r1, r2) -> Double.compare(r1.getPrice(), r2.getPrice()));
                break;
                
            case "Prix (décroissant)":
                reservations.sort((r1, r2) -> Double.compare(r2.getPrice(), r1.getPrice()));
                break;
                
            default:
                log.warn("Unknown sort option: {}", sortOption);
                break;
        }
    }
    
    private void clearContainers() {
        if (allReservationsContainer != null) allReservationsContainer.getChildren().clear();
        if (upcomingReservationsContainer != null) upcomingReservationsContainer.getChildren().clear();
        if (completedReservationsContainer != null) completedReservationsContainer.getChildren().clear();
        if (cancelledReservationsContainer != null) cancelledReservationsContainer.getChildren().clear();
    }
    
    private void hideNoReservationsLabels() {
        if (noAllReservationsLabel != null) noAllReservationsLabel.setVisible(false);
        if (noUpcomingReservationsLabel != null) noUpcomingReservationsLabel.setVisible(false);
        if (noCompletedReservationsLabel != null) noCompletedReservationsLabel.setVisible(false);
        if (noCancelledReservationsLabel != null) noCancelledReservationsLabel.setVisible(false);
    }
    
    private void showNoReservationsLabels() {
        if (noAllReservationsLabel != null) {
            noAllReservationsLabel.setVisible(true);
            if (allReservationsContainer != null) {
                allReservationsContainer.getChildren().add(noAllReservationsLabel);
            }
        }
        
        if (noUpcomingReservationsLabel != null) {
            noUpcomingReservationsLabel.setVisible(true);
            if (upcomingReservationsContainer != null) {
                upcomingReservationsContainer.getChildren().add(noUpcomingReservationsLabel);
            }
        }
        
        if (noCompletedReservationsLabel != null) {
            noCompletedReservationsLabel.setVisible(true);
            if (completedReservationsContainer != null) {
                completedReservationsContainer.getChildren().add(noCompletedReservationsLabel);
            }
        }
        
        if (noCancelledReservationsLabel != null) {
            noCancelledReservationsLabel.setVisible(true);
            if (cancelledReservationsContainer != null) {
                cancelledReservationsContainer.getChildren().add(noCancelledReservationsLabel);
            }
        }
    }
    
    private VBox createReservationCard(Reservation reservation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reservation-card");
        card.setPrefWidth(Double.MAX_VALUE);
        
        // Route header with status badge
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        String routeName = "Route #" + reservation.getRouteId();
        if (reservation.getRoute() != null) {
            routeName = reservation.getRoute().getOrigin() + " → " + reservation.getRoute().getDestination();
        }
        
        Label routeLabel = new Label(routeName);
        routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        
        String statusStyle = switch (reservation.getStatus().toUpperCase()) {
            case "PENDING" -> "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "CONFIRMED" -> "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "COMPLETED" -> "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "CANCELED", "CANCELLED" -> "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            default -> "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
        };
        
        String displayStatus = switch (reservation.getStatus().toUpperCase()) {
            case "PENDING" -> "En attente";
            case "CONFIRMED" -> "Confirmée";
            case "COMPLETED" -> "Terminée";
            case "CANCELED", "CANCELLED" -> "Annulée";
            default -> reservation.getStatus();
        };
        
        Label statusLabel = new Label(displayStatus);
        statusLabel.setStyle(statusStyle);
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        HBox.setHgrow(statusLabel, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(routeLabel, statusLabel);
        
        // Content with departure/return dates, transport info, and price
        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 10 0;");
        
        // Create fields for departure date, return date, transport, and price
        LocalDateTime departureTime = reservation.getDateTime();
        String departureDate = "N/A";
        if (departureTime != null) {
            departureDate = departureTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        
        Label departureDateLabel = new Label("Départ: " + departureDate);
        departureDateLabel.setStyle("-fx-font-size: 14px;");
        
        if (reservation.isRoundTrip() && reservation.getReturnDateTime() != null) {
            String returnDate = reservation.getReturnDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Label returnDateLabel = new Label("Retour: " + returnDate);
            returnDateLabel.setStyle("-fx-font-size: 14px;");
            content.getChildren().add(returnDateLabel);
        }
        
        String transportName = "Transport #" + reservation.getTransportId();
        if (reservation.getTransport() != null) {
            transportName = reservation.getTransport().getType() + " (" + reservation.getTransport().getName() + ")";
        }
        
        Label transportLabel = new Label("Transport: " + transportName);
        transportLabel.setStyle("-fx-font-size: 14px;");
        
        String paymentStatus = reservation.isPaid() ? "Payé" : "Non payé";
        Label priceLabel = new Label("Prix: " + reservation.getPrice() + " DT (" + paymentStatus + ")");
        priceLabel.setStyle("-fx-font-size: 14px;");
        
        Label roundTripLabel = null;
        if (reservation.isRoundTrip()) {
            roundTripLabel = new Label("Aller-retour: Oui");
            roundTripLabel.setStyle("-fx-font-size: 14px;");
        }
        
        // Add all information fields to content
        content.getChildren().add(departureDateLabel);
        content.getChildren().add(transportLabel);
        content.getChildren().add(priceLabel);
        if (roundTripLabel != null) {
            content.getChildren().add(roundTripLabel);
        }
        
        // Add buttons in a horizontal box
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBar.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));
        
        // View details button
        Button viewButton = new Button("Voir détails");
        viewButton.getStyleClass().add("detail-button");
        viewButton.setOnAction(event -> showReservationDetails(reservation));
        
        // Edit button
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(event -> editReservation(reservation));
        
        // Cancel/Delete button
        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("delete-button");
        cancelButton.setOnAction(event -> cancelReservation(reservation));
        
        // Add buttons based on status
        if (!reservation.getStatus().equalsIgnoreCase("COMPLETED") && 
            !reservation.getStatus().equalsIgnoreCase("CANCELED") && 
            !reservation.getStatus().equalsIgnoreCase("CANCELLED")) {
            buttonBar.getChildren().addAll(viewButton, editButton, cancelButton);
        } else {
            buttonBar.getChildren().add(viewButton);
        }
        
        // Add all components to the card
        card.getChildren().addAll(header, content, buttonBar);
        return card;
    }
    
    private void cancelReservation(Reservation reservation) {
        log.info("Cancelling reservation: {}", reservation.getId());
        
        // Update reservation status
        try {
            reservation.setStatus("CANCELLED");
            reservationService.update(reservation);
            
            // Reload reservations to update the UI
            loadReservations();
            
            // Show success message
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Réservation Annulée");
            alert.setHeaderText(null);
            alert.setContentText("Votre réservation a été annulée avec succès.");
            alert.showAndWait();
            
        } catch (Exception e) {
            log.error("Error cancelling reservation: {}", e.getMessage(), e);
            
            // Show error message
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'annulation de la réservation.");
            alert.showAndWait();
        }
    }
    
    private void showReservationDetails(Reservation reservation) {
        log.info("Showing details for reservation: {}", reservation.getId());
        
        // Create alert with details
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Réservation");
        alert.setHeaderText("Réservation #" + reservation.getId());
        
        String content = "Itinéraire: ";
        if (reservation.getRoute() != null) {
            content += reservation.getRoute().getOrigin() + " → " + reservation.getRoute().getDestination();
        } else {
            content += "Route #" + reservation.getRouteId();
        }
        
        content += "\nDate: ";
        if (reservation.getDateTime() != null) {
            content += reservation.getDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } else {
            content += "N/A";
        }
        
        content += "\nTransport: ";
        if (reservation.getTransport() != null) {
            content += reservation.getTransport().getType() + " (" + reservation.getTransport().getName() + ")";
        } else {
            content += "Transport #" + reservation.getTransportId();
        }
        
        content += "\nPrix: " + reservation.getPrice() + " DT";
        content += "\nPayé: " + (reservation.isPaid() ? "Oui" : "Non");
        content += "\nStatus: " + reservation.getStatus();
        
        if (reservation.isRoundTrip()) {
            content += "\nAller-retour: Oui";
            if (reservation.getReturnDateTime() != null) {
                content += "\nDate de retour: " + reservation.getReturnDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
        } else {
            content += "\nAller-retour: Non";
        }
        
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void editReservation(Reservation reservation) {
        log.info("Editing reservation: {}", reservation.getId());
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/editReservation.fxml"));
            Parent root = loader.load();
            
            EditReservationController controller = loader.getController();
            controller.setReservation(reservation);
            
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("TunTransport - Modifier Réservation");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            
            // Show the dialog and wait for it to close
            stage.showAndWait();
            
            // Reload reservations to update the UI
            loadReservations();
            
        } catch (Exception e) {
            log.error("Error opening reservation editing form: {}", e.getMessage(), e);
            
            // Show error message
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de l'ouverture du formulaire de modification.");
            alert.showAndWait();
        }
    }
    
    private void createEmergencyDisplay() {
        log.info("*** CREATING EMERGENCY DISPLAY ***");
        
        // Clear containers
        clearContainers();
        
        // Add emergency label to all containers
        Label emergencyLabel = new Label("AFFICHAGE D'URGENCE");
        emergencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #ffeeee; -fx-padding: 5;");
        
        // Create generic emergency cards for each tab
        VBox pendingCard = createEmergencyCard("Départ", "Destination", "PENDING", 3);
        VBox completedCard = createEmergencyCard("Départ", "Destination", "COMPLETED", -5);
        VBox cancelledCard = createEmergencyCard("Départ", "Destination", "CANCELLED", -10);
        
        // Add to All tab
        if (allReservationsContainer != null) {
            Label allEmergencyLabel = new Label("AFFICHAGE D'URGENCE");
            allEmergencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #ffeeee; -fx-padding: 5;");
            
            allReservationsContainer.getChildren().add(allEmergencyLabel);
            allReservationsContainer.getChildren().addAll(
                createEmergencyCard("Départ", "Destination", "PENDING", 3),
                createEmergencyCard("Départ", "Destination", "COMPLETED", -5),
                createEmergencyCard("Départ", "Destination", "CANCELLED", -10)
            );
        }
        
        // Add to specific tabs
        if (upcomingReservationsContainer != null) {
            Label upcomingEmergencyLabel = new Label("AFFICHAGE D'URGENCE");
            upcomingEmergencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #ffeeee; -fx-padding: 5;");
            
            upcomingReservationsContainer.getChildren().add(upcomingEmergencyLabel);
            upcomingReservationsContainer.getChildren().add(pendingCard);
        }
        
        if (completedReservationsContainer != null) {
            Label completedEmergencyLabel = new Label("AFFICHAGE D'URGENCE");
            completedEmergencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #ffeeee; -fx-padding: 5;");
            
            completedReservationsContainer.getChildren().add(completedEmergencyLabel);
            completedReservationsContainer.getChildren().add(completedCard);
        }
        
        if (cancelledReservationsContainer != null) {
            Label cancelledEmergencyLabel = new Label("AFFICHAGE D'URGENCE");
            cancelledEmergencyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-background-color: #ffeeee; -fx-padding: 5;");
            
            cancelledReservationsContainer.getChildren().add(cancelledEmergencyLabel);
            cancelledReservationsContainer.getChildren().add(cancelledCard);
        }
    }
    
    private VBox createEmergencyCard(String origin, String destination, String status, int dayOffset) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15;");
        
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label routeLabel = new Label(origin + " → " + destination);
        routeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        String statusStyle = switch (status.toUpperCase()) {
            case "PENDING" -> "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "CONFIRMED" -> "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "COMPLETED" -> "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            case "CANCELED", "CANCELLED" -> "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
            default -> "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;";
        };
        
        String displayStatus = switch (status.toUpperCase()) {
            case "PENDING" -> "En attente";
            case "CONFIRMED" -> "Confirmée";
            case "COMPLETED" -> "Terminée";
            case "CANCELED", "CANCELLED" -> "Annulée";
            default -> status;
        };
        
        Label statusLabel = new Label(displayStatus);
        statusLabel.setStyle(statusStyle);
        statusLabel.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        javafx.scene.layout.HBox.setHgrow(statusLabel, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(routeLabel, statusLabel);
        
        // Build details
        VBox details = new VBox(5);
        
        LocalDateTime sampleDate = LocalDateTime.now().plusDays(dayOffset);
        Label departureDateLabel = new Label("Départ: " + sampleDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        Label transportLabel = new Label("Transport: Bus (Standard)");
        Label paymentLabel = new Label("Prix: 35.0 DT (Non payé)");
        
        details.getChildren().addAll(departureDateLabel, transportLabel, paymentLabel);
        
        // Action buttons
        javafx.scene.layout.HBox actionButtons = new javafx.scene.layout.HBox(10);
        actionButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        if (!status.equalsIgnoreCase("COMPLETED") && !status.equalsIgnoreCase("CANCELED") && !status.equalsIgnoreCase("CANCELLED")) {
            javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Annuler");
            cancelButton.getStyleClass().add("delete-button");
            
            javafx.scene.control.Button detailsButton = new javafx.scene.control.Button("Détails");
            detailsButton.getStyleClass().add("detail-button");
            
            actionButtons.getChildren().addAll(detailsButton, cancelButton);
        } else {
            javafx.scene.control.Button detailsButton = new javafx.scene.control.Button("Détails");
            detailsButton.getStyleClass().add("detail-button");
            
            actionButtons.getChildren().add(detailsButton);
        }
        
        card.getChildren().addAll(header, details, actionButtons);
        return card;
    }
    
    private void createNewReservation() {
        log.info("Redirecting to search view for new reservation");
        try {
            // Get the stage
            Stage stage = (Stage) createReservationButton.getScene().getWindow();
            
            // Remember fullscreen state
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            
            // Get a reference to the UserDashboardController
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/dashboard.fxml"));
            Parent root = loader.load();
            UserDashboardController dashController = loader.getController();
            
            // Set the scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            // Restore fullscreen state
            stage.setMaximized(wasMaximized);
            stage.setFullScreen(wasFullScreen);
            
            // Trigger the search button click to navigate to search view
            dashController.onSearchButtonClick();
            
            log.info("Successfully redirected to search view");
            
        } catch (Exception e) {
            log.error("Error redirecting to search view: {}", e.getMessage(), e);
            
            // Show error message
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la redirection vers la recherche.");
            alert.showAndWait();
        }
    }
}