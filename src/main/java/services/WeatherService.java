package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Properties;

@Slf4j
public class WeatherService {
    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public WeatherService() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("weather.properties")) {
            if (input == null) {
                log.error("Fichier weather.properties non trouvé");
                throw new RuntimeException("Fichier de configuration météo non trouvé");
            }
            props.load(input);
            this.apiKey = props.getProperty("weather.api.key");
            this.baseUrl = props.getProperty("weather.api.base_url");

            log.info("Configuration météo chargée - URL: {}", baseUrl);

            if (this.apiKey == null || this.apiKey.isEmpty()) {
                log.error("Clé API météo non trouvée dans la configuration");
                throw new RuntimeException("Clé API météo non configurée");
            }
        } catch (IOException e) {
            log.error("Erreur lors du chargement de la configuration météo", e);
            throw new RuntimeException("Impossible de charger la configuration météo", e);
        }
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    public WeatherInfo getWeatherForCity(String city) {
        try {
            // Ajouter le pays pour les villes tunisiennes
            if (!city.contains("(")) {
                city += ",TN"; // TN est le code ISO pour la Tunisie
            }

            // Encoder le nom de la ville pour l'URL
            String encodedCity = URLEncoder.encode(city, "UTF-8")
                .replace("+", "%20"); // Remplacer les + par %20 pour les espaces

            String url = String.format("%s?q=%s&appid=%s&units=metric&lang=fr",
                baseUrl, encodedCity, apiKey);
            log.info("Appel API météo pour {}: {}", city, url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;

                if (!response.isSuccessful()) {
                    log.error("Erreur API météo: {} - {}", response.code(), responseBody);
                    return null;
                }

                if (responseBody == null) {
                    log.error("Réponse API vide pour {}", city);
                    return null;
                }

                log.debug("Réponse API reçue pour {}: {}", city, responseBody);

                JsonNode root = mapper.readTree(responseBody);

                WeatherInfo weatherInfo = new WeatherInfo();
                weatherInfo.setTemperature(root.path("main").path("temp").asDouble());
                weatherInfo.setDescription(root.path("weather").get(0).path("description").asText());
                weatherInfo.setHumidity(root.path("main").path("humidity").asInt());
                weatherInfo.setWindSpeed(root.path("wind").path("speed").asDouble());

                log.info("Météo récupérée pour {}: {}°C, {}", city,
                    weatherInfo.getTemperature(), weatherInfo.getDescription());

                return weatherInfo;
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la météo pour " + city, e);
            return null;
        }
    }

    /**
     * Récupère les informations météo à partir des coordonnées géographiques
     * @param lat Latitude
     * @param lon Longitude
     * @return WeatherInfo ou null en cas d'erreur
     */
    public WeatherInfo getWeatherByCoordinates(double lat, double lon) {
        try {
            String url = String.format("%s?lat=%.6f&lon=%.6f&appid=%s&units=metric&lang=fr",
                baseUrl, lat, lon, apiKey);
            log.info("URL de l'appel API météo: {}", url);

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;
                log.info("Code réponse API: {}", response.code());
                log.info("Réponse API: {}", responseBody);

                if (!response.isSuccessful()) {
                    log.error("Erreur API météo: {} - {}", response.code(), responseBody);
                    return null;
                }

                if (responseBody == null) {
                    log.error("Réponse API vide pour lat={}, lon={}", lat, lon);
                    return null;
                }

                JsonNode root = mapper.readTree(responseBody);

                WeatherInfo weatherInfo = new WeatherInfo();
                weatherInfo.setTemperature(root.path("main").path("temp").asDouble());
                weatherInfo.setDescription(root.path("weather").get(0).path("description").asText());
                weatherInfo.setHumidity(root.path("main").path("humidity").asInt());
                weatherInfo.setWindSpeed(root.path("wind").path("speed").asDouble());

                log.info("Météo récupérée: {}°C, {}",
                    weatherInfo.getTemperature(),
                    weatherInfo.getDescription());

                return weatherInfo;
            }
        } catch (Exception e) {
            log.error("Erreur détaillée lors de la récupération de la météo pour lat={}, lon={}: {}",
                lat, lon, e.getMessage(), e);
            return null;
        }
    }
}
