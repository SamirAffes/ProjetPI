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
public class Transport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(nullable = false)
    private String name;
    
    private int typeId;
    
    @Column(nullable = false)
    private String type;
    
    private int capacity;
    
    private int companyId;
    
    private String licensePlate;
    
    private boolean available = true;
    
    // JavaFX properties for TableView binding
    @Transient
    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty nameProperty = new SimpleStringProperty();
    @Transient
    private final IntegerProperty typeIdProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty typeProperty = new SimpleStringProperty();
    @Transient
    private final IntegerProperty capacityProperty = new SimpleIntegerProperty();
    @Transient
    private final IntegerProperty companyIdProperty = new SimpleIntegerProperty();
    @Transient
    private final StringProperty licensePlateProperty = new SimpleStringProperty();
    @Transient
    private final BooleanProperty availableProperty = new SimpleBooleanProperty();
    
    // Convenience constructor for UI
    public Transport(int id, String name, String type, int capacity, int companyId, 
                    String licensePlate, boolean available) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.companyId = companyId;
        this.licensePlate = licensePlate;
        this.available = available;
        initializeProperties();
    }
    
    // Helper method to initialize properties from entity fields
    public void initializeProperties() {
        idProperty.set(id);
        nameProperty.set(name);
        typeIdProperty.set(typeId);
        typeProperty.set(type);
        capacityProperty.set(capacity);
        companyIdProperty.set(companyId);
        licensePlateProperty.set(licensePlate);
        availableProperty.set(available);
    }
    
    // Properties getters for TableView
    public IntegerProperty idProperty() { return idProperty; }
    public StringProperty nameProperty() { return nameProperty; }
    public IntegerProperty typeIdProperty() { return typeIdProperty; }
    public StringProperty typeProperty() { return typeProperty; }
    public IntegerProperty capacityProperty() { return capacityProperty; }
    public IntegerProperty companyIdProperty() { return companyIdProperty; }
    public StringProperty licensePlateProperty() { return licensePlateProperty; }
    public BooleanProperty availableProperty() { return availableProperty; }
    
    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
} 