package com.example.fixspotdesktop;

import com.example.fixspotdesktop.auth.AuthService;
import com.example.fixspotdesktop.ui.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import com.example.fixspotdesktop.ui.UsersController;

public class HelloApplication extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        showLogin();
    }

    public void showLogin() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = fx.load();
            LoginController c = fx.getController();
            c.setApp(this);

            Scene scene = new Scene(root, 960, 560);
            scene.getStylesheets().add(
                    getClass().getResource("styles.css").toExternalForm()
            );

            stage.setTitle("Fixspot – Acceso");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(520);
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar login-view.fxml: " + e.getMessage(), e);
        }
    }

    public void showHome() {
        try {
            var fx = new javafx.fxml.FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = fx.load();
            var c = (com.example.fixspotdesktop.ui.HomeController) fx.getController();
            c.setApp(this);

            var scene = new javafx.scene.Scene(root, 960, 600);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setTitle("Fixspot – Inicio");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar home-view.fxml: " + e.getMessage(), e);
        }
    }

    // Stubs para cada sección (por ahora placeholders)
    public void openUsers() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("users-view.fxml"));
            Parent root = fx.load();
            UsersController c = fx.getController();
            c.setApp(this);
            Scene sc = new Scene(root, 980, 600);
            sc.getStylesheets().add(getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm());
            stage.setScene(sc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openWorkshops() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Gestión de talleres (próximo módulo)");
        a.setHeaderText(null); a.showAndWait();
    }
    public void openTickets() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Gestión de tickets (próximo módulo)");
        a.setHeaderText(null); a.showAndWait();
    }

    public static void main(String[] args) { launch(); }
}