package entities;

import javafx.beans.property.*;

public class Transport {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty typeId = new SimpleIntegerProperty();
    private final StringProperty type = new SimpleStringProperty(); // BUS, TAXI, TRAIN, etc.
    private final IntegerProperty capacity = new SimpleIntegerProperty();
    private final IntegerProperty companyId = new SimpleIntegerProperty();
    private final StringProperty licensePlate = new SimpleStringProperty();
    private final BooleanProperty available = new SimpleBooleanProperty(true);
    
    public Transport() {}
    
    public Transport(int id, String name, int typeId, String type, int capacity, int companyId, 
                    String licensePlate, boolean available) {
        setId(id);
        setName(name);
        setTypeId(typeId);
        setType(type);
        setCapacity(capacity);
        setCompanyId(companyId);
        setLicensePlate(licensePlate);
        setAvailable(available);
    }
    
    // Constructor used in NewReservationController
    public Transport(int id, String name, String type, int capacity, int companyId, 
                    String licensePlate, boolean available) {
        setId(id);
        setName(name);
        setType(type);
        setCapacity(capacity);
        setCompanyId(companyId);
        setLicensePlate(licensePlate);
        setAvailable(available);
    }
    
    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    // Name
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    // Type ID
    public int getTypeId() { return typeId.get(); }
    public void setTypeId(int typeId) { this.typeId.set(typeId); }
    public IntegerProperty typeIdProperty() { return typeId; }
    
    // Type
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }
    
    // Capacity
    public int getCapacity() { return capacity.get(); }
    public void setCapacity(int capacity) { this.capacity.set(capacity); }
    public IntegerProperty capacityProperty() { return capacity; }
    
    // Company ID
    public int getCompanyId() { return companyId.get(); }
    public void setCompanyId(int companyId) { this.companyId.set(companyId); }
    public IntegerProperty companyIdProperty() { return companyId; }
    
    // License Plate
    public String getLicensePlate() { return licensePlate.get(); }
    public void setLicensePlate(String licensePlate) { this.licensePlate.set(licensePlate); }
    public StringProperty licensePlateProperty() { return licensePlate; }
    
    // Available
    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean available) { this.available.set(available); }
    public BooleanProperty availableProperty() { return available; }
    
    @Override
    public String toString() {
        return name.get() + " (" + type.get() + ")";
    }
} 