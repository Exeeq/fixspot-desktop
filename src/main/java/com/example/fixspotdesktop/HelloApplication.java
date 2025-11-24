package com.example.fixspotdesktop;

import com.example.fixspotdesktop.ui.CreateUserController;
import com.example.fixspotdesktop.ui.HomeController;
import com.example.fixspotdesktop.ui.LoginController;
import com.example.fixspotdesktop.ui.UsersController;
import com.example.fixspotdesktop.ui.EditUserController;
import com.example.fixspotdesktop.ui.CreateWorkshopController;
import com.example.fixspotdesktop.ui.EditWorkshopController;
import com.example.fixspotdesktop.net.TallerDTO;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;


import com.example.fixspotdesktop.net.UserDTO;

public class HelloApplication extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        showLogin();
    }

    // =========================================================
    // Método  para cargar cualquier escena
    // =========================================================
    public void loadScene(String fxml, java.util.function.Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Obtener controller e inyectarlo
            Object controller = loader.getController();
            controllerConsumer.accept(controller);

            // Crear escena con CSS
            Scene sc = new Scene(root);
            sc.getStylesheets().add(
                    getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm()
            );

            stage.setScene(sc);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar: " + fxml).showAndWait();
        }
    }

    // =========================================================
    // LOGIN
    // =========================================================
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

    // =========================================================
    // HOME
    // =========================================================
    public void showHome() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("home-view.fxml"));
            Parent root = fx.load();
            HomeController c = fx.getController();
            c.setApp(this);

            Scene scene = new Scene(root, 960, 600);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            stage.setTitle("Fixspot – Inicio");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar home-view.fxml: " + e.getMessage(), e);
        }
    }

    // =========================================================
    // USUARIOS
    // =========================================================
    public void openUsers() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("users-view.fxml"));
            Parent root = fx.load();
            UsersController c = fx.getController();
            c.setApp(this);

            Scene sc = new Scene(root, 980, 600);
            sc.getStylesheets().add(getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm());
            stage.setTitle("Fixspot – Usuarios");
            stage.setScene(sc);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openCreateUser() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("user-create-view.fxml"));
            Parent root = fx.load();
            CreateUserController c = fx.getController();
            c.setApp(this);

            Scene sc = new Scene(root, 800, 520);
            sc.getStylesheets().add(getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm());
            stage.setTitle("Fixspot – Nuevo usuario");
            stage.setScene(sc);
            stage.centerOnScreen();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar el formulario de usuario.").showAndWait();
            e.printStackTrace();
        }
    }

    public void openEditUser(UserDTO user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-user-view.fxml"));
            Parent root = loader.load();

            EditUserController controller = loader.getController();
            controller.setApp(this);
            controller.loadUser(user);

            Scene sc = new Scene(root);
            sc.getStylesheets().add(
                    getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm()
            );

            stage.setTitle("Fixspot – Editar usuario");
            stage.setScene(sc);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // TALLERES
    // =========================================================
    public void openWorkshops() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("workshops-view.fxml"));
            Parent root = fx.load();

            com.example.fixspotdesktop.ui.WorkshopsController c = fx.getController();
            c.setApp(this);

            Scene sc = new Scene(root, 980, 600);
            sc.getStylesheets().add(
                    getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm()
            );

            stage.setTitle("Fixspot – Talleres mecánicos");
            stage.setScene(sc);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar workshops-view.fxml").showAndWait();
        }
    }

    // =========================================================
    // CREAR TALLER
    // =========================================================
    public void openCreateWorkshop() {
        try {
            FXMLLoader fx = new FXMLLoader(getClass().getResource("workshop-create-view.fxml"));
            Parent root = fx.load();
            CreateWorkshopController c = fx.getController();
            c.setApp(this);

            Scene sc = new Scene(root, 980, 700);
            sc.getStylesheets().add(getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm());

            stage.setTitle("Fixspot – Crear taller");
            stage.setScene(sc);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar el formulario de creación de taller.").showAndWait();
        }
    }

    // =========================================================
    // EDITAR TALLER
    // =========================================================
    public void openEditWorkshop(TallerDTO taller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-workshop-view.fxml"));
            Parent root = loader.load();

            EditWorkshopController controller = loader.getController();
            controller.setApp(this);
            controller.loadTaller(taller);

            Scene sc = new Scene(root, 980, 700);
            sc.getStylesheets().add(
                    getClass().getResource("/com/example/fixspotdesktop/styles.css").toExternalForm()
            );

            stage.setTitle("Fixspot – Editar Taller");
            stage.setScene(sc);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // =========================================================
    // Tickets
    // =========================================================
    public void openTickets() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "Gestión de tickets (próximo módulo)");
        a.setHeaderText(null);
        a.showAndWait();
    }

    public static void main(String[] args) { launch(); }
}
