module com.projetpi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires org.slf4j;
    requires java.sql;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires java.dotenv;
    requires java.desktop;
    requires javafx.swing;
    requires javafx.web; // Added for WebView support

    exports controllers;
    exports test;
    opens controllers to javafx.fxml;
}