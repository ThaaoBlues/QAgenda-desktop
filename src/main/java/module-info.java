module com.qsync.qagenda {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jdk.compiler;


    opens com.qsync.qagenda to javafx.fxml;
    exports com.qsync.qagenda;
}