module com.svx.github {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;

    opens com.svx.github.main to javafx.fxml;
    exports com.svx.github.main;
}