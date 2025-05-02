package controllers;

import entities.Subscription;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.SubscriptionService;

// <-- add this import
import controllers.SubscriptionFormController;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SubscriptionController {

    @FXML private TableView<Subscription> table;
    @FXML private TableColumn<Subscription, Integer> colId;
    @FXML private TableColumn<Subscription, String>  colType;
    @FXML private TableColumn<Subscription, LocalDate> colStart;
    @FXML private TableColumn<Subscription, LocalDate> colEnd;
    @FXML private TableColumn<Subscription, Double>  colPrice;

    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;

    @FXML private TableColumn<Subscription, String> colStationStart;
    @FXML private TableColumn<Subscription, String> colStationEnd;


    @FXML
    private Label labelActiveCount;

    @FXML
    private TextField txtSearch;

    private final SubscriptionService service = new SubscriptionService();
    private final ObservableList<Subscription> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStationStart.setCellValueFactory(new PropertyValueFactory<>("stationStart"));
        colStationEnd.setCellValueFactory(new PropertyValueFactory<>("stationEnd"));

        table.setItems(data);

        btnRefresh.setOnAction(e -> loadAll());
        btnCreate .setOnAction(e -> openDialog(null));
        btnUpdate .setOnAction(e -> {
            Subscription sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) openDialog(sel);
            else showAlert("Please select a subscription to edit.");
        });
        btnDelete .setOnAction(e -> {
            Subscription sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    service.supprimer(sel);
                    loadAll();
                } catch (SQLException ex) {
                    handleException(ex);
                }
            } else {
                showAlert("Please select a subscription to delete.");
            }
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSubscriptions(newValue);
        });



        int total = service.getTotalSubscriptions();
        labelActiveCount.setText(String.valueOf(total));



        loadAll();
    }

    private void loadAll() {
        try {
            List<Subscription> list = service.afficher_tout();
            data.setAll(list);
            updateActiveSubscriptionCount();
        } catch (Exception ex) {
            handleException(ex);
        }
    }

    private void filterSubscriptions(String query) {
        if (query == null || query.isEmpty()) {
            table.setItems(data);
            return;
        }

        ObservableList<Subscription> filteredList = FXCollections.observableArrayList(
                data.stream()
                        .filter(sub -> sub.getType().toLowerCase().contains(query.toLowerCase()))
                        .toList()
        );

        table.setItems(filteredList);
    }

    public void updateActiveSubscriptionCount() {
        int total = service.getTotalSubscriptions();
        labelActiveCount.setText(String.valueOf(total));
    }


    private void openDialog(Subscription existing) {
        try {
            Dialog<ButtonType> dlg = new Dialog<>();
            dlg.setTitle(existing == null ? "Add Subscription" : "Edit Subscription");
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscription_form.fxml"));
            Node form = loader.load();

            // EXPLICITLY cast to your form controller
            SubscriptionFormController formCtrl = loader.getController();
            formCtrl.setSubscription(existing);

            dlg.getDialogPane().setContent(form);
            dlg.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

            Node ok = dlg.getDialogPane().lookupButton(ButtonType.OK);
            ok.addEventFilter(
                    ActionEvent.ACTION,
                    evt -> {
                        try {
                            formCtrl.handleSave();
                            loadAll();
                        } catch (Exception ex) {
                            formCtrl.showError(ex);
                        }
                        evt.consume();
                        dlg.close();
                    });

            if (formCtrl.isSaved()) {
                loadAll();
            }
            Optional<ButtonType> result = dlg.showAndWait();
            if (result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Subscription toSave = formCtrl.getSubscription();
                if (existing == null) service.ajouter(toSave);
                else                 service.modifier(toSave);
                loadAll();
            }
        } catch (IOException | SQLException ex) {
            handleException(ex);
        }

    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
    private void handleException(Exception ex) {
        ex.printStackTrace();
        showAlert(ex.getMessage());
    }

    @FXML
    private void handleViewPayments() {
        Subscription sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Please select a subscription first.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/payments_list.fxml"));
            Parent root = loader.load();

            PaymentsListController ctrl = loader.getController();
            ctrl.setSubscription(sel);

            Stage stage = new Stage();
            stage.setTitle("Payments for Subscription " + sel.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException ex) {
            handleException(ex);
        }
    }



}