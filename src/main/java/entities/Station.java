package entities;

import jakarta.persistence.*;
import lombok.*;
import javafx.beans.property.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String city;
    
    private String address;
    
    // GPS coordinates
    private double latitude;
    private double longitude;
    
    // Type of station: bus, train, metro, airport, port, etc.
    private String stationType;
    
    // Optional organization ID if this station belongs to a specific organization
    private Integer organisationId;
    
    // Station code (e.g., airport code, station identifier)
    private String stationCode;
    
    // Opening hours
    private String openingHours;
    
    // JavaFX properties for TableView binding
    @Transient
    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty nameProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty cityProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty addressProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty stationTypeProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty stationCodeProperty = new SimpleStringProperty();
    
    private static final Map<String, double[]> DEFAULT_COORDINATES = new HashMap<>() {{
        // Major Tunisian cities
        put("Tunis", new double[]{36.8065, 10.1815});
        put("Sfax", new double[]{34.7398, 10.7600});
        put("Sousse", new double[]{35.8245, 10.6346});
        put("Kairouan", new double[]{35.6781, 10.0959});
        put("Bizerte", new double[]{37.2746, 9.8732});
        put("Gabès", new double[]{33.8812, 10.0982});
        put("Ariana", new double[]{36.8625, 10.1956});
        put("Gafsa", new double[]{34.4311, 8.7757});
        put("Monastir", new double[]{35.7780, 10.8262});
        put("Nabeul", new double[]{36.4562, 10.7310});
        put("Ben Arous", new double[]{36.7528, 10.2320});
        put("La Marsa", new double[]{36.8842, 10.3249});
        put("Kasserine", new double[]{35.1691, 8.8309});
        put("Douz", new double[]{33.4665, 9.0233});
        put("Tozeur", new double[]{33.9197, 8.1335});
        put("Tataouine", new double[]{32.9298, 10.4509});
        put("Hammamet", new double[]{36.4022, 10.6122});
        put("Mahdia", new double[]{35.5047, 11.0622});
        put("Medenine", new double[]{33.3399, 10.5017});
        put("Tabarka", new double[]{36.9542, 8.7600});

        // Locations within Tunis
        put("Tunis - Centre Ville", new double[]{36.7992, 10.1802});
        put("Tunis - Lac 1", new double[]{36.8327, 10.2352});
        put("Tunis - Lac 2", new double[]{36.8425, 10.2562});
        put("Tunis - La Goulette", new double[]{36.8183, 10.3050});
        put("Tunis - Carthage", new double[]{36.8583, 10.3236});
        put("Tunis - Sidi Bou Said", new double[]{36.8688, 10.3416});
        put("Tunis - Bardo", new double[]{36.8092, 10.1394});
        put("Tunis - El Menzah", new double[]{36.8361, 10.1689});
        put("Tunis - Aéroport", new double[]{36.8514, 10.2271});

        // International destinations
        put("Paris (France)", new double[]{48.8566, 2.3522});
        put("Marseille (France)", new double[]{43.2965, 5.3698});
        put("Rome (Italie)", new double[]{41.9028, 12.4964});
        put("Barcelone (Espagne)", new double[]{41.3851, 2.1734});
        put("Alger (Algérie)", new double[]{36.7538, 3.0588});
    }};

    /**
     * Récupère les coordonnées GPS d'une station à partir de son code
     * @param code Le code de la station
     * @return Un tableau de deux doubles [latitude, longitude] ou null si le code n'existe pas
     */
    public static double[] getCoordinates(String code) {
        return DEFAULT_COORDINATES.get(code);
    }

    // Helper method to initialize properties from entity fields
    public void initializeProperties() {
        idProperty.set(id);
        nameProperty.set(name);
        cityProperty.set(city);
        if (address != null) addressProperty.set(address);
        if (stationType != null) stationTypeProperty.set(stationType);
        if (stationCode != null) stationCodeProperty.set(stationCode);
    }
    
    // Properties getters for TableView
    public IntegerProperty idProperty() { return idProperty; }
    public StringProperty nameProperty() { return nameProperty; }
    public StringProperty cityProperty() { return cityProperty; }
    public StringProperty addressProperty() { return addressProperty; }
    public StringProperty stationTypeProperty() { return stationTypeProperty; }
    public StringProperty stationCodeProperty() { return stationCodeProperty; }
    
    @Override
    public String toString() {
        return name + (stationCode != null ? " (" + stationCode + ")" : "");
    }
}

