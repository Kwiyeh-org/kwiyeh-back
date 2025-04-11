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
            .formLogin(httpForm -> {
                httpForm.loginPage("/login.html").permitAll();

            })  // to redirect the login to a custom one
            .authorizeHttpRequests(registry ->{
                registry.requestMatchers("/signup").permitAll();
                registry.requestMatchers("/login").permitAll();
                registry.requestMatchers("/login-page").permitAll();
                registry.requestMatchers("/get").permitAll();
                registry.anyRequest().authenticated();
            })
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

}
