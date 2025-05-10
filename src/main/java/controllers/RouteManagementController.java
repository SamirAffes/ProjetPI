package controllers;

import entities.Organisation;
import entities.OrganisationRoute;
import entities.Route;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.OrganisationRouteService;
import services.RouteService;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class RouteManagementController implements Initializable {

    // My Routes Tab
    @FXML private TableView<OrganisationRoute> myRoutesTable;
    @FXML private TableColumn<OrganisationRoute, String> codeColumn;
    @FXML private TableColumn<OrganisationRoute, String> originColumn;
    @FXML private TableColumn<OrganisationRoute, String> destinationColumn;
    @FXML private TableColumn<OrganisationRoute, String> transportTypeColumn;
    @FXML private TableColumn<OrganisationRoute, String> distanceColumn;
    @FXML private TableColumn<OrganisationRoute, Integer> frequencyColumn;
    @FXML private TableColumn<OrganisationRoute, String> statusColumn;
    @FXML private TableColumn<OrganisationRoute, Void> actionsColumn;
    @FXML private ComboBox<String> filterTransportTypeComboBox;
    @FXML private TextField searchField;

    // Add Routes Tab
    @FXML private TableView<Route> availableRoutesTable;
    @FXML private TableColumn<Route, String> availableOriginColumn;
    @FXML private TableColumn<Route, String> availableDestinationColumn;
    @FXML private TableColumn<Route, String> availableTransportTypeColumn;
    @FXML private TableColumn<Route, String> availableDistanceColumn;
    @FXML private TableColumn<Route, String> availableDurationColumn;
    @FXML private TableColumn<Route, String> availablePriceColumn;
    @FXML private TableColumn<Route, Void> availableActionsColumn;
    @FXML private ComboBox<String> searchTransportTypeComboBox;
    @FXML private ComboBox<String> originComboBox;
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private Button searchRoutesButton;

    private Organisation organisation;
    private final RouteService routeService = new RouteService();
    private final OrganisationRouteService organisationRouteService = new OrganisationRouteService();
    
    private ObservableList<OrganisationRoute> organisationRoutes = FXCollections.observableArrayList();
    private FilteredList<OrganisationRoute> filteredOrganisationRoutes;
    private ObservableList<Route> availableRoutes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing RouteManagementController");
        
        // Initialize transport type options
        List<String> transportTypes = Arrays.asList(
            "Bus", "Train", "Taxi", "Métro", "TGM", "Avion", "Ferry", "Tous"
        );
        
        filterTransportTypeComboBox.getItems().addAll(transportTypes);
        filterTransportTypeComboBox.setValue("Tous");
        
        searchTransportTypeComboBox.getItems().addAll(transportTypes);
        searchTransportTypeComboBox.setValue("Tous");
        
        // Setup my routes table
        setupMyRoutesTable();
        
        // Setup available routes table
        setupAvailableRoutesTable();
        
        // Setup search functionality
        setupSearch();
        
        // Setup transport type filter
        setupTransportTypeFilter();
        
        // Initialize available routes table with empty list
        availableRoutesTable.setItems(availableRoutes);
    }
    
    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
        log.info("Organisation set in RouteManagementController: {}", organisation.getNom());
        
        // Load organisation routes
        loadOrganisationRoutes();
        
        // Load cities for search
        loadCities();
        
        // Load initial available routes
        loadAvailableRoutes();
    }
    
    /**
     * Refresh all data in the controller.
     * This reloads organization routes and available routes.
     */
    public void refreshData() {
        log.info("Refreshing route management data");
        
        if (organisation != null) {
            // Reload organisation routes
            loadOrganisationRoutes();
            
            // Reload cities and available routes
            loadCities();
            
            // Load available routes
            loadAvailableRoutes();
            
            // If there's an active search, refresh the search results
            if (searchTransportTypeComboBox.getValue() != null) {
                searchRoutes();
            }
        }
    }
    
    /**
     * Load all available routes that can be added to the organization
     */
    private void loadAvailableRoutes() {
        List<Route> routes = routeService.afficher_tout();
        log.info("Loaded {} total routes from database", routes.size());
        
        // Update table with all routes
        availableRoutes.setAll(routes);
        availableRoutesTable.setItems(availableRoutes);
        
        log.info("Available routes table updated with {} routes", availableRoutes.size());
    }
    
    private void setupMyRoutesTable() {
        // Setup columns for my routes table
        codeColumn.setCellValueFactory(cellData -> {
            String code = cellData.getValue().getInternalRouteCode();
            return new SimpleStringProperty(code != null ? code : "");
        });
        
        originColumn.setCellValueFactory(cellData -> {
            Route route = routeService.afficher(cellData.getValue().getRouteId());
            return new SimpleStringProperty(route != null ? route.getOrigin() : "");
        });
        
        destinationColumn.setCellValueFactory(cellData -> {
            Route route = routeService.afficher(cellData.getValue().getRouteId());
            return new SimpleStringProperty(route != null ? route.getDestination() : "");
        });
        
        transportTypeColumn.setCellValueFactory(cellData -> {
            Route route = routeService.afficher(cellData.getValue().getRouteId());
            return new SimpleStringProperty(route != null ? route.getTransportMode() : "");
        });
        
        distanceColumn.setCellValueFactory(cellData -> {
            Route route = routeService.afficher(cellData.getValue().getRouteId());
            return new SimpleStringProperty(route != null ? route.getDistance() + " km" : "");
        });
        
        frequencyColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getFrequencyPerDay()).asObject());
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? "Actif" : "Inactif"));
        
        // Setup actions column with edit and delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                // Edit button setup
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(16);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("button-small");
                editButton.setTooltip(new Tooltip("Modifier"));
                
                editButton.setOnAction(event -> {
                    OrganisationRoute orgRoute = getTableView().getItems().get(getIndex());
                    showEditDialog(orgRoute);
                });
                
                // Delete button setup
                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(16);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("button-small");
                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setTooltip(new Tooltip("Supprimer"));
                
                deleteButton.setOnAction(event -> {
                    OrganisationRoute orgRoute = getTableView().getItems().get(getIndex());
                    confirmAndDeleteRoute(orgRoute);
                });
                
                pane.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }
    
    private void setupAvailableRoutesTable() {
        // Setup columns for available routes table
        availableOriginColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getOrigin()));
        
        availableDestinationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDestination()));
        
        availableTransportTypeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTransportMode()));
        
        availableDistanceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDistance() + " km"));
        
        availableDurationColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatDuration(cellData.getValue().getEstimatedDuration())));
        
        availablePriceColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getBasePrice() + " DT"));
        
        // Setup actions column with add button
        availableActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button addButton = new Button("Ajouter");
            
            {
                addButton.getStyleClass().add("button-small");
                addButton.getStyleClass().add("button-primary");
                
                addButton.setOnAction(event -> {
                    Route route = getTableView().getItems().get(getIndex());
                    showAddRouteDialog(route);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    Route route = getTableView().getItems().get(getIndex());
                    boolean alreadyAssigned = organisationRouteService.isRouteAssignedToOrganisation(
                        route.getId(), organisation.getId());
                    
                    if (alreadyAssigned) {
                        Label assignedLabel = new Label("Déjà ajouté");
                        assignedLabel.getStyleClass().add("label-info");
                        setGraphic(assignedLabel);
                    } else {
                        setGraphic(addButton);
                    }
                }
            }
        });
    }
    
    private void setupSearch() {
        // Setup search functionality for my routes
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredOrganisationRoutes != null) {
                filteredOrganisationRoutes.setPredicate(orgRoute -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    
                    String lowerCaseFilter = newValue.toLowerCase();
                    Route route = routeService.afficher(orgRoute.getRouteId());
                    
                    if (route == null) {
                        return false;
                    }
                    
                    if (route.getOrigin().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (route.getDestination().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    } else if (orgRoute.getInternalRouteCode() != null && 
                              orgRoute.getInternalRouteCode().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }
                    
                    return false;
                });
            }
        });
    }
    
    private void setupTransportTypeFilter() {
        // Setup transport type filter for my routes
        filterTransportTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredOrganisationRoutes != null) {
                filteredOrganisationRoutes.setPredicate(orgRoute -> {
                    if (newValue == null || newValue.equals("Tous")) {
                        return true;
                    }
                    
                    Route route = routeService.afficher(orgRoute.getRouteId());
                    return route != null && route.getTransportMode().equals(newValue);
                });
            }
        });
        
        // Setup transport type filter for search
        searchTransportTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateCitiesByTransportType(newValue);
        });
    }
    
    private void loadOrganisationRoutes() {
        if (organisation != null) {
            List<OrganisationRoute> routes = organisationRouteService.findByOrganisationId(organisation.getId());
            organisationRoutes.setAll(routes);
            
            // Create filtered list
            filteredOrganisationRoutes = new FilteredList<>(organisationRoutes, p -> true);
            myRoutesTable.setItems(filteredOrganisationRoutes);
            
            log.info("Loaded {} routes for organisation {}", routes.size(), organisation.getNom());
        }
    }
    
    private void loadCities() {
        List<Route> allRoutes = routeService.afficher_tout();
        log.info("Loading cities from {} routes", allRoutes.size());
        
        // Extract unique origins and destinations
        List<String> origins = allRoutes.stream()
            .map(Route::getOrigin)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
            
        List<String> destinations = allRoutes.stream()
            .map(Route::getDestination)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        
        log.info("Found {} unique origins and {} unique destinations", origins.size(), destinations.size());
            
        // Add to combo boxes
        originComboBox.getItems().clear();
        originComboBox.getItems().add("Tous");
        originComboBox.getItems().addAll(origins);
        originComboBox.setValue("Tous");
        
        destinationComboBox.getItems().clear();
        destinationComboBox.getItems().add("Tous");
        destinationComboBox.getItems().addAll(destinations);
        destinationComboBox.setValue("Tous");
    }
    
    private void updateCitiesByTransportType(String transportType) {
        if (transportType == null || transportType.equals("Tous")) {
            loadCities();
            return;
        }
        
        List<Route> filteredRoutes = routeService.afficher_tout().stream()
            .filter(route -> route.getTransportMode().equals(transportType))
            .collect(Collectors.toList());
        
        log.info("Filtered routes by transport type '{}': found {} routes", transportType, filteredRoutes.size());
            
        // Extract unique origins and destinations
        List<String> origins = filteredRoutes.stream()
            .map(Route::getOrigin)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
            
        List<String> destinations = filteredRoutes.stream()
            .map(Route::getDestination)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
            
        // Update combo boxes
        String currentOrigin = originComboBox.getValue();
        String currentDestination = destinationComboBox.getValue();
        
        originComboBox.getItems().clear();
        originComboBox.getItems().add("Tous");
        originComboBox.getItems().addAll(origins);
        
        destinationComboBox.getItems().clear();
        destinationComboBox.getItems().add("Tous");
        destinationComboBox.getItems().addAll(destinations);
        
        // Restore selection if still valid
        if (origins.contains(currentOrigin)) {
            originComboBox.setValue(currentOrigin);
        } else {
            originComboBox.setValue("Tous");
        }
        
        if (destinations.contains(currentDestination)) {
            destinationComboBox.setValue(currentDestination);
        } else {
            destinationComboBox.setValue("Tous");
        }
    }
    
    @FXML
    private void searchRoutes() {
        String transportType = searchTransportTypeComboBox.getValue();
        String origin = originComboBox.getValue();
        String destination = destinationComboBox.getValue();
        
        log.info("Searching routes with criteria - Transport: {}, Origin: {}, Destination: {}", 
                transportType, origin, destination);
        
        List<Route> routes = routeService.afficher_tout();
        log.info("Total routes in database: {}", routes.size());
        
        // Apply filters
        if (transportType != null && !transportType.equals("Tous")) {
            routes = routes.stream()
                .filter(route -> route.getTransportMode().equals(transportType))
                .collect(Collectors.toList());
            log.info("After transport type filter: {} routes", routes.size());
        }
        
        if (origin != null && !origin.equals("Tous")) {
            routes = routes.stream()
                .filter(route -> route.getOrigin().equals(origin))
                .collect(Collectors.toList());
            log.info("After origin filter: {} routes", routes.size());
        }
        
        if (destination != null && !destination.equals("Tous")) {
            routes = routes.stream()
                .filter(route -> route.getDestination().equals(destination))
                .collect(Collectors.toList());
            log.info("After destination filter: {} routes", routes.size());
        }
        
        // Update table
        availableRoutes.clear();
        availableRoutes.addAll(routes);
        
        // Log some sample routes for debugging
        if (!routes.isEmpty()) {
            int sampleSize = Math.min(routes.size(), 3);
            for (int i = 0; i < sampleSize; i++) {
                Route route = routes.get(i);
                log.info("Sample route {}: {} → {} ({}), {} km", 
                        i+1, route.getOrigin(), route.getDestination(), 
                        route.getTransportMode(), route.getDistance());
            }
        }
        
        log.info("Updated available routes table with {} routes", routes.size());
    }
    
    private void showAddRouteDialog(Route route) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organisation/addRouteDialog.fxml"));
            Parent root = loader.load();
            
            AddRouteDialogController controller = loader.getController();
            controller.setRoute(route);
            controller.setOrganisation(organisation);
            controller.setOrganisationRouteService(organisationRouteService);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un itinéraire");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(myRoutesTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            // Refresh data
            loadOrganisationRoutes();
            
        } catch (IOException e) {
            log.error("Error showing add route dialog", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout d'itinéraire");
        }
    }
    
    private void showEditDialog(OrganisationRoute orgRoute) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organisation/editRouteDialog.fxml"));
            Parent root = loader.load();
            
            EditRouteDialogController controller = loader.getController();
            controller.setOrganisationRoute(orgRoute);
            controller.setRoute(routeService.afficher(orgRoute.getRouteId()));
            controller.setOrganisationRouteService(organisationRouteService);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier un itinéraire");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(myRoutesTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            dialogStage.showAndWait();
            
            // Refresh data
            loadOrganisationRoutes();
            
        } catch (IOException e) {
            log.error("Error showing edit route dialog", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification d'itinéraire");
        }
    }
    
    private void confirmAndDeleteRoute(OrganisationRoute orgRoute) {
        Route route = routeService.afficher(orgRoute.getRouteId());
        String routeInfo = route != null ? 
            route.getOrigin() + " → " + route.getDestination() : 
            "Itinéraire #" + orgRoute.getRouteId();
            
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'itinéraire");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'itinéraire " + routeInfo + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                organisationRouteService.supprimer(orgRoute);
                loadOrganisationRoutes();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'itinéraire a été supprimé avec succès");
            } catch (Exception e) {
                log.error("Error deleting organisation route", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression");
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            return hours + "h " + (remainingMinutes > 0 ? remainingMinutes + "min" : "");
        }
    }
} 