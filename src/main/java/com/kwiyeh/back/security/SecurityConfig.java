package com.kwiyeh.back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kwiyeh.back.firebase.AuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationFilter authFilter = new AuthenticationFilter();

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disable) // enable for production
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(registry ->{
                registry.requestMatchers(
                    "/signup",
                    "/login",
                    "/forgetPassword",
                    "/verifyCode",
                    "/google-login"
                ).permitAll();
                registry.requestMatchers("OPTIONS", "/**").permitAll(); // Allow OPTIONS requests
                registry.anyRequest().authenticated();
            })
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8081", 
            "http://localhost:8082", 
            "http://localhost:19006",
            "http://localhost:3000",
            "http://localhost:3001",
            "http://localhost:4200",
            "http://localhost:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
