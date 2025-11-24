package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public class EstadoTicketsApi {
    private static final String BASE = AuthService.API_BASE + "/estados-tickets/";

    public static Map<Integer, String> getMap() {
        Map<Integer, String> m = new HashMap<>();

        try {
            JsonNode n = ApiClient.getJson(BASE, AuthService.getAccessToken());

            if (n != null && n.isArray()) {
                for (JsonNode e : n) {
                    int id = e.path("idEstado").asInt();
                    String nombre = e.path("NombreEstado").asText("");

                    m.put(id, nombre);
                }
            }
        } catch (Exception ignored) {}

        return m;
    }
}
