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
                    AppUser user = new AppUser(
                            userRecord.getUid(),
                            request.getEmail(),
                            request.getFullName(),
                            request.getPhoneNumber(),
                            request.getRole()
                    );
                    userService.addUserInfo(user);
                    System.out.println("Signup of "+request.getEmail()+" successful");
                    LoginRequest loginRequest = new LoginRequest();
                    loginRequest.setEmail(userRecord.getEmail());
                    loginRequest.setPassword(request.getPassword());
                    return login(loginRequest);
                }
                else {
                    System.out.println(e.getErrorCode().toString());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                }
            } catch (FirebaseAuthException e1) {
                System.out.println(e1.getErrorCode().toString());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e1.getMessage());
            }catch (InterruptedException | ExecutionException e1) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Unknown error, please try again");
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
            System.out.println("Login of "+request.getEmail()+" successful");
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        String idTokenString = request.getToken();
        String role = request.getRole();
        try {
            // 1. Try Firebase verification (web)
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idTokenString);
            String email = decodedToken.getEmail();
            String uid = decodedToken.getUid();
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            // Only create user in Firestore if not present
            if ("talent".equals(role)) {
                UserTalent user = userService.getTalentInfo(uid);
                if (user == null) {
                    mailService.sendSignupMail(userRecord.getEmail(), userRecord.getDisplayName());
                    AppUser user2 = new AppUser(
                        userRecord.getUid(),
                        userRecord.getEmail(),
                        userRecord.getDisplayName(),
                        userRecord.getPhoneNumber(),
                        role
                    );
                    userService.addUserInfo(user2);
                }
            } else {
                UserClient user = userService.getClientInfo(uid);
                if (user == null) {
                    mailService.sendSignupMail(userRecord.getEmail(), userRecord.getDisplayName());
                    AppUser user2 = new AppUser(
                        userRecord.getUid(),
                        userRecord.getEmail(),
                        userRecord.getDisplayName(),
                        userRecord.getPhoneNumber(),
                        role
                    );
                    userService.addUserInfo(user2);
                }
            }
            System.out.println("Google login of " + userRecord.getEmail() + " successful (Firebase)");
            return ResponseEntity.ok("Google login of " + userRecord.getEmail() + " successful");
        } catch (FirebaseAuthException | InterruptedException | ExecutionException firebaseEx) {
            // 2. If Firebase fails, try GoogleIdTokenVerifier (mobile)
            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                        .setAudience(java.util.Collections.singletonList(googleMobileClientId))
                        .build();
                GoogleIdToken idToken = verifier.verify(idTokenString);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    String email = payload.getEmail();
                    String name = (String) payload.get("name");
                    String phone = (String) payload.get("phone_number"); // May be null
                    UserRecord userRecord = null;
                    String uid = null;
                    boolean isNewUser = false;
                    try {
                        userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
                        uid = userRecord.getUid();
                    } catch (FirebaseAuthException e) {
                        // If user not found in Firebase Auth, treat as new user
                        if ("USER_NOT_FOUND".equals(e.getErrorCode()) || e.getMessage().contains("No user record")) {
                            // Use Google subject as UID for Firestore
                            uid = (String) payload.getSubject();
                            isNewUser = true;
                        } else {
                            System.out.println("GoogleIdToken verification failed: " + e);
                            return ResponseEntity.status(401).body("Invalid Google token");
                        }
                    }
                    // Only create user in Firestore if not present
                    if ("talent".equals(role)) {
                        UserTalent user = userService.getTalentInfo(uid);
                        if (user == null) {
                            mailService.sendSignupMail(email, name);
                            AppUser user2 = new AppUser(
                                uid,
                                email,
                                name,
                                phone,
                                role
                            );
                            userService.addUserInfo(user2);
                        }
                    } else {
                        UserClient user = userService.getClientInfo(uid);
                        if (user == null) {
                            mailService.sendSignupMail(email, name);
                            AppUser user2 = new AppUser(
                                uid,
                                email,
                                name,
                                phone,
                                role
                            );
                            userService.addUserInfo(user2);
                        }
                    }
                    System.out.println("Google login of " + email + " successful (GoogleIdToken)");
                    return ResponseEntity.ok("Google login of " + email + " successful");
                } else {
                    return ResponseEntity.status(401).body("Invalid Google token");
                }
            } catch (Exception googleEx) {
                System.out.println("GoogleIdToken verification failed: " + googleEx);
                return ResponseEntity.status(401).body("Invalid Google token");
            }
        }
    }

    @GetMapping("/forgetPassword")
    public ResponseEntity<?> forgetPasswordMail(@RequestParam String email) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
            String code = MyAppFunctions.GenerateForgetPasswordCode();
            PasswordReset passwordReset = new PasswordReset();
            passwordReset.setForgetPasswordCode(code);
            passwordReset.setCreatedAt(Calendar.getInstance().getTime());
            userService.createPasswordReset(email,passwordReset);
            mailService.sendForgetPasswordMail(email,userRecord.getDisplayName(),code);
            System.out.println("Forget password mail sent to "+email);
            return ResponseEntity.ok("Reset password mail sent");
        } catch (FirebaseAuthException e) {
            System.out.println(e.getErrorCode().toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Unknown error, please try again");
        }
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest verifyCodeReq) {
        try {
            PasswordReset expectedPasswordReset = userService.getPasswordReset(verifyCodeReq.getEmail());
            if(expectedPasswordReset == null)
                return ResponseEntity.status(HttpStatus.CONFLICT).body("code expired or already used");
            if(expectedPasswordReset.getForgetPasswordCode().equals(verifyCodeReq.getForgetPasswordCode()) ){
                userService.deletePasswordReset(verifyCodeReq.getEmail());
                String token = JwtUtil.generateToken(verifyCodeReq.getEmail());
                System.out.println("Token generated for "+verifyCodeReq.getEmail());
                return ResponseEntity.ok("{\"passwordToken\": \""+token+"\", \"expiresIn\": \"600\"}");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong code");
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.toString());
            return ResponseEntity.status(HttpStatus.valueOf(500)).body("Unknown error, please try again");
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String authHeader, @RequestBody PasswordResetRequest passwordResetReq) {
        String idToken = authHeader.replace("Bearer ", "");
        try {
            Claims decodedToken = JwtUtil.parseToken(idToken);
            if (decodedToken == null) {
                System.out.println("Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            if (decodedToken.getExpiration().getTime() < Calendar.getInstance().getTimeInMillis()) {
                System.out.println("Token expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired");
            }
            UserRecord userRecord = FirebaseAuth.getInstance().getUserByEmail(decodedToken.getSubject());
            UserRecord.UpdateRequest updateRequest = new UserRecord.UpdateRequest(userRecord.getUid())
            .setPassword(passwordResetReq.getPassword());
            FirebaseAuth.getInstance().updateUser(updateRequest);
            System.out.println("Password reset for "+decodedToken.getSubject()+" successful");
            return ResponseEntity.ok("Password reset done");
        } catch (FirebaseAuthException e) {
            System.out.println(e.getErrorCode().toString());
            return ResponseEntity.status(e.getErrorCode().ordinal()).body(e.getMessage());
        }
    }

}

