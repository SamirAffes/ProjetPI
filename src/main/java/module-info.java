module com.projetpi {
    // Required modules
    requires java.logging;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.slf4j;
    
    // Exports - make packages available to other modules
    exports controllers;
    exports entities;
    exports services;
    exports test;
    exports utils;
    
    // Opens - allow reflection access
    opens controllers to javafx.fxml;
    opens entities to javafx.base, javafx.fxml;
    opens test to javafx.graphics, javafx.fxml;
    opens services to java.sql;
}