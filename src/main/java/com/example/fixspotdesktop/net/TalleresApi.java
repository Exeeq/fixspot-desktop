package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;



public class TalleresApi {

    private static final String BASE = AuthService.API_BASE + "/talleres/";

    public static List<TallerDTO> listAll() {
        JsonNode n = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<TallerDTO> out = new ArrayList<>();

        if (n != null && n.isArray()) {
            for (JsonNode t : n) {
                out.add(TallerDTO.from(t));
            }
        } else if (n != null && n.has("results")) { // en caso de usar paginación DRF
            for (JsonNode t : n.get("results")) {
                out.add(TallerDTO.from(t));
            }
        }
        return out;
    }


    public static JsonNode create(Map<String, Object> tallerData) {
        return ApiClient.postJson(BASE, tallerData, AuthService.getAccessToken());
    }

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
        body.put("descripcion", descripcion);
        body.put("direccion", direccion);
        body.put("telefono", telefono);
        if (latitud != null) body.put("latitud", latitud);
        if (longitud != null) body.put("longitud", longitud);
        if (idUsuario != null) body.put("idUsuario", idUsuario);
        if (idComuna != null) body.put("idComuna", idComuna);
        return create(body);
    }


    public static JsonNode update(int idTaller, Map<String, String> body, File imagenFile) {
        try {
            String url = BASE + idTaller + "/";

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request;

            if (imagenFile == null) {
                // SIN IMAGEN: PUT JSON NORMAL
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(body);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + AuthService.getAccessToken())
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

            } else {
                String boundary = "----FixSpotBoundary" + System.currentTimeMillis();

                var builder = new StringBuilder();

                body.forEach((k, v) -> {
                    builder.append("--").append(boundary).append("\r\n");
                    builder.append("Content-Disposition: form-data; name=\"").append(k).append("\"\r\n\r\n");
                    builder.append(v).append("\r\n");
                });

                builder.append("--").append(boundary).append("\r\n");
                builder.append("Content-Disposition: form-data; name=\"imagen\"; filename=\"")
                        .append(imagenFile.getName()).append("\"\r\n");
                builder.append("Content-Type: image/jpeg\r\n\r\n");

                byte[] fileBytes = java.nio.file.Files.readAllBytes(imagenFile.toPath());
                byte[] prefix = builder.toString().getBytes(StandardCharsets.UTF_8);
                byte[] suffix = ("\r\n--" + boundary + "--").getBytes(StandardCharsets.UTF_8);

                byte[] multipart = new byte[prefix.length + fileBytes.length + suffix.length];
                System.arraycopy(prefix, 0, multipart, 0, prefix.length);
                System.arraycopy(fileBytes, 0, multipart, prefix.length, fileBytes.length);
                System.arraycopy(suffix, 0, multipart, prefix.length + fileBytes.length, suffix.length);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .header("Authorization", "Bearer " + AuthService.getAccessToken())
                        .PUT(HttpRequest.BodyPublishers.ofByteArray(multipart))
                        .build();
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Error del servidor (" + response.statusCode() + "): " + response.body());
            }

            return new ObjectMapper().readTree(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el taller: " + e.getMessage());
        }
    }

    public static void deleteById(int idTaller) {
        ApiClient.delete(BASE + idTaller + "/", AuthService.getAccessToken());
    }

    public static TallerDTO getById(int idTaller) {
        JsonNode n = ApiClient.getJson(BASE + idTaller + "/", AuthService.getAccessToken());

        if (n == null) {
            System.out.println("No se encontró el taller con ID: " + idTaller);
            return null;
        }

        return TallerDTO.from(n);
    }

    public static JsonNode createWithImage(Map<String, Object> tallerData, File imagenFile) {
        try {
            // Crea un nuevo cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Crea un archivo de tipo Multipart para enviar la imagen
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE)) // URL base de la API
                    .header("User-Agent", "FixSpotDesktop/1.0 (contact@yourcompany.com)")
                    .header("Authorization", "Bearer " + AuthService.getAccessToken())
                    .method("POST", HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(tallerData)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return new ObjectMapper().readTree(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Error al crear el taller con la imagen: " + e.getMessage());
        }
    }
}
