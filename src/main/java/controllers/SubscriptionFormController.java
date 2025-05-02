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
import lombok.Getter;
import lombok.Setter;
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
            new Line("12", 10.0, "Station A", "Station B"),
            new Line("56", 12.5, "Station C", "Station D"),
            new Line("84", 8.75, "Station E", "Station F"),
            new Line("125", 15.0, "Station G", "Station H"),
            new Line("127", 9.0, "Station I", "Station J"),
            new Line("106", 11.25, "Station K", "Station L")
    );

    // inner class representing a line
    public static class Line {
        private final SimpleStringProperty number;
        private final double price;
        private final BooleanProperty selected = new SimpleBooleanProperty(false);

        @Getter @Setter
        private final String stationStart;

        @Getter @Setter
        private final String stationEnd;


        public Line(String number, double price, String stationStart, String stationEnd) {
            this.number = new SimpleStringProperty(number);
            this.price = price;
            this.stationStart = stationStart;
            this.stationEnd = stationEnd;
        }
        public String getNumber() { return number.get(); }
        public double getPrice() { return price; }
        public BooleanProperty selectedProperty() { return selected; }
        @Override
        public String toString() {
            return String.format("Line: %s - $%.2f - (From: %s, To: %s)", getNumber(), getPrice(), getStationStart(), getStationEnd());
        }
    }

    public void setSubscription(Subscription s) {
        if (s == null) {
            this.subscription = new Subscription(); // Initialize a new Subscription if null
        } else {
            this.subscription = s;
            // Pre-fill the form with subscription data
            dpStart.setValue(s.getStartDate());
            dpEnd.setValue(s.getEndDate());
            String[] lines = s.getType().split(",");
            for (String line : lines) {
                LINES.stream()
                        .filter(l -> l.getNumber().equals(line))
                        .findFirst()
                        .ifPresent(lineObj -> lineObj.selectedProperty().set(true));
            }
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

        // Set the first and last selected stations
        String stationStart = LINES.stream()
                .filter(line -> line.selectedProperty().get())
                .findFirst()
                .map(Line::getStationStart)
                .orElse(null);

        String stationEnd = LINES.stream()
                .filter(line -> line.selectedProperty().get())
                .reduce((first, second) -> second)
                .map(Line::getStationEnd)
                .orElse(null);

        subscription.setType(type);
        subscription.setStartDate(dpStart.getValue());
        subscription.setEndDate(dpEnd.getValue());
        subscription.setPrice(total);
        subscription.setStationStart(stationStart);
        subscription.setStationEnd(stationEnd);

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

            // Validate selected lines
            for (Line line : LINES) {
                if (line.selectedProperty().get()) {
                    boolean isLineActive = subscriptionService.isLineActive(line.getNumber());
                    if (isLineActive) {
                        new Alert(Alert.AlertType.ERROR,
                                "Cannot subscribe to line " + line.getNumber() + " as it is still active.",
                                null).showAndWait();
                        return;
                    }
                }
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