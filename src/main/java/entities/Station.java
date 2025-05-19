package entities;

import jakarta.persistence.*;
import lombok.*;
import javafx.beans.property.*;

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