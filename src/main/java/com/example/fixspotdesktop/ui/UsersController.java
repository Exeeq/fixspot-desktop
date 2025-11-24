package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.auth.AuthService;
import com.example.fixspotdesktop.net.ComunasApi;
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

    // ===== Tabla =====
    @FXML private TableView<UserRow> tbl;
    @FXML private TableColumn<UserRow, Number> colId;
    @FXML private TableColumn<UserRow, String> colRun;
    @FXML private TableColumn<UserRow, String> colUsername;
    @FXML private TableColumn<UserRow, String> colNombre;
    @FXML private TableColumn<UserRow, String> colApPaterno;
    @FXML private TableColumn<UserRow, String> colCorreo;
    @FXML private TableColumn<UserRow, String> colDir;
    @FXML private TableColumn<UserRow, String> colComuna;
    @FXML private TableColumn<UserRow, String> colRol;
    @FXML private TableColumn<UserRow, Void>   colAcciones;

    // ===== Botones =====
    @FXML private Button btnCreate;
    @FXML private Button btnBack;
    @FXML private Button btnLogout;

    // ===== Estado =====
    private final ObservableList<UserRow> data = FXCollections.observableArrayList();
    private Map<Integer, String> rolesMap;
    private Map<Integer, String> comunasMap;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        // Columnas
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colRun.setCellValueFactory(c -> c.getValue().runProperty());
        colUsername.setCellValueFactory(c -> c.getValue().usernameProperty());
        // aquí va el primer nombre
        colNombre.setCellValueFactory(c -> c.getValue().pnombreProperty());
        // aquí va el apellido paterno
        colApPaterno.setCellValueFactory(c -> c.getValue().apPaternoProperty());
        colCorreo.setCellValueFactory(c -> c.getValue().correoProperty());
        colDir.setCellValueFactory(c -> c.getValue().direccionProperty());
        colComuna.setCellValueFactory(c -> c.getValue().comunaProperty());
        colRol.setCellValueFactory(c -> c.getValue().rolProperty());

        // Acciones (Editar / Eliminar)
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            final Button btnEdit = new Button("Modificar");
            final Button btnDel  = new Button("Eliminar");
            final HBox box       = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().addAll("pill", "btn-edit");
                btnDel.getStyleClass().addAll("pill", "btn-delete");

                btnEdit.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    UserDTO dto = new UserDTO(
                            row.getId(),
                            row.getRun(),
                            row.getUsername(),
                            row.getPnombre(),
                            row.getApPaterno(),
                            row.getCorreo(),
                            row.getDireccion(),
                            row.getIdRol(),
                            row.getIdComuna(),
                            true
                    );

                    app.openEditUser(dto);
                });

                btnDel.setOnAction(e -> {
                    UserRow row = getTableView().getItems().get(getIndex());
                    boolean ok = confirm("¿Eliminar usuario: " + row.getPnombre() + "?");
                    if (!ok) return;
                    try {
                        UsersApi.deleteById(row.getId());
                        getTableView().getItems().remove(row);
                    } catch (RuntimeException ex) {
                        showError(ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tbl.setItems(data);

        // Carga de datos
        new Thread(this::loadUsers).start();
    }

    private void loadUsers() {
        try {
            rolesMap   = RolesApi.getMap();    // id -> nombre rol
            comunasMap = ComunasApi.getMap();  // id -> nombre comuna

            List<UserDTO> list = UsersApi.listAll();
            ObservableList<UserRow> tmp = FXCollections.observableArrayList();

            for (UserDTO u : list) {
                String rolName    = rolesMap.getOrDefault(u.idRol(), String.valueOf(u.idRol()));
                String comunaName = comunasMap.getOrDefault(u.idComuna(), String.valueOf(u.idComuna()));
                tmp.add(UserRow.from(u, rolName, comunaName));
            }

            Platform.runLater(() -> {
                data.setAll(tmp);
                if (!data.isEmpty()) tbl.getSelectionModel().selectFirst();
            });
        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    // ===== Navegación =====
    @FXML private void onBack()       { app.showHome(); }
    @FXML private void onLogout()     { AuthService.logout(); app.showLogin(); }
    @FXML private void onOpenCreate() { app.openCreateUser(); }

    // ===== Helpers UI =====
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

    // ===== Modelo para TableView =====
    public static class UserRow {
        private final IntegerProperty id        = new SimpleIntegerProperty();
        private final StringProperty  run       = new SimpleStringProperty();
        private final StringProperty  username  = new SimpleStringProperty();
        private final StringProperty  pnombre   = new SimpleStringProperty();
        private final StringProperty  apPaterno = new SimpleStringProperty();
        private final StringProperty  correo    = new SimpleStringProperty();
        private final StringProperty  direccion = new SimpleStringProperty();
        private final StringProperty  comuna    = new SimpleStringProperty();
        private final StringProperty  rol       = new SimpleStringProperty();
        private final IntegerProperty idRol     = new SimpleIntegerProperty();
        private final IntegerProperty idComuna  = new SimpleIntegerProperty();

        public static UserRow from(UserDTO u, String rolName, String comunaName) {
            UserRow r = new UserRow();
            r.setId(u.id());
            r.setRun(u.run());
            r.setUsername(u.username());
            r.setPnombre(u.pnombre());
            r.setApPaterno(u.apPaterno());
            r.setCorreo(u.correo());
            r.setDireccion(u.direccion());
            r.setComuna(comunaName);
            r.setRol(rolName);
            r.setIdRol(u.idRol());
            r.setIdComuna(u.idComuna());
            return r;
        }

        // getters / setters / properties

        public int getId() { return id.get(); }
        public void setId(int v) { id.set(v); }
        public IntegerProperty idProperty() { return id; }

        public String getRun() { return run.get(); }
        public void setRun(String v) { run.set(v); }
        public StringProperty runProperty() { return run; }

        public String getUsername() { return username.get(); }
        public void setUsername(String v) { username.set(v); }
        public StringProperty usernameProperty() { return username; }

        public String getPnombre() { return pnombre.get(); }
        public void setPnombre(String v) { pnombre.set(v); }
        public StringProperty pnombreProperty() { return pnombre; }

        public String getApPaterno() { return apPaterno.get(); }
        public void setApPaterno(String v) { apPaterno.set(v); }
        public StringProperty apPaternoProperty() { return apPaterno; }

        public String getCorreo() { return correo.get(); }
        public void setCorreo(String v) { correo.set(v); }
        public StringProperty correoProperty() { return correo; }

        public String getDireccion() { return direccion.get(); }
        public void setDireccion(String v) { direccion.set(v); }
        public StringProperty direccionProperty() { return direccion; }

        public String getComuna() { return comuna.get(); }
        public void setComuna(String v) { comuna.set(v); }
        public StringProperty comunaProperty() { return comuna; }

        public String getRol() { return rol.get(); }
        public void setRol(String v) { rol.set(v); }
        public StringProperty rolProperty() { return rol; }

        public int getIdRol() { return idRol.get(); }
        public void setIdRol(int v) { idRol.set(v); }
        public IntegerProperty idRolProperty() { return idRol; }

        public int getIdComuna() { return idComuna.get(); }
        public void setIdComuna(int v) { idComuna.set(v); }
        public IntegerProperty idComunaProperty() { return idComuna; }
    }
}
