package spammy.eve.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import spammy.eve.global.auth.JwtFilter;

import java.util.List;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Value("${spammy.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(frontendUrl));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }));
        return http.build();
    }
}