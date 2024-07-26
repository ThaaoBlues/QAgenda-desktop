module com.qsync.qagenda {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jdk.compiler;


    opens com.ecosys.qagenda to javafx.fxml;
    exports com.ecosys.qagenda;
}