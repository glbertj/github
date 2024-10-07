module edu.bluejack24_1.github {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.svx.github to javafx.fxml;
    exports com.svx.github.main;
}