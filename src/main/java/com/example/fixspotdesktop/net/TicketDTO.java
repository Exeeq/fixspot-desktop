package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record TicketDTO(
        int idTicket,
        String asunto,
        int solicitante,
        int estadoId,
        String estadoNombre
) {
    public static TicketDTO from(JsonNode n, String estadoNombre) {
        return new TicketDTO(
                n.get("idTicket").asInt(),
                n.get("asunto").asText(""),
                n.get("solicitante").asInt(),
                n.get("EstadoTicket").asInt(),
                estadoNombre
        );
    }
}
