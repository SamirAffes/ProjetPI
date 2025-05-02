package entities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

public class Line {



    @Getter @Setter
    private double price;
    @Getter @Setter
    private final SimpleStringProperty number;

    @Getter @Setter
    private final String stationStart;

    @Getter @Setter
    private final String stationEnd;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public String getNumber() { return number.get(); }

    public BooleanProperty selectedProperty() { return selected; }

    public Line(String number, double price, String stationStart, String stationEnd) {
        this.number = new SimpleStringProperty(number);
        this.price = price;
        this.stationStart = stationStart;
        this.stationEnd = stationEnd;
    }

@Override
public String toString() {
    return String.format("Line %s - $%.2f", getNumber(), getPrice());
}
}