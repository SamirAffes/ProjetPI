module tn.esprit.testpifx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.slf4j;

    opens tn.esprit.testpifx to javafx.fxml;
    opens tn.esprit.testpifx.controllers to javafx.fxml;
    opens tn.esprit.testpifx.models to javafx.base;
    opens tn.esprit.testpifx.services to javafx.base;

    exports tn.esprit.testpifx;
    exports tn.esprit.testpifx.controllers;
    exports tn.esprit.testpifx.models;
    exports tn.esprit.testpifx.services;
    exports tn.esprit.testpifx.repositories;
    exports tn.esprit.testpifx.utils;
    uses tn.esprit.testpifx.services.UserService;
}
