package com.kwiyeh.back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = {
    "http://localhost:8081", 
    "http://localhost:8082", 
    "http://localhost:19006",
    "http://localhost:3000",
    "http://localhost:3001",
    "http://localhost:4200",
    "http://localhost:5173"
}, allowCredentials = "true")
public class ProtectedController {

    @GetMapping("/protected")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<String> protectedEndpoint() {
            return ResponseEntity.ok("Hello, "+ "protected endpoint accessed!");
    }
}
