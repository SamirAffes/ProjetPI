package entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class Subscription {
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private String type;
    @Getter
    @Setter
    private LocalDate startDate;
    @Getter
    @Setter
    private LocalDate endDate;
    @Setter
    @Getter
    private double price;


    private boolean isValid;

   /* @Setter
    @Getter
    private TransportMode transportMode;
    @Setter
    @Getter
    private Lines[] lines; */

    public boolean isValid() {
        return isValid;
    }

    public String getLines() {
        return this.type;
    }

    public void setLines(String lines) {
        this.type = lines;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Subscription() {
    }


    public Subscription(String type, LocalDate startDate, LocalDate endDate, double price, boolean isValid) {
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", lines='" + type + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", price=" + price +
                ", isValid=" + isValid +
                '}';
    }
}
