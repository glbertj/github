module com.svx.github {
    requires javafx.controls;
    requires java.sql;
    requires jbcrypt;
    requires com.fasterxml.jackson.databind;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires org.fxmisc.richtext;

    exports com.svx.github.main;
}