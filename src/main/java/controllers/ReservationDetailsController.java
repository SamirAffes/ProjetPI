package controllers;

import entities.Reservation;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ReservationService;
import services.ReservationServiceImpl;
import utils.AlertUtils;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationDetailsController implements Initializable {
    
    private static final Logger logger = Logger.getLogger(ReservationDetailsController.class.getName());
    
    @FXML private Label idLabel;
    @FXML private Label routeLabel;
    @FXML private Label transportLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private Label priceLabel;
    @FXML private Label isPaidLabel;
    @FXML private CheckBox isPaidCheckBox;
    @FXML private Label roundTripLabel;
    @FXML private Label returnDateTimeLabel;
    @FXML private Label createdAtLabel;
    
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button closeButton;
    
    private Reservation reservation;
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final List<String> statusOptions = Arrays.asList("PENDING", "CONFIRMED", "CANCELLED", "COMPLETED");
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 DT");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize status options
        statusComboBox.setItems(FXCollections.observableArrayList(statusOptions));
    }
    
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        displayReservationDetails();
    }
    
    @FXML
    private void close(ActionEvent event) {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleEdit(ActionEvent event) {
        // Switch to edit mode
        statusLabel.setVisible(false);
        statusComboBox.setVisible(true);
        statusComboBox.setValue(reservation.getStatus());
        
        isPaidLabel.setVisible(false);
        isPaidCheckBox.setVisible(true);
        isPaidCheckBox.setSelected(reservation.getIsPaid());
        
        editButton.setVisible(false);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // Update the reservation with new values
            reservation.setStatus(statusComboBox.getValue());
            reservation.setIsPaid(isPaidCheckBox.isSelected());
            
            // Save to database
            reservationService.modifier(reservation);
            
            // Switch back to view mode
            switchToViewMode();
            
            // Refresh display
            displayReservationDetails();
            
            // Show success message
            AlertUtils.showInformation("Succès", "Mise à jour réussie", "La réservation a été mise à jour avec succès.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating reservation", e);
            AlertUtils.showError("Erreur", "Échec de la mise à jour", "Impossible de mettre à jour la réservation: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        // Switch back to view mode without saving changes
        switchToViewMode();
    }
    
    private void switchToViewMode() {
        statusLabel.setVisible(true);
        statusComboBox.setVisible(false);
        
        isPaidLabel.setVisible(true);
        isPaidCheckBox.setVisible(false);
        
        editButton.setVisible(true);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
    }
    
    private void displayReservationDetails() {
        if (reservation == null) return;
        
        idLabel.setText(String.valueOf(reservation.getId()));
        
        if (reservation.getRoute() != null) {
            routeLabel.setText(reservation.getRoute().getOrigin() + " → " + reservation.getRoute().getDestination());
        } else {
            routeLabel.setText("Route non disponible");
        }
        
        if (reservation.getTransport() != null) {
            transportLabel.setText(reservation.getTransport().getName() + " (" + reservation.getTransport().getType() + ")");
        } else {
            transportLabel.setText("Transport non disponible");
        }
        
        dateTimeLabel.setText(reservation.getDateTime() != null ? 
                reservation.dateTimeProperty().get() : "Non programmé");
        
        statusLabel.setText(translateStatus(reservation.getStatus()));
        priceLabel.setText(decimalFormat.format(reservation.getPrice()));
        isPaidLabel.setText(reservation.getIsPaid() ? "Oui" : "Non");
        roundTripLabel.setText(reservation.isRoundTrip() ? "Oui" : "Non");
        
        if (reservation.isRoundTrip() && reservation.getReturnDateTime() != null) {
            returnDateTimeLabel.setText(reservation.getReturnDateTime().toString());
        } else {
            returnDateTimeLabel.setText("N/A");
        }
        
        createdAtLabel.setText(reservation.getCreatedAt() != null ? 
                reservation.getCreatedAt().toString() : "Inconnu");
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
} 