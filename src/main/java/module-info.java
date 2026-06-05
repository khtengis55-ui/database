module com.library {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.library to javafx.fxml;
    opens com.library.controllers to javafx.fxml;

    opens com.library.models to javafx.base, javafx.fxml;

    exports com.library;
}
