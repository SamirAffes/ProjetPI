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
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String origin;
    
    @Column(nullable = false)
    private String destination;
    
    private double distance;
    
    private int estimatedDuration;
    
    @Column(nullable = false)
    private double basePrice;
    
    private int companyId;
    
    private String companyName;
    
    private String routeType;
    
    // New fields for enhanced routing
    private String transportMode; // Bus, Train, Taxi, Métro, TGM, Avion, Ferry
    
    private boolean isInternational;
    
    private boolean isIntraCity; // For routes within the same city
    
    // JavaFX properties for TableView binding
    @Transient
    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty originProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty destinationProperty = new SimpleStringProperty();
    @Transient
    private final DoubleProperty distanceProperty = new SimpleDoubleProperty();
    @Transient
    private final IntegerProperty estimatedDurationProperty = new SimpleIntegerProperty();
    @Transient
    private final DoubleProperty basePriceProperty = new SimpleDoubleProperty();
    @Transient
    private final IntegerProperty companyIdProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty companyNameProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty routeTypeProperty = new SimpleStringProperty();
    @Transient
    private final StringProperty transportModeProperty = new SimpleStringProperty();
    @Transient
    private final BooleanProperty isInternationalProperty = new SimpleBooleanProperty();
    @Transient
    private final BooleanProperty isIntraCityProperty = new SimpleBooleanProperty();
    
    // Convenience constructor for UI
    public Route(int id, String origin, String destination, double basePrice, int companyId) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.basePrice = basePrice;
        this.companyId = companyId;
        initializeProperties();
    }
    
    // Helper method to initialize properties from entity fields
    public void initializeProperties() {
        idProperty.set(id);
        originProperty.set(origin);
        destinationProperty.set(destination);
        distanceProperty.set(distance);
        estimatedDurationProperty.set(estimatedDuration);
        basePriceProperty.set(basePrice);
        companyIdProperty.set(companyId);
        companyNameProperty.set(companyName);
        routeTypeProperty.set(routeType);
        transportModeProperty.set(transportMode);
        isInternationalProperty.set(isInternational);
        isIntraCityProperty.set(isIntraCity);
    }
    
    // Properties getters for TableView
    public IntegerProperty idProperty() { return idProperty; }
    public StringProperty originProperty() { return originProperty; }
    public StringProperty destinationProperty() { return destinationProperty; }
    public DoubleProperty distanceProperty() { return distanceProperty; }
    public IntegerProperty estimatedDurationProperty() { return estimatedDurationProperty; }
    public DoubleProperty basePriceProperty() { return basePriceProperty; }
    public IntegerProperty companyIdProperty() { return companyIdProperty; }
    public StringProperty companyNameProperty() { return companyNameProperty; }
    public StringProperty routeTypeProperty() { return routeTypeProperty; }
    public StringProperty transportModeProperty() { return transportModeProperty; }
    public BooleanProperty isInternationalProperty() { return isInternationalProperty; }
    public BooleanProperty isIntraCityProperty() { return isIntraCityProperty; }
    
    // Convenience method for price calculations
    public double getPrice() { return getBasePrice(); }
    
    @Override
    public String toString() {
        return origin + " → " + destination;
    }
} 