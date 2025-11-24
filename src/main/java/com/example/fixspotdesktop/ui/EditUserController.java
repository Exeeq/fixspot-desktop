package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.ComunasApi;
import com.example.fixspotdesktop.net.RolesApi;
import com.example.fixspotdesktop.net.UserDTO;
import com.example.fixspotdesktop.net.UsersApi;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Map;

public class EditUserController {

    @FXML private TextField txtUsername;
    @FXML private TextField txtRun;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDireccion;
    @FXML private ComboBox<String> cbRol;
    @FXML private ComboBox<String> cbComuna;

    private HelloApplication app;
    private int userId;
    private Map<Integer, String> rolesMap;
    private Map<Integer, String> comunasMap;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    /** Cargar roles, comunas y datos del usuario */
    public void loadUser(UserDTO u) {
        this.userId = u.id();

        rolesMap   = RolesApi.getMap();
        comunasMap = ComunasApi.getMap();

        cbRol.getItems().setAll(rolesMap.values());
        cbComuna.getItems().setAll(comunasMap.values());

        txtUsername.setText(u.username());
        txtRun.setText(u.run());
        txtCorreo.setText(u.correo());
        txtNombre.setText(u.pnombre());
        txtApellido.setText(u.apPaterno());
        txtDireccion.setText(u.direccion());

        cbRol.setValue(rolesMap.get(u.idRol()));
        cbComuna.setValue(comunasMap.get(u.idComuna()));
    }

    @FXML
    private void onCancel() {
        app.openUsers();
    }

    @FXML
    private void onSave() {
        String rolNombre    = cbRol.getValue();
        String comunaNombre = cbComuna.getValue();

        Integer rolId = rolesMap.entrySet().stream()
                .filter(e -> e.getValue().equals(rolNombre))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        Integer comunaId = comunasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(comunaNombre))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (rolId == null || comunaId == null) {
            showError("Rol o comuna inválidos.");
            return;
        }


        Map<String, String> body = Map.of(
                "username",   txtUsername.getText(),
                "correo",     txtCorreo.getText(),
                "pnombre",    txtNombre.getText(),
                "ap_paterno", txtApellido.getText(),
                "direccion",  txtDireccion.getText(),
                "idRol",      String.valueOf(rolId),
                "idComuna",   String.valueOf(comunaId)
        );

        try {
            UsersApi.updateUser(userId, body);
            showOK("Usuario actualizado correctamente.");
            app.openUsers();
        } catch (Exception e) {
            showError("Error al actualizar usuario: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        // Intenta limpiar errores JSON de DRF
        if (msg.contains("{")) {
            msg = msg.replace("{", "")
                    .replace("}", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace(":", ": ");
        }

        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showOK(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
