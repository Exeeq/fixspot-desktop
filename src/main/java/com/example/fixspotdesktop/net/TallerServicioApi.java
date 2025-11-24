package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static void eliminarTodosLosServicios(int idTaller) {
        try {
            String url = BASE + "eliminar/" + idTaller + "/";

            ApiClient.delete(url, AuthService.getAccessToken());

        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar los servicios del taller: " + e.getMessage());
        }
    }

    public static List<Integer> listServiciosDeTaller(int idTaller) {
        try {
            String url = BASE + "?idTaller=" + idTaller;

            JsonNode response = ApiClient.getJson(url, AuthService.getAccessToken());

            List<Integer> result = new ArrayList<>();

            if (!response.isArray()) {
                throw new RuntimeException("Respuesta inválida del servidor.");
            }

            for (JsonNode n : response) {
                if (n.has("idServicio")) {
                    result.add(n.get("idServicio").asInt());
                }
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener servicios del taller: " + e.getMessage());
        }
    }
}
