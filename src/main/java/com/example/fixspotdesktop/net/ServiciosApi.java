package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class ServiciosApi {

    private static final String BASE = AuthService.API_BASE + "/servicios/";

    public static List<ServicioDTO> listAll() {
        JsonNode json = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<ServicioDTO> out = new ArrayList<>();

        if (json != null && json.isArray()) {
            for (JsonNode s : json) out.add(ServicioDTO.from(s));
        }
        return out;
    }
}
