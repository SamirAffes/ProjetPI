package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.db_context;

import java.sql.Connection;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("Starting Application");
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.setTitle("TunTransport");
        primaryStage.setScene(scene);

        // Set application to fullscreen mode by default
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        
        primaryStage.show();
        Connection con = db_context.getInstance().getConn();
        if (con != null) {
            logger.info("Connected to database");
        }
    }
}