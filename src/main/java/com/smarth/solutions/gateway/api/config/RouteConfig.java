package com.smarth.solutions.gateway.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${auth.service.uri:http://smartsolutions-auth-alb-1154380074.sa-east-1.elb.amazonaws.com}")
    private String authServiceUri;

    @Value("${payment.service.uri:http://localhost:3000}")
    private String paymentServiceUri;

    @Bean
    public RouteLocator additionalRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-admin", r -> r
                        .path("/api/v1/admin/**", "/api/v1/roles/**", "/api/v1/healths/**")
                        .uri(authServiceUri))
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .uri(paymentServiceUri))
                .build();
    }
}
