package tn.esprit.testpifx.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.testpifx.Main;
import tn.esprit.testpifx.controllers.ResetPasswordController;
import tn.esprit.testpifx.controllers.VerifyAccountController;
import tn.esprit.testpifx.services.UserService;
import tn.esprit.testpifx.services.ServiceProvider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Handles URLs for token-based actions like account verification and password reset.
 */
public class TokenUrlHandler {
    private static final Logger logger = LoggerFactory.getLogger(TokenUrlHandler.class);
    
    /**
     * Processes a URL that may contain token-based actions.
     * 
     * @param url The URL to process
     * @return true if the URL was handled, false otherwise
     */
    public static boolean handleUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();
            
            // Only handle localhost URLs for security
            if (!"localhost".equals(host)) {
                return false;
            }
            
            // Parse query parameters
            Map<String, String> queryParams = parseQueryParameters(query);
            String token = queryParams.get("token");
            
            if (token == null || token.isEmpty()) {
                return false;
            }
            
            if ("/verify-account".equals(path)) {
                handleVerifyAccount(token);
                return true;
            } else if ("/password-reset".equals(path)) {
                handlePasswordReset(token);
                return true;
            }
            
            return false;
        } catch (URISyntaxException e) {
            logger.error("Invalid URL format", e);
            return false;
        }
    }
    
    /**
     * Handle account verification token.
     * 
     * @param token The verification token
     */
    private static void handleVerifyAccount(String token) {
        Platform.runLater(() -> {
            try {
                UserService userService = ServiceProvider.getUserService();
                
                FXMLLoader loader = new FXMLLoader(TokenUrlHandler.class.getResource("/tn/esprit/testpifx/views/verify-account.fxml"));
                Parent root = loader.load();
                
                VerifyAccountController controller = loader.getController();
                controller.initialize(userService, token);
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(TokenUrlHandler.class.getResource(Main.getCurrentCssFile())).toExternalForm());
                
                Stage stage = new Stage();
                stage.setTitle("Account Verification");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.error("Failed to load verification screen", e);
            }
        });
    }
    
    /**
     * Handle password reset token.
     * 
     * @param token The password reset token
     */
    private static void handlePasswordReset(String token) {
        Platform.runLater(() -> {
            try {
                UserService userService = ServiceProvider.getUserService();
                
                FXMLLoader loader = new FXMLLoader(TokenUrlHandler.class.getResource("/tn/esprit/testpifx/views/reset-password.fxml"));
                Parent root = loader.load();
                
                ResetPasswordController controller = loader.getController();
                controller.initialize(userService, token);
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(TokenUrlHandler.class.getResource(Main.getCurrentCssFile())).toExternalForm());
                
                Stage stage = new Stage();
                stage.setTitle("Reset Password");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.error("Failed to load password reset screen", e);
            }
        });
    }
    
    /**
     * Parse query parameters from a URL query string.
     * 
     * @param query The query string (format: param1=value1&param2=value2)
     * @return Map of parameter names to values
     */
    private static Map<String, String> parseQueryParameters(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = pair.substring(0, idx);
                String value = idx < pair.length() - 1 ? pair.substring(idx + 1) : "";
                params.put(key, value);
            }
        }
        
        return params;
    }
}
