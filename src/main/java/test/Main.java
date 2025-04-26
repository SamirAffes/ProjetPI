package test;

import entities.Organisation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import services.OrganisationService;
import utils.db_context;

import java.sql.Connection;

@Slf4j
public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting Application");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("TunTransport");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}