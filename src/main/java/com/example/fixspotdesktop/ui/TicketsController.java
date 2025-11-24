package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.TicketDTO;
import com.example.fixspotdesktop.net.TicketsApi;
import com.example.fixspotdesktop.net.UsersApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;



public class TicketsController {

    private HelloApplication app;

    @FXML private TableView<TicketDTO> tableTickets;

    @FXML private TableColumn<TicketDTO, Number> colId;
    @FXML private TableColumn<TicketDTO, String> colAsunto;
    @FXML private TableColumn<TicketDTO, String> colSolicitante;
    @FXML private TableColumn<TicketDTO, String> colEstado;
    @FXML private TableColumn<TicketDTO, Void> colAccion;


    private final ObservableList<TicketDTO> ticketsList = FXCollections.observableArrayList();

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().idTicket()));
        colAsunto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().asunto()));

        colSolicitante.setCellValueFactory(data -> {
            int idUser = data.getValue().solicitante();
            String nombre = UsersApi.getById(idUser).nombreCompleto();
            return new SimpleStringProperty(nombre);
        });

        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().estadoNombre()));

        tableTickets.setItems(ticketsList);

        agregarBotonAccion();

        loadTickets();
    }

    private void agregarBotonAccion() {
        colAccion.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button("Acción");

            {
                btn.getStyleClass().add("primary-btn-2");
                btn.setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                TicketDTO ticket = getTableView().getItems().get(getIndex());

                // Si NO está pendiente → botón deshabilitado
                if (!ticket.estadoNombre().equalsIgnoreCase("Pendiente")) {
                    btn.setText("N/A");
                    btn.setDisable(true);
                } else {
                    btn.setText("Resolver");
                    btn.setDisable(false);

                    btn.setOnAction(e -> abrirPopup(ticket));
                }

                setGraphic(btn);
            }
        });
    }

    private void abrirPopup(TicketDTO ticket) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resolver ticket");
        alert.setHeaderText("Ticket #" + ticket.idTicket());
        alert.setContentText("¿Qué desea hacer con este ticket?");

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnRechazar = new ButtonType("Rechazar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAceptar, btnRechazar, btnCancelar);

        alert.showAndWait().ifPresent(type -> {

            if (type == btnAceptar) {
                cambiarEstado(ticket.idTicket(), 2);
            }

            if (type == btnRechazar) {
                cambiarEstado(ticket.idTicket(), 3);
            }
        });
    }

    private void cambiarEstado(int ticketId, int nuevoEstado) {

        new Thread(() -> {
            try {
                TicketsApi.updateEstado(ticketId, nuevoEstado);

                Platform.runLater(() -> {
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setHeaderText(null);
                    ok.setContentText("Ticket actualizado correctamente.");
                    ok.showAndWait();
                    loadTickets(); // refrescar tabla
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    Alert err = new Alert(Alert.AlertType.ERROR,
                            "Error actualizando ticket: " + ex.getMessage());
                    err.showAndWait();
                });
            }
        }).start();
    }

    private void loadTickets() {
        new Thread(() -> {
            try {
                List<TicketDTO> tickets = TicketsApi.listAll();

                Platform.runLater(() -> {
                    ticketsList.clear();
                    ticketsList.addAll(tickets);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Error cargando tickets: " + e.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    private void onBack() {
        app.showHome();
    }
}
