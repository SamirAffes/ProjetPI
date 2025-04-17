package entities;

import utils.DateUtils;
import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class Reservation {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty routeId = new SimpleIntegerProperty();
    private final IntegerProperty transportId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty("PENDING");
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final BooleanProperty isPaid = new SimpleBooleanProperty(false);
    private final BooleanProperty roundTrip = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> returnDateTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<Route> route = new SimpleObjectProperty<>();
    private final ObjectProperty<Transport> transport = new SimpleObjectProperty<>();
    private final ObjectProperty<User> user = new SimpleObjectProperty<>();

    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public IntegerProperty routeIdProperty() { return routeId; }
    public IntegerProperty transportIdProperty() { return transportId; }
    public StringProperty statusProperty() { return status; }
    public DoubleProperty priceProperty() { return price; }
    public BooleanProperty roundTripProperty() { return roundTrip; }
    public BooleanProperty isPaidProperty() { return isPaid; }
    
    public StringProperty routeProperty() {
        if (route.get() == null) {
            return new SimpleStringProperty("N/A");
        }
        return new SimpleStringProperty(
            route.get().getOrigin() + " → " + route.get().getDestination()
        );
    }
    
    public StringProperty transportProperty() {
        if (transport.get() == null) {
            return new SimpleStringProperty("N/A");
        }
        return new SimpleStringProperty(
            transport.get().getName() + " (" + transport.get().getType() + ")"
        );
    }
    
    public StringProperty dateTimeProperty() {
        LocalDateTime dt = dateTime.get();
        return new SimpleStringProperty(dt == null ? "" : DateUtils.formatDateTime(dt));
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getUserId() { return userId.get(); }
    public void setUserId(int userId) { this.userId.set(userId); }

    public int getRouteId() { return routeId.get(); }
    public void setRouteId(int routeId) { this.routeId.set(routeId); }

    public int getTransportId() { return transportId.get(); }
    public void setTransportId(int transportId) { this.transportId.set(transportId); }

    public LocalDateTime getDateTime() { return dateTime.get(); }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime.set(dateTime); }
    public void setDateTime(Timestamp timestamp) { 
        this.dateTime.set(timestamp != null ? timestamp.toLocalDateTime() : null); 
    }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }

    public boolean getIsPaid() { return isPaid.get(); }
    public void setIsPaid(boolean isPaid) { this.isPaid.set(isPaid); }

    public boolean isRoundTrip() { return roundTrip.get(); }
    public void setRoundTrip(boolean roundTrip) { this.roundTrip.set(roundTrip); }

    public LocalDateTime getReturnDateTime() { return returnDateTime.get(); }
    public void setReturnDateTime(LocalDateTime returnDateTime) { this.returnDateTime.set(returnDateTime); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    public Route getRoute() { return route.get(); }
    public void setRoute(Route route) { this.route.set(route); }

    public Transport getTransport() { return transport.get(); }
    public void setTransport(Transport transport) { this.transport.set(transport); }

    public User getUser() { return user.get(); }
    public void setUser(User user) { this.user.set(user); }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + getId() +
                ", userId=" + getUserId() +
                ", routeId=" + getRouteId() +
                ", transportId=" + getTransportId() +
                ", dateTime=" + getDateTime() +
                ", status=" + getStatus() +
                ", price=" + getPrice() +
                ", isRoundTrip=" + isRoundTrip() +
                ", returnDateTime=" + getReturnDateTime() +
                "}";
    }
} 