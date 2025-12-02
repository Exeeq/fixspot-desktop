package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.auth.AuthService;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;

public class HomeController {
    private HelloApplication app;

    @FXML private SplitPane rootSplit;
    @FXML private Label lblUser;

    public void setApp(HelloApplication app) {
        this.app = app;

        // Mostrar nombre del usuario conectado (displayName)
        if (lblUser != null) {
            String who = AuthService.getDisplayName();
            lblUser.setText((who != null && !who.isBlank()) ? "Sesión activa: " + who : "Sesión activa");
        }

        setupResponsive();
    }

    private void setupResponsive() {
        rootSplit.widthProperty().addListener((obs, oldW, newW) -> {
            double w = newW == null ? 0 : newW.doubleValue();
            if (w < 900) {
                if (rootSplit.getOrientation() != Orientation.VERTICAL) {
                    rootSplit.setOrientation(Orientation.VERTICAL);
                    rootSplit.setDividerPositions(0.52);
                }
                if (!rootSplit.getStyleClass().contains("compact")) {
                    rootSplit.getStyleClass().add("compact");
                }
            } else {
                if (rootSplit.getOrientation() != Orientation.HORIZONTAL) {
                    rootSplit.setOrientation(Orientation.HORIZONTAL);
                    rootSplit.setDividerPositions(0.55);
                }
                rootSplit.getStyleClass().remove("compact");
            }
        });

        if (rootSplit.getDividerPositions() == null || rootSplit.getDividerPositions().length == 0) {
            rootSplit.setDividerPositions(0.55);
        }
    }

    @FXML private void onLogout()    { app.showLogin(); }
    @FXML private void goUsers()     { app.openUsers(); }
    @FXML private void goWorkshops() { app.openWorkshops(); }
    @FXML private void goTickets()   { app.openTickets(); }
}
