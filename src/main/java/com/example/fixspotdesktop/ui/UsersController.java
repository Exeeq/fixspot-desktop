package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.RolesApi;
import com.example.fixspotdesktop.net.UserDTO;
import com.example.fixspotdesktop.net.UsersApi;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;

public class UsersController {
    private HelloApplication app;

    @FXML private TableView<UserRow> tbl;
    @FXML private TableColumn<UserRow, Number> colId;
    @FXML private TableColumn<UserRow, String> colRun;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colNombre;
    @FXML private TableColumn<UserRow, String> colCorreo;
    @FXML private TableColumn<UserRow, String> colDir;
    @FXML private TableColumn<UserRow, String> colRol;
    @FXML private TableColumn<UserRow, Void>   colAcciones;
    @FXML private Button btnCreate;

    private final ObservableList<UserRow> data = FXCollections.observableArrayList();
    private Map<Integer,String> rolesMap;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        // columnas
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colRun.setCellValueFactory(c -> c.getValue().runProperty());
        colUsername.setCellValueFactory(c -> c.getValue().usernameProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colCorreo.setCellValueFactory(c -> c.getValue().correoProperty());
        colDir.setCellValueFactory(c -> c.getValue().direccionProperty());
        colRol.setCellValueFactory(c -> c.getValue().rolProperty());

        // acciones
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            final Button btnEdit = new Button("Modificar");
            final Button btnDel  = new Button("Eliminar");
            final HBox box = new HBox(8, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().addAll("pill","btn-edit");
                btnDel.getStyleClass().addAll("pill","btn-delete");

                btnEdit.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    // TODO: abrir modal de edición
                    new Alert(Alert.AlertType.INFORMATION, "Editar usuario ID " + row.getId()).showAndWait();
                });

                btnDel.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    boolean ok = confirm("¿Eliminar usuario ID " + row.getId() + "?");
                    if (!ok) return;
                    try {
                        UsersApi.deleteById(row.getId());
                        getTableView().getItems().remove(row);
                    } catch (RuntimeException ex) {
                        showError(ex.getMessage());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tbl.setItems(data);

        // botón crear (placeholder)
        btnCreate.setOnAction(e ->
                new Alert(Alert.AlertType.INFORMATION, "Crear usuario (pendiente)").showAndWait()
        );

        // carga de datos
        new Thread(this::loadUsers).start();
    }

    private void loadUsers() {
        try {
            rolesMap = RolesApi.getMap();
            List<UserDTO> list = UsersApi.listAll();
            ObservableList<UserRow> tmp = FXCollections.observableArrayList();
            for (UserDTO u : list) {
                String rolName = rolesMap.getOrDefault(u.idRol(), String.valueOf(u.idRol()));
                tmp.add(UserRow.from(u, rolName));
            }
            Platform.runLater(() -> {
                data.setAll(tmp);
                if (!data.isEmpty()) tbl.getSelectionModel().selectFirst();
            });
        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ===== Row model para TableView =====
    public static class UserRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty run = new SimpleStringProperty();
        private final StringProperty username = new SimpleStringProperty();
        private final StringProperty nombre = new SimpleStringProperty();
        private final StringProperty correo = new SimpleStringProperty();
        private final StringProperty direccion = new SimpleStringProperty();
        private final StringProperty rol = new SimpleStringProperty();

        public static UserRow from(UserDTO u, String rolName) {
            UserRow r = new UserRow();
            r.setId(u.id());
            r.setRun(u.run());
            r.setUsername(u.username());
            r.setNombre(u.nombreCompleto());
            r.setCorreo(u.correo());
            r.setDireccion(u.direccion());
            r.setRol(rolName);
            return r;
        }

        // getters/setters + properties
        public int getId() { return id.get(); }
        public void setId(int v) { id.set(v); }
        public IntegerProperty idProperty() { return id; }

        public String getRun() { return run.get(); }
        public void setRun(String v) { run.set(v); }
        public StringProperty runProperty() { return run; }

        public String getUsername() { return username.get(); }
        public void setUsername(String v) { username.set(v); }
        public StringProperty usernameProperty() { return username; }

        public String getNombre() { return nombre.get(); }
        public void setNombre(String v) { nombre.set(v); }
        public StringProperty nombreProperty() { return nombre; }

        public String getCorreo() { return correo.get(); }
        public void setCorreo(String v) { correo.set(v); }
        public StringProperty correoProperty() { return correo; }

        public String getDireccion() { return direccion.get(); }
        public void setDireccion(String v) { direccion.set(v); }
        public StringProperty direccionProperty() { return direccion; }

        public String getRol() { return rol.get(); }
        public void setRol(String v) { rol.set(v); }
        public StringProperty rolProperty() { return rol; }
    }
}
