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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.scene.control.Tooltip;
import services.OrganisationService;
import utils.OrganisationContext;

import java.io.IOException;

public class Home {

    private static final Logger logger = LoggerFactory.getLogger(Home.class);
    private final OrganisationService organisationService = new OrganisationService();

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

            // Set WebView to be responsive
            webView.setPrefSize(mapContainer.getPrefWidth(), mapContainer.getPrefHeight());

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
            setCurrentLocationPin(ESPRIT_LATITUDE, ESPRIT_LONGITUDE);
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
            <style>
                html, body, #map {
                    height: 100%%;
                    width: 100%%;
                    margin: 0;
                    padding: 0;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map').setView([%f, %f], %d);
                var currentLocationMarker = null;
    
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
                        .bindPopup(title);
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
            webEngine.executeScript(String.format("addMarker(%f, %f, '%s')", lat, lng, title));
        }
    }

    @FXML
    public void onSearchButtonClick() {
        String query = searchField.getText();
        if (query != null && !query.isEmpty()) {
            logger.info("Search requested: {}", query);
            // TODO: Implement search functionality
            showInfo("Search", "Searching for: " + query);
        }
    }

    @FXML
    public void onSignInButtonClick() {
        logger.info("Sign in button clicked");
        // Show sign-in form
        signInForm.setVisible(true);
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
        if (username .equals("admin") && password.equals("admin")) {
            navigateToAdminDashboard();
        } else if (username.equals("org") && password.equals("org")) {
            navigateToOrganisationDashboard(3);
        } else {
            showError("Login Error", "Invalid username or password");
            return;
        }

        // TODO: Implement actual authentication
        hideSignInForm();

        // Update sign in button text to show logged in state
        signInButton.setText("Profile");
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
        // TODO: Implement accessibility options
        setCurrentLocationPin(ESPRIT_LATITUDE, ESPRIT_LONGITUDE);
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
