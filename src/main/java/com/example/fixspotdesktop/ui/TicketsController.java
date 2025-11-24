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



public class TicketsController {

    private HelloApplication app;

    @FXML private TableView<TicketDTO> tableTickets;

    @FXML private TableColumn<TicketDTO, Number> colId;
    @FXML private TableColumn<TicketDTO, String> colAsunto;
    @FXML private TableColumn<TicketDTO, String> colSolicitante;
    @FXML private TableColumn<TicketDTO, String> colEstado;

    private final ObservableList<TicketDTO> ticketsList = FXCollections.observableArrayList();

    public void setApp(HelloApplication app) {
        this.app = app;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().idTicket()));
        colAsunto.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().asunto()));

        colSolicitante.setCellValueFactory(data -> {
            int idUser = data.getValue().solicitante();
            String nombre = UsersApi.getById(idUser).nombreCompleto();
            return new javafx.beans.property.SimpleStringProperty(nombre);
        });

        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().estadoNombre())
        );

        tableTickets.setItems(ticketsList);

        loadTickets();
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
