package com.smarth.solutions.gateway.api.config;

import com.smarth.solutions.gateway.api.service.JwtService;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/**",
            "/api/v1/healths/**",
            "/api/v1/regions/**",
            "/api/v1/communes/**",
            "/api/v1/addresses/**",
            "/api/v1/plans",
            "/swagger-ui/**",
            "/v3/api-docs/**");

    public JwtGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (request.getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }

        boolean isPublic = PUBLIC_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));

        if (isPublic) {
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