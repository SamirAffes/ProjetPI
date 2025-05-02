package controllers;

import entities.Subscription;
import entities.Payment;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.Callback;
import services.SubscriptionService;

import java.time.LocalDate;
import java.util.stream.Collectors;

public class SubscriptionFormController {

    @FXML private ListView<Line> listLines;
    @FXML private Label       lblTotalPrice;
    @FXML private DatePicker  dpStart;
    @FXML private DatePicker  dpEnd;

    private boolean saved = false;
    private Subscription subscription;
    private final SubscriptionService subscriptionService = new SubscriptionService();

    // static list of available lines
    private static final ObservableList<Line> LINES = FXCollections.observableArrayList(
            new Line("12(k)", 10.0),
            new Line("56(o)", 12.5),
            new Line("84(f)", 8.75),
            new Line("125(l)", 15.0),
            new Line("127(d)", 9.0),
            new Line("106(n)", 11.25)
    );

    // inner class representing a line
    public static class Line {
        private final SimpleStringProperty number;
        private final double price;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        public Line(String number, double price) {
            this.number = new SimpleStringProperty(number);
            this.price = price;
        }
        public String getNumber() { return number.get(); }
        public double getPrice() { return price; }
        public BooleanProperty selectedProperty() { return selected; }
        @Override
        public String toString() { return getNumber() + " ($" + price + ")"; }
    }

    public void setSubscription(Subscription s) {
        if (s == null) {
            // new subscription: reset form
            clearForm();
            subscription = new Subscription();
        } else {
            // editing existing subscription: populate selections
            subscription = s;
            // parse saved type (comma-separated numbers)
            String[] nums = s.getType().split(",");
            LINES.forEach(line ->
                    line.selectedProperty().set(
                            java.util.Arrays.asList(nums).contains(line.getNumber())
                    )
            );
            dpStart.setValue(s.getStartDate());
            dpEnd.setValue(s.getEndDate());
            updateTotal();
        }
    }

    private void clearForm() {
        // deselect all lines and clear dates
        LINES.forEach(line -> line.selectedProperty().set(false));
        dpStart.setValue(null);
        dpEnd.setValue(null);
        updateTotal();
    }

    public Subscription getSubscription() {
        String type = LINES.stream()
                .filter(line -> line.selectedProperty().get())
                .map(Line::getNumber)
                .collect(Collectors.joining(","));
        double total = LINES.stream()
                .filter(line -> line.selectedProperty().get())
                .mapToDouble(Line::getPrice)
                .sum();

        subscription.setType(type);
        subscription.setStartDate(dpStart.getValue());
        subscription.setEndDate(dpEnd.getValue());
        subscription.setPrice(total);
        return subscription;
    }

    @FXML
    public void initialize() {
        // initialize cell factory
        listLines.setItems(LINES);
        listLines.setCellFactory(CheckBoxListCell.forListView(
                new Callback<Line, javafx.beans.value.ObservableValue<Boolean>>() {
                    @Override public javafx.beans.value.ObservableValue<Boolean> call(Line item) {
                        return item.selectedProperty();
                    }
                },
                new StringConverter<Line>() {
                    @Override public String toString(Line item) {
                        return item.toString();
                    }
                    @Override public Line fromString(String string) {
                        return null;
                    }
                }
        ));

        // update total when selections change
        LINES.forEach(line ->
                line.selectedProperty().addListener((obs, oldVal, newVal) -> updateTotal())
        );

        // initial total
        updateTotal();
    }

    private void updateTotal() {
        double sum = LINES.stream()
                .filter(line -> line.selectedProperty().get())
                .mapToDouble(Line::getPrice)
                .sum();
        lblTotalPrice.setText(String.format("$%.2f", sum));
    }

    @FXML
    public void handleSave() {
        try {
            if (LINES.stream().noneMatch(line -> line.selectedProperty().get())
                    || dpStart.getValue() == null
                    || dpEnd.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Please select at least one line and dates", null)
                        .showAndWait();
                return;
            }

            Subscription toSave = getSubscription();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payment.fxml"));
            Parent root = loader.load();
            PaymentController payCtrl = loader.getController();
            payCtrl.setSubscription(toSave);

            Stage payStage = new Stage();
            payStage.setTitle("Payment");
            payStage.initModality(Modality.APPLICATION_MODAL);
            payStage.setScene(new Scene(root));
            payStage.showAndWait();

            Payment payment = payCtrl.getPayment();
            if (payment == null) return;

            subscriptionService.ajouterWithPayment(toSave, payment);
            saved = true;

            // reset form for next entry (instead of closing)
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, e.getMessage(), javafx.scene.control.ButtonType.OK)
                    .showAndWait();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public void showError(Exception ex) {
        ex.printStackTrace();
        new Alert(Alert.AlertType.ERROR, ex.getMessage(), javafx.scene.control.ButtonType.OK)
                .showAndWait();
    }
}