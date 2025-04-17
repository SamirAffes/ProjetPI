package entities;

import javafx.beans.property.*;

public class Route {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty origin = new SimpleStringProperty();
    private final StringProperty destination = new SimpleStringProperty();
    private final DoubleProperty distance = new SimpleDoubleProperty();
    private final IntegerProperty estimatedDuration = new SimpleIntegerProperty();
    private final DoubleProperty basePrice = new SimpleDoubleProperty();
    private final IntegerProperty companyId = new SimpleIntegerProperty();

    public Route() {}

    public Route(int id, String origin, String destination, double distance, 
                 int estimatedDuration, double basePrice, int companyId) {
        setId(id);
        setOrigin(origin);
        setDestination(destination);
        setDistance(distance);
        setEstimatedDuration(estimatedDuration);
        setBasePrice(basePrice);
        setCompanyId(companyId);
    }
    
    // Constructor used in NewReservationController
    public Route(int id, String origin, String destination, double basePrice, int companyId) {
        setId(id);
        setOrigin(origin);
        setDestination(destination);
        setBasePrice(basePrice);
        setCompanyId(companyId);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Origin
    public String getOrigin() { return origin.get(); }
    public void setOrigin(String origin) { this.origin.set(origin); }
    public StringProperty originProperty() { return origin; }

    // Destination
    public String getDestination() { return destination.get(); }
    public void setDestination(String destination) { this.destination.set(destination); }
    public StringProperty destinationProperty() { return destination; }

    // Distance
    public double getDistance() { return distance.get(); }
    public void setDistance(double distance) { this.distance.set(distance); }
    public DoubleProperty distanceProperty() { return distance; }

    // Estimated Duration
    public int getEstimatedDuration() { return estimatedDuration.get(); }
    public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration.set(estimatedDuration); }
    public IntegerProperty estimatedDurationProperty() { return estimatedDuration; }

    // Base Price
    public double getBasePrice() { return basePrice.get(); }
    public void setBasePrice(double basePrice) { this.basePrice.set(basePrice); }
    public DoubleProperty basePriceProperty() { return basePrice; }
    
    // Alias for getBasePrice for compatibility with NewReservationController
    public double getPrice() { return getBasePrice(); }

    // Company ID
    public int getCompanyId() { return companyId.get(); }
    public void setCompanyId(int companyId) { this.companyId.set(companyId); }
    public IntegerProperty companyIdProperty() { return companyId; }

    @Override
    public String toString() {
        return origin.get() + " → " + destination.get();
    }
} 