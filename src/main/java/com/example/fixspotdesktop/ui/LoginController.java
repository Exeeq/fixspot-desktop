package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.auth.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.concurrent.CompletableFuture;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;

    private HelloApplication app;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void onLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            statusLabel.setText("Completa usuario y contraseña.");
            return;
        }

        loginButton.setDisable(true);
        statusLabel.setText("Autenticando...");

        CompletableFuture
                .supplyAsync(() -> AuthService.login(u, p))
                .whenComplete((ok, ex) -> Platform.runLater(() -> {
                    loginButton.setDisable(false);

                    if (ex != null) {
                        // Mensaje limpio (no exponer detalles)
                        String msg = AuthService.getLastError();
                        statusLabel.setText((msg == null || msg.isBlank())
                                ? "No se pudo iniciar sesión."
                                : msg);
                        return;
                    }

                    if (Boolean.TRUE.equals(ok)) {
                        statusLabel.setText("¡Bienvenido!");
                        app.showHome();
                    } else {
                        String msg = AuthService.getLastError();
                        statusLabel.setText((msg == null || msg.isBlank())
                                ? "Credenciales inválidas o sin permisos."
                                : msg);
                    }
                }));
    }
}
