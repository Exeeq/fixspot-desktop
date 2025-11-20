package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class TallerServicioApi {

    private static final String BASE = AuthService.API_BASE + "/taller-servicio/";

    public static JsonNode agregarServicio(int idTaller, int idServicio) {
        Map<String, Object> body = new HashMap<>();
        body.put("idTaller", idTaller);
        body.put("idServicio", idServicio);
        return ApiClient.postJson(BASE, body, AuthService.getAccessToken());
    }
}
