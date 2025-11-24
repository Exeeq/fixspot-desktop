package com.example.fixspotdesktop.net;

import com.fasterxml.jackson.databind.JsonNode;

public record TallerDTO(
        int id,
        int idUsuario,
        String nombreTaller,
        String telefono,
        String direccion,
        int idComuna,
        String descripcion,
        double latitud,
        double longitud,
        String imagen  // Nuevo campo agregado para la imagen
) {
    // Constructor para convertir JsonNode a TallerDTO
    public static TallerDTO from(JsonNode n) {
        System.out.println("Datos del taller: " + n.toString());
        return new TallerDTO(
                n.get("idTaller").asInt(),
                n.get("idUsuario").asInt(),
                n.get("nombreTaller").asText(""),
                n.get("telefono").asText(""),
                n.get("direccion").asText(""),
                n.get("idComuna").asInt(),
                n.get("descripcion").asText(""),
                n.get("latitud").asDouble(),
                n.get("longitud").asDouble(),
                n.get("imagen").asText("")  // Extraemos la imagen
        );
    }

    // Métodos "getter"
    public int getId() { return id; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombreTaller; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public int getIdComuna() { return idComuna; }
    public String getDescripcion() { return descripcion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getImagen() { return imagen; }

    // Método adicional para obtener el nombre de la comuna
    public String getComunaNombre() {
        return ComunasApi.getById(idComuna);
    }
}