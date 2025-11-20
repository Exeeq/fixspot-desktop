package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record TallerDTO(
        int id,
        int idUsuario,
        String nombre,
        String telefono,
        String direccion,
        int idComuna
) {
    public static TallerDTO from(JsonNode n) {
        return new TallerDTO(
                n.get("idTaller").asInt(),
                n.get("idUsuario").asInt(),
                n.get("nombreTaller").asText(""),
                n.get("telefono").asText(""),
                n.get("direccion").asText(""),
                n.get("idComuna").asInt()
        );
    }
}
