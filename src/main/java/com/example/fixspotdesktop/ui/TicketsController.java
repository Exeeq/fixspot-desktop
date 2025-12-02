package com.example.fixspotdesktop.ui;

import com.example.fixspotdesktop.HelloApplication;
import com.example.fixspotdesktop.net.TicketDTO;
import com.example.fixspotdesktop.net.TicketsApi;
import com.example.fixspotdesktop.net.UsersApi;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import java.util.List;

public class TicketsController {

    private HelloApplication app;

    @FXML private VBox ticketsContainer;

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        loadTickets();
    }

    private void loadTickets() {
        new Thread(() -> {
            try {
                List<TicketDTO> tickets = TicketsApi.listAll();

                Platform.runLater(() -> {
                    ticketsContainer.getChildren().clear();
                    for (TicketDTO t : tickets) {
                        ticketsContainer.getChildren().add(crearTarjeta(t));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR,
                            "Error cargando tickets: " + e.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    private HBox crearTarjeta(TicketDTO t) {

        HBox card = new HBox(20);
        card.getStyleClass().add("ticket-card");
        card.setPadding(new Insets(18));
        card.setFillHeight(true);

        // TÍTULO
        Label title = new Label("#" + t.idTicket() + " – " + t.asunto());
        title.getStyleClass().add("ticket-title");

        // SOLICITANTE
        String nombreSolicitante = UsersApi.getById(t.solicitante()).nombreCompleto();
        Label solicitante = new Label("Solicitante: " + nombreSolicitante);
        solicitante.getStyleClass().add("ticket-info");

        // CHIP ESTADO
        Label chip = new Label(t.estadoNombre());
        chip.getStyleClass().add("estado-chip");

        String estado = t.estadoNombre().toLowerCase();
        if (estado.contains("pendiente")) chip.getStyleClass().add("estado-abierto");
        else if (estado.contains("proceso")) chip.getStyleClass().add("estado-proceso");
        else chip.getStyleClass().add("estado-resuelto");

        // BOTÓN DE ACCIÓN
        Button btn = new Button();
        btn.getStyleClass().add("ticket-btn");

        if (estado.contains("pendiente")) {
            btn.setText("Resolver");
            btn.setDisable(false);
            btn.setOnAction(e -> abrirPopup(t));
        } else {
            btn.setText("N/A");
            btn.setDisable(true);
        }

        // CONTENEDOR IZQUIERDO
        VBox info = new VBox(5, title, solicitante, chip);

        // ESPACIADOR
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(info, spacer, btn);

        return card;
    }

    private void abrirPopup(TicketDTO ticket) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Resolver ticket");
        alert.setHeaderText("Ticket #" + ticket.idTicket());
        alert.setContentText("¿Qué desea hacer con este ticket?");

        ButtonType btnAceptar = new ButtonType("Aceptar");
        ButtonType btnRechazar = new ButtonType("Rechazar");
        ButtonType btnCancelar =
                new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(btnAceptar, btnRechazar, btnCancelar);

        alert.showAndWait().ifPresent(type -> {
            if (type == btnAceptar) cambiarEstado(ticket.idTicket(), 2);
            if (type == btnRechazar) cambiarEstado(ticket.idTicket(), 3);
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
                    loadTickets();
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

    @FXML
    private void onBack() {
        app.showHome();
    }
}
