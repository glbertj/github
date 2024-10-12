module com.svx.github {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.svx.github.main to javafx.fxml;
    exports com.svx.github.main;
}