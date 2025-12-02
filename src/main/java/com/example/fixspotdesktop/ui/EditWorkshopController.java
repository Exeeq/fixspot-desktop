package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.scene.control.cell.CheckBoxListCell;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EditWorkshopController {

    private HelloApplication app;
    private WebEngine engine;

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cbComuna;
    @FXML private ComboBox<String> cbUsuario;
    @FXML private TextField txtImagen;
    @FXML private ListView<String> listServicios;
    @FXML private TextField txtDireccionBusqueda;
    @FXML private TextField txtLat;
    @FXML private TextField txtLon;
    @FXML private Button btnGuardar;
    @FXML private WebView webMap;
    @FXML private ListView<String> listDireccionSugerida;

    private double lat = 0;
    private double lon = 0;
    private int tallerId;
    private File imagenFile;
    private Map<Integer, String> comunasMap;
    private Map<Integer, String> serviciosMap;
    private List<UserDTO> usuarios;
    private List<ServicioDTO> servicios;
    private Map<String, BooleanProperty> serviciosSeleccionados = new HashMap<>();
    private Timer direccionBusquedaTimer = null;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        engine = webMap.getEngine();
        engine.load(getClass().getResource("/map/leaflet_map.html").toString());

        engine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
            Platform.runLater(() -> {
                engine.executeScript("updateMarker(" + lat + "," + lon + ");");
            });
        });
    }

    public void loadTaller(TallerDTO t) {
        this.tallerId = t.id();

        engine = webMap.getEngine();
        engine.load(getClass().getResource("/map/leaflet_map.html").toString());

        new Thread(() -> {
            try {
                comunasMap = ComunasApi.getMap();
                servicios = ServiciosApi.listAll();
                usuarios = UsersApi.listAll();

                String nombreEncargado = UsersApi.getById(Integer.valueOf(t.idUsuario())).nombreCompleto();

                Platform.runLater(() -> {
                    // SETEAR CAMPOS DEL TALLER
                    txtNombre.setText(t.nombreTaller());
                    txtDescripcion.setText(t.descripcion());
                    txtTelefono.setText(t.telefono());
                    txtDireccionBusqueda.setText(t.direccion());
                    txtLat.setText(String.valueOf(t.latitud()));
                    txtLon.setText(String.valueOf(t.longitud()));
                    lat = t.latitud();
                    lon = t.longitud();

                    // Pre-cargar la imagen (ruta/URL almacenada)
                    txtImagen.setText(t.imagen());

                    // Cargar las comunas y usuarios
                    cbComuna.getItems().setAll(comunasMap.values());
                    cbComuna.setValue(comunasMap.get(t.idComuna()));

                    cbUsuario.getItems().setAll(
                            usuarios.stream()
                                    .map(u -> u.pnombre() + " " + u.apPaterno())
                                    .toList()
                    );
                    cbUsuario.setValue(nombreEncargado);

                    // Cargar servicios
                    var nombresServicios = servicios.stream().map(ServicioDTO::nombre).toList();
                    listServicios.getItems().setAll(nombresServicios);

                    nombresServicios.forEach(nombre -> {
                        serviciosSeleccionados.put(nombre, new SimpleBooleanProperty(false));
                    });

                    listServicios.setCellFactory(lv -> new CheckBoxListCell<>(item -> serviciosSeleccionados.get(item)));

                    // Marcar servicios ya asociados al taller
                    List<Integer> idsServiciosTaller = TallerServicioApi.listServiciosDeTaller(tallerId);

                    for (ServicioDTO s : servicios) {
                        if (idsServiciosTaller.contains(s.id())) {
                            serviciosSeleccionados.get(s.nombre()).set(true);
                        }
                    }

                    // Mapa
                    Platform.runLater(this::tryUpdateMarker);

                    // ==== Listener direcciones ====
                    txtDireccionBusqueda.textProperty().addListener((obs, oldVal, newVal) -> onDireccionChanged());

                    listDireccionSugerida.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            txtDireccionBusqueda.setText(newVal);
                            onObtenerDireccion();
                        }
                    });
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error cargando taller: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onDireccionChanged() {
        String query = txtDireccionBusqueda.getText().trim();

        // Limpiar la lista antes de nuevas sugerencias
        listDireccionSugerida.getSelectionModel().clearSelection();
        listDireccionSugerida.setItems(FXCollections.emptyObservableList());

        if (query.isEmpty()) {
            return; // No hacer nada si la consulta está vacía
        }

        // Cancelar el temporizador anterior si existe
        if (direccionBusquedaTimer != null) {
            direccionBusquedaTimer.cancel();
        }

        // Iniciar un nuevo temporizador para retrasar la consulta
        direccionBusquedaTimer = new Timer();
        direccionBusquedaTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                buscarDirecciones(query);
            }
        }, 500);
    }

    private void buscarDirecciones(String query) {
        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/search?q=" +
                        URLEncoder.encode(query, StandardCharsets.UTF_8) +
                        "&format=json&limit=5";

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "FixSpotDesktop")
                        .build();

                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode json = new ObjectMapper().readTree(res.body());

                if (!json.isArray() || json.size() == 0) return;

                List<String> list = new ArrayList<>();
                for (JsonNode item : json) list.add(item.get("display_name").asText());

                Platform.runLater(() -> listDireccionSugerida.setItems(FXCollections.observableList(list)));

            } catch (Exception ignored) {}
        }).start();
    }

    @FXML
    private void onObtenerDireccion() {
        String address = txtDireccionBusqueda.getText().trim();
        if (address.isEmpty()) {
            showError("Debe ingresar una dirección.");
            return;
        }

        new Thread(() -> {
            try {
                String url = "https://nominatim.openstreetmap.org/search?q=" +
                        URLEncoder.encode(address, StandardCharsets.UTF_8) +
                        "&format=json&limit=1"; // Limitar a un solo resultado

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "FixSpotDesktop")
                        .build();

                HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode json = new ObjectMapper().readTree(res.body());

                if (json.size() == 0) return;

                double lat = json.get(0).get("lat").asDouble();
                double lon = json.get(0).get("lon").asDouble();

                Platform.runLater(() -> {
                    txtLat.setText(String.valueOf(lat));
                    txtLon.setText(String.valueOf(lon));
                    btnGuardar.setDisable(false);
                    engine.executeScript("updateMarker(" + lat + "," + lon + ");");
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error obteniendo coordenadas."));
            }
        }).start();
    }

    @FXML
    private void tryUpdateMarker() {
        try {
            double lat = Double.parseDouble(txtLat.getText());
            double lon = Double.parseDouble(txtLon.getText());

            engine.executeScript("updateMarker(" + lat + "," + lon + ");");
        } catch (Exception ignored) {}
    }

    @FXML
    private void onSelectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar imagen del taller");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(null);
        if (file != null) {
            imagenFile = file;
            txtImagen.setText(file.getAbsolutePath());
        }
    }

    private boolean validarTelefonoFormato(String telefono) {
        if (telefono == null) return false;
        String t = telefono.trim();
        return t.matches("^\\+569\\d{8}$");
    }

    private boolean validarFormulario() {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
        String telefono = txtTelefono.getText() != null ? txtTelefono.getText().trim() : "";
        String direccion = txtDireccionBusqueda.getText() != null ? txtDireccionBusqueda.getText().trim() : "";
        String latStr = txtLat.getText() != null ? txtLat.getText().trim() : "";
        String lonStr = txtLon.getText() != null ? txtLon.getText().trim() : "";
        String imagenTexto = txtImagen.getText() != null ? txtImagen.getText().trim() : "";

        if (nombre.isEmpty() || nombre.length() < 3) {
            showError("El nombre del taller debe tener al menos 3 caracteres.");
            return false;
        }

        if (descripcion.isEmpty() || descripcion.length() < 8) {
            showError("La descripción debe tener al menos 8 caracteres.");
            return false;
        }

        if (telefono.isEmpty()) {
            showError("Debe ingresar un número de teléfono.");
            return false;
        }

        if (!validarTelefonoFormato(telefono)) {
            showError("El teléfono debe tener el formato +569xxxxxxxx (ej: +56912345678).");
            return false;
        }

        if (cbComuna.getValue() == null) {
            showError("Debe seleccionar una comuna.");
            return false;
        }

        if (cbUsuario.getValue() == null) {
            showError("Debe seleccionar un encargado para el taller.");
            return false;
        }

        if (direccion.isEmpty()) {
            showError("Debe ingresar una dirección y obtener su ubicación en el mapa.");
            return false;
        }

        if (latStr.isEmpty() || lonStr.isEmpty()) {
            showError("Debe obtener la dirección en el mapa antes de guardar.");
            return false;
        }

        if ((imagenFile == null) && imagenTexto.isEmpty()) {
            showError("El taller debe tener una imagen. Mantenga la existente o seleccione una nueva.");
            return false;
        }
        boolean hayServicioSeleccionado = serviciosSeleccionados.values().stream()
                .anyMatch(BooleanProperty::get);

        if (!hayServicioSeleccionado) {
            showError("Debe seleccionar al menos un servicio para el taller.");
            return false;
        }

        return true;
    }

    @FXML
    private void onSave() {

        // Validar todo antes de intentar actualizar
        if (!validarFormulario()) {
            return;
        }

        Integer comunaId = comunasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(cbComuna.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

        UserDTO encargado = usuarios.stream()
                .filter(u -> (u.pnombre() + " " + u.apPaterno()).equals(cbUsuario.getValue()))
                .findFirst().orElse(null);

        if (encargado == null) {
            showError("Debe seleccionar un encargado válido.");
            return;
        }

        try {
            Map<String, String> body = Map.of(
                    "nombreTaller", txtNombre.getText(),
                    "descripcion", txtDescripcion.getText(),
                    "direccion", txtDireccionBusqueda.getText(),
                    "telefono", txtTelefono.getText(),
                    "latitud", txtLat.getText(),
                    "longitud", txtLon.getText(),
                    "idUsuario", String.valueOf(encargado.id()),
                    "idComuna", String.valueOf(comunaId)
            );

            TalleresApi.update(tallerId, body, imagenFile);

            // Actualizar servicios asociados al taller
            TallerServicioApi.eliminarTodosLosServicios(tallerId);

            var seleccionados = serviciosSeleccionados.entrySet().stream()
                    .filter(e -> e.getValue().get())
                    .map(Map.Entry::getKey)
                    .toList();

            servicios.stream()
                    .filter(s -> seleccionados.contains(s.nombre()))
                    .forEach(s -> {
                        try {
                            TallerServicioApi.agregarServicio(tallerId, s.id());
                        } catch (Exception ignored) {}
                    });

            showInfo("Taller actualizado correctamente.");
            app.openWorkshops();

        } catch (Exception e) {
            showError("Error al actualizar taller: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        app.openWorkshops();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
