package com.ynov.coworking.reservation.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class RoomClient {

    private final RestTemplate restTemplate;

    @Value("${services.room-service.url:http://room-service}")
    private String roomServiceUrl;

    public RoomClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isAvailable(Long roomId) {
        try {
            var response = restTemplate.exchange(
                    roomServiceUrl + "/rooms/" + roomId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body == null) return false;
            Object available = body.get("available");
            return Boolean.TRUE.equals(available);
        } catch (HttpClientErrorException e) {
            return false;
        }
    }

    public void setAvailability(Long roomId, boolean available) {
        try {
            restTemplate.patchForObject(
                    roomServiceUrl + "/rooms/" + roomId + "/availability",
                    Map.of("available", available),
                    Map.class
            );
        } catch (Exception e) {
            // log silencieux — la disponibilité sera re-synchronisée via Kafka si besoin
        }
    }
}
