package com.kwiyeh.back.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.model.UserClient;
import com.kwiyeh.back.model.UserTalent;
import com.kwiyeh.back.service.UserService;
import com.kwiyeh.back.utils.TalentInfo;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;


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
public class UserController {
    public UserService userService= new UserService();

    private static final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
        "cloud_name", "dq0nzinge",
        "api_key", "269775811873817",
        "api_secret", "CwEGWkcKRCe1qj0su5YhgiMqc7s"
    ));
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/getUserInfo")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<String> getUserInfo(@RequestHeader("Authorization") String token, @RequestParam String role) {
        token = token.replace("Bearer ", "");
        System.out.println("[getUserInfo] Request: token=***, role=" + role);
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try{
            String uid = decodedToken.get().getUid();
            System.out.println("[getUserInfo] UID: " + uid + ", Role: " + role);
            AppUser user = userService.getUserInfo(uid);
            if (user == null) {
                System.out.println("[getUserInfo] No user found for UID: " + uid);
                return ResponseEntity.ok("{}");
            }
            // Unified response
            String photoURL = null;
            if ("client".equals(role)) photoURL = user.getClientImageUrl();
            else if ("talent".equals(role)) photoURL = user.getTalentImageUrl();
            String location = "client".equals(role) ? user.getClientLocation() : user.getTalentLocation();
            var resp = new java.util.LinkedHashMap<String, Object>();
            resp.put("uid", user.getUid());
            resp.put("email", user.getEmail());
            resp.put("fullName", user.getFullName());
            resp.put("phoneNumber", user.getPhoneNumber());
            resp.put("role", user.getRole());
            resp.put("photoURL", photoURL);
            resp.put("location", location);
            if ("talent".equals(role)) {
                resp.put("talentCategory", user.getTalentCategory());
                resp.put("talentDescription", user.getTalentDescription());
                resp.put("pricing", user.getPricing());
                resp.put("availability", user.getAvailability());
            }
            String json = objectMapper.writeValueAsString(resp);
            System.out.println("[getUserInfo] Response: " + json);
            return ResponseEntity.ok(json);
        }
        catch (InterruptedException | ExecutionException e){
            System.out.println("[getUserInfo] Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error retrieving user information: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[getUserInfo] Unexpected Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    @DeleteMapping("/deleteAccount")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        System.out.println("[deleteAccount] Request: token=***");
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try {
            String uid = decodedToken.get().getUid();
            System.out.println("[deleteAccount] UID: " + uid);
            FirebaseAuth.getInstance().deleteUser(uid);
            try {
                FirestoreClient.getFirestore().collection("userInfo").document(uid).delete();
                System.out.println("[deleteAccount] Firestore user deleted for UID: " + uid);
            } catch (Exception firestoreEx) {
                System.out.println("[deleteAccount] Firestore user deletion failed: " + firestoreEx.getMessage());
            }
            System.out.println("[deleteAccount] Account deleted successfully for UID: " + uid);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (FirebaseAuthException e) {
            System.out.println("[deleteAccount] FirebaseAuthException: " + e.getMessage());
            return ResponseEntity.status(500).body("Error deleting account: " + e.getMessage());
        }catch (InterruptedException | ExecutionException e){
            System.out.println("[deleteAccount] Exception: " + e.getMessage());
            return ResponseEntity.status(500).body("Error retrieving user information: " + e.getMessage());
        }
    }
    @PostMapping("/updateUserInfo")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<?> updateUserInfo(@RequestBody AppUser user) {
        System.out.println("[updateUserInfo] Payload: " + user.toString());
        System.out.println("[updateUserInfo] Debug - Location: " + user.getLocation());
        System.out.println("[updateUserInfo] Debug - ClientLocation: " + user.getClientLocation());
        System.out.println("[updateUserInfo] Debug - TalentLocation: " + user.getTalentLocation());
        try {
            String uid = user.getUid();
            String role = user.getRole();
            AppUser existingUser = userService.getUserInfo(uid);
            if (existingUser == null) {
                existingUser = new AppUser();
                existingUser.setUid(uid);
                existingUser.setEmail(user.getEmail());
                existingUser.setRole(role);
            }
            String imageUrl = null;
            if ("client".equals(role)) {
                if (user.getClientImageUrl() != null && user.getClientImageUrl().startsWith("data:image")) {
                    System.out.println("[updateUserInfo] Uploading client image to Cloudinary");
                    try {
                        String base64Data = user.getClientImageUrl();
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                        }
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap("resource_type", "image"));
                        imageUrl = (String) uploadResult.get("secure_url");
                        System.out.println("[updateUserInfo] Cloudinary upload result: " + imageUrl);
                        existingUser.setClientImageUrl(imageUrl);
                    } catch (Exception e) {
                        System.out.println("[updateUserInfo] Cloudinary upload error: " + e.getMessage());
                        return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
                    }
                } else if (user.getClientImageUrl() != null && user.getClientImageUrl().startsWith("http")) {
                    existingUser.setClientImageUrl(user.getClientImageUrl());
                }
                if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) existingUser.setFullName(user.getFullName());
                if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) existingUser.setPhoneNumber(user.getPhoneNumber());
                // Handle both clientLocation and generic location field
                if (user.getClientLocation() != null && !user.getClientLocation().trim().isEmpty()) {
                    existingUser.setClientLocation(user.getClientLocation());
                } else if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) {
                    existingUser.setClientLocation(user.getLocation());
                }
                userService.updateUser(existingUser);
                // Unified response
                var resp = new java.util.LinkedHashMap<String, Object>();
                resp.put("uid", existingUser.getUid());
                resp.put("email", existingUser.getEmail());
                resp.put("fullName", existingUser.getFullName());
                resp.put("phoneNumber", existingUser.getPhoneNumber());
                resp.put("role", existingUser.getRole());
                resp.put("photoURL", existingUser.getClientImageUrl());
                resp.put("location", existingUser.getClientLocation());
                String json = objectMapper.writeValueAsString(resp);
                System.out.println("[updateUserInfo] Response: " + json);
                return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(json);
            } else if ("talent".equals(role)) {
                if (user.getTalentImageUrl() != null && user.getTalentImageUrl().startsWith("data:image")) {
                    System.out.println("[updateUserInfo] Uploading talent image to Cloudinary");
                    try {
                        String base64Data = user.getTalentImageUrl();
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                        }
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                        Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap("resource_type", "image"));
                        imageUrl = (String) uploadResult.get("secure_url");
                        System.out.println("[updateUserInfo] Cloudinary upload result: " + imageUrl);
                        existingUser.setTalentImageUrl(imageUrl);
                    } catch (Exception e) {
                        System.out.println("[updateUserInfo] Cloudinary upload error: " + e.getMessage());
                        return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
                    }
                } else if (user.getTalentImageUrl() != null && user.getTalentImageUrl().startsWith("http")) {
                    existingUser.setTalentImageUrl(user.getTalentImageUrl());
                }
                if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) existingUser.setFullName(user.getFullName());
                if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) existingUser.setPhoneNumber(user.getPhoneNumber());
                // Handle both talentLocation and generic location field
                if (user.getTalentLocation() != null && !user.getTalentLocation().trim().isEmpty()) {
                    existingUser.setTalentLocation(user.getTalentLocation());
                } else if (user.getLocation() != null && !user.getLocation().trim().isEmpty()) {
                    existingUser.setTalentLocation(user.getLocation());
                }
                if (user.getTalentCategory() != null && !user.getTalentCategory().trim().isEmpty()) existingUser.setTalentCategory(user.getTalentCategory());
                if (user.getTalentDescription() != null && !user.getTalentDescription().trim().isEmpty()) existingUser.setTalentDescription(user.getTalentDescription());
                if (user.getPricing() != null && !user.getPricing().trim().isEmpty()) existingUser.setPricing(user.getPricing());
                if (user.getAvailability() != null && !user.getAvailability().trim().isEmpty()) existingUser.setAvailability(user.getAvailability());
                userService.updateUser(existingUser);
                // Unified response
                var resp = new java.util.LinkedHashMap<String, Object>();
                resp.put("uid", existingUser.getUid());
                resp.put("email", existingUser.getEmail());
                resp.put("fullName", existingUser.getFullName());
                resp.put("phoneNumber", existingUser.getPhoneNumber());
                resp.put("role", existingUser.getRole());
                resp.put("photoURL", existingUser.getTalentImageUrl());
                resp.put("location", existingUser.getTalentLocation());
                resp.put("talentCategory", existingUser.getTalentCategory());
                resp.put("talentDescription", existingUser.getTalentDescription());
                resp.put("pricing", existingUser.getPricing());
                resp.put("availability", existingUser.getAvailability());
                System.out.println("[updateUserInfo] Response: " + resp);
                return ResponseEntity.ok(resp);
            } else {
                System.out.println("[updateUserInfo] Unknown role: " + role);
                return ResponseEntity.status(400).body("Unknown role: " + role);
            }
        } catch (Exception e) {
            System.out.println("[updateUserInfo] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating user information: " + e.getMessage());
        }
    }

    @GetMapping("/getTalents")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<String> getTalents(@RequestParam String uid) {
        System.out.println("[getTalents] Request for UID: " + uid);
        try {
            List<TalentInfo> talentList = userService.getTalents();
            String talentsInJson = "";
            if (talentList != null) {
                for (TalentInfo talent : talentList) {
                    talentsInJson += talent.toJson() + ",";
                }
            }
            String result = talentsInJson.isEmpty() ? "No talents found" : "[" + talentsInJson.substring(0, talentsInJson.length() - 1) + "]";
            System.out.println("[getTalents] Response: " + result);
            return ResponseEntity.ok(result);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[getTalents] Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error retrieving talent information: " + e.getMessage());
        }
    }
}
