package com.ynov.coworking.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        // HttpComponentsClientHttpRequestFactory supporte la méthode HTTP PATCH
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
}
