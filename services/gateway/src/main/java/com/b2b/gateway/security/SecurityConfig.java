package com.b2b.gateway.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig
{

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter)
    {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http)
    {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // public
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // payment - ADMIN only for full list, authenticated rest
                        .pathMatchers(HttpMethod.GET, "/payment/all").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/payment").authenticated()
                        .pathMatchers(HttpMethod.GET, "/payment/*").authenticated()

                        // account - management operations ADMIN only
                        .pathMatchers(HttpMethod.GET, "/account/all").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/account/email/*").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/account").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/account/*/balance").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/account/*/balance/*").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/account/*").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/account/*").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
