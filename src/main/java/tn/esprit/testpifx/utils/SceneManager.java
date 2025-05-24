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
     */    public static <T> T changeScene(Stage currentStage, String fxmlPath, T controller) throws IOException {
        System.out.println("SceneManager.changeScene: Loading " + fxmlPath);
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        
        // If a pre-configured controller is provided, use it
        if (controller != null) {
            loader.setController(controller);
            System.out.println("Using pre-configured controller of type: " + controller.getClass().getSimpleName());
        }
        
        // Load the FXML file
        Parent root = loader.load();
        T actualController = controller != null ? controller : loader.getController();
        System.out.println("Controller loaded: " + actualController.getClass().getSimpleName());
          Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(
            SceneManager.class.getResource(Main.getCurrentCssFile())).toExternalForm());
        
        // During auto-login, avoid changing maximization state to prevent flickering
        boolean isAutoLogin = UserSessionManager.isSessionValid() && UserPreferences.hasRememberedCredentials();
        if (!isAutoLogin) {
            // Only for normal navigation, temporarily reset maximized state
            currentStage.setMaximized(false);
        }
        
        // Set the new scene
        currentStage.setScene(scene);
        System.out.println("New scene set on stage");
        
        // Apply standard window settings after scene change
        applyStandardWindowSettings(currentStage);
        
        // Verify that we properly maintain application state during scene changes
        if (UserSessionManager.isSessionValid()) {
            System.out.println("Session is valid during scene change. Current user: " + 
                              UserSessionManager.getCurrentUser().getUsername());
        } else {
            System.out.println("No valid user session during scene change");
        }
        
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
     * This helps with situations where the window might get unmaximized unexpectedly,
     * but only performs actions when needed to avoid screen flickering.
     *
     * @param stage The stage to monitor
     */
    private static void addMaximizationListener(Stage stage) {
        // Remove any existing listeners to avoid duplicates and multiple triggers
        stage.maximizedProperty().removeListener(maximizeListener);
        
        // For now, we'll disable the automatic maximization listener to avoid screen flickering
        // This means the user can manually resize the window if desired
        // If the automatic maximization behavior is needed, this can be uncommented
        // stage.maximizedProperty().addListener(maximizeListener);
    }
    
    /**
     * Listener that detects when a window becomes unmaximized and restores maximized state.
     * This is static to avoid creating multiple instances.
     */
    private static final javafx.beans.value.ChangeListener<Boolean> maximizeListener = 
        (observable, oldValue, newValue) -> {
            // Only respond if there's a real state change from maximized to non-maximized
            if (oldValue && !newValue) {
                // Window was maximized and now isn't - restore maximized state
                Stage stage = (Stage) ((javafx.beans.property.ReadOnlyBooleanProperty) observable).getBean();
                // We'll do this once and not in Platform.runLater to avoid cascading events
                stage.setMaximized(true);
            }
        };    /**
     * A simplified approach to maximizing a JavaFX window that avoids excessive changes
     * to prevent screen flickering.
     *
     * @param stage The stage to maximize
     */
    private static void maximizeStageReliably(Stage stage) {
        // Skip maximization during auto-login to reduce screen flickering
        if (UserSessionManager.isSessionValid() && UserPreferences.hasRememberedCredentials()) {
            System.out.println("Auto-login detected, skipping window maximization to prevent flickering");
            
            // Still ensure the window is visible and has focus
            Platform.runLater(() -> {
                stage.toFront();
                stage.requestFocus();
            });
            return;
        }
        
        // For normal navigation, apply maximization
        if (!stage.isMaximized()) {
            // Simple one-time maximization
            stage.setMaximized(true);
            stage.toFront();
            stage.requestFocus();
        }
    }
}