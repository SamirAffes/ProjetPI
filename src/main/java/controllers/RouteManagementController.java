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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.OrganisationRouteService;
import services.RouteService;
import utils.OrganisationContext;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @FXML private VBox myRoutesContainer; // New container for route cards

    // Add Routes Tab
    @FXML private TableView<Route> availableRoutesTable;
    @FXML private TableColumn<Route, String> availableOriginColumn;
    @FXML private TableColumn<Route, String> availableDestinationColumn;
    @FXML private TableColumn<Route, String> availableTransportTypeColumn;
    @FXML private TableColumn<Route, String> availableDistanceColumn;
    @FXML private TableColumn<Route, String> availableDurationColumn;
    @FXML private TableColumn<Route, String> availablePriceColumn;
    @FXML private TableColumn<Route, Void> availableActionsColumn;
    @FXML private VBox availableRoutesContainer; // New container for available route cards
    @FXML private ComboBox<String> searchTransportTypeComboBox;
    @FXML private ComboBox<String> originComboBox;
    @FXML private ComboBox<String> destinationComboBox;
    @FXML private Button searchRoutesButton;
    @FXML private Button resetFiltersButton;

    private Organisation organisation;
    private final RouteService routeService = new RouteService();
    private final OrganisationRouteService organisationRouteService = new OrganisationRouteService();

    private ObservableList<OrganisationRoute> organisationRoutes = FXCollections.observableArrayList();
    private FilteredList<OrganisationRoute> filteredOrganisationRoutes;
    private ObservableList<Route> availableRoutes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing RouteManagementController");

        // Get organization from context
        organisation = OrganisationContext.getInstance().getCurrentOrganisation();

        if (organisation != null) {
            log.info("Organization loaded: {}", organisation.getNom());
        } else {
            log.warn("Organization not found in context");
        }

        // Setup tables
        setupMyRoutesTable();
        setupAvailableRoutesTable();

        // Initialize transport type filters
        filterTransportTypeComboBox.getItems().add("Tous");

        searchTransportTypeComboBox.getItems().add("Tous");
        searchTransportTypeComboBox.getItems().addAll(
            "Bus", "Train", "Métro", "TGM", "Taxi", "Ferry", "Avion"
        );
        searchTransportTypeComboBox.setValue("Tous");

        // Setup the transport mode combo box for the filter
        Arrays.asList("Bus", "Train", "Métro", "TGM", "Taxi", "Ferry", "Avion")
            .forEach(type -> filterTransportTypeComboBox.getItems().add(type));
        filterTransportTypeComboBox.setValue("Tous");

        // Load data
        loadOrganisationRoutes();
        loadAvailableRoutes();
        availableRoutesTable.setItems(availableRoutes);

        // Setup city selection for search
        loadCities();

        // Setup search functionality
        setupSearch();
        setupTransportTypeFilter();

        // Button actions
        searchRoutesButton.setOnAction(event -> searchRoutes());
        resetFiltersButton.setOnAction(event -> resetFilters());

        log.info("RouteManagementController initialized successfully");
    }

    public void setOrganisation(Organisation organisation) {
        try {
            this.organisation = organisation;

            if (organisation != null) {
                log.info("Organisation set in RouteManagementController: {}", organisation.getNom());

                // Also update the OrganisationContext
                OrganisationContext.getInstance().setCurrentOrganisation(organisation);

                // Load organisation routes
                loadOrganisationRoutes();

                // Load cities for search
                loadCities();

                // Load initial available routes
                loadAvailableRoutes();

                // Update the organization-dependent UI elements
                if (availableRoutesTable != null && availableRoutesTable.getColumns() != null && 
                    !availableRoutesTable.getColumns().isEmpty()) {
                    availableRoutesTable.refresh();
                }
            } else {
                log.warn("Null organisation set in RouteManagementController");
            }
        } catch (Exception e) {
            log.error("Error setting organisation in RouteManagementController", e);
        }
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

            // Refresh available routes with current filter criteria
            refreshAvailableRoutes();
        }
    }

    /**
     * Load all available routes that can be added to the organization
     */
    private void loadAvailableRoutes() {
        try {
            List<Route> allRoutes = routeService.afficher_tout();
            log.info("Loaded {} routes for available routes table", allRoutes.size());

            // Get all routes currently assigned to this organization
            List<Integer> assignedRouteIds = new ArrayList<>();
            if (organisation != null) {
                assignedRouteIds = organisationRouteService
                    .findByOrganisationId(organisation.getId())
                    .stream()
                    .map(OrganisationRoute::getRouteId)
                    .collect(Collectors.toList());

                log.info("Organisation has {} routes already assigned", assignedRouteIds.size());
            }

            // Filter out assigned routes and eliminate duplicates using a map to track route IDs
            Map<Integer, Route> uniqueRoutes = new HashMap<>();
            for (Route route : allRoutes) {
                // Skip routes already assigned to this organisation
                if (assignedRouteIds.contains(route.getId())) {
                    continue;
                }

                // Keep only one instance of each route ID
                if (!uniqueRoutes.containsKey(route.getId())) {
                    uniqueRoutes.put(route.getId(), route);
                }
            }

            // Update the observable list
            availableRoutes.clear();
            availableRoutes.addAll(uniqueRoutes.values());
            log.info("Added {} unique routes to available routes table", availableRoutes.size());

            // Update the modern UI container
            try {
                if (availableRoutesContainer != null) {
                    availableRoutesContainer.getChildren().clear();

                    if (uniqueRoutes.isEmpty()) {
                        Label noRoutesLabel = new Label("Aucun itinéraire disponible");
                        noRoutesLabel.getStyleClass().add("no-results-label");
                        availableRoutesContainer.getChildren().add(noRoutesLabel);
                    } else {
                        // Add route cards to the container
                        for (Route route : uniqueRoutes.values()) {
                            availableRoutesContainer.getChildren().add(createAvailableRouteCard(route));
                        }
                    }
                } else {
                    log.warn("availableRoutesContainer is null, cannot update UI");
                }
            } catch (Exception ex) {
                log.error("Error updating availableRoutesContainer", ex);
                // Continue execution - don't let UI update failure prevent data loading
            }

        } catch (Exception e) {
            log.error("Error loading available routes", e);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", 
                    "Une erreur est survenue lors du chargement des itinéraires disponibles.");
        }
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
                // Button styling and icon
                FontIcon plusIcon = new FontIcon("fas-plus");
                plusIcon.setIconSize(14);
                addButton.setGraphic(plusIcon);
                addButton.getStyleClass().add("button-small");
                addButton.getStyleClass().add("button-success");

                addButton.setOnAction(event -> {
                    Route route = getTableView().getItems().get(getIndex());
                    log.info("Add button clicked for route: {} → {}", 
                            route.getOrigin(), route.getDestination());

                    // Make sure we have an organization
                    if (organisation == null) {
                        organisation = OrganisationContext.getInstance().getCurrentOrganisation();
                    }

                    if (organisation == null) {
                        log.warn("Cannot add route: organization is null");
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Organisation non définie. Veuillez vous reconnecter.");
                        return;
                    }

                    // Check if route is already assigned to this organization
                    if (organisationRouteService.isRouteAssignedToOrganisation(route.getId(), organisation.getId())) {
                        log.warn("Route {} is already assigned to organisation {}", 
                                route.getId(), organisation.getId());
                        showAlert(Alert.AlertType.WARNING, "Route déjà assignée", 
                                "Cet itinéraire est déjà assigné à votre organisation.");
                        return;
                    }

                    showAddRouteDialog(route);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Get the route for this row
                    Route route = getTableView().getItems().get(getIndex());
                    if (route == null) {
                        setGraphic(null);
                        return;
                    }

                    if (organisation != null) {
                        // Check if this route is already assigned to the organization
                        boolean isAssigned = organisationRouteService.isRouteAssignedToOrganisation(
                                route.getId(), organisation.getId());

                        // Disable the button if the route is already assigned
                        addButton.setDisable(isAssigned);
                        addButton.setText(isAssigned ? "Déjà ajouté" : "Ajouter");
                    }

                    setGraphic(addButton);
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

                // Update the modern UI container with filtered routes
                updateRouteCardsContainer();
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
                    return route != null && route.getTransportMode().equalsIgnoreCase(newValue);
                });

                // Update the modern UI container with filtered routes
                updateRouteCardsContainer();
            }
        });

        // Setup transport type filter for search
        searchTransportTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateCitiesByTransportType(newValue);
        });
    }

    /**
     * Updates the route cards container with the current filtered routes
     */
    private void updateRouteCardsContainer() {
        if (myRoutesContainer != null) {
            myRoutesContainer.getChildren().clear();

            if (filteredOrganisationRoutes.isEmpty()) {
                Label noRoutesLabel = new Label("Aucun itinéraire trouvé");
                noRoutesLabel.getStyleClass().add("no-results-label");
                myRoutesContainer.getChildren().add(noRoutesLabel);
            } else {
                // Add route cards to the container
                for (OrganisationRoute orgRoute : filteredOrganisationRoutes) {
                    myRoutesContainer.getChildren().add(createRouteCard(orgRoute));
                }
            }
        }
    }

    private void loadOrganisationRoutes() {
        if (organisation != null) {
            List<OrganisationRoute> routes = organisationRouteService.findByOrganisationId(organisation.getId());
            organisationRoutes.setAll(routes);

            // Create filtered list
            filteredOrganisationRoutes = new FilteredList<>(organisationRoutes, p -> true);
            myRoutesTable.setItems(filteredOrganisationRoutes);

            // Update the modern UI container
            updateRouteCardsContainer();

            log.info("Loaded {} routes for organisation {}", routes.size(), organisation.getNom());
        }
    }

    /**
     * Creates a card-style UI element for a route
     * 
     * @param orgRoute The organisation route to display
     * @return An HBox containing the route information
     */
    private HBox createRouteCard(OrganisationRoute orgRoute) {
        // Get the base route information
        Route route = routeService.afficher(orgRoute.getRouteId());
        if (route == null) {
            log.warn("Could not find route with ID {} for organisation route", orgRoute.getRouteId());
            return new HBox(); // Return empty HBox if route not found
        }

        // Create the main container
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("upcoming-reservation-item");
        card.setPadding(new Insets(10));

        // Create the code container (left side)
        VBox codeContainer = new VBox();
        codeContainer.setAlignment(Pos.CENTER);
        codeContainer.getStyleClass().add("reservation-date-container");

        // Set background color based on transport type
        String transportType = route.getTransportMode();
        String bgColorStyle = "-fx-background-color: rgba(77, 171, 247, 0.2);"; // Default blue
        String iconColor = "#4dabf7"; // Default blue

        switch (transportType.toLowerCase()) {
            case "bus":
                bgColorStyle = "-fx-background-color: rgba(52, 152, 219, 0.2);";
                iconColor = "#3498db";
                break;
            case "train":
                bgColorStyle = "-fx-background-color: rgba(231, 76, 60, 0.2);";
                iconColor = "#e74c3c";
                break;
            case "métro":
                bgColorStyle = "-fx-background-color: rgba(155, 89, 182, 0.2);";
                iconColor = "#9b59b6";
                break;
            case "tgm":
                bgColorStyle = "-fx-background-color: rgba(46, 204, 113, 0.2);";
                iconColor = "#2ecc71";
                break;
            case "taxi":
                bgColorStyle = "-fx-background-color: rgba(241, 196, 15, 0.2);";
                iconColor = "#f1c40f";
                break;
            case "ferry":
                bgColorStyle = "-fx-background-color: rgba(41, 128, 185, 0.2);";
                iconColor = "#2980b9";
                break;
            case "avion":
                bgColorStyle = "-fx-background-color: rgba(52, 152, 219, 0.2);";
                iconColor = "#3498db";
                break;
        }

        codeContainer.setStyle(bgColorStyle);

        // Add icon and code
        FontIcon transportIcon = new FontIcon();
        transportIcon.setIconSize(20);
        transportIcon.setIconColor(javafx.scene.paint.Color.web(iconColor));

        // Set icon based on transport type
        switch (transportType.toLowerCase()) {
            case "bus":
                transportIcon.setIconLiteral("fas-bus");
                break;
            case "train":
                transportIcon.setIconLiteral("fas-train");
                break;
            case "métro":
                transportIcon.setIconLiteral("fas-subway");
                break;
            case "tgm":
                transportIcon.setIconLiteral("fas-tram");
                break;
            case "taxi":
                transportIcon.setIconLiteral("fas-taxi");
                break;
            case "ferry":
                transportIcon.setIconLiteral("fas-ship");
                break;
            case "avion":
                transportIcon.setIconLiteral("fas-plane");
                break;
            default:
                transportIcon.setIconLiteral("fas-route");
                break;
        }

        // Route code
        String code = orgRoute.getInternalRouteCode();
        if (code == null || code.isEmpty()) {
            code = "R" + orgRoute.getId();
        }

        Label codeLabel = new Label(code);
        codeLabel.getStyleClass().add("reservation-date-day");
        codeLabel.setStyle("-fx-font-size: 14px;");

        codeContainer.getChildren().addAll(transportIcon, codeLabel);

        // Create the details container (middle)
        VBox detailsContainer = new VBox();
        HBox.setHgrow(detailsContainer, javafx.scene.layout.Priority.ALWAYS);

        // Route title
        Label routeTitle = new Label(route.getOrigin() + " → " + route.getDestination());
        routeTitle.getStyleClass().add("reservation-route");

        // Details row
        HBox detailsRow = new HBox();
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        detailsRow.setSpacing(15);

        // Transport type
        HBox transportBox = new HBox(5);
        transportBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon transportTypeIcon = new FontIcon();
        transportTypeIcon.setIconSize(12);
        transportTypeIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        transportTypeIcon.setIconLiteral("fas-" + (transportType.equalsIgnoreCase("métro") ? "subway" : transportType.toLowerCase()));
        Label transportLabel = new Label(transportType);
        transportLabel.getStyleClass().add("reservation-detail");
        transportBox.getChildren().addAll(transportTypeIcon, transportLabel);

        // Distance
        HBox distanceBox = new HBox(5);
        distanceBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon distanceIcon = new FontIcon("fas-road");
        distanceIcon.setIconSize(12);
        distanceIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        Label distanceLabel = new Label(route.getDistance() + " km");
        distanceLabel.getStyleClass().add("reservation-detail");
        distanceBox.getChildren().addAll(distanceIcon, distanceLabel);

        // Frequency
        HBox frequencyBox = new HBox(5);
        frequencyBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon frequencyIcon = new FontIcon("fas-clock");
        frequencyIcon.setIconSize(12);
        frequencyIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));

        String frequencyText = "Toutes les heures";
        if (orgRoute.getFrequencyPerDay() > 0) {
            if (orgRoute.getFrequencyPerDay() == 24) {
                frequencyText = "Toutes les heures";
            } else if (orgRoute.getFrequencyPerDay() == 48) {
                frequencyText = "Toutes les 30 min";
            } else if (orgRoute.getFrequencyPerDay() == 12) {
                frequencyText = "Toutes les 2 heures";
            } else {
                frequencyText = orgRoute.getFrequencyPerDay() + " fois/jour";
            }
        }

        Label frequencyLabel = new Label(frequencyText);
        frequencyLabel.getStyleClass().add("reservation-detail");
        frequencyBox.getChildren().addAll(frequencyIcon, frequencyLabel);

        // Status
        Label statusLabel = new Label(orgRoute.isActive() ? "Actif" : "Inactif");
        statusLabel.getStyleClass().add(orgRoute.isActive() ? "reservation-status-confirmed" : "reservation-status-pending");

        detailsRow.getChildren().addAll(transportBox, distanceBox, frequencyBox, statusLabel);

        detailsContainer.getChildren().addAll(routeTitle, detailsRow);

        // Create the actions container (right)
        HBox actionsContainer = new HBox(5);
        actionsContainer.setAlignment(Pos.CENTER);

        // Edit button
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("edit-button");
        FontIcon editIcon = new FontIcon("fas-edit");
        editIcon.setIconSize(12);
        editButton.setGraphic(editIcon);
        editButton.setOnAction(event -> showEditDialog(orgRoute));

        // Delete button
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.setIconSize(12);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(event -> confirmAndDeleteRoute(orgRoute));

        actionsContainer.getChildren().addAll(editButton, deleteButton);

        // Add all components to the card
        card.getChildren().addAll(codeContainer, detailsContainer, actionsContainer);

        return card;
    }

    /**
     * Creates a card-style UI element for an available route
     * 
     * @param route The route to display
     * @return An HBox containing the route information
     */
    private HBox createAvailableRouteCard(Route route) {
        if (route == null) {
            log.warn("Null route passed to createAvailableRouteCard");
            return new HBox(); // Return empty HBox if route is null
        }

        // Create the main container
        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("upcoming-reservation-item");
        card.setPadding(new Insets(10));

        // Create the icon container (left side)
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getStyleClass().add("reservation-date-container");

        // Set background color based on transport type
        String transportType = route.getTransportMode();
        String bgColorStyle = "-fx-background-color: rgba(77, 171, 247, 0.2);"; // Default blue
        String iconColor = "#4dabf7"; // Default blue

        switch (transportType.toLowerCase()) {
            case "bus":
                bgColorStyle = "-fx-background-color: rgba(52, 152, 219, 0.2);";
                iconColor = "#3498db";
                break;
            case "train":
                bgColorStyle = "-fx-background-color: rgba(231, 76, 60, 0.2);";
                iconColor = "#e74c3c";
                break;
            case "métro":
                bgColorStyle = "-fx-background-color: rgba(155, 89, 182, 0.2);";
                iconColor = "#9b59b6";
                break;
            case "tgm":
                bgColorStyle = "-fx-background-color: rgba(46, 204, 113, 0.2);";
                iconColor = "#2ecc71";
                break;
            case "taxi":
                bgColorStyle = "-fx-background-color: rgba(241, 196, 15, 0.2);";
                iconColor = "#f1c40f";
                break;
            case "ferry":
                bgColorStyle = "-fx-background-color: rgba(41, 128, 185, 0.2);";
                iconColor = "#2980b9";
                break;
            case "avion":
                bgColorStyle = "-fx-background-color: rgba(52, 152, 219, 0.2);";
                iconColor = "#3498db";
                break;
        }

        iconContainer.setStyle(bgColorStyle);

        // Add icon
        FontIcon transportIcon = new FontIcon();
        transportIcon.setIconSize(20);
        transportIcon.setIconColor(javafx.scene.paint.Color.web(iconColor));

        // Set icon based on transport type
        switch (transportType.toLowerCase()) {
            case "bus":
                transportIcon.setIconLiteral("fas-bus");
                break;
            case "train":
                transportIcon.setIconLiteral("fas-train");
                break;
            case "métro":
                transportIcon.setIconLiteral("fas-subway");
                break;
            case "tgm":
                transportIcon.setIconLiteral("fas-tram");
                break;
            case "taxi":
                transportIcon.setIconLiteral("fas-taxi");
                break;
            case "ferry":
                transportIcon.setIconLiteral("fas-ship");
                break;
            case "avion":
                transportIcon.setIconLiteral("fas-plane");
                break;
            default:
                transportIcon.setIconLiteral("fas-route");
                break;
        }

        iconContainer.getChildren().add(transportIcon);

        // Create the details container (middle)
        VBox detailsContainer = new VBox();
        HBox.setHgrow(detailsContainer, javafx.scene.layout.Priority.ALWAYS);

        // Route title
        Label routeTitle = new Label(route.getOrigin() + " → " + route.getDestination());
        routeTitle.getStyleClass().add("reservation-route");

        // Details row
        HBox detailsRow = new HBox();
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        detailsRow.setSpacing(15);

        // Transport type
        HBox transportBox = new HBox(5);
        transportBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon transportTypeIcon = new FontIcon();
        transportTypeIcon.setIconSize(12);
        transportTypeIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        transportTypeIcon.setIconLiteral("fas-" + (transportType.equalsIgnoreCase("métro") ? "subway" : transportType.toLowerCase()));
        Label transportLabel = new Label(transportType);
        transportLabel.getStyleClass().add("reservation-detail");
        transportBox.getChildren().addAll(transportTypeIcon, transportLabel);

        // Distance
        HBox distanceBox = new HBox(5);
        distanceBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon distanceIcon = new FontIcon("fas-road");
        distanceIcon.setIconSize(12);
        distanceIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        Label distanceLabel = new Label(route.getDistance() + " km");
        distanceLabel.getStyleClass().add("reservation-detail");
        distanceBox.getChildren().addAll(distanceIcon, distanceLabel);

        // Duration
        HBox durationBox = new HBox(5);
        durationBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon durationIcon = new FontIcon("fas-clock");
        durationIcon.setIconSize(12);
        durationIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        Label durationLabel = new Label(formatDuration(route.getEstimatedDuration()));
        durationLabel.getStyleClass().add("reservation-detail");
        durationBox.getChildren().addAll(durationIcon, durationLabel);

        // Price
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        FontIcon priceIcon = new FontIcon("fas-tag");
        priceIcon.setIconSize(12);
        priceIcon.setIconColor(javafx.scene.paint.Color.web("#7f8c8d"));
        Label priceLabel = new Label(route.getBasePrice() + " DT");
        priceLabel.getStyleClass().add("reservation-detail");
        priceBox.getChildren().addAll(priceIcon, priceLabel);

        detailsRow.getChildren().addAll(transportBox, distanceBox, durationBox, priceBox);

        detailsContainer.getChildren().addAll(routeTitle, detailsRow);

        // Create the actions container (right)
        HBox actionsContainer = new HBox(5);
        actionsContainer.setAlignment(Pos.CENTER);

        // Add button
        Button addButton = new Button("Ajouter");
        addButton.getStyleClass().add("button-primary");
        FontIcon addIcon = new FontIcon("fas-plus");
        addIcon.setIconSize(12);
        addButton.setGraphic(addIcon);

        // Set the action for the add button
        addButton.setOnAction(event -> {
            log.info("Add button clicked for route: {} → {}", 
                    route.getOrigin(), route.getDestination());

            // Make sure we have an organization
            if (organisation == null) {
                organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            }

            if (organisation == null) {
                log.warn("Cannot add route: organization is null");
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Organisation non définie. Veuillez vous reconnecter.");
                return;
            }

            // Check if route is already assigned to this organization
            try {
                if (organisationRouteService.isRouteAssignedToOrganisation(route.getId(), organisation.getId())) {
                    log.warn("Route {} is already assigned to organisation {}", 
                            route.getId(), organisation.getId());
                    showAlert(Alert.AlertType.WARNING, "Route déjà assignée", 
                            "Cet itinéraire est déjà assigné à votre organisation.");
                    return;
                }
            } catch (Exception ex) {
                log.error("Error checking if route is assigned to organisation", ex);
                // Continue execution - don't let this check prevent adding the route
            }

            showAddRouteDialog(route);
        });

        // Disable the button if the route is already assigned to the organization
        if (organisation != null) {
            try {
                boolean isAssigned = organisationRouteService.isRouteAssignedToOrganisation(
                        route.getId(), organisation.getId());
                addButton.setDisable(isAssigned);
                if (isAssigned) {
                    addButton.setText("Déjà ajouté");
                    addButton.getStyleClass().remove("button-primary");
                    addButton.getStyleClass().add("button-secondary");
                }
            } catch (Exception ex) {
                log.error("Error checking if route is assigned to organisation for button state", ex);
                // Don't disable the button if we can't check - let the user try to add it
            }
        }

        actionsContainer.getChildren().add(addButton);

        // Add all components to the card
        card.getChildren().addAll(iconContainer, detailsContainer, actionsContainer);

        return card;
    }

    /**
     * Refresh the available routes table based on the currently selected transport type
     */
    private void refreshAvailableRoutes() {
        String transportType = searchTransportTypeComboBox.getValue();
        String origin = originComboBox.getValue();
        String destination = destinationComboBox.getValue();

        log.info("Refreshing available routes with criteria - Transport: {}, Origin: {}, Destination: {}", 
                transportType, origin, destination);

        // Perform a search with current criteria
        searchRoutes();
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
        log.info("Updating cities for transport type: {}", transportType);

        if (transportType == null || transportType.equals("Tous")) {
            loadCities();
            return;
        }

        // Get all routes
        List<Route> allRoutes = routeService.afficher_tout();
        if (allRoutes.isEmpty()) {
            log.warn("No routes found in database when filtering by transport type");
            showAlert(Alert.AlertType.WARNING, "Aucun itinéraire", 
                    "Aucun itinéraire n'a été trouvé dans la base de données.");
            return;
        }

        // Use case-insensitive comparison
        List<Route> filteredRoutes = allRoutes.stream()
            .filter(route -> {
                // Handle null transport modes safely
                if (route.getTransportMode() == null) {
                    return false;
                }
                return route.getTransportMode().equalsIgnoreCase(transportType);
            })
            .collect(Collectors.toList());

        log.info("Filtered routes by transport type '{}': found {} routes", transportType, filteredRoutes.size());

        if (filteredRoutes.isEmpty()) {
            log.warn("No routes found for transport type: {}", transportType);
            // Keep previous values but show warning
            showAlert(Alert.AlertType.INFORMATION, "Aucun itinéraire trouvé", 
                    "Aucun itinéraire n'a été trouvé pour le type de transport '" + transportType + "'.");
            return;
        }

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

        log.info("Updated city options: {} origins and {} destinations for transport type '{}'", 
                origins.size(), destinations.size(), transportType);
    }

    @FXML
    private void searchRoutes() {
        // Get organization from context if it's null
        if (organisation == null) {
            organisation = OrganisationContext.getInstance().getCurrentOrganisation();
            if (organisation == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Organisation non définie. Veuillez vous reconnecter.");
                return;
            }
        }

        String transportType = searchTransportTypeComboBox.getValue();
        String origin = originComboBox.getValue();
        String destination = destinationComboBox.getValue();

        log.info("Searching routes with criteria - Transport: {}, Origin: {}, Destination: {}", 
                transportType, origin, destination);

        // Use the enhanced searchRoutes method from RouteService
        List<Route> routes;

        // Handle "Tous" values by converting them to null for the search method
        String searchOrigin = (origin != null && !origin.equals("Tous")) ? origin : null;
        String searchDestination = (destination != null && !destination.equals("Tous")) ? destination : null;

        // If both origin and destination are "Tous", get all routes
        if (searchOrigin == null && searchDestination == null) {
            routes = routeService.afficher_tout();
            log.info("No specific origin/destination - displaying all {} routes", routes.size());
        } else {
            // Use the improved search method for better results
            routes = routeService.searchRoutes(searchOrigin, searchDestination);

            if (searchOrigin != null && searchDestination != null) {
                log.info("Searched for routes from {} to {}, found {}", origin, destination, routes.size());
            } else if (searchOrigin != null) {
                log.info("Searched for routes from {}, found {}", origin, routes.size());
            } else {
                log.info("Searched for routes to {}, found {}", destination, routes.size());
            }
        }

        // Apply transport type filter (use case-insensitive comparison)
        if (transportType != null && !transportType.equals("Tous")) {
            routes = routes.stream()
                .filter(route -> {
                    if (route.getTransportMode() == null) {
                        return false;
                    }
                    return route.getTransportMode().equalsIgnoreCase(transportType);
                })
                .collect(Collectors.toList());
            log.info("After transport type filter ({}): {} routes", transportType, routes.size());
        }

        // Filter out routes already assigned to the organization
        if (organisation != null) {
            final List<Integer> assignedRouteIds = organisationRouteService
                .findByOrganisationId(organisation.getId())
                .stream()
                .map(OrganisationRoute::getRouteId)
                .collect(Collectors.toList());

            if (!assignedRouteIds.isEmpty()) {
                routes = routes.stream()
                    .filter(route -> !assignedRouteIds.contains(route.getId()))
                    .collect(Collectors.toList());
                log.info("After filtering out assigned routes: {} routes remain", routes.size());
            }
        }

        // Update table (kept for compatibility)
        availableRoutes.clear();
        availableRoutes.addAll(routes);

        // Update the modern UI container
        try {
            if (availableRoutesContainer != null) {
                availableRoutesContainer.getChildren().clear();

                if (routes.isEmpty()) {
                    Label noRoutesLabel = new Label("Aucun itinéraire trouvé");
                    noRoutesLabel.getStyleClass().add("no-results-label");
                    availableRoutesContainer.getChildren().add(noRoutesLabel);
                } else {
                    // Add route cards to the container
                    for (Route route : routes) {
                        availableRoutesContainer.getChildren().add(createAvailableRouteCard(route));
                    }
                }
            } else {
                log.warn("availableRoutesContainer is null in searchRoutes, cannot update UI");
            }
        } catch (Exception ex) {
            log.error("Error updating availableRoutesContainer in searchRoutes", ex);
            // Continue execution - don't let UI update failure prevent data loading
        }

        // Log some sample routes for debugging
        if (!routes.isEmpty()) {
            int sampleSize = Math.min(routes.size(), 3);
            for (int i = 0; i < sampleSize; i++) {
                Route route = routes.get(i);
                log.info("Sample route {}: {} → {} ({}), {} km", 
                        i+1, route.getOrigin(), route.getDestination(), 
                        route.getTransportMode(), route.getDistance());
            }
        } else {
            log.warn("No routes found matching criteria - transportType: {}, origin: {}, destination: {}", 
                    transportType, origin, destination);

            // Log a sample of all routes to help diagnose the issue
            List<Route> allRoutes = routeService.afficher_tout();
            if (!allRoutes.isEmpty()) {
                log.info("Sample of all available routes in database:");
                int sampleSize = Math.min(allRoutes.size(), 5);
                for (int i = 0; i < sampleSize; i++) {
                    Route route = allRoutes.get(i);
                    log.info("  Route {}: {} → {} ({}), {} km", 
                            i+1, route.getOrigin(), route.getDestination(), 
                            route.getTransportMode(), route.getDistance());
                }

                // Check if database has routes but they're not showing up due to organization assignments
                if (organisation != null) {
                    log.info("Checking if organisation {} already has these routes assigned", organisation.getId());
                    List<OrganisationRoute> orgRoutes = organisationRouteService.findByOrganisationId(organisation.getId());
                    if (!orgRoutes.isEmpty()) {
                        log.info("Organisation has {} routes already assigned", orgRoutes.size());
                        for (int i = 0; i < Math.min(orgRoutes.size(), 3); i++) {
                            OrganisationRoute orgRoute = orgRoutes.get(i);
                            Route route = routeService.afficher(orgRoute.getRouteId());
                            if (route != null) {
                                log.info("  Already assigned route {}: {} → {}", 
                                        i+1, route.getOrigin(), route.getDestination());
                            }
                        }
                    }
                }
            }

            // Show feedback to the user
            showAlert(Alert.AlertType.INFORMATION, "Aucun itinéraire trouvé", 
                    "Aucun itinéraire ne correspond aux critères de recherche.\n\n" +
                    "Essayez de modifier les critères de recherche ou utilisez le bouton 'Réinitialiser les filtres'.");
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
            dialogStage.initOwner(myRoutesContainer.getScene().getWindow());
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
            dialogStage.initOwner(myRoutesContainer.getScene().getWindow());
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

    @FXML
    private void resetFilters() {
        log.info("Resetting search filters");

        // Reset all search filters
        searchTransportTypeComboBox.setValue("Tous");
        originComboBox.setValue("Tous");
        destinationComboBox.setValue("Tous");

        // Load all available routes (this will reload unfiltered data)
        loadAvailableRoutes();

        // Refresh the table display
        if (availableRoutesTable != null) {
            availableRoutesTable.refresh();
        }

        log.info("Search filters reset and routes reloaded");
    }
} 
