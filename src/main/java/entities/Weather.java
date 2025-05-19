package entities;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing weather data
 */
@Data
@NoArgsConstructor
public class Weather {
    private String city;
    private double temperature;
    private int humidity;
    private String description;
    private String icon;
    private double windSpeed;
    
    /**
     * Format temperature with °C symbol
     * @return Formatted temperature string
     */
    public String getFormattedTemperature() {
        return String.format("%.1f°C", temperature);
    }
    
    /**
     * Format humidity with % symbol
     * @return Formatted humidity string
     */
    public String getFormattedHumidity() {
        return humidity + "%";
    }
    
    /**
     * Format wind speed with km/h
     * @return Formatted wind speed string
     */
    public String getFormattedWindSpeed() {
        return String.format("%.1f km/h", windSpeed);
    }
    
    /**
     * Get capitalized description
     * @return Capitalized description
     */
    public String getCapitalizedDescription() {
        if (description == null || description.isEmpty()) {
            return "";
        }
        return description.substring(0, 1).toUpperCase() + description.substring(1);
    }
}