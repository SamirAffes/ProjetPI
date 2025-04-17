package controllers;

import entities.Reservation;
import entities.Route;
import entities.Transport;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ReservationService;
import services.ReservationServiceImpl;
import services.RouteService;
import services.RouteServiceImpl;
import services.TransportService;
import services.TransportServiceImpl;
import test.Main;
import utils.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(ReservationController.class.getName());

    @FXML
    private TableView<Reservation> reservationsTable;
    @FXML
    private TableColumn<Reservation, Integer> idColumn;
    @FXML
    private TableColumn<Reservation, String> routeColumn;
    @FXML
    private TableColumn<Reservation, String> transportColumn;
    @FXML
    private TableColumn<Reservation, String> dateTimeColumn;
    @FXML
    private TableColumn<Reservation, String> statusColumn;
    @FXML
    private TableColumn<Reservation, Double> priceColumn;
    @FXML
    private TableColumn<Reservation, Void> actionsColumn;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private Label totalReservationsLabel;
    @FXML
    private Label averageCostLabel;
    @FXML
    private Label completionRateLabel;
    @FXML
    private Button addNewReservationButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button backButton;

    private ReservationService reservationService;
    private RouteService routeService;
    private TransportService transportService;
    private ObservableList<Reservation> reservationsList = FXCollections.observableArrayList();
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00 DT");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // First thing - ensure the window is maximized
        ensureWindowMaximized();
        
        // Initialize the services
        reservationService = new ReservationServiceImpl();
        routeService = new RouteServiceImpl();
        transportService = new TransportServiceImpl();
        
        setupTable();
        setupStatusFilter();
        setupDashboardMetrics();
        loadReservations();
        
        addNewReservationButton.setOnAction(this::handleAddReservation);
        
        // Setup back button if it exists in the FXML
        if (backButton != null) {
            backButton.setOnAction(event -> Main.navigateToHome());
        }
    }
    
    /**
     * Ensure the window is maximized using the primary stage
     */
    private void ensureWindowMaximized() {
        Platform.runLater(() -> {
            try {
                // Get the primary stage from Main class
                Stage stage = Main.getPrimaryStage();
                
                if (stage != null) {
                    LOGGER.info("Using primary stage from Main class to ensure maximized state");
                    
                    // Force the stage to be maximized
                    stage.setMaximized(true);
                    
                    // Log the current window state
                    LOGGER.info("Window size after maximizing: " + stage.getWidth() + "x" + 
                               stage.getHeight() + ", isMaximized: " + stage.isMaximized());
                    
                    // Add listener to maintain maximized state
                    stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            LOGGER.info("Window was un-maximized, restoring maximized state");
                            Platform.runLater(() -> stage.setMaximized(true));
                        }
                    });
                    
                    // Request focus to ensure window is active
                    stage.requestFocus();
                } else {
                    // Fallback using the scene's window if available
                    LOGGER.warning("Primary stage reference is null");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error ensuring window maximization", e);
            }
        });
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Custom cell factory for route to display route information properly
        routeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRoute() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getRoute().getOrigin() + " → " + 
                    cellData.getValue().getRoute().getDestination()
                );
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Custom cell factory for transport to display transport information properly
        transportColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTransport() != null) {
                return new SimpleStringProperty(
                    cellData.getValue().getTransport().getName() + " (" + 
                    cellData.getValue().getTransport().getType() + ")"
                );
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Custom cell factory for datetime to display formatted date
        dateTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateTime() != null) {
                LocalDateTime dateTime = cellData.getValue().getDateTime();
                return new SimpleStringProperty(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime)
                );
            }
            return new SimpleStringProperty("N/A");
        });
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Format price with Tunisian Dinar
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(decimalFormat.format(price));
                }
            }
        });
        
        // Format status with colored chips
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(translateStatus(status));
                    statusLabel.getStyleClass().add("status-chip");
                    
                    switch (status.toLowerCase()) {
                        case "pending":
                            statusLabel.getStyleClass().add("status-pending");
                            break;
                        case "confirmed":
                            statusLabel.getStyleClass().add("status-confirmed");
                            break;
                        case "completed":
                            statusLabel.getStyleClass().add("status-completed");
                            break;
                        case "cancelled":
                            statusLabel.getStyleClass().add("status-cancelled");
                            break;
                        default:
                            break;
                    }
                    
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
        
        // Add actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("Voir");
            private final Button editButton = new Button("Modifier");
            private final Button cancelButton = new Button("Annuler");
            
            {
                viewButton.getStyleClass().addAll("table-button", "view-button");
                editButton.getStyleClass().addAll("table-button", "edit-button");
                cancelButton.getStyleClass().addAll("table-button", "cancel-button");
                
                viewButton.setPrefWidth(95);
                editButton.setPrefWidth(95);
                cancelButton.setPrefWidth(95);
                
                viewButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    showReservationDetails(reservation);
                });
                
                editButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    editReservation(reservation);
                });
                
                cancelButton.setOnAction(event -> {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    cancelReservation(reservation);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(15); // Increased spacing between buttons
                    buttons.setAlignment(Pos.CENTER);
                    buttons.getStyleClass().add("actions-cell");
                    buttons.setPrefWidth(300); // Set preferred width
                    buttons.setMinWidth(280);  // Set minimum width
                    
                    buttons.getChildren().add(viewButton);
                    
                    if (!"cancelled".equalsIgnoreCase(reservation.getStatus()) && 
                        !"completed".equalsIgnoreCase(reservation.getStatus())) {
                        buttons.getChildren().add(editButton);
                        buttons.getChildren().add(cancelButton);
                    }
                    
                    setGraphic(buttons);
                }
            }
        });
        
        reservationsTable.setItems(reservationsList);
    }
    
    private void setupStatusFilter() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                "Tous", "En attente", "Confirmé", "Terminé", "Annulé"
        );
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue("Tous");
        
        statusFilterComboBox.setOnAction(event -> {
            String selectedStatus = statusFilterComboBox.getValue();
            filterReservationsByStatus(selectedStatus);
        });
    }
    
    private void setupDashboardMetrics() {
        updateMetrics();
    }
    
    private void updateMetrics() {
        int totalReservations = reservationService.getTotalReservations();
        double averagePrice = reservationService.getAverageReservationPrice();
        double completionRate = reservationService.getCompletionRate();
        
        totalReservationsLabel.setText(String.valueOf(totalReservations));
        averageCostLabel.setText(decimalFormat.format(averagePrice));
        completionRateLabel.setText(String.format("%.1f%%", completionRate));
    }
    
    private void loadReservations() {
        try {
            // Clear the current list
            reservationsList.clear();
            
            // Get all reservations
            List<Reservation> reservations = reservationService.getAllReservations();
            
            // Load the routes and transports for each reservation
            for (Reservation reservation : reservations) {
                loadRouteAndTransport(reservation);
            }
            
            // Add all reservations to the observable list
            reservationsList.addAll(reservations);
            
            // Update metrics
            updateMetrics();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading reservations", e);
            AlertUtils.showError("Erreur", "Chargement échoué", 
                    "Impossible de charger les réservations: " + e.getMessage());
        }
    }
    
    private void loadRouteAndTransport(Reservation reservation) {
        try {
            // Load Route
            if (reservation.getRouteId() > 0) {
                Route route = routeService.afficher(reservation.getRouteId());
                if (route != null) {
                    reservation.setRoute(route);
                }
            }
            
            // Load Transport
            if (reservation.getTransportId() > 0) {
                Transport transport = transportService.afficher(reservation.getTransportId());
                if (transport != null) {
                    reservation.setTransport(transport);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading route or transport for reservation: " + reservation.getId(), e);
        }
    }
    
    private void filterReservationsByStatus(String statusName) {
        try {
            // Clear the current list
            reservationsList.clear();
            
            List<Reservation> filteredReservations;
            if ("Tous".equals(statusName)) {
                filteredReservations = reservationService.getAllReservations();
            } else {
                String statusEnglish = translateStatusToEnglish(statusName);
                filteredReservations = reservationService.getAllReservationsByStatus(statusEnglish);
            }
            
            // Load route and transport data for each reservation
            for (Reservation reservation : filteredReservations) {
                loadRouteAndTransport(reservation);
            }
            
            // Add the filtered reservations to the observable list
            reservationsList.addAll(filteredReservations);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error filtering reservations by status", e);
            AlertUtils.showError("Erreur", "Filtrage échoué", 
                    "Impossible de filtrer les réservations: " + e.getMessage());
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
    
    private void showReservationDetails(Reservation reservation) {
        AlertUtils.showInformation("Détails de la réservation", 
                "Réservation #" + reservation.getId(), 
                "Trajet: " + reservation.getRoute() + "\n" +
                "Transport: " + reservation.getTransport() + "\n" +
                "Date: " + reservation.getDateTime() + "\n" +
                "Statut: " + translateStatus(reservation.getStatus()) + "\n" +
                "Prix: " + decimalFormat.format(reservation.getPrice()));
    }
    
    private void editReservation(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditReservationView.fxml"));
            Parent root = loader.load();
            
            // Pass the reservation to the controller
            EditReservationController controller = loader.getController();
            controller.setReservation(reservation);
            controller.setReservationService(reservationService);
            
            // Add an event handler to reload reservations after the edit is done
            controller.setOnReservationUpdated(this::loadReservations);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la Réservation #" + reservation.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(reservationsTable.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening edit view", e);
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre", 
                    "Impossible d'ouvrir le formulaire de modification: " + e.getMessage());
        }
    }
    
    private void cancelReservation(Reservation reservation) {
        boolean confirmed = AlertUtils.showConfirmation("Annuler la réservation", 
                "Annuler la réservation #" + reservation.getId(), 
                "Êtes-vous sûr de vouloir annuler cette réservation ?");
        
        if (confirmed) {
            try {
                boolean success = reservationService.cancelReservation(reservation.getId());
                if (success) {
                    AlertUtils.showInformation("Succès", "Réservation annulée", 
                            "La réservation a été annulée avec succès.");
                    loadReservations();
                } else {
                    AlertUtils.showError("Erreur", "Échec de l'annulation", 
                            "Impossible d'annuler cette réservation. Veuillez réessayer plus tard.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error cancelling reservation", e);
                AlertUtils.showError("Erreur", "Annulation échouée", 
                        "Erreur lors de l'annulation de la réservation: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleAddReservation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NewReservationView.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Nouvelle Réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(reservationsTable.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();
            
            // Reload reservations after adding a new one
            loadReservations();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error opening add reservation view", e);
            AlertUtils.showError("Erreur", "Impossible d'ouvrir le formulaire", 
                    "Impossible d'ouvrir le formulaire d'ajout de réservation: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadReservations();
        AlertUtils.showInformation("Actualisation", "Données actualisées", 
                "Les données ont été actualisées avec succès.");
    }
    
    // Navigation methods
    @FXML
    private void navigateTo(ActionEvent event) {
        Button sourceButton = (Button) event.getSource();
        String buttonText = sourceButton.getText();
        
        switch (buttonText) {
            case "Accueil":
                navigateToView("/Home.fxml", "TunTransport - Accueil");
                break;
            case "Trajets":
                navigateToView("/RouteView.fxml", "TunTransport - Trajets");
                break;
            case "Transports":
                navigateToView("/TransportView.fxml", "TunTransport - Transports");
                break;
            case "Utilisateurs":
                navigateToView("/UserView.fxml", "TunTransport - Utilisateurs");
                break;
            default:
                break;
        }
    }
    
    private void navigateToView(String fxml, String title) {
        try {
            LOGGER.info("Navigating to: " + fxml);
            Main.navigateTo(fxml, title);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error navigating to " + fxml, e);
            AlertUtils.showError("Erreur de navigation", "Impossible de naviguer", 
                    "Impossible de charger la page demandée: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToHome() {
        test.Main.navigateToHome();
    }
} 