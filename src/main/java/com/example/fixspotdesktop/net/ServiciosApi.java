package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiciosApi {

    private static final String BASE = AuthService.API_BASE + "/servicios/";

    public static List<ServicioDTO> listAll() {
        JsonNode json = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<ServicioDTO> out = new ArrayList<>();

        if (json != null && json.isArray()) {
            for (JsonNode s : json) out.add(ServicioDTO.from(s));
        }
        return out;
    }

    // Método para obtener tood el mapa de servicios
    public static Map<Integer, String> getMap() {
        Map<Integer, String> m = new HashMap<>();
        try {
            JsonNode n = com.example.fixspotdesktop.net.ApiClient.getJson(BASE, com.example.fixspotdesktop.auth.AuthService.getAccessToken());
            if (n != null && n.isArray()) {
                for (JsonNode r : n){
                    int id = r.path("idServicio").asInt(r.path("id").asInt());
                    String name = r.path("nombreServicio").asText(r.path("nombre").asText());
                    if (id != 0 && !name.isBlank()) m.put(id, name);
                }
            }
        } catch (Exception ignored) {}
        return m;
    }
}
