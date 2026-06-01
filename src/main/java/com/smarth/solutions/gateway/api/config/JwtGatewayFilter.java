package com.smarth.solutions.gateway.api.config;

import com.smarth.solutions.gateway.api.service.JwtService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        if (request.getMethod().name().equals("OPTIONS")) {
        return chain.filter(exchange);
        }


        if (path.contains("/api/v1/auth") || 
            path.contains("/api/v1/healths") ||
            path.contains("/api/v1/regions") || 
            path.contains("/api/v1/communes") || 
            path.contains("/api/v1/addresses") || 
            path.contains("/swagger-ui") || 
            path.contains("/v3/api-docs")){
            return chain.filter(exchange);
        }

        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        
        if (cookie == null || cookie.getValue().isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete(); 
        }

        String token = cookie.getValue();

        if (!jwtService.isTokenValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String email = jwtService.extractClaim(token, Claims::getSubject);
        Long id = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", String.valueOf(id))
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return 1; 
    }
}