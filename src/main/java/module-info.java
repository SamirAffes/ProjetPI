module com.projetpi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires static lombok;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires java.dotenv;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    exports controllers;
    exports test;
    exports entities;
    exports utils;
    opens test to javafx.fxml;
    opens controllers to javafx.fxml;
    opens entities to org.hibernate.orm.core;
}
