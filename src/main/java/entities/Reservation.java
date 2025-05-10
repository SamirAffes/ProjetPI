package entities;

import jakarta.persistence.*;
import lombok.*;
import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private int userId;
    
    @Column(nullable = false)
    private int routeId;
    
    @Column(nullable = false)
    private int transportId;
    
    @Column(nullable = false)
    private LocalDateTime dateTime;
    
    @Column(nullable = false)
    private String status = "PENDING";
    
    @Column(nullable = false)
    private double price;
    
    @Column(nullable = false)
    private boolean isPaid = false;
    
    @Column(nullable = false)
    private boolean roundTrip = false;
    
    @Column(nullable = false)
    private boolean paid = false;
    
    @Column(nullable = false)
    private int routeCompanyId = 0;
    
    private LocalDateTime returnDateTime;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    @Transient
    private Route route;
    
    @Transient
    private Transport transport;
    
    @Transient
    private User user;
    
    // JavaFX properties for TableView binding
    @Transient
    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    @Transient
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    @Transient
    private final IntegerProperty routeIdProperty = new SimpleIntegerProperty();
    @Transient
    private final IntegerProperty transportIdProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty statusProperty = new SimpleStringProperty();
    @Transient
    private final DoubleProperty priceProperty = new SimpleDoubleProperty();
    @Transient
    private final BooleanProperty isPaidProperty = new SimpleBooleanProperty();
    @Transient
    private final BooleanProperty roundTripProperty = new SimpleBooleanProperty();
    
    // Helper method to initialize properties from entity fields
    public void initializeProperties() {
        idProperty.set(id);
        userIdProperty.set(userId);
        routeIdProperty.set(routeId);
        transportIdProperty.set(transportId);
        statusProperty.set(status);
        priceProperty.set(price);
        isPaidProperty.set(isPaid);
        roundTripProperty.set(roundTrip);
    }
    
    // Properties getters for TableView
    public IntegerProperty idProperty() { return idProperty; }
    public IntegerProperty userIdProperty() { return userIdProperty; }
    public IntegerProperty routeIdProperty() { return routeIdProperty; }
    public IntegerProperty transportIdProperty() { return transportIdProperty; }
    public StringProperty statusProperty() { return statusProperty; }
    public DoubleProperty priceProperty() { return priceProperty; }
    public BooleanProperty isPaidProperty() { return isPaidProperty; }
    public BooleanProperty roundTripProperty() { return roundTripProperty; }
    
    public StringProperty routeProperty() {
        if (route == null) {
            return new SimpleStringProperty("N/A");
        }
        return new SimpleStringProperty(route.getOrigin() + " → " + route.getDestination());
    }
    
    public StringProperty transportProperty() {
        if (transport == null) {
            return new SimpleStringProperty("N/A");
        }
        return new SimpleStringProperty(transport.getName() + " (" + transport.getType() + ")");
    }
    
    // Specific method to match the controller call
    public void setIsPaid(boolean isPaid) {
        this.isPaid = isPaid;
        this.paid = isPaid;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
