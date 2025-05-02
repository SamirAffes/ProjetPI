package controllers;

import entities.Payment;
import entities.Subscription;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class PaymentController {

    @FXML private TextField    txtEmail;
    @FXML private TextField    txtFullName;
    @FXML private TextField    txtPhone;
    @FXML private TextField    txtCardNumber;
    @FXML private TextField    txtCardExpiry;
    @FXML private PasswordField txtCardCvv;
    @FXML private Label        lblAmount;

    private Subscription subscription;
    private Payment payment;

    /** Called by SubscriptionFormController right after loading. */
    public void setSubscription(Subscription sub) {
        this.subscription = sub;
        this.payment = new Payment();
        payment.setSubscriptionId(sub.getId());

        // calculate & display
        payment.setDate(LocalDate.now());
        payment.setAmount(sub.getPrice());
        lblAmount.setText(String.format(String.valueOf(sub.getPrice())));
    }

    /** Called by caller to get back the filled Payment (or null if cancelled). */
    public Payment getPayment() {
        return payment;
    }

    @FXML
    private void handleSave() {
        // validate
        if (txtEmail.getText().isEmpty() ||
                txtFullName.getText().isEmpty() ||
                txtPhone.getText().isEmpty() ||
                txtCardNumber.getText().isEmpty() ||
                txtCardExpiry.getText().isEmpty() ||
                txtCardCvv.getText().isEmpty()) {

            new Alert(Alert.AlertType.ERROR,"Please fill in all fields",
                    ButtonType.OK)
                    .showAndWait();
            return;
        }

        if (!txtEmail.getText().matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            showAlert("Invalid email format.");
            return;
        }


        if (txtFullName.getText().isEmpty()) {
            showAlert("Full name cannot be empty.");
            return;
        }


        if (!txtPhone.getText().matches("^\\d{8}$")) {
            showAlert("Phone number must be exactly 8 digits.");
            return;
        }


        if (!txtCardNumber.getText().matches("^\\d{16}$")) {
            showAlert("Card number must be 16 digits.");
            return;
        }


        if (!txtCardExpiry.getText().matches("^(0[1-9]|1[0-2])\\/\\d{2}$")) {
            showAlert("Invalid card expiry format. Use MM/YY.");
            return;
        }


        if (!txtCardCvv.getText().matches("^\\d{3}$")) {
            showAlert("CVV must be 3 digits.");
            return;
        }

        // Populate payment details
        payment.setEmail(txtEmail.getText());
        payment.setFullName(txtFullName.getText());
        payment.setPhone(txtPhone.getText());
        payment.setCardNumber(txtCardNumber.getText());
        payment.setCardExpiry(txtCardExpiry.getText());
        payment.setCardCvv(txtCardCvv.getText());

        // Close the form
        ((Stage) txtEmail.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        payment = null;
        ((Stage) txtEmail.getScene().getWindow()).close();
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
    }
}
