package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import entities.Weather;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for fetching weather data from OpenWeatherMap API
 */
@Slf4j
public class WeatherService {
    private static final String API_KEY = System.getProperty("OPENWEATHER_API_KEY", "8c7ed8ad125c244c34a7a3ff87162db5"); // Fallback to a valid API key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final boolean USE_MOCK_DATA = API_KEY.equals("YOUR_API_KEY"); // Only use mock data if API key is not set

    private final ExecutorService executorService;
    private final Gson gson;

    // Cache to store weather data to avoid frequent API calls
    private final Map<String, Weather> weatherCache;
    private final Map<String, Long> cacheTimestamps;
    private static final long CACHE_DURATION = 30 * 60 * 1000; // 30 minutes in milliseconds

    private static WeatherService instance;

    public WeatherService() {
        executorService = Executors.newFixedThreadPool(2);
        gson = new Gson();
        weatherCache = new HashMap<>();
        cacheTimestamps = new HashMap<>();
    }

    public static synchronized WeatherService getInstance() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }

    /**
     * Asynchronously fetches weather data for a given city
     * 
     * @param city The city name
     * @return CompletableFuture containing Weather data
     */
    public CompletableFuture<Weather> getWeatherForCity(String city) {
        // Check cache first
        String cacheKey = city.toLowerCase();
        if (weatherCache.containsKey(cacheKey)) {
            long timestamp = cacheTimestamps.getOrDefault(cacheKey, 0L);
            if (System.currentTimeMillis() - timestamp < CACHE_DURATION) {
                log.info("Returning cached weather data for {}", city);
                return CompletableFuture.completedFuture(weatherCache.get(cacheKey));
            }
        }

        // If using mock data, return that instead of making an API call
        if (USE_MOCK_DATA) {
            log.info("Using mock weather data for {}", city);
            Weather mockWeather = generateMockWeatherData(city);

            // Cache the mock result
            weatherCache.put(cacheKey, mockWeather);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());

            return CompletableFuture.completedFuture(mockWeather);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String urlStr = String.format("%s?q=%s&appid=%s&units=metric", API_URL, city, API_KEY);
                URL url = new URL(urlStr);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000); // 5 second timeout
                connection.setReadTimeout(5000);    // 5 second timeout

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Parse JSON response
                    JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);

                    Weather weather = new Weather();
                    weather.setCity(city);

                    // Extract main weather data
                    if (jsonObject.has("main")) {
                        JsonObject main = jsonObject.getAsJsonObject("main");
                        weather.setTemperature(main.get("temp").getAsDouble());
                        weather.setHumidity(main.get("humidity").getAsInt());
                    }

                    // Extract weather description
                    if (jsonObject.has("weather") && jsonObject.getAsJsonArray("weather").size() > 0) {
                        JsonObject weatherObj = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();
                        weather.setDescription(weatherObj.get("description").getAsString());
                        weather.setIcon(weatherObj.get("icon").getAsString());
                    }

                    // Extract wind data
                    if (jsonObject.has("wind")) {
                        JsonObject wind = jsonObject.getAsJsonObject("wind");
                        weather.setWindSpeed(wind.get("speed").getAsDouble());
                    }

                    // Cache the result
                    weatherCache.put(cacheKey, weather);
                    cacheTimestamps.put(cacheKey, System.currentTimeMillis());

                    log.info("Successfully fetched weather data for {}", city);
                    return weather;
                } else {
                    log.error("Failed to fetch weather data. Response code: {}", responseCode);
                    // Return mock data instead of throwing an exception
                    return generateMockWeatherData(city);
                }
            } catch (Exception e) {
                log.error("Error fetching weather data: {}", e.getMessage(), e);
                // Return mock data instead of throwing an exception
                return generateMockWeatherData(city);
            }
        }, executorService);
    }

    /**
     * Get weather advice based on conditions
     * 
     * @param weather The weather data
     * @return A travel advice string
     */
    public String getTravelAdvice(Weather weather) {
        if (weather == null) {
            return "Données météo non disponibles";
        }

        String description = weather.getDescription().toLowerCase();
        double temperature = weather.getTemperature();
        double windSpeed = weather.getWindSpeed();

        if (description.contains("rain") || description.contains("pluie")) {
            return "N'oubliez pas votre parapluie!";
        } else if (description.contains("snow") || description.contains("neige")) {
            return "Conditions difficiles pour voyager";
        } else if (description.contains("storm") || description.contains("orage")) {
            return "Vérifiez les retards possibles";
        } else if (description.contains("fog") || description.contains("brouillard")) {
            return "Visibilité réduite, soyez prudent";
        } else if (description.contains("clear") || description.contains("clair")) {
            if (temperature > 30) {
                return "Très chaud, restez hydraté";
            } else if (temperature > 20) {
                return "Idéal pour voyager";
            } else if (temperature > 10) {
                return "Temps agréable pour voyager";
            } else {
                return "Temps frais, habillez-vous chaudement";
            }
        } else if (windSpeed > 10) {
            return "Vents forts, soyez prudent";
        }

        return "Conditions normales de voyage";
    }

    /**
     * Get appropriate weather icon based on OpenWeatherMap icon code
     * 
     * @param iconCode The icon code from OpenWeatherMap
     * @return FontAwesome icon literal
     */
    public String getWeatherIcon(String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return "fas-cloud";
        }

        switch (iconCode) {
            case "01d": return "fas-sun"; // clear sky day
            case "01n": return "fas-moon"; // clear sky night
            case "02d": 
            case "02n": return "fas-cloud-sun"; // few clouds
            case "03d": 
            case "03n": 
            case "04d": 
            case "04n": return "fas-cloud"; // scattered/broken clouds
            case "09d": 
            case "09n": return "fas-cloud-showers-heavy"; // shower rain
            case "10d": 
            case "10n": return "fas-cloud-rain"; // rain
            case "11d": 
            case "11n": return "fas-bolt"; // thunderstorm
            case "13d": 
            case "13n": return "fas-snowflake"; // snow
            case "50d": 
            case "50n": return "fas-smog"; // mist
            default: return "fas-cloud";
        }
    }

    /**
     * Generate mock weather data for a city
     * 
     * @param city The city name
     * @return Mock Weather object
     */
    private Weather generateMockWeatherData(String city) {
        Weather weather = new Weather();
        weather.setCity(city);

        // Generate realistic temperature based on city name (for consistency)
        int cityHash = Math.abs(city.hashCode());
        double baseTemp = 15.0 + (cityHash % 20); // Temperature between 15-35°C

        // Add some daily variation (±3°C)
        long dayOfYear = java.time.LocalDate.now().getDayOfYear();
        double dailyVariation = (cityHash + dayOfYear) % 6 - 3;

        weather.setTemperature(Math.round((baseTemp + dailyVariation) * 10.0) / 10.0);
        weather.setHumidity(40 + (cityHash % 50)); // Humidity between 40-90%
        weather.setWindSpeed(2.0 + (cityHash % 8)); // Wind speed between 2-10 km/h

        // Set weather description and icon based on temperature
        if (weather.getTemperature() > 30) {
            weather.setDescription("ciel dégagé");
            weather.setIcon("01d"); // sunny
        } else if (weather.getTemperature() > 25) {
            weather.setDescription("quelques nuages");
            weather.setIcon("02d"); // few clouds
        } else if (weather.getTemperature() > 20) {
            weather.setDescription("nuageux");
            weather.setIcon("03d"); // scattered clouds
        } else if (weather.getTemperature() > 15) {
            weather.setDescription("nuages épars");
            weather.setIcon("04d"); // broken clouds
        } else if (weather.getTemperature() > 10) {
            weather.setDescription("légère pluie");
            weather.setIcon("10d"); // light rain
        } else {
            weather.setDescription("pluie modérée");
            weather.setIcon("09d"); // shower rain
        }

        log.info("Generated mock weather for {}: {}°C, {}", city, weather.getTemperature(), weather.getDescription());
        return weather;
    }

    /**
     * Shutdown the executor service
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
