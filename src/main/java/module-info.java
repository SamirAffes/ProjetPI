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
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.javafx;
    requires jbcrypt;


    exports controllers;
    exports test;
    opens controllers to javafx.fxml;
    exports entities;
    exports utils;
    opens test to javafx.fxml;
    opens entities to org.hibernate.orm.core;
}