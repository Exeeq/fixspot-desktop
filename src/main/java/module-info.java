module com.example.fixspotdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.net.http;
    requires java.desktop;

    opens com.example.fixspotdesktop.ui to javafx.fxml;
    opens com.example.fixspotdesktop.net to com.fasterxml.jackson.databind;
    exports com.example.fixspotdesktop;
    exports com.example.fixspotdesktop.ui;
    exports com.example.fixspotdesktop.net;
}