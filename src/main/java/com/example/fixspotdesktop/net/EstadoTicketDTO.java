package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record EstadoTicketDTO(
        int id,
        String nombre
) {
    public static EstadoTicketDTO from(JsonNode n) {
        return new EstadoTicketDTO(
                n.path("idEstado").asInt(),
                n.path("NombreEstado").asText("")
        );
    }
}
