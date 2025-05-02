package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import utils.DB_Context;


import java.sql.Connection;

@Slf4j
public class Main extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Starting Application");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/subscription.fxml"));

        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/subscription.css").toExternalForm());
        primaryStage.setTitle("TunTransport");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        Connection con = DB_Context.getInstance().getConn();
        if (con != null) {
            log.info("Connected to database");
        }

    }
}