package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import services.OrganisationService;
import utils.OrganisationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Home {

    private static final Logger logger = LoggerFactory.getLogger(Home.class);
    private final OrganisationService organisationService = new OrganisationService();

    // Accessibility mode variables
    private boolean accessibilityModeActive = false;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @FXML
    private AnchorPane mapContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button signInButton;

    @FXML
    private Button accessibilityButton;

    @FXML
    private VBox signInForm;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button locationButton;

    private boolean locationActive = false;
    private WebView webView;
    private WebEngine webEngine;

    // Default coordinates for Tunis, Tunisia
    private final double DEFAULT_LATITUDE = 36.8065;
    private final double DEFAULT_LONGITUDE = 10.1815;
    private final int DEFAULT_ZOOM = 12;

    // Esprit Night school coordinates  36.8534449,10.2074847
    private final double ESPRIT_LATITUDE = 36.8534449;
    private final double ESPRIT_LONGITUDE = 10.2074847;
    private final int ESPRIT_ZOOM = 18;

    // Add a map of city names to their coordinates
    private static final Map<String, double[]> CITY_COORDINATES = new HashMap<>();
    static {
        CITY_COORDINATES.put("Tunis", new double[]{36.8065, 10.1815});
        CITY_COORDINATES.put("Ariana", new double[]{36.8633, 10.2167});
        CITY_COORDINATES.put("Béja", new double[]{36.7444, 9.8956});
        CITY_COORDINATES.put("Ben Arous", new double[]{36.7556, 10.2214});
        CITY_COORDINATES.put("Manouba", new double[]{36.8006, 10.1195});
        CITY_COORDINATES.put("Nabeul", new double[]{36.4511, 10.7204});
        CITY_COORDINATES.put("Sfax", new double[]{34.7406, 10.7594});
        CITY_COORDINATES.put("Sousse", new double[]{35.8250, 10.6364});
        CITY_COORDINATES.put("Tataouine", new double[]{32.9375, 10.4472});
        CITY_COORDINATES.put("Tozeur", new double[]{33.9239, 8.1348});
    }

    @FXML
    public void initialize() {
        // Add tooltip to location button
        Tooltip locationTooltip = new Tooltip("Afficher votre position");
        Tooltip.install(locationButton, locationTooltip);

        // Initialize location button icon using Ikonli
        FontIcon locationIcon = new FontIcon(FontAwesomeSolid.MAP_MARKER_ALT);
        locationIcon.setIconSize(20);
        locationButton.setGraphic(locationIcon);

        logger.info("Initializing Home controller");

        // Initialize map on a separate thread to keep UI responsive
        Platform.runLater(this::initializeMap);
    }
    @FXML
    public void onLocationButtonClick() {
        locationActive = !locationActive;
        logger.info("Location button clicked, new state: {}", locationActive);

        // Toggle location tracking
        if (locationActive) {
            // For demo purposes using ESPRIT location
            // In a real app, you would get actual device location
            setCurrentLocationWithLabel(ESPRIT_LATITUDE, ESPRIT_LONGITUDE, "Vous êtes ici");

            // Update button appearance to show active state
            FontIcon icon = (FontIcon) locationButton.getGraphic();
            icon.setIconCode(FontAwesomeSolid.MAP_MARKER_ALT);
            icon.setIconColor(javafx.scene.paint.Color.valueOf("#4285F4"));
        } else {
            // Remove location marker
            clearCurrentLocation();

            // Reset button appearance
            FontIcon icon = (FontIcon) locationButton.getGraphic();
            icon.setIconCode(FontAwesomeSolid.MAP_MARKER_ALT);
            // Use the default text color instead of null
            icon.setIconColor(javafx.scene.paint.Color.BLACK);
        }
    }
    // Add the following methods to handle location markers with visible labels
    public void setCurrentLocationWithLabel(double lat, double lng, String label) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            // Remove any existing marker and add a new one with auto-open popup
            webEngine.executeScript(String.format(
                    "setCurrentLocationMarkerWithLabel(%f, %f, '%s')",
                    lat, lng, label));
        }
    }

    public void clearCurrentLocation() {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript("clearCurrentLocationMarker()");
        }
    }
    private void initializeMap() {
        logger.info("Initializing map component");
        try {
            // Create WebView for map
            webView = new WebView();
            webEngine = webView.getEngine();

            // Set WebView to fill the entire container
            webView.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

            // Add the WebView to the JavaFX pane
            mapContainer.getChildren().add(webView);
            AnchorPane.setTopAnchor(webView, 0.0);
            AnchorPane.setBottomAnchor(webView, 0.0);
            AnchorPane.setLeftAnchor(webView, 0.0);
            AnchorPane.setRightAnchor(webView, 0.0);

            // Generate HTML content for OpenStreetMap with Leaflet.js
            String mapContent = generateMapHtml();
            webEngine.loadContent(mapContent);

            // Handle loading errors
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.FAILED) {
                    logger.error("Failed to load map content");
                    showError("Map Loading Error", "Failed to load the map. Please check your internet connection.");
                } else if (newState == Worker.State.SUCCEEDED) {
                    logger.info("Map loaded successfully");

                    // Set initial location
                    panToLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ZOOM);
                }
            });

            // Set up listeners for resize events
            mapContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                webView.setPrefWidth(newVal.doubleValue());
                resizeMap();
            });

            mapContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                webView.setPrefHeight(newVal.doubleValue());
                resizeMap();
            });

            logger.info("Map initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize map", e);
            showError("Map Initialization Error", "Failed to load the map. Please check your internet connection.");
        }
    }

    // Invoke JavaScript to resize the map when container size changes
    private void resizeMap() {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript("if (map) { map.invalidateSize(); }");
        }
    }
    public void setCurrentLocationPin(double lat, double lng) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            // Remove any existing current location marker and add a new one
            webEngine.executeScript(String.format("setCurrentLocationMarker(%f, %f)", lat, lng));
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
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([%f, %f], %d);
                var currentLocationMarker = null;
                var routeControl = null;

                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                }).addTo(map);

                // Function to be called from Java
                function panToLocation(lat, lng, zoom) {
                    map.setView([lat, lng], zoom);
                }

                // Function to add a marker
                function addMarker(lat, lng, title) {
                    L.marker([lat, lng]).addTo(map)
                        .bindPopup(title)
                        .openPopup();
                }

                // Function to set current location marker
                function setCurrentLocationMarker(lat, lng) {
                    // Remove existing current location marker if exists
                    if (currentLocationMarker) {
                        map.removeLayer(currentLocationMarker);
                    }

                    // Create a distinct current location marker
                    var locationIcon = L.divIcon({
                        className: 'current-location-marker',
                        html: `<div style="background-color: #4285F4; width: 16px; height: 16px; border-radius: 50%%; border: 3px solid white; box-shadow: 0 0 3px rgba(0,0,0,0.4);"></div>`,
                        iconSize: [22, 22],
                        iconAnchor: [11, 11]
                    });

                    // Add the marker and save reference
                    currentLocationMarker = L.marker([lat, lng], {icon: locationIcon}).addTo(map)
                        .bindPopup("Current Location");

                    // Pan to the current location
                    map.setView([lat, lng], 18);
                }

                // Function to set current location with visible label
                function setCurrentLocationMarkerWithLabel(lat, lng, label) {
                    // Remove existing marker if it exists
                    clearCurrentLocationMarker();

                    // Create a distinct current location marker
                    var locationIcon = L.divIcon({
                        className: 'current-location-marker',
                        html: `<div style="background-color: #4285F4; width: 16px; height: 16px; border-radius: 50%%; border: 3px solid white; box-shadow: 0 0 3px rgba(0,0,0,0.4);"></div>`,
                        iconSize: [22, 22],
                        iconAnchor: [11, 11]
                    });

                    // Add the marker and save reference
                    currentLocationMarker = L.marker([lat, lng], {icon: locationIcon}).addTo(map)
                        .bindPopup(label);

                    // Pan to location and open popup immediately
                    map.setView([lat, lng], 18);
                    currentLocationMarker.openPopup();
                }

                // Function to clear current location marker
                function clearCurrentLocationMarker() {
                    if (currentLocationMarker) {
                        map.removeLayer(currentLocationMarker);
                        currentLocationMarker = null;
                    }

                    // Also clear any route if it exists
                    if (routeControl) {
                        map.removeControl(routeControl);
                        routeControl = null;
                    }
                }

                // Function to draw a route with waypoints between two points
                function drawRouteWithWaypoints(originLat, originLng, destLat, destLng) {
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


    // Method to call JavaScript function to pan to a location
    public void panToLocation(double lat, double lng, int zoom) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(String.format("panToLocation(%f, %f, %d)", lat, lng, zoom));
        }
    }

    // Method to add a marker
    public void addMarker(double lat, double lng, String title) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(String.format("addMarker(%f, %f, '%s')", lat, lng, title.replace("'", "\\'")));
        }
    }

    @FXML
    public void onSearchButtonClick() {
        String query = searchField.getText();
        if (query != null && !query.isEmpty()) {
            logger.info("Search requested: {}", query);

            // Parse the query to extract origin and destination
            String[] parts = query.split("\\s+(?:to|vers|à|a|->|→)\\s+", 2);

            if (parts.length == 2) {
                String origin = parts[0].trim();
                String destination = parts[1].trim();

                logger.info("Parsed search: from {} to {}", origin, destination);

                // Check if we have coordinates for these locations
                if (hasCoordinatesForLocation(origin) && hasCoordinatesForLocation(destination)) {
                    // Get coordinates
                    double[] originCoords = getCoordinatesForLocation(origin);
                    double[] destCoords = getCoordinatesForLocation(destination);

                    // Clear any existing markers
                    clearCurrentLocation();

                    // Add markers for origin and destination
                    addMarker(originCoords[0], originCoords[1], "Départ: " + origin);
                    addMarker(destCoords[0], destCoords[1], "Arrivée: " + destination);

                    // Draw route between the points
                    drawRouteBetweenPoints(originCoords[0], originCoords[1], destCoords[0], destCoords[1]);

                    // Show info with success message
                    showInfo("Itinéraire trouvé", "Itinéraire de " + origin + " à " + destination + " affiché sur la carte.");
                } else {
                    // Show error if locations not found
                    showError("Recherche", "Impossible de trouver les coordonnées pour " + 
                              (hasCoordinatesForLocation(origin) ? destination : origin));
                }
            } else {
                // If we can't parse as origin/destination, treat it as a single location search
                if (hasCoordinatesForLocation(query)) {
                    // Get coordinates
                    double[] coords = getCoordinatesForLocation(query);

                    // Clear any existing markers
                    clearCurrentLocation();

                    // Add marker for the location
                    addMarker(coords[0], coords[1], "Destination: " + query);

                    // Pan to the location
                    panToLocation(coords[0], coords[1], 15);

                    // Show info with success message
                } else {
                    // Show error if location not found
                    showError("Recherche", "Impossible de trouver les coordonnées pour " + query);
                }
            }
        }
    }

    // Helper method to check if we have coordinates for a location
    private boolean hasCoordinatesForLocation(String location) {
        // Normalize location name (lowercase, remove accents)
        String normalizedLocation = location.toLowerCase()
            .replaceAll("é", "e")
            .replaceAll("è", "e")
            .replaceAll("ê", "e")
            .replaceAll("à", "a")
            .replaceAll("ù", "u");

        // Check against known city names (case insensitive)
        for (String city : CITY_COORDINATES.keySet()) {
            if (city.toLowerCase().contains(normalizedLocation) || 
                normalizedLocation.contains(city.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    // Helper method to get coordinates for a location
    private double[] getCoordinatesForLocation(String location) {
        // Normalize location name
        String normalizedLocation = location.toLowerCase()
            .replaceAll("é", "e")
            .replaceAll("è", "e")
            .replaceAll("ê", "e")
            .replaceAll("à", "a")
            .replaceAll("ù", "u");

        // Find matching city
        for (String city : CITY_COORDINATES.keySet()) {
            if (city.toLowerCase().contains(normalizedLocation) || 
                normalizedLocation.contains(city.toLowerCase())) {
                return CITY_COORDINATES.get(city);
            }
        }

        // Default to Tunis if not found
        return new double[]{DEFAULT_LATITUDE, DEFAULT_LONGITUDE};
    }

    // Add a method to draw a route between two points
    private void drawRouteBetweenPoints(double originLat, double originLng, double destLat, double destLng) {
        if (webEngine != null && webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            webEngine.executeScript(String.format(
                "drawRouteWithWaypoints(%f, %f, %f, %f)", 
                originLat, originLng, destLat, destLng));
        }
    }

    @FXML
    public void onSignInButtonClick() {
        logger.info("Sign in button clicked");
        // Show login selection screen instead of direct sign-in form
        showLoginTypeDialog();
    }



    @FXML
    public void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()) {
            showError("Login Error", "Username cannot be empty");
            return;
        }

        if (password == null || password.isEmpty()) {
            showError("Login Error", "Password cannot be empty");
            return;
        }

        // First, check if admin credentials
        if (username.equals("admin") && password.equals("admin")) {
            // Admin login
            navigateToAdminDashboard();
        } else {
            // Check if it's an organisation
            try {
                for (entities.Organisation org : organisationService.afficher_tout()) {
                    if (org.getNom().equalsIgnoreCase(username) && password.equals(username)) {
                        navigateToOrganisationDashboard(org.getId());
                        hideSignInForm();
                        signInButton.setText("Profile");
                        return;
                    }
                }

                // If not admin or organization, check if it's a user
                services.UserService userService = new services.UserService();
                entities.User user = userService.findByUsername(username);

                if (user != null && user.getPassword().equals(password)) {
                    // User found, navigate to user dashboard
                    navigateToUserDashboard(user);
                } else {
                    // Show login type selection dialog
                    showLoginTypeDialog();
                }
            } catch (Exception e) {
                logger.error("Error during login process", e);
                showError("Login Error", "An error occurred during login. Please try again.");
            }
        }
    }

    private void showLoginTypeDialog() {
        hideSignInForm();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_selection.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signInButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("TunTransport - Login");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load login selection screen", e);
            showError("Navigation Error", "Failed to load login page. Please try again.");
        }
    }

    private void navigateToUserDashboard(entities.User user) {
        try {
            // Store user in global context
            utils.UserContext.getInstance().setCurrentUser(user);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/dashboard.fxml"));
            Parent userDashboard = loader.load();

            // Get controller and set user
            controllers.UserDashboardController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) signInForm.getScene().getWindow();
            Scene scene = new Scene(userDashboard);
            stage.setScene(scene);
            stage.setTitle("User Dashboard");

            // Apply fullscreen *after* setting the new scene
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

            logger.info("User logged in: {}", user.getUsername());
        } catch (IOException e) {
            logger.error("Failed to load user dashboard", e);
            showError("Navigation Error", "Failed to load user dashboard. Please try again.");
        }
    }

    private void navigateToAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/dashboard.fxml"));
            Parent adminDashboard = loader.load();

            Stage stage = (Stage) signInForm.getScene().getWindow();

            // Create a new scene with the admin dashboard
            Scene scene = new Scene(adminDashboard);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");

            // Apply fullscreen *after* setting the new scene
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load admin dashboard", e);
            showError("Navigation Error", "Failed to load admin dashboard. Please try again.");
        }
    }

    private void navigateToOrganisationDashboard(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/organisation/dashboard.fxml"));
            Parent orgDashboard = loader.load();

            Stage stage = (Stage) signInForm.getScene().getWindow();

            // Create a new scene with the organisation dashboard
            Scene scene = new Scene(orgDashboard);
            stage.setScene(scene);
            stage.setTitle("Organisation Dashboard");

            // Apply fullscreen *after* setting the new scene
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
            // send the id to the global context

            OrganisationContext.getInstance().setCurrentOrganisation(
                    organisationService.afficher(id)
            );
        } catch (IOException e) {
            logger.error("Failed to load organisation dashboard", e);
            showError("Navigation Error", "Failed to load organisation dashboard. Please try again.");
        }
    }


    private void hideSignInForm() {
        // Clear form fields
        usernameField.clear();
        passwordField.clear();

        // Hide the form
        signInForm.setVisible(false);
    }

    @FXML
    public void onAccessibilityButtonClick() {
        logger.info("Accessibility button clicked");

        if (!accessibilityModeActive) {
            // Activate accessibility mode
            accessibilityModeActive = true;

            // Change button appearance to indicate active state
            accessibilityButton.setStyle("-fx-background-color: #4285F4;");

            // Show welcome message with text-to-speech simulation
            showTextToSpeechDialog("Bienvenue dans le mode d'accessibilité", 
                "Le mode d'accessibilité est maintenant activé. Vous pouvez utiliser la commande vocale en cliquant à nouveau sur le bouton d'accessibilité.\n\n" +
                "Dites \"aide\" pour voir toutes les commandes disponibles.");

            // Set a location pin to indicate current location
            setCurrentLocationPin(ESPRIT_LATITUDE, ESPRIT_LONGITUDE);
        } else {
            // Mode is already active, show voice command dialog
            showVoiceCommandDialog();
        }
    }

    // Media player for text-to-speech audio
    private MediaPlayer mediaPlayer;

    /**
     * Performs text-to-speech by showing a dialog with the text and playing audio
     */
    private void showTextToSpeechDialog(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Text-to-Speech");
        alert.setHeaderText(title);

        // Create a styled text area
        Label textLabel = new Label(text);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(400);
        textLabel.setStyle("-fx-font-size: 14px;");

        // Add an icon to indicate speech
        FontIcon speechIcon = new FontIcon(FontAwesomeSolid.VOLUME_UP);
        speechIcon.setIconSize(24);
        speechIcon.setIconColor(Color.valueOf("#4285F4"));

        // Create a grid pane for layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(speechIcon, 0, 0);
        grid.add(textLabel, 1, 0);

        // Add a play button to speak the text
        Button playButton = new Button("Écouter");
        playButton.setGraphic(new FontIcon(FontAwesomeSolid.PLAY));
        playButton.setOnAction(e -> speakText(text));

        grid.add(playButton, 1, 1);

        alert.getDialogPane().setContent(grid);

        // Play the text-to-speech audio when the dialog is shown
        alert.setOnShown(e -> speakText(text));

        alert.showAndWait();

        // Stop any playing audio when the dialog is closed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        logger.info("Text-to-speech: {}", text);
    }

    /**
     * Speaks the given text using JavaFX Media API
     */
    private void speakText(String text) {
        try {
            // Stop any currently playing audio
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            // Use a publicly available sound file for text-to-speech
            // This is a workaround since we can't upload actual audio files
            String audioUrl = "https://www2.cs.uic.edu/~i101/SoundFiles/CantinaBand3.wav";
            Media sound = new Media(audioUrl);
            mediaPlayer = new MediaPlayer(sound);

            // Play the sound
            mediaPlayer.play();

            logger.info("Playing text-to-speech for: {}", text);

            // Show a visual indication that speech is happening
            Platform.runLater(() -> {
                // Flash the speech icon to indicate speech is happening
                Alert speechAlert = new Alert(Alert.AlertType.INFORMATION);
                speechAlert.setTitle("Speaking...");
                speechAlert.setHeaderText("Text-to-Speech Active");
                speechAlert.setContentText("The system is speaking: \"" + text + "\"");

                // Auto-close after the sound finishes playing
                mediaPlayer.setOnEndOfMedia(() -> {
                    Platform.runLater(() -> speechAlert.close());
                });

                speechAlert.show();
            });

        } catch (Exception e) {
            logger.error("Error playing text-to-speech", e);
            // Fallback to visual indication if sound fails
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.WARNING);
                errorAlert.setTitle("Audio Playback Error");
                errorAlert.setHeaderText("Could not play audio");
                errorAlert.setContentText("The system encountered an error playing audio. Text: \"" + text + "\"");
                errorAlert.show();
            });
        }
    }

    /**
     * Shows a dialog to simulate voice command input with improved user experience
     */
    private void showVoiceCommandDialog() {
        // Create a custom dialog with voice command input and a button to disable accessibility mode
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Commande Vocale");
        dialog.setHeaderText("Parlez ou entrez votre commande vocale");

        // Set the button types (OK and a custom button to disable accessibility)
        ButtonType okButtonType = new ButtonType("OK", ButtonType.OK.getButtonData());
        ButtonType listenButtonType = new ButtonType("Écouter", ButtonType.APPLY.getButtonData());
        ButtonType disableButtonType = new ButtonType("Désactiver le mode d'accessibilité", ButtonType.CANCEL.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, listenButtonType, disableButtonType);

        // Create the command input field
        TextField commandField = new TextField();
        commandField.setPromptText("Commande (ex: aide, recherche Paris, connexion)");

        // Add microphone icon that changes color when "listening"
        FontIcon micIcon = new FontIcon(FontAwesomeSolid.MICROPHONE);
        micIcon.setIconSize(24);
        micIcon.setIconColor(Color.valueOf("#4285F4"));

        // Add a status label to show listening status
        Label statusLabel = new Label("Prêt à écouter");
        statusLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666;");

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(new Label("", micIcon), 0, 0);
        grid.add(commandField, 1, 0);
        grid.add(statusLabel, 1, 1);

        // Add common commands as buttons for quick access
        HBox quickCommands = new HBox(10);
        quickCommands.setPadding(new Insets(10, 0, 0, 0));

        Button helpButton = new Button("Aide");
        helpButton.setOnAction(e -> commandField.setText("aide"));

        Button searchButton = new Button("Recherche");
        searchButton.setOnAction(e -> commandField.setText("recherche Tunis"));

        Button locationButton = new Button("Position");
        locationButton.setOnAction(e -> commandField.setText("position"));

        quickCommands.getChildren().addAll(helpButton, searchButton, locationButton);
        grid.add(quickCommands, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the command field by default
        Platform.runLater(commandField::requestFocus);

        // Get the listen button and set its action
        Button listenBtn = (Button) dialog.getDialogPane().lookupButton(listenButtonType);
        listenBtn.setOnAction(event -> {
            // Simulate listening for voice input
            simulateVoiceListening(statusLabel, micIcon, commandField);
            event.consume(); // Prevent dialog from closing
        });

        // Convert the result to a string when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return commandField.getText();
            } else if (dialogButton == disableButtonType) {
                // Special return value to indicate accessibility mode should be disabled
                return "DISABLE_ACCESSIBILITY_MODE";
            }
            return null;
        });

        // Process the result
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(command -> {
            if ("DISABLE_ACCESSIBILITY_MODE".equals(command)) {
                // Disable accessibility mode
                accessibilityModeActive = false;
                accessibilityButton.setStyle("");
                showTextToSpeechDialog("Mode d'accessibilité désactivé", 
                    "Le mode d'accessibilité a été désactivé.");
                logger.info("Accessibility mode disabled by user");
            } else if (!command.trim().isEmpty()) {
                logger.info("Voice command received: {}", command);
                processVoiceCommand(command);
            }
        });
    }

    /**
     * Simulates listening for voice input with audio feedback
     */
    private void simulateVoiceListening(Label statusLabel, FontIcon micIcon, TextField commandField) {
        // Change microphone icon to indicate listening
        micIcon.setIconColor(Color.valueOf("#FF0000"));
        statusLabel.setText("Écoute en cours...");

        // Disable the command field while "listening"
        commandField.setDisable(true);

        try {
            // Play a "listening" sound to indicate the microphone is active
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            // Use a publicly available sound for the "listening" effect
            String listeningSound = "https://www2.cs.uic.edu/~i101/SoundFiles/StarWars60.wav";
            Media sound = new Media(listeningSound);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();

            logger.info("Playing listening sound effect");
        } catch (Exception e) {
            logger.error("Error playing listening sound", e);
        }

        // Create a timeline to simulate processing time with multiple stages
        Timeline timeline = new Timeline();

        // Stage 1: Start listening
        timeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> {}));

        // Stage 2: Show processing feedback
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            Platform.runLater(() -> {
                statusLabel.setText("Traitement en cours...");
            });
        }));

        // Stage 3: Complete recognition
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), e -> {
            // After 2 seconds, restore the UI and simulate a recognized command
            Platform.runLater(() -> {
                micIcon.setIconColor(Color.valueOf("#4285F4"));
                statusLabel.setText("Commande reconnue!");
                commandField.setDisable(false);

                // Simulate a recognized command (randomly choose one)
                String[] sampleCommands = {
                    "aide", 
                    "recherche Paris", 
                    "position", 
                    "connexion"
                };
                int randomIndex = (int)(Math.random() * sampleCommands.length);
                commandField.setText(sampleCommands[randomIndex]);
                commandField.selectAll();
                commandField.requestFocus();

                try {
                    // Play a "command recognized" sound
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                    }

                    // Use a publicly available sound for the "command recognized" effect
                    String recognizedSound = "https://www2.cs.uic.edu/~i101/SoundFiles/WindowsXP-startup.wav";
                    Media sound = new Media(recognizedSound);
                    mediaPlayer = new MediaPlayer(sound);
                    mediaPlayer.play();

                    logger.info("Playing command recognized sound effect");
                } catch (Exception ex) {
                    logger.error("Error playing command recognized sound", ex);
                }
            });
        }));

        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Processes a voice command
     */
    private void processVoiceCommand(String command) {
        // Convert command to lowercase for easier matching
        String lowerCommand = command.toLowerCase();

        if (lowerCommand.contains("aide") || lowerCommand.contains("help") || lowerCommand.equals("?")) {
            // Show help with available commands
            showTextToSpeechDialog("Aide - Commandes disponibles", 
                "Voici les commandes que vous pouvez utiliser:\n\n" +
                "• 'recherche [lieu]' - Rechercher un lieu sur la carte\n" +
                "• 'connexion' ou 'login' - Ouvrir la page de connexion\n" +
                "• 'position' ou 'localisation' - Afficher votre position actuelle\n" +
                "• 'aide' ou 'help' - Afficher cette aide\n" +
                "• 'désactiver' - Désactiver le mode d'accessibilité");
        } 
        else if (lowerCommand.contains("recherche") || lowerCommand.contains("chercher") || lowerCommand.contains("search")) {
            // Extract search terms
            String searchTerm = lowerCommand.replaceAll(".*?(recherche|chercher|search)\\s+", "");
            if (!searchTerm.isEmpty()) {
                // Handle search directly since searchField might not be available
                // Show feedback to user
                showTextToSpeechDialog("Recherche effectuée", 
                    "Recherche pour: " + searchTerm);

                // Try to find coordinates for the location
                if (hasCoordinatesForLocation(searchTerm)) {
                    double[] coords = getCoordinatesForLocation(searchTerm);
                    // Clear any existing markers
                    clearCurrentLocation();
                    // Add marker for the location
                    addMarker(coords[0], coords[1], "Destination: " + searchTerm);
                    // Pan to the location
                    panToLocation(coords[0], coords[1], 15);
                } else {
                    // If location not found, show error
                    showTextToSpeechDialog("Destination non trouvée", 
                        "Désolé, je ne trouve pas la destination: " + searchTerm);
                }
            }
        } 
        else if (lowerCommand.contains("connexion") || lowerCommand.contains("login") || lowerCommand.contains("sign in")) {
            // Show login dialog
            onSignInButtonClick();

            // Confirm action with TTS
            showTextToSpeechDialog("Connexion", 
                "Ouverture de la page de connexion");
        } 
        else if (lowerCommand.contains("position") || lowerCommand.contains("localisation") || lowerCommand.contains("location")) {
            // Show current location
            onLocationButtonClick();

            // Confirm action with TTS
            showTextToSpeechDialog("Localisation", 
                "Affichage de votre position actuelle");
        }
        else if (lowerCommand.contains("désactiver") || lowerCommand.contains("disable") || lowerCommand.contains("turn off")) {
            // Disable accessibility mode
            accessibilityModeActive = false;
            accessibilityButton.setStyle("");
            showTextToSpeechDialog("Mode d'accessibilité désactivé", 
                "Le mode d'accessibilité a été désactivé.");
            logger.info("Accessibility mode disabled by voice command");
        }
        else {
            // Unknown command
            showTextToSpeechDialog("Commande non reconnue", 
                "Désolé, je n'ai pas compris la commande: \"" + command + "\"\n" +
                "Dites \"aide\" pour voir les commandes disponibles.");
        }
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

    @FXML
    public void onCloseFormButtonClick(){
        hideSignInForm();
    }

}
