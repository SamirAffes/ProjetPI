package controllers;

import entities.Route;
import entities.Transport;
import entities.User;
import entities.Organisation;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.ReservationService;
import services.ReservationServiceImpl;
import services.RouteService;
import services.TransportService;
import services.OrganisationService;
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

    @FXML private ComboBox<String> departureComboBox;
    @FXML private ComboBox<String> arrivalComboBox;
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
    private User currentUser;
    
    // Default coordinates for Tunisia center
    private final double DEFAULT_LATITUDE = 36.8065;
    private final double DEFAULT_LONGITUDE = 10.1815;
    private final int DEFAULT_ZOOM = 7;
    
    // City coordinates mapping (sample data with real coordinates)
    private final java.util.Map<String, double[]> CITY_COORDINATES = new java.util.HashMap<>() {{
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
        
        // Locations within Sousse
        put("Sousse - Centre Ville", new double[]{35.8283, 10.6383});
        put("Sousse - Port El Kantaoui", new double[]{35.8917, 10.5983});
        put("Sousse - Hammam Sousse", new double[]{35.8611, 10.5986});
        
        // Locations within Sfax
        put("Sfax - Centre Ville", new double[]{34.7406, 10.7603});
        put("Sfax - Thyna", new double[]{34.6761, 10.7514});
        put("Sfax - Port", new double[]{34.7258, 10.7717});
        
        // International destinations (airports and ports)
        put("Paris (France)", new double[]{48.8566, 2.3522});
        put("Marseille (France)", new double[]{43.2965, 5.3698});
        put("Rome (Italie)", new double[]{41.9028, 12.4964});
        put("Barcelone (Espagne)", new double[]{41.3851, 2.1734});
        put("Alger (Algérie)", new double[]{36.7538, 3.0588});
        put("Le Caire (Égypte)", new double[]{30.0444, 31.2357});
        put("Istanbul (Turquie)", new double[]{41.0082, 28.9784});
        put("Dubaï (UAE)", new double[]{25.2048, 55.2708});
        put("Genève (Suisse)", new double[]{46.2044, 6.1432});
        put("Francfort (Allemagne)", new double[]{50.1109, 8.6821});
        
        // Ports for ferries
        put("Port de La Goulette", new double[]{36.8183, 10.3050});
        put("Port de Marseille", new double[]{43.3054, 5.3670});
        put("Port de Gênes (Italie)", new double[]{44.4056, 8.9463});
        put("Port de Civitavecchia (Italie)", new double[]{42.0924, 11.7958});
        put("Port de Barcelone (Espagne)", new double[]{41.3773, 2.1835});
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
        
        // Initialize location lists from our coordinates map
        List<String> allLocations = new ArrayList<>(CITY_COORDINATES.keySet());
        // Sort locations alphabetically
        Collections.sort(allLocations);
        
        // Set default date to today
        departureDatePicker.setValue(LocalDate.now());
        
        // Initialize time options
        setupTimeOptions();
        
        // Setup transport type combo box with icons
        setupTransportTypeComboBox();
        
        // Initially clear departure and arrival combo boxes until transport type is selected
        departureComboBox.getItems().clear();
        arrivalComboBox.getItems().clear();
        
        // Update destinations based on default transport type
        updateDestinationsByTransportType(transportTypeComboBox.getValue());
        
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
     * Updates the available destinations based on the selected transport type
     */
    private void updateDestinationsByTransportType(String transportType) {
        // Save current selection if any
        String currentDeparture = departureComboBox.getValue();
        String currentArrival = arrivalComboBox.getValue();
        
        // Get all locations
        List<String> allLocations = new ArrayList<>(CITY_COORDINATES.keySet());
        Collections.sort(allLocations);
        
        // Filter locations based on transport type
        List<String> filteredDepartures = new ArrayList<>();
        List<String> filteredArrivals = new ArrayList<>();
        
        // Update the prompt text to reflect the transport type
        String departurePrompt = "Sélectionner une ville de départ";
        String arrivalPrompt = "Sélectionner une ville d'arrivée";
        
        switch (transportType) {
            case "Avion":
                // For planes: airports and international destinations
                departurePrompt = "Sélectionner un aéroport de départ";
                arrivalPrompt = "Sélectionner un aéroport d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> loc.contains("Aéroport") || 
                                  loc.equals("Tunis") || 
                                  loc.equals("Monastir") || 
                                  loc.equals("Sfax") || 
                                  loc.equals("Djerba"))
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(allLocations.stream()
                    .filter(loc -> loc.contains("(") || // International destinations
                                  loc.contains("Aéroport") ||
                                  loc.equals("Tunis") || 
                                  loc.equals("Monastir") || 
                                  loc.equals("Sfax") || 
                                  loc.equals("Djerba"))
                    .collect(Collectors.toList()));
                break;
                
            case "Ferry":
                // For ferries: ports only
                departurePrompt = "Sélectionner un port de départ";
                arrivalPrompt = "Sélectionner un port d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> loc.contains("Port"))
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(allLocations.stream()
                    .filter(loc -> loc.contains("Port"))
                    .collect(Collectors.toList()));
                break;
                
            case "Train":
                // For trains: major cities and train stations
                departurePrompt = "Sélectionner une gare de départ";
                arrivalPrompt = "Sélectionner une gare d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> !loc.contains("(") && // No international destinations
                                  !loc.contains("Port") && // No ports
                                  !loc.contains("Aéroport")) // No airports
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(filteredDepartures);
                break;
                
            case "Métro":
                // For metro: only locations within Tunis
                departurePrompt = "Sélectionner une station de départ";
                arrivalPrompt = "Sélectionner une station d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> loc.startsWith("Tunis -"))
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(filteredDepartures);
                break;
                
            case "TGM":
                // For TGM: only locations between La Goulette and La Marsa
                departurePrompt = "Sélectionner une station de départ";
                arrivalPrompt = "Sélectionner une station d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> loc.contains("La Goulette") || 
                                  loc.contains("Carthage") || 
                                  loc.contains("Sidi Bou Said") || 
                                  loc.contains("La Marsa"))
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(filteredDepartures);
                break;
                
            case "Taxi":
                // Taxis can go anywhere within Tunisia
                departurePrompt = "Sélectionner un lieu de départ";
                arrivalPrompt = "Sélectionner un lieu d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> !loc.contains("(")) // No international destinations
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(filteredDepartures);
                break;
                
            case "Bus":
                // Buses can go to major cities and some locations within cities
                departurePrompt = "Sélectionner un arrêt de départ";
                arrivalPrompt = "Sélectionner un arrêt d'arrivée";
                
                filteredDepartures.addAll(allLocations.stream()
                    .filter(loc -> !loc.contains("(")) // No international destinations
                    .collect(Collectors.toList()));
                
                filteredArrivals.addAll(filteredDepartures);
                break;
                
            default: // "Tous" or any other value
                // Show all locations
                filteredDepartures.addAll(allLocations);
                filteredArrivals.addAll(allLocations);
                break;
        }
        
        // Update combo boxes
        departureComboBox.getItems().clear();
        departureComboBox.getItems().addAll(filteredDepartures);
        departureComboBox.setPromptText(departurePrompt);
        
        arrivalComboBox.getItems().clear();
        arrivalComboBox.getItems().addAll(filteredArrivals);
        arrivalComboBox.setPromptText(arrivalPrompt);
        
        // Restore previous selections if they're still valid
        if (currentDeparture != null && filteredDepartures.contains(currentDeparture)) {
            departureComboBox.setValue(currentDeparture);
        } else {
            departureComboBox.setValue(null);
        }
        
        if (currentArrival != null && filteredArrivals.contains(currentArrival)) {
            arrivalComboBox.setValue(currentArrival);
        } else {
            arrivalComboBox.setValue(null);
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
                    
                    // Add some spacing between icon and text
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    hbox.getChildren().addAll(icon, new Label(item));
                    setGraphic(hbox);
                }
            }
        });
        
        // Apply the same cell factory to the button cell (what shows when selected)
        transportTypeComboBox.setButtonCell(transportTypeComboBox.getCellFactory().call(null));
        
        // Add the transport types
        transportTypeComboBox.getItems().addAll(
            "Bus", "Train", "Taxi", "Métro", "TGM", "Avion", "Ferry", "Tous"
        );
        
        // Set default value
        transportTypeComboBox.setValue("Tous");
        
        // Add listener to transport type to filter destinations
        transportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateDestinationsByTransportType(newVal);
            }
        });
    }
    
    @FXML
    private void handleSearch() {
        String transportType = transportTypeComboBox.getValue();
        String departure = departureComboBox.getValue();
        String arrival = arrivalComboBox.getValue();
        LocalDate date = departureDatePicker.getValue();
        String departureTime = departureTimeComboBox.getValue();
        String passengers = passengersTextField.getText();
        
        log.info("Search request - Transport: {}, From: {}, To: {}, Date: {}, Time: {}, Passengers: {}", 
                transportType, departure, arrival, date, departureTime, passengers);
        
        // Validate inputs in the correct order
        if (transportType == null) {
            showError("Validation Error", "Veuillez sélectionner un type de transport.");
            transportTypeComboBox.requestFocus();
            return;
        }
        
        if (departure == null) {
            showError("Validation Error", "Veuillez sélectionner un lieu de départ.");
            departureComboBox.requestFocus();
            return;
        }
        
        if (arrival == null) {
            showError("Validation Error", "Veuillez sélectionner un lieu d'arrivée.");
            arrivalComboBox.requestFocus();
            return;
        }
        
        if (departure.equals(arrival)) {
            showError("Validation Error", "Les lieux de départ et d'arrivée ne peuvent pas être identiques.");
            arrivalComboBox.requestFocus();
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
        
        if (departureCoords == null || arrivalCoords == null) {
            showError("Map Error", "Coordonnées non disponibles pour les lieux sélectionnés.");
            return;
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
            
            Label priceHeaderLabel = new Label("Prix");
            priceHeaderLabel.setMinWidth(80);
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
            
            // Display routes as list items
            for (Route route : routes) {
                routesListContainer.getChildren().add(createRouteListItem(route, transportType, departureTime, date));
            }
            
            searchResultsContainer.getChildren().addAll(headerBox, routesListContainer);
        }
    }
    
    private List<Route> findRoutes(String origin, String destination) {
        try {
            // Get all routes from service
            List<Route> allRoutes = routeService.afficher_tout();
            List<Route> matchingRoutes = new ArrayList<>();
            
            // Filter routes based on search criteria
            matchingRoutes = allRoutes.stream()
                .filter(route -> route.getOrigin().equalsIgnoreCase(origin) && 
                                route.getDestination().equalsIgnoreCase(destination))
                .collect(Collectors.toList());
            
            // If no routes found, generate random sample routes
            if (matchingRoutes.isEmpty()) {
                log.info("No matching routes found in database, generating random routes");
                matchingRoutes = generateRandomRoutes(origin, destination, 3);
            }
            
            return matchingRoutes;
        } catch (Exception e) {
            log.error("Error finding routes", e);
            
            // Generate random routes if database error occurs
            log.info("Generating fallback random routes");
            return generateRandomRoutes(origin, destination, 3);
        }
    }
    
    /**
     * Generates random sample routes between the origin and destination
     */
    private List<Route> generateRandomRoutes(String origin, String destination, int count) {
        List<Route> routes = new ArrayList<>();
        Random random = new Random();
        
        // Calculate actual distance between coordinates
        double[] originCoords = CITY_COORDINATES.get(origin);
        double[] destCoords = CITY_COORDINATES.get(destination);
        
        if (originCoords == null || destCoords == null) {
            // Fallback coordinates
            originCoords = new double[]{36.8065, 10.1815}; // Default to Tunis
            destCoords = new double[]{35.8245, 10.6346};   // Default to Sousse
        }
        
        double baseDistance = calculateDistance(originCoords[0], originCoords[1], 
                                            destCoords[0], destCoords[1]);
        
        // Get actual organisations from the database
        List<Organisation> organisations = organisationService.afficher_tout();
        
        // If no organisations are found, use some default names
        List<String> transportCompanies = new ArrayList<>();
        if (organisations.isEmpty()) {
            log.warn("No organisations found in database. Using default company names.");
            transportCompanies.addAll(Arrays.asList(
                "TunisAir", "SNCFT", "TransTunisie", "SahaTours", "Express Travel", 
                "Transtu", "CTN", "Nouvelair", "TunisiaFerries", "TUT"
            ));
        } else {
            log.info("Found {} organisations in database", organisations.size());
            for (Organisation org : organisations) {
                transportCompanies.add(org.getNom());
            }
        }
        
        // Determine if this is an international route
        boolean isInternational = origin.contains("(") || destination.contains("(") || 
                                  origin.contains("Port") && destination.contains("Port");
        
        // Determine if this is an intra-city route (within same city)
        boolean isIntraCity = false;
        if (origin.contains(" - ") && destination.contains(" - ")) {
            String originCity = origin.substring(0, origin.indexOf(" - "));
            String destCity = destination.substring(0, destination.indexOf(" - "));
            isIntraCity = originCity.equals(destCity);
        }
        
        // Determine appropriate transport modes based on the route type
        List<String> availableTransportModes = new ArrayList<>();
        
        if (isInternational) {
            if (origin.contains("Port") || destination.contains("Port")) {
                availableTransportModes.add("Ferry");
            } else {
                availableTransportModes.add("Avion");
            }
        } else if (isIntraCity) {
            // Within city transport options
            availableTransportModes.add("Taxi");
            availableTransportModes.add("Bus");
            
            // Add metro/TGM for Tunis
            if (origin.startsWith("Tunis")) {
                availableTransportModes.add("Métro");
                
                // Add TGM only for routes between La Goulette, Carthage, Sidi Bou Said, La Marsa
                if ((origin.contains("La Goulette") || origin.contains("Carthage") || 
                     origin.contains("Sidi Bou Said") || origin.contains("La Marsa")) &&
                    (destination.contains("La Goulette") || destination.contains("Carthage") || 
                     destination.contains("Sidi Bou Said") || destination.contains("La Marsa"))) {
                    availableTransportModes.add("TGM");
                }
            }
        } else {
            // Inter-city transport options
            availableTransportModes.add("Bus");
            availableTransportModes.add("Taxi");
            
            // Major cities have train connections
            List<String> majorCities = Arrays.asList("Tunis", "Sousse", "Sfax", "Gabès", "Monastir", 
                                                     "Nabeul", "Bizerte", "Kairouan");
            if (majorCities.contains(origin) && majorCities.contains(destination)) {
                availableTransportModes.add("Train");
            }
        }
        
        // Filter by selected transport type if specified
        String selectedTransportType = transportTypeComboBox.getValue();
        if (selectedTransportType != null && !selectedTransportType.equals("Tous")) {
            if (!availableTransportModes.contains(selectedTransportType)) {
                // If the selected transport type is not available for this route, return empty list
                return routes;
            }
            availableTransportModes.clear();
            availableTransportModes.add(selectedTransportType);
        }
        
        // Route types based on service level
        String[] routeTypes = {"Direct", "Express", "Standard", "Économique", "Premium"};
        
        // Generate routes for each available transport mode
        for (String transportMode : availableTransportModes) {
            // Adjust count to have more variety when showing all transport types
            int routesPerMode = availableTransportModes.size() > 1 ? 
                Math.max(1, count / availableTransportModes.size()) : count;
            
            for (int i = 0; i < routesPerMode; i++) {
                // Adjust distance and duration based on transport mode
                double distanceMultiplier = 1.0;
                double durationMultiplier = 1.0;
                double priceMultiplier = 1.0;
                
                switch (transportMode) {
                    case "Avion":
                        // Planes are faster but more expensive
                        durationMultiplier = 0.3;
                        priceMultiplier = 5.0;
                        break;
                    case "Ferry":
                        // Ferries are slower but cover longer distances
                        durationMultiplier = 2.0;
                        priceMultiplier = 2.0;
                        break;
                    case "Train":
                        // Trains are slightly faster than buses
                        durationMultiplier = 0.8;
                        priceMultiplier = 0.9;
                        break;
                    case "Métro":
                    case "TGM":
                        // Metro is for short distances
                        distanceMultiplier = 0.9; // Slightly shorter route
                        durationMultiplier = 0.7;
                        priceMultiplier = 0.3; // Much cheaper
                        break;
                    case "Taxi":
                        // Taxis are faster but more expensive
                        durationMultiplier = 0.7;
                        priceMultiplier = 2.5;
                        break;
                    case "Bus":
                    default:
                        // Bus is the baseline
                        break;
                }
                
                // Randomize route properties
                double distanceVariation = (random.nextDouble() * 0.2) - 0.1; // +/- 10%
                double distance = baseDistance * distanceMultiplier * (1 + distanceVariation);
                
                int duration = (int)(calculateDuration(distance) * durationMultiplier);
                double price = calculatePrice(distance) * priceMultiplier * (1 + (random.nextDouble() * 0.4) - 0.2); // +/- 20%
                
                // Get a random organisation appropriate for this transport mode
                String companyName;
                int companyId;
                
                if (organisations.isEmpty()) {
                    companyId = i + 1;
                    
                    // Select appropriate company for transport mode
                    switch (transportMode) {
                        case "Avion":
                            companyName = "TunisAir";
                            break;
                        case "Ferry":
                            companyName = "CTN";
                            break;
                        case "Train":
                            companyName = "SNCFT";
                            break;
                        case "Métro":
                        case "TGM":
                            companyName = "Transtu";
                            break;
                        default:
                            companyName = transportCompanies.get(random.nextInt(transportCompanies.size()));
                    }
                } else {
                    Organisation selectedOrg = organisations.get(random.nextInt(organisations.size()));
                    companyId = selectedOrg.getId();
                    companyName = selectedOrg.getNom();
                }
                
                // Create route
                Route route = Route.builder()
                        .id(routes.size() + 1)
                        .origin(origin)
                        .destination(destination)
                        .distance(Math.round(distance * 10.0) / 10.0)
                        .estimatedDuration(duration)
                        .basePrice(Math.round(price * 10.0) / 10.0)
                        .companyId(companyId)
                        .companyName(companyName)
                        .routeType(routeTypes[random.nextInt(routeTypes.length)])
                        .transportMode(transportMode)
                        .isInternational(isInternational)
                        .isIntraCity(isIntraCity)
                        .build();
                
                route.initializeProperties();
                routes.add(route);
            }
        }
        
        return routes;
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
    
    private HBox createRouteListItem(Route route, String transportType, String departureTime, LocalDate date) {
        HBox item = new HBox();
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setSpacing(20);
        item.setPadding(new Insets(15, 10, 15, 10));
        item.getStyleClass().add("route-list-item");
        
        // Add hover effect for better UX
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #f0f7ff;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: white;"));
        
        // Route information
        Label routeLabel = new Label(route.getOrigin() + " → " + route.getDestination());
        routeLabel.setMinWidth(150);
        routeLabel.setWrapText(true);
        
        // Company information
        String companyName = route.getCompanyName() != null ? route.getCompanyName() : "Compagnie #" + route.getCompanyId();
        Label companyLabel = new Label(companyName);
        companyLabel.setMinWidth(120);
        
        // Route type
        String routeType = route.getRouteType() != null ? route.getRouteType() : "Standard";
        Label typeLabel = new Label(routeType);
        typeLabel.setMinWidth(100);
        
        // Distance
        Label distanceLabel = new Label(route.getDistance() + " km");
        distanceLabel.setMinWidth(80);
        
        // Duration
        Label durationLabel = new Label(formatDuration(route.getEstimatedDuration()));
        durationLabel.setMinWidth(80);
        
        // Price
        Label priceLabel = new Label(route.getBasePrice() + " DT");
        priceLabel.setMinWidth(80);
        priceLabel.getStyleClass().add("price-label");
        
        // Reserve button
        Button reserveButton = new Button("Réserver");
        reserveButton.getStyleClass().add("button-primary");
        reserveButton.setMinWidth(120);
        
        // Set action to go to payment directly
        reserveButton.setOnAction(event -> showPaymentForm(route, transportType, departureTime, date));
        
        // Create container for buttons
        HBox buttonsContainer = new HBox(10);
        buttonsContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        // View route details button
        Button viewRouteButton = new Button();
        viewRouteButton.setGraphic(new FontIcon("fas-map-marked-alt"));
        viewRouteButton.getStyleClass().addAll("button-secondary", "icon-button");
        viewRouteButton.setTooltip(new Tooltip("Voir sur la carte"));
        
        // Add action to highlight this route on the map
        viewRouteButton.setOnAction(event -> {
            // Get coordinates for origin and destination
            double[] originCoords = CITY_COORDINATES.get(route.getOrigin());
            double[] destCoords = CITY_COORDINATES.get(route.getDestination());
            
            if (originCoords != null && destCoords != null) {
                // Clear previous markers and routes
                clearMarkers();
                
                // Add markers for departure and arrival
                addMarker(originCoords[0], originCoords[1], "Départ: " + route.getOrigin(), true);
                addMarker(destCoords[0], destCoords[1], "Arrivée: " + route.getDestination(), false);
                
                // Create detailed route info
                String routeInfo = String.format("%s → %s (%s)", 
                    route.getOrigin(), route.getDestination(), companyName);
                
                // Draw the route
                drawRoute(originCoords[0], originCoords[1], destCoords[0], destCoords[1], routeInfo);
                
                // Scroll to the map
                mapWebView.requestFocus();
                mapWebView.getParent().requestFocus();
            }
        });
        
        buttonsContainer.getChildren().addAll(viewRouteButton, reserveButton);
        
        item.getChildren().addAll(routeLabel, companyLabel, typeLabel, distanceLabel, 
                                 durationLabel, priceLabel, buttonsContainer);
        
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
    
    private void showPaymentForm(Route route, String transportType, String departureTime, LocalDate date) {
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
} 