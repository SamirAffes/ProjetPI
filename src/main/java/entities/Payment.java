package entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

public class Payment {
    @Getter @Setter private int id;
    @Getter @Setter private int subscriptionId;

    @Getter @Setter private String email;
    @Getter @Setter private String fullName;
    @Getter @Setter private String phone;

    // split card fields
    @Getter @Setter private String cardNumber;
    @Getter @Setter private String cardExpiry;
    @Getter @Setter private String cardCvv;

    @Getter @Setter private LocalDate date;
    @Getter @Setter private double amount;

    public Payment() { }

    public Payment(int id, int subscriptionId, String email, String fullName, String phone,
                   String cardNumber, String cardExpiry, String cardCvv,
                   LocalDate date, double amount) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.cardNumber = cardNumber;
        this.cardExpiry = cardExpiry;
        this.cardCvv = cardCvv;
        this.date = date;
        this.amount = amount;
    }
}
