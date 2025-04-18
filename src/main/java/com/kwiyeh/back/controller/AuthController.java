package com.kwiyeh.back.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.kwiyeh.back.service.MailService;

@RestController
public class AuthController {

    @Value("${firebase.api.key}")
    private String firebaseApiKey;
    private final MailService mailService = new MailService();;

    @PostMapping("/signup")
public ResponseEntity<String> signUp(@RequestBody SignUpRequest request) {
    try {
        // Check if email already exists in Firebase
        FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use");
    } catch (FirebaseAuthException e) {
        try {
            if (e.getErrorCode().toString().equals("NOT_FOUND")) {
                // Email doesn't exist → create the user
                UserRecord userRecord;
                    userRecord = FirebaseAuth.getInstance().createUser(
                            new UserRecord.CreateRequest()
                                    .setEmail(request.getEmail())
                                    .setPassword(request.getPassword())
                                    .setDisplayName(request.getFullName())
                                    .setPhoneNumber(request.getPhoneNumber())
                                    );
                    mailService.sendSignupMail(request.getEmail(),request.getFullName());
                    return ResponseEntity.ok("User created: " + userRecord.getUid());
                } 
                else {
                    System.out.println(e.getErrorCode().toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                }
            } catch (FirebaseAuthException e1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e1.getMessage());
            }
    }
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Request body for Firebase
        String requestBody = String.format(
                "{\"email\":\"%s\", \"password\":\"%s\", \"returnSecureToken\":true}",
                request.getEmail(), request.getPassword()
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/google-login")
public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
    try {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getToken());
        String uid = decodedToken.getUid();

        // Check if user exists in Firebase (optional, since token verification implies existence)
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);

        // Optional: Update user info if needed (e.g., name)
        /*UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(uid)
            .setDisplayName(decodedToken.getName());
        FirebaseAuth.getInstance().updateUser(updateRequest);*/
        System.err.println("google login ok");
        return ResponseEntity.ok(Map.of(
            "uid", userRecord.getUid(),
            "email", userRecord.getEmail()
        ));
    } catch (FirebaseAuthException e) {
        return ResponseEntity.status(401).body("Unauthorized");
    }
}
}

