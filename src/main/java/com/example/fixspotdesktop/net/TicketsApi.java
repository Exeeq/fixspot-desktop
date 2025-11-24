package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class TicketsApi {

    private static final String BASE = AuthService.API_BASE + "/tickets/";

    public static List<TicketDTO> listAll() {
        JsonNode n = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<TicketDTO> out = new ArrayList<>();

        // Mapa ID → Nombre del estado
        Map<Integer, String> estados = EstadoTicketsApi.getMap();

        if (n != null && n.isArray()) {
            for (JsonNode t : n) {
                int estadoId = t.path("EstadoTicket").asInt();
                String nombreEstado = estados.getOrDefault(estadoId, "Desconocido");

                out.add(TicketDTO.from(t, nombreEstado));
            }
        }

        return out;
    }
}
