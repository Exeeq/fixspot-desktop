package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class RolesApi {
    private static final String BASE = AuthService.API_BASE + "/roles/";

    public static Map<Integer,String> getMap() {
        Map<Integer,String> m = new HashMap<>();
        try {
            JsonNode n = com.example.fixspotdesktop.net.ApiClient.getJson(BASE, AuthService.getAccessToken());
            if (n != null && n.isArray()) {
                for (JsonNode r : n) {
                    int id = r.path("idRol").asInt(r.path("id").asInt());
                    String name = r.path("nombreRol").asText(r.path("nombre").asText(""));
                    if (id != 0 && !name.isBlank()) m.put(id, name);
                }
            }
        } catch (Exception ignored) {}
        m.putIfAbsent(1, "Cliente");
        m.putIfAbsent(2, "Encargado taller");
        m.putIfAbsent(3, "Administrador");
        return m;
    }
}
