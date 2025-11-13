package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record UserDTO(
        int id,
        String run,
        String username,
        String pnombre,
        String apPaterno,
        String correo,
        String direccion,
        int idRol,
        int idComuna,
        boolean isActive
) {
    public String nombreCompleto() {
        String n = ((pnombre == null ? "" : pnombre) + " " + (apPaterno == null ? "" : apPaterno)).trim();
        return n.isBlank() ? username : n;
    }

    public static UserDTO from(JsonNode n) {
        return new UserDTO(
                n.path("id").asInt(),
                n.path("run").asText(""),
                n.path("username").asText(""),
                n.path("pnombre").asText(""),
                n.path("ap_paterno").asText(""),
                n.path("correo").asText(n.path("email").asText("")),
                n.path("direccion").asText(""),
                n.path("idRol").asInt(0),
                n.path("idComuna").asInt(0),
                n.path("is_active").asBoolean(true)
        );
    }
}
