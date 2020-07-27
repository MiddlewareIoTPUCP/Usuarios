package pucp.middlewareiot.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(exchanges ->
                exchanges
                        .pathMatchers(HttpMethod.GET, "/user/me").hasAuthority("SCOPE_all")
                        .pathMatchers(HttpMethod.GET, "/user/createOwnerToken").hasAuthority("SCOPE_all")
                        .anyExchange().permitAll()
                )
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                .csrf().disable()
                .build();
    }
}
