package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.auth.AuthService;
import com.example.fixspotdesktop.net.RolesApi;
import com.example.fixspotdesktop.net.ComunasApi;
import com.example.fixspotdesktop.net.UsersApi;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

public class CreateUserController {

    private HelloApplication app;

    // Campos formulario
    @FXML private TextField txtUsername;
    @FXML private TextField txtRun;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<String> cbRol;
    @FXML private ComboBox<String> cbComuna;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;

    private Map<Integer, String> rolesMap;
    private Map<Integer, String> comunasMap;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        // Cargar roles para el combobox
        new Thread(this::loadRoles).start();

        // Cargar comunas para el combobox
        new Thread(this::loadComunas).start();
    }


    private void loadRoles() {
        try {
            rolesMap = RolesApi.getMap(); //
            List<String> nombres = rolesMap.values().stream().toList();

            Platform.runLater(() -> cbRol.getItems().setAll(nombres));
        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    private void loadComunas() {
        try {
            comunasMap = ComunasApi.getMap();
            List<String> nombres = comunasMap.values().stream().toList();

            Platform.runLater(() -> cbComuna.getItems().setAll(nombres));
        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    @FXML
    private void onSave() {
        String username = txtUsername.getText();
        String run = txtRun.getText();
        String correo = txtCorreo.getText();
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String direccion = txtDireccion.getText();
        String rolNombre = cbRol.getValue();
        String comunaNombre = cbComuna.getValue();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (username.isEmpty() || run.isEmpty() || correo.isEmpty() || nombre.isEmpty()
                || apellido.isEmpty() || direccion.isEmpty() || rolNombre == null
                || password.isEmpty() || !password.equals(confirmPassword)) {
            showError("Todos los campos son obligatorios y las contraseñas deben coincidir.");
            return;
        }

        // Convertir nombre de rol a idRol
        Integer idRol = null;
        if (rolesMap != null) {
            for (Map.Entry<Integer, String> e : rolesMap.entrySet()) {
                if (e.getValue().equalsIgnoreCase(rolNombre)) {
                    idRol = e.getKey();
                    break;
                }
            }
        }
        if (idRol == null) {
            showError("Rol inválido. Selecciona un rol válido.");
            return;
        }

        // Convertir nombe de comuna a idComuna
        Integer idComuna = null;
        if(comunasMap != null) {
            for (Map.Entry<Integer, String> e : comunasMap.entrySet()) {
                if (e.getValue().equalsIgnoreCase(comunaNombre)) {
                    idComuna = e.getKey();
                    break;
                }
            }
        }
        if (idComuna == null) {
            showError("Comuna inválido. Selecciona un comuna válido.");
            return;
        }

        Map<String, String> newUser = Map.of(
                "username",   username,
                "run",        run,
                "correo",     correo,
                "pnombre",    nombre,
                "ap_paterno", apellido,
                "direccion",  direccion,
                "idRol",      String.valueOf(idRol),
                "idComuna",   String.valueOf(idComuna),
                "password",   password
        );

        try {
            UsersApi.createUser(newUser);
            showSuccess("Usuario creado exitosamente.");
            app.openUsers(); // Vuelve al listado
        } catch (Exception e) {
            showError("Error al crear el usuario: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        app.openUsers();
    }

    // Helpers
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}

