module com.projetpi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires org.slf4j;
    requires java.sql;
    requires ch.qos.logback.core;
    requires java.dotenv;


    exports controllers;
    exports test;
    opens controllers to javafx.fxml;
}