package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;
import services.WeatherService;
import services.WeatherInfo;

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

    private final WeatherService weatherService;
    private String defaultCity = "Tunis";

    public WeatherWidgetController() {
        this.weatherService = new WeatherService();
    }

    @FXML
    public void initialize() {
        loadWeatherData(defaultCity);
    }

    public void setCity(String city) {
        if (city != null && !city.isEmpty()) {
            this.defaultCity = city;
            loadWeatherData(city);
        }
    }

    private void loadWeatherData(String city) {
        updateUIForLoading();

        // Utiliser un thread séparé pour ne pas bloquer l'interface
        new Thread(() -> {
            try {
                WeatherInfo weather = weatherService.getWeatherForCity(city);
                Platform.runLater(() -> updateWeatherUI(weather));
            } catch (Exception ex) {
                Platform.runLater(() -> handleWeatherError(ex));
            }
        }).start();
    }

    private void updateUIForLoading() {
        Platform.runLater(() -> {
            cityLabel.setText(defaultCity);
            temperatureLabel.setText("--°C");
            descriptionLabel.setText("Chargement...");
            adviceLabel.setText("");
            weatherIcon.setIconLiteral("fas-cloud");
        });
    }

    private void updateWeatherUI(WeatherInfo weather) {
        if (weather != null) {
            cityLabel.setText(defaultCity);
            temperatureLabel.setText(String.format("%.1f°C", weather.getTemperature()));
            descriptionLabel.setText(weather.getDescription());

            // Définir un conseil en fonction de la température
            if (weather.getTemperature() > 30) {
                adviceLabel.setText("Pensez à bien vous hydrater");
                weatherIcon.setIconLiteral("fas-sun");
            } else if (weather.getTemperature() < 10) {
                adviceLabel.setText("Couvrez-vous bien");
                weatherIcon.setIconLiteral("fas-snowflake");
            } else {
                adviceLabel.setText("Température agréable");
                weatherIcon.setIconLiteral("fas-cloud-sun");
            }
        } else {
            handleWeatherError(new Exception("Données météo non disponibles"));
        }
    }

    private void handleWeatherError(Throwable ex) {
        log.error("Erreur lors de la récupération de la météo", ex);
        cityLabel.setText(defaultCity);
        temperatureLabel.setText("--°C");
        descriptionLabel.setText("Erreur de chargement");
        adviceLabel.setText("Veuillez réessayer plus tard");
        weatherIcon.setIconLiteral("fas-exclamation-triangle");
    }
}

