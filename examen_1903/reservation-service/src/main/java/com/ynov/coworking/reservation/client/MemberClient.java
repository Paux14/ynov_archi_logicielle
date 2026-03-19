package com.ynov.coworking.reservation.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class MemberClient {

    private final RestTemplate restTemplate;

    @Value("${services.member-service.url:http://member-service}")
    private String memberServiceUrl;

    public MemberClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isSuspended(Long memberId) {
        try {
            var response = restTemplate.exchange(
                    memberServiceUrl + "/members/" + memberId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body == null) return true;
            Object suspended = body.get("suspended");
            return Boolean.TRUE.equals(suspended);
        } catch (HttpClientErrorException e) {
            return true;
        }
    }

    public Integer getMaxConcurrentBookings(Long memberId) {
        try {
            var response = restTemplate.exchange(
                    memberServiceUrl + "/members/" + memberId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();
            if (body == null) return 0;
            Object max = body.get("maxConcurrentBookings");
            return max instanceof Number n ? n.intValue() : 0;
        } catch (HttpClientErrorException e) {
            return 0;
        }
    }
}
