package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalleresApi {

    // Si tu API base es algo como http://127.0.0.1:8000/api,
    // esto queda en: http://127.0.0.1:8000/api/talleres/
    private static final String BASE = AuthService.API_BASE + "/talleres/";

    /** Listar todos los talleres */
    public static List<TallerDTO> listAll() {
        JsonNode n = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<TallerDTO> out = new ArrayList<>();

        if (n != null && n.isArray()) {
            for (JsonNode t : n) out.add(TallerDTO.from(t));
        } else if (n != null && n.has("results")) { // por si usas paginación DRF
            for (JsonNode t : n.get("results")) out.add(TallerDTO.from(t));
        }
        return out;
    }

    /** Crear taller (sin imagen, solo datos básicos) */
    public static JsonNode create(Map<String, Object> tallerData) {
        // Keys deben coincidir con los nombres de campos del modelo DRF:
        // nombreTaller, descripcion, direccion, telefono,
        // latitud, longitud, idUsuario, idComuna, imagen (si la usas)
        return ApiClient.postJson(BASE, tallerData, AuthService.getAccessToken());
    }

    /** Crear taller helper simple */
    public static JsonNode createSimple(
            String nombreTaller,
            String descripcion,
            String direccion,
            String telefono,
            Double latitud,
            Double longitud,
            Integer idUsuario,
            Integer idComuna
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("nombreTaller", nombreTaller);
        body.put("descripcion",  descripcion);
        body.put("direccion",    direccion);
        body.put("telefono",     telefono);
        if (latitud  != null) body.put("latitud",  latitud);
        if (longitud != null) body.put("longitud", longitud);
        if (idUsuario != null) body.put("idUsuario", idUsuario);
        if (idComuna  != null) body.put("idComuna",  idComuna);
        // la imagen la dejamos para más adelante (multipart)
        return create(body);
    }

    /** Eliminar taller por idTaller */
    public static void deleteById(int idTaller) {
        ApiClient.delete(BASE + idTaller + "/", AuthService.getAccessToken());
    }
}
