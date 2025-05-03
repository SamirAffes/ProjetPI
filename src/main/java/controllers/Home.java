package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Home {
    
    private static final Logger logger = LoggerFactory.getLogger(Home.class);

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

    private WebView webView;
    private WebEngine webEngine;
    
    // Default coordinates for Tunis, Tunisia
    private final double DEFAULT_LATITUDE = 36.8065;
    private final double DEFAULT_LONGITUDE = 10.1815;
    private final int DEFAULT_ZOOM = 12;

    @FXML
    public void initialize() {
        logger.info("Initializing Home controller");
        
        // Initialize map on a separate thread to keep UI responsive
        Platform.runLater(this::initializeMap);
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
        
        logger.info("Login attempted for user: {}", username);
        // TODO: Implement actual authentication
        showInfo("Login Success", "Welcome, " + username + "!");
        hideSignInForm();
        
        // Update sign in button text to show logged in state
        signInButton.setText("Profile");
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
        showInfo("Accessibility", "Accessibility features will be implemented soon.");
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
}
