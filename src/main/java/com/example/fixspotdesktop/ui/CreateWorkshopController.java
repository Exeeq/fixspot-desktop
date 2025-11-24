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
                // Asegurarse de que no se intente seleccionar en una lista vacía
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
        }, 500);  // Esperar 500ms antes de realizar la búsqueda
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
                        "&format=json&limit=1"; // Limitar a un solo resultado

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

    @FXML
    private void onCrear() {

        if (txtLat.getText().isEmpty() || txtLon.getText().isEmpty()) {
            showError("Debe obtener la dirección antes de crear el taller.");
            return;
        }

        if (imagenFile == null) {
            showError("Debe seleccionar una imagen del taller.");
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
