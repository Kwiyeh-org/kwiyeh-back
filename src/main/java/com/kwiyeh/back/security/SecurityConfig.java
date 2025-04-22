package com.kwiyeh.back.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kwiyeh.back.firebase.FirebaseAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final FirebaseAuthenticationFilter firebaseAuthFilter = new FirebaseAuthenticationFilter();
    
    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
            .csrf(AbstractHttpConfigurer::disable) // enable for production
            .authorizeHttpRequests(registry ->{
                registry.requestMatchers("/signup").permitAll();
                registry.requestMatchers("/login").permitAll();
                registry.requestMatchers("/forgetPassword").permitAll();
                registry.requestMatchers("/resetPassword").permitAll();
                registry.anyRequest().authenticated();
            })
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

}
