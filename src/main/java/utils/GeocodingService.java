package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service for geocoding (converting place names to coordinates) using the Nominatim API.
 */
public class GeocodingService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);
    private static final String NOMINATIM_API_URL = "https://nominatim.openstreetmap.org/search";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Get coordinates for a location name asynchronously.
     * 
     * @param locationName The name of the location to geocode
     * @return A CompletableFuture that will contain the coordinates [lat, lng] or null if not found
     */
    public static CompletableFuture<double[]> getCoordinatesAsync(String locationName) {
        try {
            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8);
            String url = NOMINATIM_API_URL + 
                    "?q=" + encodedLocation + 
                    "&format=json&limit=1";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .header("User-Agent", "ProjetPI JavaFX Application")
                    .build();
            
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(GeocodingService::parseResponse)
                    .exceptionally(e -> {
                        logger.error("Error getting coordinates for location: " + locationName, e);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Error preparing geocoding request for: " + locationName, e);
            CompletableFuture<double[]> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Get coordinates for a location name synchronously.
     * 
     * @param locationName The name of the location to geocode
     * @return The coordinates [lat, lng] or null if not found or error occurs
     */
    public static double[] getCoordinates(String locationName) {
        try {
            return getCoordinatesAsync(locationName).get();
        } catch (Exception e) {
            logger.error("Error getting coordinates synchronously for: " + locationName, e);
            return null;
        }
    }
    
    /**
     * Parse the JSON response from Nominatim API.
     * 
     * @param jsonResponse The JSON response string
     * @return The coordinates [lat, lng] or null if not found
     */
    private static double[] parseResponse(String jsonResponse) {
        try {
            JsonArray results = JsonParser.parseString(jsonResponse).getAsJsonArray();
            
            if (results.size() > 0) {
                JsonObject result = results.get(0).getAsJsonObject();
                double lat = result.get("lat").getAsDouble();
                double lon = result.get("lon").getAsDouble();
                return new double[]{lat, lon};
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error parsing geocoding response: " + jsonResponse, e);
            return null;
        }
    }
    
    /**
     * Check if coordinates are available for a given location.
     * 
     * @param locationName The name of the location to check
     * @return true if coordinates are available, false otherwise
     */
    public static boolean hasCoordinates(String locationName) {
        double[] coords = getCoordinates(locationName);
        return coords != null;
    }
}