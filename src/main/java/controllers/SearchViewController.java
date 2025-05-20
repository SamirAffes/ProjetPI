package controllers;

import entities.Route;
import entities.Transport;
import entities.User;
import entities.Organisation;
import entities.OrganisationRoute;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.Node;
import controllers.WeatherWidgetController;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.OrganisationRouteService;
import services.ReservationService;
import services.ReservationServiceImpl;
import services.RouteService;
import services.TransportService;
import services.OrganisationService;
import utils.AutoCompleteTextField;
import utils.UserContext;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the search view with map integration
 */
@Slf4j
public class SearchViewController implements Initializable {

    @FXML private AutoCompleteTextField departureTextField;
    @FXML private AutoCompleteTextField arrivalTextField;
    @FXML private DatePicker departureDatePicker;
    @FXML private TextField passengersTextField;
    @FXML private ComboBox<String> transportTypeComboBox;
    @FXML private ComboBox<String> departureTimeComboBox;
    @FXML private Button searchButton;
    @FXML private VBox searchResultsContainer;
    @FXML private WebView mapWebView;
    @FXML private VBox itinerariesContainer;

    private WebEngine webEngine;
    private final RouteService routeService = new RouteService();
    private final TransportService transportService = new TransportService();
    private final ReservationService reservationService = new ReservationServiceImpl();
    private final OrganisationService organisationService = new OrganisationService();
    private final OrganisationRouteService organisationRouteService = new OrganisationRouteService();

    /**
     * Loads locations from the database and populates the CITY_COORDINATES map
     */
    private void loadLocationsFromDatabase() {
        log.info("Loading locations from database");

        // Clear existing coordinates
        CITY_COORDINATES.clear();

        // Get all locations from the database
        List<String> dbLocations = routeService.getAllLocations();
        log.info("Found {} locations in database", dbLocations.size());

        // For each location, add coordinates from DEFAULT_COORDINATES if available
        for (String location : dbLocations) {
            if (DEFAULT_COORDINATES.containsKey(location)) {
                CITY_COORDINATES.put(location, DEFAULT_COORDINATES.get(location));
                log.debug("Added coordinates for location: {}", location);
            } else {
                // For locations without predefined coordinates, use default Tunisia coordinates
                // In a real app, you might want to use a geocoding service here
                CITY_COORDINATES.put(location, new double[]{DEFAULT_LATITUDE, DEFAULT_LONGITUDE});
                log.debug("Using default coordinates for location: {}", location);
            }
        }

        // If no locations were found in the database, use the default coordinates
        if (CITY_COORDINATES.isEmpty()) {
            log.warn("No locations found in database, using default coordinates");
            CITY_COORDINATES.putAll(DEFAULT_COORDINATES);
        }

        log.info("Loaded {} locations with coordinates", CITY_COORDINATES.size());
    }
    private User currentUser;

    // Default coordinates for Tunisia center
    private final double DEFAULT_LATITUDE = 36.8065;
    private final double DEFAULT_LONGITUDE = 10.1815;
    private final int DEFAULT_ZOOM = 7;

    // City coordinates mapping for locations found in routes
    // This will be populated from the database
    private final java.util.Map<String, double[]> CITY_COORDINATES = new java.util.HashMap<>();

    // Default coordinates for common cities (fallback if not in database)
    private final java.util.Map<String, double[]> DEFAULT_COORDINATES = new java.util.HashMap<>() {{
        // Major Tunisian cities
        put("Tunis", new double[]{36.8065, 10.1815});
        put("Sfax", new double[]{34.7398, 10.7600});
        put("Sousse", new double[]{35.8245, 10.6346});
        put("Kairouan", new double[]{35.6781, 10.0959});
        put("Bizerte", new double[]{37.2746, 9.8732});
        put("Gabès", new double[]{33.8812, 10.0982});
        put("Ariana", new double[]{36.8625, 10.1956});
        put("Gafsa", new double[]{34.4311, 8.7757});
        put("Monastir", new double[]{35.7780, 10.8262});
        put("Nabeul", new double[]{36.4562, 10.7310});
        put("Ben Arous", new double[]{36.7528, 10.2320});
        put("La Marsa", new double[]{36.8842, 10.3249});
        put("Kasserine", new double[]{35.1691, 8.8309});
        put("Douz", new double[]{33.4665, 9.0233});
        put("Tozeur", new double[]{33.9197, 8.1335});
        put("Tataouine", new double[]{32.9298, 10.4509});
        put("Hammamet", new double[]{36.4022, 10.6122});
        put("Mahdia", new double[]{35.5047, 11.0622});
        put("Medenine", new double[]{33.3399, 10.5017});
        put("Tabarka", new double[]{36.9542, 8.7600});

        // Locations within Tunis
        put("Tunis - Centre Ville", new double[]{36.7992, 10.1802});
        put("Tunis - Lac 1", new double[]{36.8327, 10.2352});
        put("Tunis - Lac 2", new double[]{36.8425, 10.2562});
        put("Tunis - La Goulette", new double[]{36.8183, 10.3050});
        put("Tunis - Carthage", new double[]{36.8583, 10.3236});
        put("Tunis - Sidi Bou Said", new double[]{36.8688, 10.3416});
        put("Tunis - Bardo", new double[]{36.8092, 10.1394});
        put("Tunis - El Menzah", new double[]{36.8361, 10.1689});
        put("Tunis - Aéroport", new double[]{36.8514, 10.2271});

        // International destinations
        put("Paris (France)", new double[]{48.8566, 2.3522});
        put("Marseille (France)", new double[]{43.2965, 5.3698});
        put("Rome (Italie)", new double[]{41.9028, 12.4964});
        put("Barcelone (Espagne)", new double[]{41.3851, 2.1734});
        put("Alger (Algérie)", new double[]{36.7538, 3.0588});
    }};

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing SearchViewController");

        // Get current user from context
        currentUser = UserContext.getInstance().getCurrentUser();
        if (currentUser == null) {
            log.error("No user found in SearchViewController");
        } else {
            log.info("User loaded in SearchViewController: {}", currentUser.getUsername());
        }

        // Load locations from database
        loadLocationsFromDatabase();

        // Set default date to today
        departureDatePicker.setValue(LocalDate.now());

        // Initialize time options
        setupTimeOptions();

        // Setup transport type combo box with icons
        setupTransportTypeComboBox();

        // Initialize the autocomplete text fields
        departureTextField.setMaxEntries(10);
        arrivalTextField.setMaxEntries(10);

        // Add onAction handlers to make Enter key trigger search
        departureTextField.setOnAction(event -> handleSearch());
        arrivalTextField.setOnAction(event -> handleSearch());
        passengersTextField.setOnAction(event -> handleSearch());

        // Update destinations based on default transport type
        updateLocationsByTransportType(transportTypeComboBox.getValue());

        // Add listener to transport type to filter destinations
        transportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateLocationsByTransportType(newVal);
            }
        });

        // Set up search button action
        searchButton.setOnAction(event -> handleSearch());

        // Initialize map after UI is fully loaded
        Platform.runLater(this::initializeMap);

        // Add listener for window resize to adjust map
        mapWebView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        Stage stage = (Stage) newWindow;
                        stage.widthProperty().addListener((obs3, oldWidth, newWidth) -> resizeMap());
                        stage.heightProperty().addListener((obs3, oldHeight, newHeight) -> resizeMap());
                    }
                });
            }
        });
    }

    /**
     * Updates the available locations based on the selected transport type
     * Uses the database to get locations for the selected transport type
     */
    private void updateLocationsByTransportType(String transportType) {
        // Save current values if any
        String currentDeparture = departureTextField.getText();
        String currentArrival = arrivalTextField.getText();

        // Get locations from database based on transport type
        List<String> filteredLocations = routeService.getLocationsByTransportMode(transportType);

        // If no locations found for this transport type, try to use all locations
        if (filteredLocations.isEmpty()) {
            log.warn("No locations found for transport type: {}, using all locations", transportType);
            filteredLocations = new ArrayList<>(CITY_COORDINATES.keySet());
            Collections.sort(filteredLocations);
        }

        // For departure and arrival, we use the same list of locations
        List<String> filteredDepartures = new ArrayList<>(filteredLocations);
        List<String> filteredArrivals = new ArrayList<>(filteredLocations);

        // Update the prompt text to reflect the transport type
        String departurePrompt = "Saisir une ville de départ";
        String arrivalPrompt = "Saisir une ville d'arrivée";

        switch (transportType) {
            case "Avion":
                departurePrompt = "Saisir un aéroport de départ";
                arrivalPrompt = "Saisir un aéroport d'arrivée";
                break;
            case "Ferry":
                departurePrompt = "Saisir un port de départ";
                arrivalPrompt = "Saisir un port d'arrivée";
                break;
            case "Train":
                departurePrompt = "Saisir une gare de départ";
                arrivalPrompt = "Saisir une gare d'arrivée";
                break;
            case "Métro":
                departurePrompt = "Saisir une station de départ";
                arrivalPrompt = "Saisir une station d'arrivée";
                break;
            case "TGM":
                departurePrompt = "Saisir une station de départ";
                arrivalPrompt = "Saisir une station d'arrivée";
                break;
            case "Taxi":
                departurePrompt = "Saisir un lieu de départ";
                arrivalPrompt = "Saisir un lieu d'arrivée";
                break;
            case "Bus":
                departurePrompt = "Saisir un arrêt de départ";
                arrivalPrompt = "Saisir un arrêt d'arrivée";
                break;
            default: // "Tous" or any other value
                // Default prompts are already set
                break;
        }

        // Update autocomplete entries
        departureTextField.setEntries(filteredDepartures);
        arrivalTextField.setEntries(filteredArrivals);

        // Update prompt text
        departureTextField.setPromptText(departurePrompt);
        arrivalTextField.setPromptText(arrivalPrompt);

        // Keep previous values if they're still valid
        if (!currentDeparture.isEmpty() && filteredDepartures.contains(currentDeparture)) {
            departureTextField.setText(currentDeparture);
        } else {
            departureTextField.clear();
        }

        if (!currentArrival.isEmpty() && filteredArrivals.contains(currentArrival)) {
            arrivalTextField.setText(currentArrival);
        } else {
            arrivalTextField.clear();
        }

        // Log the number of available options for debugging
        log.info("Transport type '{}' selected: {} departure options, {} arrival options", 
                transportType, filteredDepartures.size(), filteredArrivals.size());
    }

    private void initializeMap() {
        log.info("Initializing map component in SearchViewController");
        try {
            if (mapWebView == null) {
                log.error("WebView component is null - check FXML file");
                return;
            }

            // Set up WebView and WebEngine
            webEngine = mapWebView.getEngine();

            // Make WebView responsive to container size
            mapWebView.prefWidthProperty().bind(((StackPane) mapWebView.getParent()).widthProperty());
            mapWebView.prefHeightProperty().bind(((StackPane) mapWebView.getParent()).heightProperty());

            // Generate HTML content for OpenStreetMap with Leaflet.js
            String mapContent = generateMapHtml();
            webEngine.loadContent(mapContent);

            // Handle loading errors and success
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.FAILED) {
                    log.error("Failed to load map content");
                    showError("Map Loading Error", "Failed to load the map. Please check your internet connection.");
                } else if (newState == Worker.State.SUCCEEDED) {
                    log.info("Map loaded successfully");
                    // Center map on Tunisia
                    panToLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ZOOM);

                    // Add resize handler to ensure map fills container
                    mapWebView.getParent().layoutBoundsProperty().addListener((ov, oldBounds, newBounds) -> resizeMap());
                }
            });

            log.info("Map initialization complete");
        } catch (Exception e) {
            log.error("Error initializing map", e);
            showError("Map Error", "Could not initialize map: " + e.getMessage());
        }
    }

    private String generateMapHtml() {
        return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <title>TunTransport Map</title>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
            <script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.js"></script>
            <link rel="stylesheet" href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css" />
            <style>
                html, body, #map {
                    height: 100%%;
                    width: 100%%;
                    margin: 0;
                    padding: 0;
                }
                .route-info {
                    padding: 10px;
                    background-color: white;
                    border-radius: 5px;
                    box-shadow: 0 0 5px rgba(0,0,0,0.3);
                }
                .leaflet-routing-container {
                    background-color: white;
                    padding: 10px;
                    border-radius: 5px;
                    box-shadow: 0 0 5px rgba(0,0,0,0.3);
                    max-width: 320px;
                    max-height: 300px;
                    overflow-y: auto;
                }
                .leaflet-routing-alt {
                    max-height: none;
                }
                .origin-marker {
                    background-color: #4285F4;
                    border: 3px solid white;
                    border-radius: 50%%;
                    box-shadow: 0 0 3px rgba(0,0,0,0.4);
                }
                .destination-marker {
                    background-color: #EA4335;
                    border: 3px solid white;
                    border-radius: 50%%;
                    box-shadow: 0 0 3px rgba(0,0,0,0.4);
                }
                .waypoint-marker {
                    background-color: #FBBC05;
                    border: 3px solid white;
                    border-radius: 50%%;
                    box-shadow: 0 0 3px rgba(0,0,0,0.4);
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([%f, %f], %d);
                var routeControl = null;
                var markersLayer = L.layerGroup().addTo(map);

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                }).addTo(map);

                // Function to be called from Java to pan/zoom the map
                function panToLocation(lat, lng, zoom) {
                    map.setView([lat, lng], zoom);
                }

                // Function to add a marker with a popup
                function addMarker(lat, lng, title, isOrigin) {
                    var icon = L.divIcon({
                        className: isOrigin ? 'origin-marker' : 'destination-marker',
                        html: `<div style="background-color: ${isOrigin ? '#4285F4' : '#EA4335'}; width: 14px; height: 14px; border-radius: 50%%; border: 3px solid white; box-shadow: 0 0 3px rgba(0,0,0,0.4);"></div>`,
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                    });

                    var marker = L.marker([lat, lng], {icon: icon})
                        .addTo(markersLayer)
                        .bindPopup(title);

                    marker.openPopup();
                    return marker;
                }

                // Function to clear all markers
                function clearMarkers() {
                    markersLayer.clearLayers();
                    if (routeControl) {
                        map.removeControl(routeControl);
                        routeControl = null;
                    }
                }

                // Function to draw a route between two points with waypoints
                function drawRoute(originLat, originLng, destLat, destLng, routeInfo) {
                    // Clear previous route if exists
                    if (routeControl) {
                        map.removeControl(routeControl);
                    }

                    // Generate intermediate waypoints for a more realistic route
                    var waypoints = generateWaypoints(originLat, originLng, destLat, destLng);

                    // Create waypoints array for routing machine
                    var routingWaypoints = [
                        L.latLng(originLat, originLng),
                        ...waypoints.map(wp => L.latLng(wp.lat, wp.lng)),
                        L.latLng(destLat, destLng)
                    ];

                    // Add the route using Leaflet Routing Machine
                    routeControl = L.Routing.control({
                        waypoints: routingWaypoints,
                        routeWhileDragging: false,
                        showAlternatives: false,
                        fitSelectedRoutes: true,
                        lineOptions: {
                            styles: [
                                {color: '#4285F4', opacity: 0.8, weight: 6},
                                {color: '#ffffff', opacity: 0.3, weight: 4}
                            ]
                        },
                        createMarker: function() { return null; }, // We'll add our own markers
                        addWaypoints: false,
                        draggableWaypoints: false
                    }).addTo(map);

                    // Add waypoint markers
                    waypoints.forEach((wp, index) => {
                        addWaypointMarker(wp.lat, wp.lng, "Point intermédiaire " + (index + 1));
                    });

                    // Add route info popup in the middle of the route
                    if (routeInfo) {
                        var midWaypoint = waypoints[Math.floor(waypoints.length / 2)] || 
                                         {lat: (originLat + destLat) / 2, lng: (originLng + destLng) / 2};

                        L.popup()
                            .setLatLng([midWaypoint.lat, midWaypoint.lng])
                            .setContent('<div class="route-info">' + routeInfo + '</div>')
                            .openOn(map);
                    }
                }

                // Function to add a waypoint marker
                function addWaypointMarker(lat, lng, title) {
                    var icon = L.divIcon({
                        className: 'waypoint-marker',
                        html: `<div style="background-color: #FBBC05; width: 10px; height: 10px; border-radius: 50%%; border: 2px solid white; box-shadow: 0 0 3px rgba(0,0,0,0.4);"></div>`,
                        iconSize: [14, 14],
                        iconAnchor: [7, 7]
                    });

                    L.marker([lat, lng], {icon: icon})
                        .addTo(markersLayer)
                        .bindPopup(title);
                }

                // Generate realistic waypoints between origin and destination
                function generateWaypoints(originLat, originLng, destLat, destLng) {
                    var waypoints = [];
                    var numWaypoints = getRandomInt(2, 5); // Random number of waypoints
                    var directLine = {
                        x: destLat - originLat,
                        y: destLng - originLng
                    };
                    var distance = Math.sqrt(directLine.x * directLine.x + directLine.y * directLine.y);

                    // Generate waypoints along the path with some randomness
                    for (var i = 1; i <= numWaypoints; i++) {
                        var ratio = i / (numWaypoints + 1);

                        // Add some randomness to the waypoint position
                        var perpendicular = {
                            x: -directLine.y / distance,
                            y: directLine.x / distance
                        };

                        var randomOffset = (Math.random() - 0.5) * 0.05; // Random offset perpendicular to direct path

                        var waypoint = {
                            lat: originLat + directLine.x * ratio + perpendicular.x * randomOffset,
                            lng: originLng + directLine.y * ratio + perpendicular.y * randomOffset
                        };

                        waypoints.push(waypoint);
                    }

                    return waypoints;
                }

                // Helper function to get random integer between min and max (inclusive)
                function getRandomInt(min, max) {
                    return Math.floor(Math.random() * (max - min + 1)) + min;
                }
            </script>
        </body>
        </html>
        """, DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ZOOM);
    }

    // Helper method to pan/zoom the map
    private void panToLocation(double lat, double lng, int zoom) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(String.format("panToLocation(%f, %f, %d)", lat, lng, zoom));
        }
    }

    // Helper method to add a marker
    private void addMarker(double lat, double lng, String title, boolean isOrigin) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(String.format("addMarker(%f, %f, '%s', %b)", lat, lng, title, isOrigin));
        }
    }

    // Helper method to clear markers
    private void clearMarkers() {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript("clearMarkers()");
        }
    }

    // Helper method to draw a route between two points
    private void drawRoute(double originLat, double originLng, double destLat, double destLng, String routeInfo) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            // Calculate distance and format it
            double distance = calculateDistance(originLat, originLng, destLat, destLng);
            int duration = calculateDuration(distance);

            // Create a more detailed route info with HTML formatting
            String enhancedRouteInfo = String.format(
                "<div style='font-size: 14px;'>" +
                "<strong>%s</strong><br/>" +
                "<span style='color: #555;'>Distance: %.1f km</span><br/>" +
                "<span style='color: #555;'>Durée estimée: %s</span>" +
                "</div>",
                routeInfo.replace("<br>", "").replace("<br/>", ""), 
                distance, 
                formatDuration(duration)
            );

            webEngine.executeScript(String.format(
                "drawRoute(%f, %f, %f, %f, '%s')", 
                originLat, originLng, destLat, destLng, enhancedRouteInfo.replace("'", "\\'"))
            );
        }
    }

    private void setupTimeOptions() {
        if (departureTimeComboBox != null) {
            // Populate time options at 30-minute intervals
            ObservableList<String> timeOptions = FXCollections.observableArrayList();
            for (int hour = 6; hour < 22; hour++) {
                timeOptions.add(String.format("%02d:00", hour));
                timeOptions.add(String.format("%02d:30", hour));
            }

            departureTimeComboBox.setItems(timeOptions);
            departureTimeComboBox.setValue("08:00"); // Default time
        }
    }

    /**
     * Sets up the transport type combo box with icons
     */
    private void setupTransportTypeComboBox() {
        // Clear any existing items
        transportTypeComboBox.getItems().clear();

        // Create a custom cell factory with icons
        transportTypeComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Don't set text directly as it will be in the Label
                    // But for the button cell, we need to ensure the text is visible
                    setText("");

                    // Create icon based on transport type
                    FontIcon icon = new FontIcon();
                    icon.setIconSize(16);

                    switch (item) {
                        case "Bus":
                            icon.setIconLiteral("fas-bus");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                            break;
                        case "Train":
                            icon.setIconLiteral("fas-train");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#e74c3c"));
                            break;
                        case "Taxi":
                            icon.setIconLiteral("fas-taxi");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#f1c40f"));
                            break;
                        case "Métro":
                            icon.setIconLiteral("fas-subway");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#9b59b6"));
                            break;
                        case "TGM":
                            icon.setIconLiteral("fas-tram");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#2ecc71"));
                            break;
                        case "Avion":
                            icon.setIconLiteral("fas-plane");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                            break;
                        case "Ferry":
                            icon.setIconLiteral("fas-ship");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#2980b9"));
                            break;
                        case "Tous":
                        default:
                            icon.setIconLiteral("fas-globe");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#7f8c8d"));
                            break;
                    }

                    // Add some spacing between icon and text
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    hbox.getChildren().addAll(icon, new Label(item));
                    setGraphic(hbox);
                }
            }
        });

        // Create a specific button cell that will properly show both icon and text
        transportTypeComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Set the text directly for the button cell
                    setText(item);

                    // Create icon based on transport type
                    FontIcon icon = new FontIcon();
                    icon.setIconSize(16);

                    switch (item) {
                        case "Bus":
                            icon.setIconLiteral("fas-bus");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                            break;
                        case "Train":
                            icon.setIconLiteral("fas-train");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#e74c3c"));
                            break;
                        case "Taxi":
                            icon.setIconLiteral("fas-taxi");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#f1c40f"));
                            break;
                        case "Métro":
                            icon.setIconLiteral("fas-subway");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#9b59b6"));
                            break;
                        case "TGM":
                            icon.setIconLiteral("fas-tram");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#2ecc71"));
                            break;
                        case "Avion":
                            icon.setIconLiteral("fas-plane");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                            break;
                        case "Ferry":
                            icon.setIconLiteral("fas-ship");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#2980b9"));
                            break;
                        case "Tous":
                        default:
                            icon.setIconLiteral("fas-globe");
                            icon.setIconColor(javafx.scene.paint.Color.valueOf("#7f8c8d"));
                            break;
                    }

                    // Set the icon as the graphic
                    setGraphic(icon);
                }
            }
        });

        // Add the transport types
        transportTypeComboBox.getItems().addAll(
            "Bus", "Train", "Taxi", "Métro", "TGM", "Avion", "Ferry", "Tous"
        );

        // Set default value
        transportTypeComboBox.setValue("Tous");

        // Add listener to transport type to filter destinations
        transportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateLocationsByTransportType(newVal);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String transportType = transportTypeComboBox.getValue();
        String departure = departureTextField.getText();
        String arrival = arrivalTextField.getText();
        LocalDate date = departureDatePicker.getValue();
        String departureTime = departureTimeComboBox.getValue();
        String passengers = passengersTextField.getText();

        log.info("Search request - Transport: {}, From: {}, To: {}, Date: {}, Time: {}, Passengers: {}", 
                transportType, departure, arrival, date, departureTime, passengers);

        // Update weather widget with departure city
        updateWeatherWidget(departure);

        // Validate inputs in the correct order
        if (transportType == null) {
            showError("Validation Error", "Veuillez sélectionner un type de transport.");
            transportTypeComboBox.requestFocus();
            return;
        }

        if (departure == null || departure.isEmpty()) {
            showError("Validation Error", "Veuillez saisir un lieu de départ.");
            departureTextField.requestFocus();
            return;
        }

        // Check if the entered departure location is valid
        if (!CITY_COORDINATES.containsKey(departure)) {
            showError("Validation Error", "Le lieu de départ saisi n'est pas reconnu. Veuillez sélectionner un lieu dans la liste.");
            departureTextField.requestFocus();
            return;
        }

        if (arrival == null || arrival.isEmpty()) {
            showError("Validation Error", "Veuillez saisir un lieu d'arrivée.");
            arrivalTextField.requestFocus();
            return;
        }

        // Check if the entered arrival location is valid
        if (!CITY_COORDINATES.containsKey(arrival)) {
            showError("Validation Error", "Le lieu d'arrivée saisi n'est pas reconnu. Veuillez sélectionner un lieu dans la liste.");
            arrivalTextField.requestFocus();
            return;
        }

        if (departure.equals(arrival)) {
            showError("Validation Error", "Les lieux de départ et d'arrivée ne peuvent pas être identiques.");
            arrivalTextField.requestFocus();
            return;
        }

        if (date == null) {
            showError("Validation Error", "Veuillez sélectionner une date de départ.");
            departureDatePicker.requestFocus();
            return;
        }

        if (departureTime == null) {
            showError("Validation Error", "Veuillez sélectionner une heure de départ.");
            departureTimeComboBox.requestFocus();
            return;
        }

        // Validate passengers field
        if (passengers == null || passengers.trim().isEmpty()) {
            showError("Validation Error", "Veuillez indiquer le nombre de passagers.");
            passengersTextField.requestFocus();
            return;
        }

        try {
            int numPassengers = Integer.parseInt(passengers);
            if (numPassengers <= 0) {
                showError("Validation Error", "Le nombre de passagers doit être supérieur à 0.");
                passengersTextField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Le nombre de passagers doit être un nombre entier.");
            passengersTextField.requestFocus();
            return;
        }

        // Clear previous results
        searchResultsContainer.getChildren().clear();
        itinerariesContainer.getChildren().clear();

        // Clear previous markers and routes
        clearMarkers();

        // Get coordinates for cities
        double[] departureCoords = CITY_COORDINATES.get(departure);
        double[] arrivalCoords = CITY_COORDINATES.get(arrival);

        // If coordinates not found in our map, check if they exist in DEFAULT_COORDINATES
        if (departureCoords == null) {
            departureCoords = DEFAULT_COORDINATES.get(departure);
            if (departureCoords == null) {
                log.warn("No coordinates found for departure location: {}", departure);
                // Use default coordinates for Tunisia
                departureCoords = new double[]{DEFAULT_LATITUDE, DEFAULT_LONGITUDE};
                // Add to CITY_COORDINATES for future use
                CITY_COORDINATES.put(departure, departureCoords);
            }
        }

        if (arrivalCoords == null) {
            arrivalCoords = DEFAULT_COORDINATES.get(arrival);
            if (arrivalCoords == null) {
                log.warn("No coordinates found for arrival location: {}", arrival);
                // Use default coordinates for Tunisia with slight offset to avoid overlap
                arrivalCoords = new double[]{DEFAULT_LATITUDE + 0.05, DEFAULT_LONGITUDE + 0.05};
                // Add to CITY_COORDINATES for future use
                CITY_COORDINATES.put(arrival, arrivalCoords);
            }
        }

        // Add markers for departure and arrival
        addMarker(departureCoords[0], departureCoords[1], "Départ: " + departure, true);
        addMarker(arrivalCoords[0], arrivalCoords[1], "Arrivée: " + arrival, false);

        // Calculate distance and duration
        double distance = calculateDistance(departureCoords[0], departureCoords[1], 
                                           arrivalCoords[0], arrivalCoords[1]);
        int durationMinutes = calculateDuration(distance);

        // Format departure time and date for display
        String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // Create a more Google Maps-like route info
        String routeInfo = String.format("%s → %s", departure, arrival);

        // Draw route with enhanced info (actual info is added in drawRoute method)
        drawRoute(departureCoords[0], departureCoords[1], arrivalCoords[0], arrivalCoords[1], routeInfo);

        // Find routes in database that match the search criteria
        List<Route> routes = findRoutes(departure, arrival);

        // Display results
        if (routes.isEmpty()) {
            Label noResultsLabel = new Label("Aucun itinéraire disponible pour les critères sélectionnés.");
            noResultsLabel.getStyleClass().add("no-results-label");
            searchResultsContainer.getChildren().add(noResultsLabel);
        } else {
            // Create a more prominent title for available routes
            HBox titleBox = new HBox();
            titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            titleBox.setSpacing(10);
            titleBox.setPadding(new Insets(5, 0, 15, 0));

            // Add journey summary at the top (Google Maps style)
            VBox journeySummary = new VBox();
            journeySummary.setSpacing(5);
            journeySummary.setPadding(new Insets(10, 15, 15, 15));
            journeySummary.getStyleClass().add("journey-summary");
            journeySummary.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dadce0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

            // Add transport type icon to the journey title
            HBox journeyTitleBox = new HBox(10);
            journeyTitleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Create icon for the selected transport type
            FontIcon transportIcon = new FontIcon();
            transportIcon.setIconSize(20);

            switch (transportType) {
                case "Bus":
                    transportIcon.setIconLiteral("fas-bus");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                    break;
                case "Train":
                    transportIcon.setIconLiteral("fas-train");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#e74c3c"));
                    break;
                case "Taxi":
                    transportIcon.setIconLiteral("fas-taxi");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#f1c40f"));
                    break;
                case "Métro":
                    transportIcon.setIconLiteral("fas-subway");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#9b59b6"));
                    break;
                case "TGM":
                    transportIcon.setIconLiteral("fas-tram");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#2ecc71"));
                    break;
                case "Avion":
                    transportIcon.setIconLiteral("fas-plane");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                    break;
                case "Ferry":
                    transportIcon.setIconLiteral("fas-ship");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#2980b9"));
                    break;
                default:
                    transportIcon.setIconLiteral("fas-globe");
                    transportIcon.setIconColor(javafx.scene.paint.Color.valueOf("#7f8c8d"));
                    break;
            }

            Label journeyTitleLabel = new Label(departure + " → " + arrival);
            journeyTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            journeyTitleBox.getChildren().addAll(transportIcon, journeyTitleLabel);

            Label journeyDetails = new Label(String.format("%.1f km • %s • %s à %s • %s", 
                distance, formatDuration(durationMinutes), formattedDate, departureTime, transportType));
            journeyDetails.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 14px;");

            journeySummary.getChildren().addAll(journeyTitleBox, journeyDetails);
            searchResultsContainer.getChildren().add(journeySummary);

            // Add results count
            Label resultsLabel = new Label("Itinéraires disponibles (" + routes.size() + "):");
            resultsLabel.getStyleClass().add("results-label");
            resultsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

            FontIcon icon = new FontIcon("fas-route");
            icon.setIconSize(18);
            icon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));

            titleBox.getChildren().addAll(icon, resultsLabel);
            searchResultsContainer.getChildren().add(titleBox);

            // Create a header for the list of routes
            HBox headerBox = new HBox();
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            headerBox.setSpacing(20);
            headerBox.setPadding(new Insets(10, 10, 10, 10));
            headerBox.getStyleClass().add("route-list-header");

            Label routeHeaderLabel = new Label("Itinéraire");
            routeHeaderLabel.setMinWidth(150);
            routeHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label companyHeaderLabel = new Label("Compagnie");
            companyHeaderLabel.setMinWidth(120);
            companyHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label typeHeaderLabel = new Label("Type");
            typeHeaderLabel.setMinWidth(100);
            typeHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label distanceHeaderLabel = new Label("Distance");
            distanceHeaderLabel.setMinWidth(80);
            distanceHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label durationHeaderLabel = new Label("Durée");
            durationHeaderLabel.setMinWidth(80);
            durationHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label priceHeaderLabel = new Label("Prix (Base/Total)");
            priceHeaderLabel.setMinWidth(120);
            priceHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            Label actionHeaderLabel = new Label("Action");
            actionHeaderLabel.setMinWidth(120);
            actionHeaderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

            headerBox.getChildren().addAll(routeHeaderLabel, companyHeaderLabel, typeHeaderLabel,
                                           distanceHeaderLabel, durationHeaderLabel, 
                                          priceHeaderLabel, actionHeaderLabel);

            // Create a container for the routes list
            VBox routesListContainer = new VBox();
            routesListContainer.setSpacing(5);
            routesListContainer.getStyleClass().add("routes-list-container");

            // Parse passenger count
            int passengerCount = Integer.parseInt(passengers);

            // Display routes as list items
            for (Route route : routes) {
                routesListContainer.getChildren().add(createRouteListItem(route, transportType, departureTime, date, passengerCount));
            }

            searchResultsContainer.getChildren().addAll(headerBox, routesListContainer);
        }
    }

    private List<Route> findRoutes(String origin, String destination) {
        try {
            // Find routes in database using OrganisationRouteService
            List<OrganisationRoute> orgRoutes = organisationRouteService.findOrganisationRoutesByLocations(origin, destination);

            // Only use routes that have organizations
            if (orgRoutes.isEmpty()) {
                log.info("No organisation routes found for {}→{}", origin, destination);
                // No fallback to base routes without organizations
                return List.of(); // Return empty list
            }

            log.info("Found {} organisation routes for {}→{}", orgRoutes.size(), origin, destination);
            List<Route> routes = new ArrayList<>();

            // Get selected transport type filter
            String selectedTransportType = transportTypeComboBox.getValue();
            boolean filterByTransportType = selectedTransportType != null && !selectedTransportType.equals("Tous");

            for (OrganisationRoute orgRoute : orgRoutes) {
                // Get the basic route
                Route route = routeService.afficher(orgRoute.getRouteId());

                if (route != null) {
                    // Skip if transport type doesn't match the selected filter
                    if (filterByTransportType && !selectedTransportType.equals(route.getTransportMode())) {
                        log.debug("Skipping route {} due to transport type filter: {} vs {}", 
                                 route.getId(), selectedTransportType, route.getTransportMode());
                        continue;
                    }

                    // Get organisation info
                    Organisation org = organisationService.afficher(orgRoute.getOrganisationId());

                    if (org != null) {
                        // Set organisation info on the route
                        route.setCompanyId(org.getId());
                        route.setCompanyName(org.getNom());

                        // Use the organisation's custom price and duration if available
                        if (orgRoute.getCustomPrice() != null) {
                            route.setBasePrice(orgRoute.getCustomPrice());
                        }

                        if (orgRoute.getCustomDuration() != null) {
                            route.setEstimatedDuration(orgRoute.getCustomDuration());
                        }

                        routes.add(route);
                    }
                }
            }

            log.info("Returning {} filtered routes", routes.size());
            return routes;
        } catch (Exception e) {
            log.error("Error finding routes", e);
            return List.of(); // Return empty list instead of generating random routes
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two coordinates
        final int R = 6371; // Earth radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return Math.round(distance * 10.0) / 10.0; // Round to 1 decimal place
    }

    private int calculateDuration(double distance) {
        // Simple duration estimation: 1 hour per 80 km
        return (int) Math.ceil(distance / 80.0 * 60); // Duration in minutes
    }

    private double calculatePrice(double distance) {
        // Simple price calculation: 0.5 DT per km
        return Math.round(distance * 0.5 * 10.0) / 10.0; // Round to 1 decimal place
    }

    private HBox createRouteListItem(Route route, String transportType, String departureTime, LocalDate date, int passengerCount) {
        HBox item = new HBox();
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setSpacing(20);
        item.setPadding(new Insets(10, 10, 10, 10));
        item.getStyleClass().add("route-list-item");
        item.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        // Find the organization route data for this route
        OrganisationRoute orgRoute = null;
        Organisation organisation = null;

        if (route.getCompanyId() > 0) {
            organisation = organisationService.afficher(route.getCompanyId());
            if (organisation != null) {
                orgRoute = organisationRouteService.findByRouteAndOrganisation(route.getId(), organisation.getId());
                log.debug("Found organization route data for route {}: {}", route.getId(), orgRoute != null);
            }
        }

        // Route info with icon
        HBox routeBox = new HBox(5);
        routeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        routeBox.setMinWidth(150);

        // Create icon based on transport type
        FontIcon typeIcon = new FontIcon();
        typeIcon.setIconSize(16);

        switch (route.getTransportMode()) {
            case "Bus":
                typeIcon.setIconLiteral("fas-bus");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                break;
            case "Train":
                typeIcon.setIconLiteral("fas-train");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#e74c3c"));
                break;
            case "Taxi":
                typeIcon.setIconLiteral("fas-taxi");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#f1c40f"));
                break;
            case "Métro":
                typeIcon.setIconLiteral("fas-subway");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#9b59b6"));
                break;
            case "TGM":
                typeIcon.setIconLiteral("fas-tram");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#2ecc71"));
                break;
            case "Avion":
                typeIcon.setIconLiteral("fas-plane");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                break;
            case "Ferry":
                typeIcon.setIconLiteral("fas-ship");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#2980b9"));
                break;
            default:
                typeIcon.setIconLiteral("fas-route");
                typeIcon.setIconColor(javafx.scene.paint.Color.valueOf("#7f8c8d"));
                break;
        }

        Label routeLabel = new Label(route.getOrigin() + " → " + route.getDestination());
        routeBox.getChildren().addAll(typeIcon, routeLabel);

        // Company info
        Label companyLabel = new Label();
        companyLabel.setMinWidth(120);

        if (organisation != null) {
            // Use real organization data
            companyLabel.setText(organisation.getNom());
        } else if (route.getCompanyName() != null && !route.getCompanyName().isEmpty()) {
            // Use route's company info if available
            companyLabel.setText(route.getCompanyName());
        } else {
            // Fallback to placeholder
            companyLabel.setText("Transport Company");
        }

        // Transport type
        Label typeLabel = new Label(route.getTransportMode());
        typeLabel.setMinWidth(100);

        // Distance
        Label distanceLabel = new Label(String.format("%.1f km", route.getDistance()));
        distanceLabel.setMinWidth(80);

        // Duration 
        Label durationLabel = new Label(formatDuration(route.getEstimatedDuration()));
        durationLabel.setMinWidth(80);

        // Price - show base price and total price for all passengers
        double basePrice = route.getBasePrice();
        double totalPrice = basePrice * passengerCount;
        Label priceLabel = new Label(String.format("%.2f DT (Total: %.2f DT)", basePrice, totalPrice));
        priceLabel.setMinWidth(120);

        // Create a button container with info and book buttons
        VBox buttonContainer = new VBox(5);
        buttonContainer.setMinWidth(120);
        buttonContainer.setAlignment(javafx.geometry.Pos.CENTER);

        Button bookButton = new Button("Réserver");
        bookButton.getStyleClass().add("button-primary");
        bookButton.setMaxWidth(Double.MAX_VALUE);

        // Create time container with appropriate schedule information
        VBox timeContainer = new VBox(3);
        timeContainer.setAlignment(javafx.geometry.Pos.CENTER);

        // Get the appropriate schedule for the selected day
        String scheduleText = departureTime;
        if (orgRoute != null) {
            // Get the day of week (1=Monday, 7=Sunday)
            int dayOfWeek = date.getDayOfWeek().getValue();
            boolean isHoliday = false; // In a real app, you might check against a holiday calendar

            String schedule = null;
            if (isHoliday && orgRoute.getHolidaySchedule() != null) {
                schedule = orgRoute.getHolidaySchedule();
            } else if (dayOfWeek == 6 && orgRoute.getSaturdaySchedule() != null) { // Saturday
                schedule = orgRoute.getSaturdaySchedule();
            } else if (dayOfWeek == 7 && orgRoute.getSundaySchedule() != null) { // Sunday
                schedule = orgRoute.getSundaySchedule();
            } else if (orgRoute.getWeekdaySchedule() != null) {
                schedule = orgRoute.getWeekdaySchedule();
            }

            if (schedule != null) {
                scheduleText = schedule;
            } else if (orgRoute.getDepartureTime() != null && orgRoute.getArrivalTime() != null) {
                scheduleText = orgRoute.getDepartureTime() + " → " + orgRoute.getArrivalTime();
            }
        } else {
            // Calculate estimated arrival time based on departure and duration
            try {
                LocalTime departTime = LocalTime.parse(departureTime, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime arriveTime = departTime.plusMinutes(route.getEstimatedDuration());
                scheduleText = departTime.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                            " → " + arriveTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                log.error("Error calculating time: {}", e.getMessage());
            }
        }

        Label timeLabel = new Label(scheduleText);
        timeLabel.setStyle("-fx-font-size: 12px;");
        timeContainer.getChildren().add(timeLabel);

        // Add amenities if available from organisation route
        if (orgRoute != null) {
            HBox amenitiesBox = new HBox(5);
            amenitiesBox.setAlignment(javafx.geometry.Pos.CENTER);

            // Add amenity icons
            if (Boolean.TRUE.equals(orgRoute.getWifiAvailable())) {
                FontIcon wifiIcon = new FontIcon("fas-wifi");
                wifiIcon.setIconSize(12);
                wifiIcon.setIconColor(javafx.scene.paint.Color.valueOf("#3498db"));
                amenitiesBox.getChildren().add(wifiIcon);
            }

            if (Boolean.TRUE.equals(orgRoute.getAccessible())) {
                FontIcon accessibleIcon = new FontIcon("fas-wheelchair");
                accessibleIcon.setIconSize(12);
                accessibleIcon.setIconColor(javafx.scene.paint.Color.valueOf("#9b59b6"));
                amenitiesBox.getChildren().add(accessibleIcon);
            }

            if (Boolean.TRUE.equals(orgRoute.getAirConditioned())) {
                FontIcon acIcon = new FontIcon("fas-snowflake");
                acIcon.setIconSize(12);
                acIcon.setIconColor(javafx.scene.paint.Color.valueOf("#2ecc71"));
                amenitiesBox.getChildren().add(acIcon);
            }

            if (Boolean.TRUE.equals(orgRoute.getFoodService())) {
                FontIcon foodIcon = new FontIcon("fas-utensils");
                foodIcon.setIconSize(12);
                foodIcon.setIconColor(javafx.scene.paint.Color.valueOf("#e74c3c"));
                amenitiesBox.getChildren().add(foodIcon);
            }

            if (!amenitiesBox.getChildren().isEmpty()) {
                timeContainer.getChildren().add(amenitiesBox);
            }

            // Add platform info if available
            if (orgRoute.getPlatformInfo() != null && !orgRoute.getPlatformInfo().isEmpty()) {
                Label platformLabel = new Label("Quai: " + orgRoute.getPlatformInfo());
                platformLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
                timeContainer.getChildren().add(platformLabel);
            }
        }

        bookButton.setOnAction(event -> showPaymentForm(route, transportType, departureTime, date, passengerCount));

        buttonContainer.getChildren().addAll(timeContainer, bookButton);

        // Add all components to the item
        item.getChildren().addAll(routeBox, companyLabel, typeLabel, distanceLabel, durationLabel, priceLabel, buttonContainer);

        return item;
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

    private void showPaymentForm(Route route, String transportType, String departureTime, LocalDate date, int passengerCount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/createReservation.fxml"));
            Parent root = loader.load();

            // Get the controller and set route, transport type and time
            CreateReservationController controller = loader.getController();
            controller.setRouteAndTransportType(route, transportType);

            // Set the departure time and date
            if (departureTime != null && date != null) {
                controller.setDepartureTimeAndDate(departureTime, date);
            }

            // Set passenger count
            controller.setPassengerCount(passengerCount);

            // Set payment mode
            controller.setPaymentMode(true);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Paiement de réservation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            log.error("Error showing payment form", e);
            showError("Erreur", "Impossible d'ouvrir le formulaire de paiement: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper method to resize the map when container size changes
    private void resizeMap() {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript("if (map) { map.invalidateSize(); }");
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
            if (searchResultsContainer.getScene() != null) {
                // Find the weather widget by its ID
                VBox weatherWidget = (VBox) searchResultsContainer.getScene().lookup("#weatherWidget");
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
