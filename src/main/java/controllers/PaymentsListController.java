package controllers;

import entities.Payment;
import entities.Subscription;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.PaymentService;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentsListController {
    @FXML private Label lblSubInfo;
    @FXML private TableView<Payment> table;
    @FXML private TableColumn<Payment, String> colDate;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, String> colEmail;
    @FXML private TableColumn<Payment, String> colName;
    @FXML private TableColumn<Payment, String> colPhone;
    @FXML private TableColumn<Payment, String> colCardNo;

    private final PaymentService service = new PaymentService();
    private final ObservableList<Payment> data = FXCollections.observableArrayList();
    private Subscription subscription;

    public void setSubscription(Subscription sub) {
        this.subscription = sub;
        lblSubInfo.setText(sub.getLines() + " (ID: " + sub.getId() + ")");
        loadPayments();
    }

    private void loadPayments() {
        try {
            List<Payment> list = service.getBySubscriptionId(subscription.getId());
            data.setAll(list);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void initialize() {
        // format date as yyyy-MM-dd
        colDate.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue()
                        .getDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE)));

        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colEmail .setCellValueFactory(new PropertyValueFactory<>("email"));
        colName  .setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone .setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCardNo.setCellValueFactory(new PropertyValueFactory<>("cardNumber"));

        table.setItems(data);
    }

    @FXML
    private void handleClose() {
        ((Stage) table.getScene().getWindow()).close();
    }
}
