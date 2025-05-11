package tn.esprit.testpifx.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.testpifx.Main;

import java.io.IOException;
import java.util.Objects;

/**
 * A utility class to standardize scene transitions in the application.
 * This class ensures consistent window size, positioning, and focus across all screens.
 */
public class SceneManager {

    /**
     * Changes the scene in the current window to a new FXML layout.
     * 
     * @param currentStage The current stage/window
     * @param fxmlPath The path to the FXML file to load
     * @param controller Optional controller instance if you need to pre-configure it before scene change
     * @return The controller instance of the new scene
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> T changeScene(Stage currentStage, String fxmlPath, T controller) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        
        // If a pre-configured controller is provided, use it
        if (controller != null) {
            loader.setController(controller);
        }
        
        Parent root = loader.load();
        T actualController = controller != null ? controller : loader.getController();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(
            SceneManager.class.getResource(Main.getCurrentCssFile())).toExternalForm());
        
        // First ensure the window is NOT maximized while changing scenes
        // This helps prevent issues with some JavaFX implementations
        currentStage.setMaximized(false);
        
        // Set the new scene
        currentStage.setScene(scene);
        
        // Apply standard window settings after scene change
        applyStandardWindowSettings(currentStage);
        
        return actualController;
    }
    
    /**
     * Changes the scene in the current window to a new FXML layout.
     * 
     * @param currentStage The current stage/window
     * @param fxmlPath The path to the FXML file to load
     * @return The controller instance of the new scene
     * @throws IOException If the FXML file cannot be loaded
     */
    public static <T> T changeScene(Stage currentStage, String fxmlPath) throws IOException {
        return changeScene(currentStage, fxmlPath, null);
    }
    
    /**
     * Applies standard window settings to ensure consistent behavior across the application.
     * This centralizes all window state management in one place.
     * 
     * @param stage The stage to configure
     */
    public static void applyStandardWindowSettings(Stage stage) {
        if (stage == null) return;
        
        // Set application title
        stage.setTitle("User Management System");
        
        // Set minimum dimensions
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        // Critical immediate settings
        stage.setIconified(false); // Ensure window is not minimized
        
        if (!stage.isShowing()) {
            stage.show();
        }
        
        // Apply multi-phase approach to ensure reliable window maximization
        maximizeStageReliably(stage);
        
        // Add a listener to monitor and maintain maximized state
        addMaximizationListener(stage);
    }
    
    /**
     * Adds a listener to ensure the window stays maximized.
     * This helps with situations where the window might get unmaximized unexpectedly.
     *
     * @param stage The stage to monitor
     */
    private static void addMaximizationListener(Stage stage) {
        // Remove any existing listener first to avoid duplicates
        stage.maximizedProperty().removeListener(maximizeListener);
        
        // Add listener to monitor maximized state
        stage.maximizedProperty().addListener(maximizeListener);
    }
    
    /**
     * Listener that detects when a window becomes unmaximized and restores maximized state.
     * This is static to avoid creating multiple instances.
     */
    private static final javafx.beans.value.ChangeListener<Boolean> maximizeListener = 
        (observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                // Window was maximized and now isn't - restore maximized state
                Stage stage = (Stage) ((javafx.beans.property.ReadOnlyBooleanProperty) observable).getBean();
                // Use Platform.runLater to avoid potential threading issues
                Platform.runLater(() -> stage.setMaximized(true));
            }
        };
    
    /**
     * A multi-phase reliable approach to maximizing a JavaFX window.
     * This handles various edge cases with JavaFX window management.
     *
     * @param stage The stage to maximize
     */
    private static void maximizeStageReliably(Stage stage) {
        // Phase 1: First attempt - immediate action
        stage.setMaximized(true);
        
        // Phase 2: Platform.runLater for UI thread synchronization
        Platform.runLater(() -> {
            // Center stage for better positioning
            stage.centerOnScreen();
            
            // Make sure window is at the front and has focus
            stage.toFront();
            stage.requestFocus();
            
            // Enforce maximized state
            stage.setMaximized(true);
            
            // Phase 3: Double-check with delayed runLater
            // This handles cases where the first maximization didn't stick
            Platform.runLater(() -> {
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }
                
                // Phase 4: Final check after a short delay
                // Some platforms need extra time to process window state changes
                new Thread(() -> {
                    try {
                        Thread.sleep(100);
                        Platform.runLater(() -> {
                            if (!stage.isMaximized()) {
                                stage.setMaximized(true);
                            }
                        });
                    } catch (InterruptedException e) {
                        // Ignore interruption
                    }
                }).start();
            });
        });
    }
}