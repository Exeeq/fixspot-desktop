package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.ComunasApi;
import com.example.fixspotdesktop.net.TallerDTO;
import com.example.fixspotdesktop.net.TalleresApi;
import com.example.fixspotdesktop.net.UsersApi;
import com.example.fixspotdesktop.net.UserDTO;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;

public class WorkshopsController {

    private HelloApplication app;

    @FXML private TableView<TallerRow> tbl;
    @FXML private TableColumn<TallerRow, Number> colId;
    @FXML private TableColumn<TallerRow, String> colEncargado;
    @FXML private TableColumn<TallerRow, String> colNombre;
    @FXML private TableColumn<TallerRow, String> colTelefono;
    @FXML private TableColumn<TallerRow, String> colDireccion;
    @FXML private TableColumn<TallerRow, String> colComuna;
    @FXML private TableColumn<TallerRow, Void> colAcciones;

    @FXML private Button btnCreate;
    @FXML private Button btnBack;
    @FXML private Button btnLogout;

    private final ObservableList<TallerRow> data = FXCollections.observableArrayList();
    private Map<Integer, String> comunasMap;

    public void setApp(HelloApplication app) { this.app = app; }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colEncargado.setCellValueFactory(c -> c.getValue().encargadoProperty());
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colDireccion.setCellValueFactory(c -> c.getValue().direccionProperty());
        colComuna.setCellValueFactory(c -> c.getValue().comunaProperty());

        colAcciones.setCellFactory(tc -> new TableCell<>() {
            final Button btnEdit = new Button("Modificar");
            final Button btnDel  = new Button("Eliminar");
            final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().addAll("pill", "btn-edit");
                btnDel.getStyleClass().addAll("pill", "btn-delete");

                btnDel.setOnAction(e -> {
                    TallerRow row = getTableView().getItems().get(getIndex());
                    if (!confirm("¿Eliminar taller: " + row.getNombre() + "?")) return;

                    try {
                        TalleresApi.deleteById(row.getId());
                        tbl.getItems().remove(row);
                    } catch (Exception ex) {
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

        new Thread(this::loadWorkshops).start();
    }

    private void loadWorkshops() {
        try {
            comunasMap = ComunasApi.getMap();

            List<TallerDTO> list = TalleresApi.listAll();
            ObservableList<TallerRow> tmp = FXCollections.observableArrayList();

            for (TallerDTO t : list) {

                // obtener nombre REAL del usuario encargado
                UserDTO u = UsersApi.getById(t.idUsuario());
                String encargadoName = u.pnombre() + " " + u.apPaterno();

                String comunaName = comunasMap.getOrDefault(t.idComuna(), "—");

                TallerRow row = TallerRow.from(t, comunaName);
                row.setEncargado(encargadoName);

                tmp.add(row);
            }

            Platform.runLater(() -> data.setAll(tmp));

        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    @FXML private void onBack()   { app.showHome(); }
    @FXML private void onLogout() { /* implementar logout */ }
    @FXML private void onOpenCreate() { app.openCreateWorkshop(); }

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

    // ========== Row Model ==========
    public static class TallerRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final StringProperty encargado = new SimpleStringProperty();
        private final StringProperty nombre = new SimpleStringProperty();
        private final StringProperty telefono = new SimpleStringProperty();
        private final StringProperty direccion = new SimpleStringProperty();
        private final StringProperty comuna = new SimpleStringProperty();

        public static TallerRow from(TallerDTO t, String comunaName) {
            TallerRow r = new TallerRow();
            r.setId(t.id());
            r.setNombre(t.nombre());
            r.setTelefono(t.telefono());
            r.setDireccion(t.direccion());
            r.setComuna(comunaName);
            r.setEncargado("—"); // será reemplazado luego
            return r;
        }

        public int getId() { return id.get(); }
        public String getEncargado() { return encargado.get(); }
        public String getNombre() { return nombre.get(); }
        public String getTelefono() { return telefono.get(); }
        public String getDireccion() { return direccion.get(); }
        public String getComuna() { return comuna.get(); }

        public void setId(int v) { id.set(v); }
        public void setEncargado(String s) { encargado.set(s); }
        public void setNombre(String s) { nombre.set(s); }
        public void setTelefono(String s) { telefono.set(s); }
        public void setDireccion(String s) { direccion.set(s); }
        public void setComuna(String s) { comuna.set(s); }

        public IntegerProperty idProperty() { return id; }
        public StringProperty encargadoProperty() { return encargado; }
        public StringProperty nombreProperty() { return nombre; }
        public StringProperty telefonoProperty() { return telefono; }
        public StringProperty direccionProperty() { return direccion; }
        public StringProperty comunaProperty() { return comuna; }
    }
}
