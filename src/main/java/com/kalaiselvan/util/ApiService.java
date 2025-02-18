package com.kalaiselvan.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {
    private static final String API_URL = "http://localhost:3000/endpoint";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiResponse makeApiCall() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return objectMapper.readValue(connection.getInputStream(), ApiResponse.class);
        } else {
            throw new IOException("API call failed with HTTP code: " + responseCode);
        }
    }
}