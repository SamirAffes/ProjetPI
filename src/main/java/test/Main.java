package test;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import utils.DbContext;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    
    // Static reference to the primary stage for access throughout the application
    private static Stage primaryStage;
    // Flag to track if we've already attempted to maximize the window
    private static boolean maximizationAttempted = false;
    // Track the current view for navigation
    private static String currentView = "/Home.fxml";
    
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Get the primary stage of the application
     * @return The primary Stage instance
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Navigate to a specific view
     * @param fxmlFile The FXML file to load
     * @param title The title for the window
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateTo(String fxmlFile, String title) {
        try {
            LOGGER.info("Navigating to: " + fxmlFile + " from: " + currentView);
            
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(fxmlFile));
            Parent root = loader.load();
            
            // Create scene
            Scene scene = new Scene(root);
            
            // Update stage
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            
            // Track the current view
            currentView = fxmlFile;
            
            // Ensure the window is maximized
            Platform.runLater(() -> {
                primaryStage.setMaximized(false);
                primaryStage.setMaximized(true);
                
                LOGGER.info("Navigation complete - window state: " + primaryStage.isMaximized() + 
                           ", size: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
            });
            
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to " + fxmlFile, e);
            showError("Navigation Error", "Cannot navigate to " + fxmlFile, e.getMessage());
            return false;
        }
    }
    
    /**
     * Navigate back to the home view
     * @return true if navigation was successful, false otherwise
     */
    public static boolean navigateToHome() {
        return navigateTo("/Home.fxml", "TunTransport");
    }
    
    /**
     * Show an error dialog
     */
    private static void showError(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Force the primary stage to be maximized
     * This method can be called from other controllers if needed
     */
    public static void forceMaximize() {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                try {
                    // Reset then maximize
                    primaryStage.setMaximized(true);
                    
                    // Log results
                    LOGGER.info("Force maximized: " + primaryStage.isMaximized() + 
                                ", Window size: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
                    
                    // Request focus to ensure window is active
                    primaryStage.requestFocus();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error forcing window maximization", e);
                }
            });
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Store reference to primary stage
            Main.primaryStage = primaryStage;
            
            LOGGER.log(Level.INFO, "Starting Application");
            
            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            LOGGER.info("Primary screen size: " + screenBounds.getWidth() + "x" + screenBounds.getHeight());
            
            // Ensure proper loading of FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();
            
            // Set minimum dimensions based on a percentage of screen size
            double minWidth = Math.min(1024, screenBounds.getWidth() * 0.8);
            double minHeight = Math.min(768, screenBounds.getHeight() * 0.8);
            
            // Configure stage - set dimensions to match screen
            primaryStage.setTitle("TunTransport");
            primaryStage.setMinWidth(minWidth);
            primaryStage.setMinHeight(minHeight);
            
            // Create scene with dimensions based on screen size - important for initial layout
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            
            // Initialize to full screen size 
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            
            // Handle close request - add confirmation if not on home view
            primaryStage.setOnCloseRequest(event -> {
                if (!"/Home.fxml".equals(currentView)) {
                    event.consume(); // Prevent automatic closing
                    
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Return to Home Screen");
                    alert.setContentText("Would you like to return to the home screen or exit the application?");
                    
                    ButtonType homeButton = new ButtonType("Return to Home");
                    ButtonType exitButton = new ButtonType("Exit Application");
                    
                    alert.getButtonTypes().setAll(homeButton, exitButton, ButtonType.CANCEL);
                    
                    alert.showAndWait().ifPresent(buttonType -> {
                        if (buttonType == homeButton) {
                            navigateToHome();
                        } else if (buttonType == exitButton) {
                            Platform.exit();
                        }
                    });
                }
            });
            
            // Setup maximize state monitoring with strong enforcement
            ChangeListener<Boolean> maximizeStateListener = (obs, oldVal, newVal) -> {
                LOGGER.info("Window maximized state changed: " + oldVal + " -> " + newVal);
                if (!newVal && maximizationAttempted) {
                    LOGGER.info("Window was un-maximized, restoring state");
                    Platform.runLater(() -> {
                        // Try multiple approaches to ensure maximization
                        primaryStage.setMaximized(true);
                        
                        // If that doesn't work, set to screen bounds directly
                        if (!primaryStage.isMaximized()) {
                            LOGGER.info("Direct maximization failed, using screen bounds");
                            primaryStage.setX(screenBounds.getMinX());
                            primaryStage.setY(screenBounds.getMinY());
                            primaryStage.setWidth(screenBounds.getWidth());
                            primaryStage.setHeight(screenBounds.getHeight());
                        }
                    });
                }
            };
            primaryStage.maximizedProperty().addListener(maximizeStateListener);
            
            // Also monitor window size for debugging
            primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (maximizationAttempted) {
                    LOGGER.fine("Window width changed: " + oldVal + " -> " + newVal);
                }
            });
            
            // Add window shown handler to ensure maximization occurs after window is fully initialized
            primaryStage.setOnShown(event -> {
                LOGGER.info("Window shown event received, now maximizing");
                Platform.runLater(() -> {
                    // Try multiple approaches for maximization
                    primaryStage.setMaximized(true);
                    
                    // If that doesn't work, set to screen bounds directly
                    if (!primaryStage.isMaximized()) {
                        LOGGER.info("Initial maximization failed, using screen bounds");
                        primaryStage.setX(screenBounds.getMinX());
                        primaryStage.setY(screenBounds.getMinY());
                        primaryStage.setWidth(screenBounds.getWidth());
                        primaryStage.setHeight(screenBounds.getHeight());
                    }
                    
                    maximizationAttempted = true;
                    LOGGER.info("Window maximized on initialization, state: " + primaryStage.isMaximized() + 
                               ", size: " + primaryStage.getWidth() + "x" + primaryStage.getHeight());
                    
                    // Force repaint and focus
                    primaryStage.requestFocus();
                });
            });
            
            // Show the stage - maximization will happen in the onShown handler
            primaryStage.show();
            
            // Connect to database
            Connection con = DbContext.getInstance().getConn();
            if (con != null) {
                LOGGER.log(Level.INFO, "Connected to database");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting application", e);
            throw e;
        }
    }
}