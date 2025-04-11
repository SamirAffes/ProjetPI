module com.pijava {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.pijava to javafx.fxml;
    exports com.pijava;
}