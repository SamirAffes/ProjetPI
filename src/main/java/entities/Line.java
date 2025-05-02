package entities;

import lombok.Getter;
import lombok.Setter;

public class Line {
    @Getter @Setter
    private int number;
    @Getter @Setter
    private double price;

    public Line(int number, double price) {
        this.number = number;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Line " + number + " ($" + price + ")";
    }
}