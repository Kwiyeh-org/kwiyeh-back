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
        FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            System.out.println("[signup] Email already in use: " + request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(java.util.Map.of("error", "Email already in use"));
    } catch (FirebaseAuthException e) {
        try {
            if (e.getErrorCode().toString().equals("NOT_FOUND")) {
                    UserRecord userRecord = FirebaseAuth.getInstance().createUser(
                            new UserRecord.CreateRequest()
                                    .setEmail(request.getEmail())
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
                    // After creating the user, sign in to get tokens
                    String signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;
                    HttpHeaders signInHeaders = new HttpHeaders();
                    signInHeaders.setContentType(MediaType.APPLICATION_JSON);
                    String signInRequestBody = String.format(
                        "{\"email\":\"%s\", \"password\":\"%s\", \"returnSecureToken\":true}",
                        request.getEmail(), request.getPassword()
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
                        resp.put("pricing", savedUser.getPricing());
                        resp.put("availability", savedUser.getAvailability());
                    }
                    String json = objectMapper.writeValueAsString(resp);
                    System.out.println("[signup] Response: " + json);
                    return ResponseEntity.ok(json);
                } else {
                    System.out.println("[signup] Firebase error: " + e.getErrorCode().toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e.getMessage()));
                }
            } catch (FirebaseAuthException e1) {
                System.out.println("[signup] FirebaseAuthException: " + e1.getErrorCode().toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("error", e1.getMessage()));
            } catch (InterruptedException | ExecutionException e1) {
                System.out.println("[signup] Exception: " + e1.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", "Unknown error, please try again"));
            } catch (Exception e2) {
                System.out.println("[signup] Unexpected Exception: " + e2.toString());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", e2.getMessage()));
            }
        } catch (Exception e) {
            System.out.println("[signup] Unexpected Exception: " + e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of("error", e.getMessage()));
    }
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("[login] Payload: " + request.toString());
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String requestBody = String.format(
                "{\"email\":\"%s\", \"password\":\"%s\", \"returnSecureToken\":true}",
                request.getEmail(), request.getPassword()
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
            // Fetch user profile from Firestore
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(request.getEmail());
            AppUser user = userService.getUserInfo(userRecord.getUid());
            if (user == null) {
                System.out.println("[login] No user profile found for UID: " + userRecord.getUid());
                return ResponseEntity.status(404).body(java.util.Map.of("error", "User profile not found"));
            }
            // Enforce role check
            if (request.getRole() != null && !request.getRole().equals(user.getRole())) {
                System.out.println("[login] Role mismatch: requested=" + request.getRole() + ", actual=" + user.getRole());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("error", "Role mismatch. Please log in with the correct account type."));
            }
            // Return the expected format for frontend compatibility
            var resp = new java.util.LinkedHashMap<String, Object>();
            resp.put("kind", "identitytoolkit#VerifyPasswordResponse");
            resp.put("localId", user.getUid());
            resp.put("email", user.getEmail());
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
                resp.put("pricing", user.getPricing());
                resp.put("availability", user.getAvailability());
            }
            String json = objectMapper.writeValueAsString(resp);
            System.out.println("[login] Response: " + json);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
        } catch (HttpClientErrorException e) {
            System.out.println("[login] Failed for: " + request.getEmail() + ", Error: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (FirebaseAuthException e) {
            System.out.println("[login] FirebaseAuthException: " + e.getMessage());
            return ResponseEntity.status(401).body(java.util.Map.of("error", e.getMessage()));
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
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(java.util.Collections.singletonList(googleMobileClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String phone = (String) payload.get("phone_number");
                String googleUid = (String) payload.getSubject(); // Use Google's subject as UID
                
                System.out.println("[google-login] Google UID: " + googleUid + ", Role: " + role + ", Email: " + email);
                
                // Check if user exists in Firebase, create if not
                UserRecord userRecord = null;
                String firebaseUid = null;
                try {
                    userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                    firebaseUid = userRecord.getUid();
                } catch (FirebaseAuthException e) {
                    if (e.getErrorCode().name().equals("USER_NOT_FOUND")) {
                        // Create new Firebase user
                        userRecord = FirebaseAuth.getInstance().createUser(
                            new UserRecord.CreateRequest()
                                .setEmail(email)
                                .setDisplayName(name)
                                .setPhoneNumber(phone)
                        );
                        firebaseUid = userRecord.getUid();
                        System.out.println("[google-login] Created new Firebase user: " + firebaseUid);
                    } else {
                        throw e;
                    }
                }
                
                // Check if user exists in Firestore and create if needed
                boolean created = false;
                if ("talent".equals(role)) {
                    UserTalent user = userService.getTalentInfo(firebaseUid);
                    if (user == null) {
                        mailService.sendSignupMail(email, name);
                        AppUser user2 = new AppUser(
                            firebaseUid,
                            email,
                            name,
                            phone,
                            role
                        );
                        userService.addUserInfo(user2);
                        created = true;
                        System.out.println("[google-login] Created new talent user in Firestore: " + email);
                    } else {
                        System.out.println("[google-login] Talent user already exists in Firestore: " + email);
                    }
                } else {
                    UserClient user = userService.getClientInfo(firebaseUid);
                    if (user == null) {
                        mailService.sendSignupMail(email, name);
                        AppUser user2 = new AppUser(
                            firebaseUid,
                            email,
                            name,
                            phone,
                            role
                        );
                        userService.addUserInfo(user2);
                        created = true;
                        System.out.println("[google-login] Created new client user in Firestore: " + email);
                    } else {
                        System.out.println("[google-login] Client user already exists in Firestore: " + email);
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
                resp.put("email", savedUser.getEmail());
                resp.put("displayName", savedUser.getFullName());
                resp.put("idToken", idToken2);
                resp.put("registered", true);
                resp.put("refreshToken", refreshToken2);
                resp.put("expiresIn", expiresIn2);
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
                    resp.put("pricing", savedUser.getPricing());
                    resp.put("availability", savedUser.getAvailability());
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

