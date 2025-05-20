package controllers;

import entities.Organisation;
import entities.OrgType;
import entities.OrganisationRoute;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationRouteService;
import services.OrganisationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AdminDashboardController {

    @FXML
    private Button dashboardButton;
      @FXML
    private Button organisationsButton;

    @FXML
    private Button reclamationsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox dashboardView;

    @FXML
    private VBox organisationsView;

    @FXML
    private VBox reclamationsView;

    @FXML
    private PieChart orgTypeChart;

    @FXML
    private BarChart<String, Number> fleetSizeChart;

    @FXML
    private GridPane statsGrid;

    @FXML
    private Label totalOrgsLabel;

    @FXML
    private Label totalFleetLabel;

    @FXML
    private Label totalDriversLabel;

    @FXML
    private Label avgFleetSizeLabel;

    private OrganisationService organisationService;
    private OrganisationRouteService organisationRouteService;

    @FXML
    public void initialize() {
        // Initialize services
        organisationService = new OrganisationService();
        organisationRouteService = new OrganisationRouteService();

        // Load statistics
        loadStatistics();

        // Set default view
        showDashboard();

        // Ensure fullscreen is maintained
        dashboardButton.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Stage stage = (Stage) newValue.getWindow();
                if (stage != null) {
                    stage.setMaximized(true);
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                }
            }
        });
    }

    /**
     * Loads all statistics and updates the UI
     */
    private void loadStatistics() {
        try {
            // Get all organizations
            List<Organisation> organisations = organisationService.afficher_tout();

            // Calculate statistics
            int totalOrgs = organisations.size();
            int totalFleet = 0;
            int totalDrivers = 0;

            // Count organizations by type
            Map<OrgType, Integer> orgTypeCount = new HashMap<>();
            for (OrgType type : OrgType.values()) {
                orgTypeCount.put(type, 0);
            }

            // Process each organization
            for (Organisation org : organisations) {
                totalFleet += org.getTailleFlotte();
                totalDrivers += org.getNombreConducteurs();

                // Count by type
                OrgType type = org.getType();
                if (type != null) {
                    orgTypeCount.put(type, orgTypeCount.get(type) + 1);
                }
            }

            // Calculate average fleet size
            double avgFleetSize = totalOrgs > 0 ? (double) totalFleet / totalOrgs : 0;

            // Update statistics labels
            updateStatLabels(totalOrgs, totalFleet, totalDrivers, avgFleetSize);

            // Update charts
            updateOrgTypeChart(orgTypeCount);
            updateFleetSizeChart(organisations);

            log.info("Statistics loaded successfully");
        } catch (Exception e) {
            log.error("Error loading statistics", e);
        }
    }

    /**
     * Updates the statistics labels with the calculated values
     */
    private void updateStatLabels(int totalOrgs, int totalFleet, int totalDrivers, double avgFleetSize) {
        if (totalOrgsLabel != null) totalOrgsLabel.setText(String.valueOf(totalOrgs));
        if (totalFleetLabel != null) totalFleetLabel.setText(String.valueOf(totalFleet));
        if (totalDriversLabel != null) totalDriversLabel.setText(String.valueOf(totalDrivers));
        if (avgFleetSizeLabel != null) avgFleetSizeLabel.setText(String.format("%.1f", avgFleetSize));
    }

    /**
     * Updates the organization type pie chart
     */
    private void updateOrgTypeChart(Map<OrgType, Integer> orgTypeCount) {
        if (orgTypeChart == null) return;

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<OrgType, Integer> entry : orgTypeCount.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey().toString(), entry.getValue()));
            }
        }

        orgTypeChart.setData(pieChartData);
        orgTypeChart.setTitle("Organisations par Type");
        orgTypeChart.setLegendSide(Side.RIGHT);
    }

    /**
     * Updates the fleet size bar chart
     */
    private void updateFleetSizeChart(List<Organisation> organisations) {
        if (fleetSizeChart == null) return;

        // Clear previous data
        fleetSizeChart.getData().clear();

        // Create series for fleet sizes
        XYChart.Series<String, Number> fleetSeries = new XYChart.Series<>();
        fleetSeries.setName("Taille de la Flotte");

        XYChart.Series<String, Number> driverSeries = new XYChart.Series<>();
        driverSeries.setName("Nombre de Conducteurs");

        // Add top 5 organizations by fleet size
        organisations.stream()
                .sorted((o1, o2) -> Integer.compare(o2.getTailleFlotte(), o1.getTailleFlotte()))
                .limit(5)
                .forEach(org -> {
                    fleetSeries.getData().add(new XYChart.Data<>(org.getNom(), org.getTailleFlotte()));
                    driverSeries.getData().add(new XYChart.Data<>(org.getNom(), org.getNombreConducteurs()));
                });

        fleetSizeChart.getData().addAll(fleetSeries, driverSeries);
        fleetSizeChart.setTitle("Top 5 Organisations par Taille de Flotte");
    }
      @FXML
    public void showDashboard() {
        dashboardView.setVisible(true);
        organisationsView.setVisible(false);
        reclamationsView.setVisible(false);
        setActiveButton(dashboardButton);
    }
      @FXML
    public void showOrganisations() {
        dashboardView.setVisible(false);
        organisationsView.setVisible(true);
        reclamationsView.setVisible(false);
        setActiveButton(organisationsButton);
    }

    @FXML
    public void showReclamations() {
        dashboardView.setVisible(false);
        organisationsView.setVisible(false);
        reclamationsView.setVisible(true);
        setActiveButton(reclamationsButton);
    }

    @FXML
    public void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setTitle("TuniTransport");
            stage.setScene(scene);
            stage.show();
            stage.setFullScreen(true);
            stage.setMaximized(true);
            // dont show the fullscreen hint
            stage.setFullScreenExitHint("");
            log.info("Admin logged out");
        } catch (IOException e) {
            log.error("Error returning to home view", e);
        }
    }
      private void setActiveButton(Button button) {
        // Reset styles
        dashboardButton.getStyleClass().remove("active");
        organisationsButton.getStyleClass().remove("active");
        reclamationsButton.getStyleClass().remove("active");

        // Set active style
        button.getStyleClass().add("active");
    }
}
