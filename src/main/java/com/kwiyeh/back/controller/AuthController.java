package com.kwiyeh.back.controller;

import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.model.PasswordReset;
import com.kwiyeh.back.model.UserClient;
import com.kwiyeh.back.model.UserTalent;
import com.kwiyeh.back.service.MailService;
import com.kwiyeh.back.service.UserService;
import com.kwiyeh.back.utils.GoogleLoginRequest;
import com.kwiyeh.back.utils.JwtUtil;
import com.kwiyeh.back.utils.LoginRequest;
import com.kwiyeh.back.utils.MyAppFunctions;
import com.kwiyeh.back.utils.PasswordResetRequest;
import com.kwiyeh.back.utils.SignUpRequest;
import com.kwiyeh.back.utils.VerifyCodeRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;


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
public class AuthController {

    @Value("${firebase.api.key}")
    private String firebaseApiKey;
    @Value("${google.mobile.client.id}")
    private String googleMobileClientId;
    private final MailService mailService = new MailService();
    private final UserService userService = new UserService();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/signup")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
public ResponseEntity<?> signUp(@RequestBody SignUpRequest request) {
        System.out.println("[signup] Payload: " + request.toString());
        if (request.getFullName() == null || request.getFullName().trim().isEmpty() ||
            request.getEmail() == null || request.getEmail().trim().isEmpty() ||
            request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty() ||
            request.getRole() == null || request.getRole().trim().isEmpty()) {
            System.out.println("[signup] Missing required field(s): " + request.toString());
            return ResponseEntity.status(400).body(java.util.Map.of("error", "Missing required field(s)"));
        }
        
        try {
            // Check if user exists in Firebase Auth with original email
            UserRecord existingUser = null;
            String firebaseEmail = request.getEmail();
            
            try {
                existingUser = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
                // If user exists, check if they have the same role in Firestore
                AppUser existingAppUser = userService.getUserInfo(existingUser.getUid());
                if (existingAppUser != null && existingAppUser.getRole().equals(request.getRole())) {
                    System.out.println("[signup] Email already in use for this role: " + request.getEmail() + ", Role: " + request.getRole());
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", "This email is already in use for this role."));
                }
                // If user exists but with different role, we need to use modified email for Firebase
                firebaseEmail = request.getEmail().replace("@", "+" + request.getRole() + "@");
                System.out.println("[signup] Email exists but with different role, using modified email: " + firebaseEmail);
                
                // Check if the modified email already exists in Firebase
                try {
                    FirebaseAuth.getInstance().getUserByEmail(firebaseEmail);
                    // Modified email already exists, this shouldn't happen in normal flow
                    System.out.println("[signup] Modified email already exists: " + firebaseEmail);
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", "Account already exists for this role."));
                } catch (FirebaseAuthException e2) {
                    if (e2.getErrorCode().toString().equals("USER_NOT_FOUND") || e2.getErrorCode().toString().equals("NOT_FOUND")) {
                        // Modified email doesn't exist, we can proceed
                        System.out.println("[signup] Modified email available: " + firebaseEmail);
                    } else {
                        throw e2;
                    }
                }
    } catch (FirebaseAuthException e) {
                if (!e.getErrorCode().toString().equals("USER_NOT_FOUND") && !e.getErrorCode().toString().equals("NOT_FOUND")) {
                    System.out.println("[signup] Firebase error: " + e.getErrorCode().toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
                }
                // User doesn't exist, we can use original email
                System.out.println("[signup] User doesn't exist, using original email: " + request.getEmail());
            }
            
            // Create new Firebase user with appropriate email
                    UserRecord userRecord = FirebaseAuth.getInstance().createUser(
                            new UserRecord.CreateRequest()
                            .setEmail(firebaseEmail)
                                    .setPassword(request.getPassword())
                                    .setDisplayName(request.getFullName())
                                    .setPhoneNumber(request.getPhoneNumber())
                                    );
            
                    mailService.sendSignupMail(request.getEmail(), request.getFullName());
                    AppUser user = new AppUser(
                            userRecord.getUid(),
                            request.getEmail(),
                            request.getFullName(),
                            request.getPhoneNumber(),
                            request.getRole()
                    );
                    userService.addUserInfo(user);
                    System.out.println("[signup] Signup successful for: " + request.getEmail() + ", UID: " + userRecord.getUid() + ", Role: " + request.getRole());
            
            // After creating the user, sign in to get tokens using the email we used for Firebase
            String signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;
            HttpHeaders signInHeaders = new HttpHeaders();
            signInHeaders.setContentType(MediaType.APPLICATION_JSON);
            String signInRequestBody = String.format(
                "{\"email\":\"%s\", \"password\":\"%s\", \"returnSecureToken\":true}",
                firebaseEmail, request.getPassword()
            );
            HttpEntity<String> signInEntity = new HttpEntity<>(signInRequestBody, signInHeaders);
            RestTemplate signInRestTemplate = new RestTemplate();
            ResponseEntity<String> signInResponse = signInRestTemplate.postForEntity(signInUrl, signInEntity, String.class);
            com.fasterxml.jackson.databind.JsonNode signInJson = objectMapper.readTree(signInResponse.getBody());
            String idToken = signInJson.has("idToken") ? signInJson.get("idToken").asText() : null;
            String refreshToken = signInJson.has("refreshToken") ? signInJson.get("refreshToken").asText() : null;
            String expiresIn = signInJson.has("expiresIn") ? signInJson.get("expiresIn").asText() : null;
            
                    // Fetch and return full profile
                    AppUser savedUser = userService.getUserInfo(userRecord.getUid());
            // Return the expected format for frontend compatibility
                    var resp = new java.util.LinkedHashMap<String, Object>();
            resp.put("kind", "identitytoolkit#VerifyPasswordResponse");
            resp.put("localId", savedUser.getUid());
            resp.put("email", savedUser.getEmail());
            resp.put("displayName", savedUser.getFullName());
            resp.put("idToken", idToken); // Use real token
            resp.put("registered", true);
            resp.put("refreshToken", refreshToken); // Use real refresh token
            resp.put("expiresIn", expiresIn); // Use real expiresIn
            // Add additional fields for your app
                    resp.put("uid", savedUser.getUid());
                    resp.put("fullName", savedUser.getFullName());
                    resp.put("phoneNumber", savedUser.getPhoneNumber());
                    resp.put("role", savedUser.getRole());
                    String photoURL = "client".equals(savedUser.getRole()) ? savedUser.getClientImageUrl() : savedUser.getTalentImageUrl();
                    String location = "client".equals(savedUser.getRole()) ? savedUser.getClientLocation() : savedUser.getTalentLocation();
                    resp.put("photoURL", photoURL);
                    resp.put("location", location);
                    if ("talent".equals(savedUser.getRole())) {
                        resp.put("talentCategory", savedUser.getTalentCategory());
                        resp.put("talentDescription", savedUser.getTalentDescription());
                        resp.put("experience", (savedUser instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)savedUser).getExperience() : null);
                        resp.put("pricing", savedUser.getPricing());
                        resp.put("availability", savedUser.getAvailability());
                        resp.put("services", (savedUser instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)savedUser).getServices() : null);
                    }
                    String json = objectMapper.writeValueAsString(resp);
                    System.out.println("[signup] Response: " + json);
                    return ResponseEntity.ok(json);
        } catch (FirebaseAuthException e) {
            System.out.println("[signup] FirebaseAuthException: " + e.getErrorCode().toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[signup] Exception: " + e.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Unknown error, please try again"));
        } catch (Exception e) {
            System.out.println("[signup] Unexpected Exception: " + e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", e.getMessage()));
    }
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("[login] Payload: " + request.toString());
        
        // First, try to find user by original email in Firestore
        AppUser user = null;
        String firebaseEmail = request.getEmail();
        
        // Try to find user with the correct role
        user = null;
        firebaseEmail = request.getEmail();
        
        // First try with original email
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            AppUser foundUser = userService.getUserInfo(userRecord.getUid());
            
            if (foundUser != null && request.getRole().equals(foundUser.getRole())) {
                // Perfect match - user exists with original email and correct role
                user = foundUser;
                firebaseEmail = request.getEmail();
                System.out.println("[login] Found user with original email and correct role: " + request.getEmail() + ", role: " + user.getRole());
            } else if (foundUser != null && !request.getRole().equals(foundUser.getRole())) {
                // User exists with original email but wrong role - try modified email
                System.out.println("[login] User found with original email but wrong role: " + request.getEmail() + ", found role: " + foundUser.getRole() + ", requested role: " + request.getRole());
                String modifiedEmail = request.getEmail().replace("@", "+" + request.getRole() + "@");
                try {
                    UserRecord userRecord2 = FirebaseAuth.getInstance().getUserByEmail(modifiedEmail);
                    AppUser talentUser = userService.getUserInfo(userRecord2.getUid());
                    if (talentUser != null && request.getRole().equals(talentUser.getRole())) {
                        user = talentUser;
                        firebaseEmail = modifiedEmail;
                        System.out.println("[login] Found user with modified email: " + modifiedEmail + ", role: " + user.getRole());
                    } else {
                        System.out.println("[login] User not found with modified email or wrong role: " + modifiedEmail);
                        return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid email or password"));
                    }
                } catch (FirebaseAuthException e2) {
                    System.out.println("[login] User not found with modified email: " + modifiedEmail);
                    return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid email or password"));
                } catch (InterruptedException | ExecutionException e3) {
                    System.out.println("[login] Exception: " + e3.toString());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Unknown error, please try again"));
                }
            }
        } catch (FirebaseAuthException e) {
            if (e.getErrorCode().toString().equals("USER_NOT_FOUND") || e.getErrorCode().toString().equals("NOT_FOUND")) {
                // Original email not found - try with modified email for the requested role
                String modifiedEmail = request.getEmail().replace("@", "+" + request.getRole() + "@");
                try {
                    UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(modifiedEmail);
                    AppUser foundUser = userService.getUserInfo(userRecord.getUid());
                    if (foundUser != null && request.getRole().equals(foundUser.getRole())) {
                        user = foundUser;
                        firebaseEmail = modifiedEmail;
                        System.out.println("[login] Found user with modified email: " + modifiedEmail + ", role: " + user.getRole());
                    } else {
                        System.out.println("[login] User not found with modified email or wrong role: " + modifiedEmail);
                        return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid email or password"));
                    }
                } catch (FirebaseAuthException e2) {
                    System.out.println("[login] User not found with original or modified email: " + request.getEmail());
                    return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid email or password"));
                } catch (InterruptedException | ExecutionException e3) {
                    System.out.println("[login] Exception: " + e3.toString());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Unknown error, please try again"));
                }
            } else {
                System.out.println("[login] FirebaseAuthException: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[login] Exception: " + e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Unknown error, please try again"));
        }
        
        if (user == null) {
            System.out.println("[login] No user profile found for email: " + request.getEmail());
            return ResponseEntity.status(404).body(java.util.Map.of("error", "User profile not found"));
        }
        
        // Now try to authenticate with Firebase using the correct email
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = String.format(
                "{\"email\":\"%s\", \"password\":\"%s\", \"returnSecureToken\":true}",
                firebaseEmail, request.getPassword()
        );
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("[login] Success for: " + request.getEmail() + ", Response: " + response.getBody());
            // Parse Firebase REST API response for tokens
            com.fasterxml.jackson.databind.JsonNode firebaseJson = objectMapper.readTree(response.getBody());
            String idToken = firebaseJson.has("idToken") ? firebaseJson.get("idToken").asText() : null;
            String refreshToken = firebaseJson.has("refreshToken") ? firebaseJson.get("refreshToken").asText() : null;
            String expiresIn = firebaseJson.has("expiresIn") ? firebaseJson.get("expiresIn").asText() : null;
            
            // Return the expected format for frontend compatibility
            var resp = new java.util.LinkedHashMap<String, Object>();
            resp.put("kind", "identitytoolkit#VerifyPasswordResponse");
            resp.put("localId", user.getUid());
            resp.put("email", user.getEmail()); // Always return original email
            resp.put("displayName", user.getFullName());
            resp.put("idToken", idToken); // Use real token
            resp.put("registered", true);
            resp.put("refreshToken", refreshToken); // Use real refresh token
            resp.put("expiresIn", expiresIn); // Use real expiresIn
            // Add additional fields for your app
            resp.put("uid", user.getUid());
            resp.put("fullName", user.getFullName());
            resp.put("phoneNumber", user.getPhoneNumber());
            resp.put("role", user.getRole());
            String photoURL = "client".equals(user.getRole()) ? user.getClientImageUrl() : user.getTalentImageUrl();
            String location = "client".equals(user.getRole()) ? user.getClientLocation() : user.getTalentLocation();
            resp.put("photoURL", photoURL);
            resp.put("location", location);
            if ("talent".equals(user.getRole())) {
                resp.put("talentCategory", user.getTalentCategory());
                resp.put("talentDescription", user.getTalentDescription());
                resp.put("experience", (user instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)user).getExperience() : null);
                resp.put("pricing", user.getPricing());
                resp.put("availability", user.getAvailability());
                resp.put("services", (user instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)user).getServices() : null);
            }
            String json = objectMapper.writeValueAsString(resp);
            System.out.println("[login] Response: " + json);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (HttpClientErrorException e) {
            System.out.println("[login] Failed for: " + request.getEmail() + ", Error: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("[login] Unexpected Exception: " + e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        System.out.println("[google-login] Payload: " + request.toString());
        String idTokenString = request.getToken();
        String role = request.getRole();
        
        try {
            // Verify Google ID token directly (no Firebase verification first)
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                        .setAudience(java.util.Collections.singletonList(googleMobileClientId))
                        .build();
                GoogleIdToken idToken = verifier.verify(idTokenString);
            
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String email = payload.getEmail();
                    String name = (String) payload.get("name");
                
                System.out.println("[google-login] Email: " + email + ", Role: " + role + ", Name from token: " + name);
                
                // Check if user exists in Firebase with original email
                    UserRecord userRecord = null;
                String firebaseUid = null;
                String firebaseEmail = email;
                
                    try {
                        userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                    firebaseUid = userRecord.getUid();
                    
                    // Check if user exists in Firestore with same role
                    AppUser existingAppUser = userService.getUserInfo(firebaseUid);
                    if (existingAppUser != null && existingAppUser.getRole().equals(role)) {
                        System.out.println("[google-login] User already exists with same role: " + email + ", Role: " + role);
                        // User exists with same role, proceed with login
                    } else if (existingAppUser != null && !existingAppUser.getRole().equals(role)) {
                        // User exists but with different role, we need to create or use a Firebase user with modified email
                        System.out.println("[google-login] User exists with different role, creating or using Firebase user with modified email");
                        firebaseEmail = email.replace("@", "+" + role + "@");
                        try {
                            userRecord = FirebaseAuth.getInstance().getUserByEmail(firebaseEmail);
                            firebaseUid = userRecord.getUid();
                            System.out.println("[google-login] Modified email already exists, logging in: " + firebaseEmail);
                            // Continue to fetch user info and return login response
                        } catch (FirebaseAuthException e2) {
                            if (e2.getErrorCode().toString().equals("USER_NOT_FOUND") || e2.getErrorCode().toString().equals("NOT_FOUND")) {
                                // Modified email doesn't exist, we can proceed to create
                                System.out.println("[google-login] Modified email available: " + firebaseEmail);
                                UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                                    .setEmail(firebaseEmail)
                                    .setDisplayName(name);
                                userRecord = FirebaseAuth.getInstance().createUser(createRequest);
                                firebaseUid = userRecord.getUid();
                                System.out.println("[google-login] Created new Firebase user with modified email: " + firebaseUid);
                        } else {
                                throw e2;
                            }
                        }
                    }
                } catch (FirebaseAuthException e) {
                    if (e.getErrorCode().toString().equals("USER_NOT_FOUND") || e.getErrorCode().toString().equals("NOT_FOUND")) {
                        // Create new Firebase user with original email
                        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                            .setEmail(email)
                            .setDisplayName(name);
                        userRecord = FirebaseAuth.getInstance().createUser(createRequest);
                        firebaseUid = userRecord.getUid();
                        System.out.println("[google-login] Created new Firebase user: " + firebaseUid);
                    } else {
                        throw e;
                    }
                }
                
                // Check if user exists in Firestore and create if needed
                AppUser existingAppUser = userService.getUserInfo(firebaseUid);
                if (existingAppUser == null) {
                            mailService.sendSignupMail(email, name);
                            AppUser user2 = new AppUser(
                        firebaseUid,
                        email, // Always store original email in Firestore
                                name,
                        "", // Empty phone number for Google login
                                role
                            );
                            userService.addUserInfo(user2);
                    System.out.println("[google-login] Created new user in Firestore: " + email + ", Role: " + role);
                        } else {
                    System.out.println("[google-login] User already exists in Firestore: " + email + ", Role: " + role);
                    // Update user's name if it's null but we have a name from Google token
                    if ((existingAppUser.getFullName() == null || existingAppUser.getFullName().isEmpty()) && name != null && !name.isEmpty()) {
                        existingAppUser.setFullName(name);
                        userService.updateUser(existingAppUser);
                        System.out.println("[google-login] Updated user name from Google token: " + name);
                    }
                        }
                
                    // Fetch and return full profile
                AppUser savedUser = userService.getUserInfo(firebaseUid);
                
                // Get Firebase tokens using the Google ID token
                String googleSignInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=" + firebaseApiKey;
                HttpHeaders googleHeaders = new HttpHeaders();
                googleHeaders.setContentType(MediaType.APPLICATION_JSON);
                String googleRequestBody = String.format(
                    "{\"postBody\":\"id_token=%s&providerId=google.com\",\"requestUri\":\"http://localhost\",\"returnIdpCredential\":true,\"returnSecureToken\":true}",
                    idTokenString
                );
                HttpEntity<String> googleEntity = new HttpEntity<>(googleRequestBody, googleHeaders);
                RestTemplate googleRestTemplate = new RestTemplate();
                ResponseEntity<String> googleResponse = googleRestTemplate.postForEntity(googleSignInUrl, googleEntity, String.class);
                com.fasterxml.jackson.databind.JsonNode googleJson = objectMapper.readTree(googleResponse.getBody());
                String idToken2 = googleJson.has("idToken") ? googleJson.get("idToken").asText() : null;
                String refreshToken2 = googleJson.has("refreshToken") ? googleJson.get("refreshToken").asText() : null;
                String expiresIn2 = googleJson.has("expiresIn") ? googleJson.get("expiresIn").asText() : null;
                
                // Return the same format as login/signup
                    var resp = new java.util.LinkedHashMap<String, Object>();
                resp.put("kind", "identitytoolkit#VerifyPasswordResponse");
                resp.put("localId", savedUser.getUid());
                resp.put("email", savedUser.getEmail()); // Always return original email
                resp.put("displayName", name != null ? name : savedUser.getFullName());
                resp.put("idToken", idToken2);
                resp.put("registered", true);
                resp.put("refreshToken", refreshToken2);
                resp.put("expiresIn", expiresIn2);
                    resp.put("uid", savedUser.getUid());
                    resp.put("fullName", name != null ? name : savedUser.getFullName());
                    resp.put("phoneNumber", savedUser.getPhoneNumber());
                    resp.put("role", savedUser.getRole());
                    String photoURL = "client".equals(savedUser.getRole()) ? savedUser.getClientImageUrl() : savedUser.getTalentImageUrl();
                    String location = "client".equals(savedUser.getRole()) ? savedUser.getClientLocation() : savedUser.getTalentLocation();
                    resp.put("photoURL", photoURL);
                    resp.put("location", location);
                    if ("talent".equals(savedUser.getRole())) {
                        resp.put("talentCategory", savedUser.getTalentCategory());
                        resp.put("talentDescription", savedUser.getTalentDescription());
                        resp.put("experience", (savedUser instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)savedUser).getExperience() : null);
                        resp.put("pricing", savedUser.getPricing());
                        resp.put("availability", savedUser.getAvailability());
                        resp.put("services", (savedUser instanceof com.kwiyeh.back.model.UserTalent) ? ((com.kwiyeh.back.model.UserTalent)savedUser).getServices() : null);
                    }
                
                    String json = objectMapper.writeValueAsString(resp);
                    System.out.println("[google-login] Response: " + json);
                    return ResponseEntity.ok(json);
                } else {
                    System.out.println("[google-login] Invalid Google token");
                return ResponseEntity.status(401).body("Invalid Google token");
            }
        } catch (Exception e) {
            System.out.println("[google-login] Exception: " + e.getMessage());
            return ResponseEntity.status(500).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/forgetPassword")
    public ResponseEntity<?> forgetPasswordMail(@RequestParam String email) {
        System.out.println("[forgetPassword] Request for: " + email);
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            String code = MyAppFunctions.GenerateForgetPasswordCode();
            PasswordReset passwordReset = new PasswordReset();
            passwordReset.setForgetPasswordCode(code);
            passwordReset.setCreatedAt(Calendar.getInstance().getTime());
            userService.createPasswordReset(email, passwordReset);
            mailService.sendForgetPasswordMail(email, userRecord.getDisplayName(), code);
            System.out.println("[forgetPassword] Mail sent to: " + email);
            return ResponseEntity.ok("Reset password mail sent");
        } catch (FirebaseAuthException e) {
            System.out.println("[forgetPassword] FirebaseAuthException: " + e.getErrorCode().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[forgetPassword] Exception: " + e.toString());
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Unknown error, please try again");
        }
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest verifyCodeReq) {
        System.out.println("[verifyCode] Payload: " + verifyCodeReq.toString());
        try {
            PasswordReset expectedPasswordReset = userService.getPasswordReset(verifyCodeReq.getEmail());
            if (expectedPasswordReset == null) {
                System.out.println("[verifyCode] Code expired or already used for: " + verifyCodeReq.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("code expired or already used");
            }
            if (expectedPasswordReset.getForgetPasswordCode().equals(verifyCodeReq.getForgetPasswordCode())) {
                userService.deletePasswordReset(verifyCodeReq.getEmail());
                String token = JwtUtil.generateToken(verifyCodeReq.getEmail());
                System.out.println("[verifyCode] Token generated for: " + verifyCodeReq.getEmail());
                return ResponseEntity.ok("{\"passwordToken\": \"" + token + "\", \"expiresIn\": \"600\"}");
            }
            System.out.println("[verifyCode] Wrong code for: " + verifyCodeReq.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong code");
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[verifyCode] Exception: " + e.toString());
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Unknown error, please try again");
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String authHeader, @RequestBody PasswordResetRequest passwordResetReq) {
        String idToken = authHeader.replace("Bearer ", "");
        System.out.println("[resetPassword] Payload: " + passwordResetReq.toString());
        try {
            Claims decodedToken = JwtUtil.parseToken(idToken);
            if (decodedToken == null) {
                System.out.println("[resetPassword] Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            if (decodedToken.getExpiration().getTime() < Calendar.getInstance().getTimeInMillis()) {
                System.out.println("[resetPassword] Token expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(decodedToken.getSubject());
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
            .setPassword(passwordResetReq.getPassword());
            FirebaseAuth.getInstance().updateUser(updateRequest);
            System.out.println("[resetPassword] Password reset for: " + decodedToken.getSubject());
            return ResponseEntity.ok("Password reset done");
        } catch (FirebaseAuthException e) {
            System.out.println("[resetPassword] FirebaseAuthException: " + e.getErrorCode().toString());
            return ResponseEntity.status(e.getErrorCode().ordinal()).body(e.getMessage());
        }
    }

}

