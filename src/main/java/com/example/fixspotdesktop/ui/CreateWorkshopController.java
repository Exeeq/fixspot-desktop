package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CreateWorkshopController {

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
    @FXML private Button btnCrear;
    @FXML private WebView webMap;
    @FXML private ListView<String> listDireccionSugerida;

    private File imagenFile;
    private Map<Integer, String> comunasMap;
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

        listServicios.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Cargar los datos iniciales
        new Thread(this::loadData).start();

        // Configurar el listener para el campo de dirección
        txtDireccionBusqueda.textProperty().addListener((observable, oldValue, newValue) -> onDireccionChanged());

        // Listener para la selección de dirección sugerida
        listDireccionSugerida.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !listDireccionSugerida.getItems().isEmpty()) {
                txtDireccionBusqueda.setText(newValue);
                onObtenerDireccion();
            }
        });
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

    private void loadData() {
        try {
            // Obtener los datos necesarios
            comunasMap = ComunasApi.getMap();
            usuarios = UsersApi.listAll();
            servicios = ServiciosApi.listAll();

            Platform.runLater(() -> {

                // Cargar las comunas y usuarios
                cbComuna.getItems().setAll(comunasMap.values());
                cbUsuario.getItems().setAll(
                        usuarios.stream()
                                .map(u -> u.pnombre() + " " + u.apPaterno())
                                .toList()
                );

                // Cargar los servicios disponibles
                var nombresServicios = servicios.stream().map(ServicioDTO::nombre).toList();
                listServicios.getItems().setAll(nombresServicios);

                // Inicializar el estado de los checkboxes
                nombresServicios.forEach(nombre -> {
                    serviciosSeleccionados.put(nombre, new SimpleBooleanProperty(false));
                });

                // Configurar el CheckBoxListCell para los servicios
                listServicios.setCellFactory(lv ->
                        new CheckBoxListCell<>(item -> serviciosSeleccionados.get(item))
                );
            });

        } catch (Exception e) {
            showError("Error cargando datos: " + e.getMessage());
        }
    }

    @FXML
    private void onDireccionChanged() {
        String query = txtDireccionBusqueda.getText().trim();

        // Limpiar la lista antes de nuevas sugerencias
        listDireccionSugerida.setItems(FXCollections.emptyObservableList());

        if (query.isEmpty()) {
            return;
        }

        if (direccionBusquedaTimer != null) {
            direccionBusquedaTimer.cancel();
        }

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

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "FixSpotDesktop/1.0 (contact@yourcompany.com)")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.body().contains("Bandwidth limit exceeded")) {
                    Platform.runLater(() -> {
                        showError("Se ha alcanzado el límite de solicitudes a la API. Intente nuevamente más tarde.");
                    });
                    return;
                }

                JsonNode json = new ObjectMapper().readTree(response.body());

                if (!json.isArray() || json.size() == 0) {
                    Platform.runLater(() -> {
                        showError("No se encontraron sugerencias para la dirección.");
                    });
                    return;
                }

                List<String> suggestions = new ArrayList<>();
                for (JsonNode item : json) {
                    String address = item.path("display_name").asText();
                    suggestions.add(address);
                }

                // Solo actualizar la lista si hay sugerencias
                Platform.runLater(() -> {
                    if (!suggestions.isEmpty()) {
                        listDireccionSugerida.setItems(FXCollections.observableList(suggestions));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error al obtener las sugerencias de dirección. " + e.getMessage());
                });
            }
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
                        "&format=json&limit=1";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "FixSpotDesktop")
                        .build();

                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode json = new ObjectMapper().readTree(res.body());

                if (!json.isArray() || json.size() == 0) {
                    Platform.runLater(() -> showError("No se encontró la dirección ingresada."));
                    return;
                }

                double lat = json.get(0).get("lat").asDouble();
                double lon = json.get(0).get("lon").asDouble();

                Platform.runLater(() -> {
                    txtLat.setText(String.valueOf(lat));
                    txtLon.setText(String.valueOf(lon));
                    btnCrear.setDisable(false);
                    engine.executeScript("updateMarker(" + lat + "," + lon + ");");
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error de geolocalización: " + e.getMessage()));
            }
        }).start();
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
        String lat = txtLat.getText() != null ? txtLat.getText().trim() : "";
        String lon = txtLon.getText() != null ? txtLon.getText().trim() : "";

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

        if (lat.isEmpty() || lon.isEmpty()) {
            showError("Debe obtener la dirección en el mapa antes de crear el taller.");
            return false;
        }

        if (imagenFile == null) {
            showError("Debe seleccionar una imagen del taller.");
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
    private void onCrear() {

        // Validar todo el formulario antes de seguir
        if (!validarFormulario()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea crear este taller?",
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK)
            return;

        Integer comunaId = comunasMap.entrySet().stream()
                .filter(e -> e.getValue().equals(cbComuna.getValue()))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);

        UserDTO encargado = usuarios.stream()
                .filter(u -> (u.pnombre() + " " + u.apPaterno())
                        .equals(cbUsuario.getValue()))
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

            JsonNode result = TalleresApiMultipart.createWithImage(body, imagenFile);

            int idTallerCreado = result.get("idTaller").asInt();

            var nombresSeleccionados = serviciosSeleccionados.entrySet().stream()
                    .filter(e -> e.getValue().get())
                    .map(Map.Entry::getKey)
                    .toList();

            servicios.stream()
                    .filter(s -> nombresSeleccionados.contains(s.nombre()))
                    .forEach(s -> {
                        try {
                            TallerServicioApi.agregarServicio(idTallerCreado, s.id());
                        } catch (Exception ex) {
                            System.out.println("Error agregando servicio " + s.id());
                        }
                    });

            showInfo("Taller creado exitosamente.");
            app.openWorkshops();

        } catch (Exception e) {
            showError("Error al crear taller: " + e.getMessage());
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
