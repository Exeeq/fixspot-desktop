package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ApiClient {

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // ---------- Helpers ----------
    private static JsonNode safeParse(String body) {
        try { return json.readTree(body); } catch (Exception e) { return null; }
    }

    private static String pickErrorMessage(int code, String bodyText, String unauthorizedMsg) {
        JsonNode err = safeParse(bodyText);
        String detail = (err != null && err.has("detail")) ? err.get("detail").asText() : null;

        if (detail == null || detail.isBlank()) {
            if (code == 401) detail = unauthorizedMsg;
            else if (code == 403) detail = "Acceso no autorizado.";
            else if (code >= 500) detail = "Error del servidor. Intenta más tarde.";
            else detail = "Solicitud inválida.";
        }
        return detail;
    }

    private static HttpRequest.Builder baseBuilder(String url, String bearerToken) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (bearerToken != null && !bearerToken.isBlank()) {
            b.header("Authorization", "Bearer " + bearerToken);
        }
        return b;
    }

    // ---------- GET ----------
    public static JsonNode getJson(String url, String bearerToken) {
        try {
            HttpResponse<String> res = http.send(
                    baseBuilder(url, bearerToken).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            int code = res.statusCode();
            String bodyText = res.body();

            if (code >= 200 && code < 300) {
                JsonNode ok = safeParse(bodyText);
                if (ok != null) return ok;
                throw new RuntimeException("Respuesta inesperada del servidor.");
            }

            throw new RuntimeException(pickErrorMessage(code, bodyText, "No autorizado."));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No hay conexión con el servidor.");
        }
    }

    // ---------- POST ----------
    public static JsonNode postJson(String url, Object body, String bearerToken) {
        try {
            String payload = json.writeValueAsString(body);

            HttpResponse<String> res = http.send(
                    baseBuilder(url, bearerToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            int code = res.statusCode();
            String bodyText = res.body();

            if (code >= 200 && code < 300) {
                JsonNode ok = safeParse(bodyText);
                if (ok != null) return ok;
                return json.createObjectNode();
            }

            throw new RuntimeException("Error: " + bodyText);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No hay conexión con el servidor.");
        }
    }

    // ---------- PATCH ----------
    public static JsonNode patchJson(String url, Object body, String bearerToken) {
        try {
            String payload = json.writeValueAsString(body);

            HttpResponse<String> res = http.send(
                    baseBuilder(url, bearerToken)
                            .header("Content-Type", "application/json")
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            int code = res.statusCode();
            String bodyText = res.body();

            if (code >= 200 && code < 300) {
                JsonNode ok = safeParse(bodyText);
                if (ok != null) return ok;
                return json.createObjectNode();
            }

            throw new RuntimeException(pickErrorMessage(code, bodyText, "No autorizado."));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No hay conexión con el servidor.");
        }
    }

    // ---------- PUT ----------
    public static JsonNode putJson(String url, Object body, String bearerToken) {
        try {
            String payload = json.writeValueAsString(body);

            HttpResponse<String> res = http.send(
                    baseBuilder(url, bearerToken)
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            int code = res.statusCode();
            String bodyText = res.body();

            if (code >= 200 && code < 300) {
                JsonNode ok = safeParse(bodyText);
                if (ok != null) return ok;
                return json.createObjectNode();
            }

            throw new RuntimeException(pickErrorMessage(code, bodyText, "No autorizado."));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No hay conexión con el servidor.");
        }
    }

    // ---------- DELETE ----------
    public static void delete(String url, String bearerToken) {
        try {
            HttpResponse<String> res = http.send(
                    baseBuilder(url, bearerToken).DELETE().build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            int code = res.statusCode();
            if (code >= 200 && code < 300) {
                return; // 204/200 OK
            }

            String bodyText = res.body();
            throw new RuntimeException(pickErrorMessage(code, bodyText, "No autorizado."));

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("No hay conexión con el servidor.");
        }
    }
}
