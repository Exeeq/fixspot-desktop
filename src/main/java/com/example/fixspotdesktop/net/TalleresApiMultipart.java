package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Map;

public class TalleresApiMultipart {

    private static final String BASE = AuthService.API_BASE + "/talleres/";

    public static JsonNode createWithImage(
            Map<String, String> fields,
            File imageFile
    ) throws Exception {

        String boundary = "----FixSpotBoundary" + System.currentTimeMillis();

        var reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .header("Authorization", "Bearer " + AuthService.getAccessToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary);

        // Generar cuerpo multipart
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(body, "UTF-8"), true);

        // Campos normales
        for (var entry : fields.entrySet()) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"")
                    .append(entry.getKey()).append("\"\r\n\r\n");
            writer.append(entry.getValue()).append("\r\n");
        }

        // Archivo
        if (imageFile != null) {
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"imagen\"; filename=\"")
                    .append(imageFile.getName()).append("\"\r\n");
            writer.append("Content-Type: image/jpeg\r\n\r\n");
            writer.flush();

            body.write(Files.readAllBytes(imageFile.toPath()));
            body.write("\r\n".getBytes());
        }

        writer.append("--").append(boundary).append("--").append("\r\n");
        writer.close();

        // Enviar
        HttpRequest req = reqBuilder.POST(
                HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())
        ).build();

        HttpResponse<String> res = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        return new ObjectMapper().readTree(res.body());
    }
}
