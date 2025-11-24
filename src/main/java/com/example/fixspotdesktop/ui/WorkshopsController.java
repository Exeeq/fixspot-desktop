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
import javafx.event.ActionEvent;

import java.util.List;
import java.util.Map;

public class WorkshopsController {

    private HelloApplication app;

    // ===== Tabla =====
    @FXML private TableView<TallerRow> tbl;
    @FXML private TableColumn<TallerRow, Number> colId;
    @FXML private TableColumn<TallerRow, String> colEncargado;
    @FXML private TableColumn<TallerRow, String> colNombre;
    @FXML private TableColumn<TallerRow, String> colTelefono;
    @FXML private TableColumn<TallerRow, String> colDireccion;
    @FXML private TableColumn<TallerRow, String> colComuna;
    @FXML private TableColumn<TallerRow, Void> colAcciones;

    // ===== Botones =====
    @FXML private Button btnCreate;
    @FXML private Button btnBack;
    @FXML private Button btnLogout;

    private final ObservableList<TallerRow> data = FXCollections.observableArrayList();
    private Map<Integer, String> comunasMap;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        // Columnas
        colId.setCellValueFactory(c -> c.getValue().idProperty());
        colEncargado.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getEncargadoNombre())));
        colNombre.setCellValueFactory(c -> c.getValue().nombreProperty());
        colTelefono.setCellValueFactory(c -> c.getValue().telefonoProperty());
        colDireccion.setCellValueFactory(c -> c.getValue().direccionProperty());
        colComuna.setCellValueFactory(c -> c.getValue().comunaProperty());

        // Acciones (Editar / Eliminar)
        colAcciones.setCellFactory(tc -> new TableCell<>() {
            final Button btnEdit = new Button("Modificar");
            final Button btnDel  = new Button("Eliminar");
            final HBox box = new HBox(8, btnEdit, btnDel);

            {
                btnEdit.getStyleClass().addAll("pill", "btn-edit");
                btnDel.getStyleClass().addAll("pill", "btn-delete");

                btnEdit.setOnAction((ActionEvent e) -> {
                    TallerRow row = getTableView().getItems().get(getIndex());
                    TallerDTO dto = new TallerDTO(
                            row.getId(),
                            row.getIdUsuario(),
                            row.getNombre(),
                            row.getTelefono(),
                            row.getDireccion(),
                            row.getIdComuna(),
                            row.getDescripcion(),
                            row.getLatitud(),
                            row.getLongitud(),
                            row.getImagen()
                    );
                    // Pasar el objeto TallerDTO al controlador de edición
                    app.openEditWorkshop(dto);
                });

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

            ObservableList<TallerRow> tmp = FXCollections.observableArrayList();

            for (TallerDTO t : TalleresApi.listAll()) {

                // Obtener nombre completo del usuario encargado
                UserDTO u = UsersApi.getById(Integer.valueOf(t.idUsuario()));
                String encargadoName = u.nombreCompleto();

                // Obtener nombre de la comuna
                String comunaName = comunasMap.getOrDefault(t.idComuna(), "—");

                //  Crear fila del taller
                TallerRow row = TallerRow.from(t, comunaName);

                // Guardamos REAL ID del usuario
                row.setIdUsuario(t.idUsuario());

                // Guardamos el nombre completo del usuario (para mostrarlo)
                row.setEncargadoNombre(encargadoName);

                tmp.add(row);
            }

            Platform.runLater(() -> data.setAll(tmp));

        } catch (RuntimeException ex) {
            Platform.runLater(() -> showError(ex.getMessage()));
        }
    }

    @FXML private void onBack()   { app.showHome(); }
    @FXML private void onLogout() { app.showLogin(); }
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
        private final IntegerProperty idUsuario = new SimpleIntegerProperty();
        private final IntegerProperty idComuna = new SimpleIntegerProperty();
        private final StringProperty nombre = new SimpleStringProperty();
        private final StringProperty telefono = new SimpleStringProperty();
        private final StringProperty direccion = new SimpleStringProperty();
        private final StringProperty comuna = new SimpleStringProperty();
        private final StringProperty descripcion = new SimpleStringProperty();
        private final DoubleProperty latitud = new SimpleDoubleProperty();
        private final DoubleProperty longitud = new SimpleDoubleProperty();
        private final StringProperty imagen = new SimpleStringProperty();
        private final StringProperty encargadoNombre = new SimpleStringProperty();

        // Método para crear un objeto TallerRow desde un TallerDTO y el nombre de la comuna
        public static TallerRow from(TallerDTO t, String comunaName) {
            TallerRow r = new TallerRow();
            r.setId(t.id());
            r.setNombre(t.nombreTaller());
            r.setTelefono(t.telefono());
            r.setDireccion(t.direccion());
            r.setComuna(comunaName);
            r.setDescripcion(t.descripcion());
            r.setIdUsuario(t.idUsuario());
            r.setIdComuna(t.idComuna());
            r.setLatitud(t.latitud());
            r.setLongitud(t.longitud());
            r.setImagen(t.getImagen());
            return r;
        }

        // Método para convertir el TallerRow en un objeto TallerDTO
        public TallerDTO getTallerDTO() {
            return new TallerDTO(
                    id.get(),
                    idUsuario.get(),
                    nombre.get(),
                    telefono.get(),
                    direccion.get(),
                    idComuna.get(),
                    descripcion.get(),
                    latitud.get(),
                    longitud.get(),
                    imagen.get()
            );
        }

        // Getters y setters para las propiedades
        public int getId() { return id.get(); }
        public void setId(int v) { id.set(v); }
        public IntegerProperty idProperty() { return id; }

        public int getIdUsuario() { return idUsuario.get(); }
        public void setIdUsuario(int v) { idUsuario.set(v); }
        public IntegerProperty idUsuarioProperty() { return idUsuario; }

        public int getIdComuna() { return idComuna.get(); }
        public void setIdComuna(int v) { idComuna.set(v); }
        public IntegerProperty idComunaProperty() { return idComuna; }

        public String getNombre() { return nombre.get(); }
        public void setNombre(String s) { nombre.set(s); }
        public StringProperty nombreProperty() { return nombre; }

        public String getTelefono() { return telefono.get(); }
        public void setTelefono(String s) { telefono.set(s); }
        public StringProperty telefonoProperty() { return telefono; }

        public String getDireccion() { return direccion.get(); }
        public void setDireccion(String s) { direccion.set(s); }
        public StringProperty direccionProperty() { return direccion; }

        public String getComuna() { return comuna.get(); }
        public void setComuna(String s) { comuna.set(s); }
        public StringProperty comunaProperty() { return comuna; }

        public String getDescripcion() { return descripcion.get(); }
        public void setDescripcion(String s) { descripcion.set(s); }
        public StringProperty descripcionProperty() { return descripcion; }

        public double getLatitud() { return latitud.get(); }
        public void setLatitud(double lat) { latitud.set(lat); }
        public DoubleProperty latitudProperty() { return latitud; }

        public double getLongitud() { return longitud.get(); }
        public void setLongitud(double lon) { longitud.set(lon); }
        public DoubleProperty longitudProperty() { return longitud; }

        public String getImagen() { return imagen.get(); }
        public void setImagen(String imagen) { this.imagen.set(imagen); }
        public StringProperty imagenProperty() { return imagen; }

        public String getEncargadoNombre() { return encargadoNombre.get(); }
        public void setEncargadoNombre(String v) { encargadoNombre.set(v); }
        public StringProperty encargadoNombreProperty() { return encargadoNombre; }
    }
}
