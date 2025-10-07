module com.example.fixspotdesktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    opens com.example.fixspotdesktop.ui to javafx.fxml;

    opens com.example.fixspotdesktop to javafx.fxml;

    exports com.example.fixspotdesktop;
}
