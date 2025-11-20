package com.example.fixspotdesktop.net;

import com.example.fixspotdesktop.auth.AuthService;
import com.example.fixspotdesktop.net.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class UsersApi {
    private static final String BASE = AuthService.API_BASE + "/usuarios/";

    public static List<UserDTO> listAll() {
        JsonNode n = ApiClient.getJson(BASE, AuthService.getAccessToken());
        List<UserDTO> out = new ArrayList<>();

        if (n != null && n.isArray()) {
            for (JsonNode u : n) out.add(UserDTO.from(u));
        } else if (n != null && n.has("results")) {
            for (JsonNode u : n.get("results")) out.add(UserDTO.from(u));
        }
        return out;
    }

    public static UserDTO getById(int id) {
        JsonNode n = ApiClient.getJson(BASE + id + "/", AuthService.getAccessToken());
        return UserDTO.from(n);
    }

    public static void createUser(Map<String, String> newUser) {
        JsonNode response = ApiClient.postJson(BASE, newUser, AuthService.getAccessToken());

    }

    public static void updateUser(int id, Map<String, String> body) {
        ApiClient.patchJson(BASE + id + "/", body, AuthService.getAccessToken());
    }

    public static void deleteById(int id) {
        ApiClient.delete(BASE + id + "/", AuthService.getAccessToken());
    }
}
