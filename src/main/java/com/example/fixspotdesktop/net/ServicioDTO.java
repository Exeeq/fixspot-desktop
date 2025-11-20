package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record ServicioDTO(
        int id,
        String nombre,
        String descripcion
) {
    public static ServicioDTO from(JsonNode n) {
        return new ServicioDTO(
                n.get("idServicio").asInt(),
                n.get("nombreServicio").asText(""),
                n.get("descripcion").asText("")
        );
    }
}
