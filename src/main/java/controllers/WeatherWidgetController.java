package controllers;

import entities.Weather;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.WeatherService;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for the weather widget component
 */
@Slf4j
public class WeatherWidgetController {

    @FXML
    private VBox weatherWidget;

    @FXML
    private Label cityLabel;

    @FXML
    private Label temperatureLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label adviceLabel;

    @FXML
    private FontIcon weatherIcon;

    private final WeatherService weatherService = WeatherService.getInstance();
    private String defaultCity = "Tunis"; // Default city

    /**
     * Initialize the weather widget
     */
    @FXML
    public void initialize() {
        // Load weather data for default city
        loadWeatherData(defaultCity);
    }

    /**
     * Set the city for the weather widget and load its weather data
     * 
     * @param city The city name
     */
    public void setCity(String city) {
        if (city != null && !city.isEmpty()) {
            this.defaultCity = city;
            loadWeatherData(city);
        }
    }

    /**
     * Load weather data for the specified city
     * 
     * @param city The city name
     */
    private void loadWeatherData(String city) {
        // Show loading state
        updateUIForLoading();

        // Fetch weather data asynchronously
        CompletableFuture<Weather> weatherFuture = weatherService.getWeatherForCity(city);
        
        weatherFuture.thenAccept(weather -> {
            // Update UI on JavaFX thread
            Platform.runLater(() -> updateWeatherUI(weather));
        }).exceptionally(ex -> {
            // Handle errors on JavaFX thread
            Platform.runLater(() -> handleWeatherError(ex));
            return null;
        });
    }

    /**
     * Update UI to show loading state
     */
    private void updateUIForLoading() {
        cityLabel.setText(defaultCity);
        temperatureLabel.setText("--°C");
        descriptionLabel.setText("Chargement...");
        adviceLabel.setText("");
        weatherIcon.setIconLiteral("fas-cloud");
    }

    /**
     * Update the UI with weather data
     * 
     * @param weather The weather data
     */
    private void updateWeatherUI(Weather weather) {
        if (weather == null) {
            handleWeatherError(new RuntimeException("Weather data is null"));
            return;
        }

        try {
            cityLabel.setText(weather.getCity());
            temperatureLabel.setText(weather.getFormattedTemperature());
            descriptionLabel.setText(weather.getCapitalizedDescription());
            adviceLabel.setText(weatherService.getTravelAdvice(weather));
            
            // Set appropriate weather icon
            String iconLiteral = weatherService.getWeatherIcon(weather.getIcon());
            weatherIcon.setIconLiteral(iconLiteral);
            
            log.info("Weather widget updated for {}: {}, {}", 
                    weather.getCity(), weather.getFormattedTemperature(), weather.getDescription());
        } catch (Exception e) {
            log.error("Error updating weather UI: {}", e.getMessage(), e);
            handleWeatherError(e);
        }
    }

    /**
     * Handle errors when fetching weather data
     * 
     * @param ex The exception
     */
    private void handleWeatherError(Throwable ex) {
        log.error("Error fetching weather data: {}", ex.getMessage(), ex);
        
        cityLabel.setText(defaultCity);
        temperatureLabel.setText("--°C");
        descriptionLabel.setText("Données non disponibles");
        adviceLabel.setText("Vérifiez votre connexion internet");
        weatherIcon.setIconLiteral("fas-exclamation-triangle");
    }

    /**
     * Refresh weather data
     */
    @FXML
    public void refreshWeather() {
        loadWeatherData(defaultCity);
    }
}