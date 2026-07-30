package org.ultra.rcrs.metadata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(prefix = "spring.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    @Value("${spring.security.admin.client-id}")
    private String clientId;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new BearerTokenServerAuthenticationEntryPoint())
                        .accessDeniedHandler(new BearerTokenServerAccessDeniedHandler())
                )
                .authorizeExchange(auth -> auth
                        .pathMatchers("/api/catalog/swagger-ui/**").permitAll()
                        .pathMatchers("/api/catalog/v3/api-docs/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/catalog/**").permitAll()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN_READ")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(reactiveJwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter reactiveJwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();
        reactiveJwtAuthenticationConverter.setPrincipalClaimName("preferred_username");

        reactiveJwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null) {
                return Flux.empty();
            }

            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess == null) {
                return Flux.empty();
            }

            List<String> roles = (List<String>) clientAccess.get("roles");
            if (roles == null) {
                return Flux.empty();
            }

            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        });
        return reactiveJwtAuthenticationConverter;
    }
}
