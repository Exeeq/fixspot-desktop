package com.example.fixspotdesktop.auth;

import com.example.fixspotdesktop.net.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class AuthService {
    public static String BASE_URL    = "http://127.0.0.1:8000";
    public static String API_BASE    = BASE_URL + "/api";
    public static String LOGIN_URL   = API_BASE + "/token/";
    public static String REFRESH_URL = API_BASE + "/token/refresh/";

    private static String accessToken, refreshToken, username;
    private static String displayName;
    private static String lastError;

    public static boolean login(String user, String pass) {
        lastError = null;

        try {
            // 1) Obtener tokens
            JsonNode tokenNode = ApiClient.postJson(
                    LOGIN_URL, Map.of("username", user, "password", pass), null
            );
            if (!tokenNode.hasNonNull("access")) {
                lastError = "Credenciales inválidas.";
                return false;
            }
            accessToken  = tokenNode.get("access").asText();
            refreshToken = tokenNode.hasNonNull("refresh") ? tokenNode.get("refresh").asText() : null;
            username     = user;

            // 2) Buscar usuario
            JsonNode u = findUserByUsername(user);
            if (u == null) {
                clearSession();
                lastError = "Credenciales inválidas o cuenta inactiva.";
                return false;
            }

            // 3) Validar rol (solo idRol=3)
            int idRol = u.path("idRol").asInt(0);
            boolean active = u.path("is_active").asBoolean(true);
            if (!active) {
                clearSession();
                lastError = "Cuenta inactiva.";
                return false;
            }

            if (idRol == 3) {
                displayName = buildDisplayName(u, username);
                return true;
            }

            clearSession();
            lastError = "Acceso restringido: solo Administradores.";
            return false;

        } catch (RuntimeException e) {
            clearSession();
            lastError = (e.getMessage() == null || e.getMessage().isBlank())
                    ? "No se pudo iniciar sesión."
                    : e.getMessage();
            return false;
        }
    }

    /** Recorre /api/usuarios/ y páginas siguientes hasta encontrar username exacto. */
    private static JsonNode findUserByUsername(String targetUsername) {
        String url = API_BASE + "/usuarios/";
        int safePages = 0;

        while (url != null && safePages++ < 100) {
            JsonNode page = ApiClient.getJson(url, accessToken);

            // Paginado DRF (results/next)
            if (page.has("results") && page.get("results").isArray()) {
                for (JsonNode n : page.get("results")) {
                    if (targetUsername.equalsIgnoreCase(n.path("username").asText(""))) return n;
                }
                url = page.hasNonNull("next") ? page.get("next").asText(null) : null;

                // Lista simple
            } else if (page.isArray()) {
                for (JsonNode n : page) {
                    if (targetUsername.equalsIgnoreCase(n.path("username").asText(""))) return n;
                }
                url = null;

            } else {
                url = null;
            }
        }
        return null;
    }

    /** Construye nombre a mostrar según tus campos. */
    private static String buildDisplayName(JsonNode u, String fallbackUser) {
        String pnombre   = u.path("pnombre").asText("");
        String apPaterno = u.path("ap_paterno").asText("");

        String full = (pnombre + " " + apPaterno).trim();
        if (full.isBlank()) full = u.path("username").asText(fallbackUser);
        return full;
    }

    private static void clearSession() {
        accessToken = null;
        refreshToken = null;
        username = null;
        displayName = null;
    }

    public static void logout() {
        clearSession();
    }

    public static boolean refreshAccessToken() {
        if (refreshToken == null || refreshToken.isBlank()) {
            lastError = "No hay refresh token disponible.";
            return false;
        }

        try {
            JsonNode node = ApiClient.postJson(
                    REFRESH_URL,
                    Map.of("refresh", refreshToken),
                    null
            );

            if (!node.hasNonNull("access")) {
                lastError = "No se pudo refrescar el token.";
                return false;
            }

            accessToken = node.get("access").asText();
            return true;

        } catch (Exception e) {
            lastError = "Error al refrescar token: " + e.getMessage();
            return false;
        }
    }

    public static String ensureValidToken() {
        // Intentar usar accessToken actual
        if (accessToken != null && !accessToken.isBlank()) {
            return accessToken;
        }

        // Token vacío → intentar refrescar
        if (refreshAccessToken()) {
            return accessToken;
        }

        // Último recurso
        throw new RuntimeException("Token inválido o expirado. Por favor inicia sesión nuevamente.");
    }

    // Getters
    public static String getAccessToken() { return accessToken; }
    public static String getUsername()    { return username; }
    public static String getDisplayName() { return (displayName != null && !displayName.isBlank()) ? displayName : username; }
    public static String getLastError()   { return lastError; }
}
