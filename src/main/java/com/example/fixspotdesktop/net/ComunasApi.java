package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class ComunasApi {
    private static final String BASE = com.example.fixspotdesktop.auth.AuthService.API_BASE + "/comunas/";

    public static Map<Integer,String> getMap() {
        Map<Integer,String> m = new HashMap<>();
        try {
            JsonNode n = com.example.fixspotdesktop.net.ApiClient.getJson(BASE, com.example.fixspotdesktop.auth.AuthService.getAccessToken());
            if (n != null && n.isArray()) {
                for (JsonNode r : n) {
                    int id = r.path("idComuna").asInt(r.path("id").asInt());
                    String name = r.path("nombreComuna").asText(r.path("nombre").asText(""));
                    if (id != 0 && !name.isBlank()) m.put(id, name);
                }
            }
        } catch (Exception ignored) {}
        return m;
    }
}
