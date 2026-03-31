module com.brandex {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jakarta.mail;

    opens com.brandex to javafx.fxml, javafx.graphics;
    opens com.brandex.ui to javafx.fxml;
    opens com.brandex.models to javafx.fxml;
    exports com.brandex;
}
