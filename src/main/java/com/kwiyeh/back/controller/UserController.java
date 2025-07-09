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
        "http://localhost:8081", "http://localhost:8082", "http://localhost:19006",
        "http://localhost:3000", "http://localhost:3001", "http://localhost:4200", "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<?> updateUserInfo(@RequestBody java.util.Map<String, Object> userMap) {
        System.out.println("[updateUserInfo] RAW MAP: " + userMap);
        try {
            String uid = (String) userMap.get("uid");
            String role = (String) userMap.get("role");
            if ("talent".equals(role)) {
                com.kwiyeh.back.model.UserTalent existingUser = userService.getTalentInfo(uid);
                if (existingUser == null) {
                    existingUser = new com.kwiyeh.back.model.UserTalent();
                    existingUser.setUid(uid);
                    existingUser.setRole(role);
                }
                // Only update fields present in the request
                if (userMap.containsKey("email")) existingUser.setEmail((String) userMap.get("email"));
                if (userMap.containsKey("fullName") || userMap.containsKey("name"))
                    existingUser.setFullName((String) userMap.getOrDefault("fullName", userMap.get("name")));
                if (userMap.containsKey("phoneNumber")) existingUser.setPhoneNumber((String) userMap.get("phoneNumber"));
                // Handle image upload (base64 or URL)
                if (userMap.containsKey("photoURL") || userMap.containsKey("talentImageUrl")) {
                    String photoURL = (String) userMap.getOrDefault("photoURL", userMap.get("talentImageUrl"));
                    if (photoURL != null && photoURL.startsWith("data:image")) {
                        try {
                            String base64Data = photoURL.contains(",") ? photoURL.substring(photoURL.indexOf(",") + 1) : photoURL;
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap("resource_type", "image"));
                            photoURL = (String) uploadResult.get("secure_url");
                        } catch (Exception e) {
                            System.out.println("[updateUserInfo] Cloudinary upload error: " + e.getMessage());
                            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
                        }
                    }
                    existingUser.setTalentImageUrl(photoURL);
                }
                if (userMap.containsKey("location")) {
                    Object locationObj = userMap.get("location");
                    if (locationObj != null) {
                        existingUser.setTalentLocation(locationObj.toString());
                    }
                }
                if (userMap.containsKey("talentCategory")) existingUser.setTalentCategory((String) userMap.get("talentCategory"));
                if (userMap.containsKey("talentDescription")) existingUser.setTalentDescription((String) userMap.get("talentDescription"));
                if (userMap.containsKey("experience")) existingUser.setExperience((String) userMap.get("experience"));
                if (userMap.containsKey("pricing")) existingUser.setPricing((String) userMap.get("pricing"));
                if (userMap.containsKey("availability")) existingUser.setAvailability((String) userMap.get("availability"));
                // Handle services robustly
                if (userMap.containsKey("services")) {
                    Object servicesObj = userMap.get("services");
                    java.util.List<String> services = null;
                    if (servicesObj instanceof java.util.List<?>) {
                        services = ((java.util.List<?>) servicesObj).stream().map(Object::toString).collect(java.util.stream.Collectors.toList());
                    } else if (servicesObj instanceof String) {
                        services = java.util.Arrays.asList(((String) servicesObj).split(","));
                    }
                    existingUser.setServices(services);
                }
                // Save to Firestore
                userService.updateUser(existingUser);
                // Always read back from Firestore to get the true persisted state
                com.kwiyeh.back.model.UserTalent savedUser = userService.getTalentInfo(uid);
                // Build response from savedUser
                var resp = new java.util.LinkedHashMap<String, Object>();
                resp.put("uid", savedUser.getUid());
                resp.put("email", savedUser.getEmail());
                resp.put("fullName", savedUser.getFullName());
                resp.put("phoneNumber", savedUser.getPhoneNumber());
                resp.put("role", savedUser.getRole());
                resp.put("photoURL", savedUser.getTalentImageUrl());
                resp.put("location", savedUser.getTalentLocation());
                resp.put("talentCategory", savedUser.getTalentCategory());
                resp.put("talentDescription", savedUser.getTalentDescription());
                resp.put("experience", savedUser.getExperience());
                resp.put("pricing", savedUser.getPricing());
                resp.put("availability", savedUser.getAvailability());
                resp.put("services", savedUser.getServices() != null ? savedUser.getServices() : new java.util.ArrayList<>());
                String json = objectMapper.writeValueAsString(resp);
                System.out.println("[updateUserInfo] Response: " + json);
                return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(json);
            } else if ("client".equals(role)) {
                com.kwiyeh.back.model.UserClient existingUser = userService.getClientInfo(uid);
                if (existingUser == null) {
                    existingUser = new com.kwiyeh.back.model.UserClient();
                    existingUser.setUid(uid);
                    existingUser.setRole(role);
                }
                // Only update fields present in the request
                if (userMap.containsKey("email")) existingUser.setEmail((String) userMap.get("email"));
                if (userMap.containsKey("fullName") || userMap.containsKey("name"))
                    existingUser.setFullName((String) userMap.getOrDefault("fullName", userMap.get("name")));
                if (userMap.containsKey("phoneNumber")) existingUser.setPhoneNumber((String) userMap.get("phoneNumber"));
                // Handle image upload (base64 or URL)
                if (userMap.containsKey("photoURL") || userMap.containsKey("clientImageUrl")) {
                    String photoURL = (String) userMap.getOrDefault("photoURL", userMap.get("clientImageUrl"));
                    if (photoURL != null && photoURL.startsWith("data:image")) {
                        try {
                            String base64Data = photoURL.contains(",") ? photoURL.substring(photoURL.indexOf(",") + 1) : photoURL;
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                            Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap("resource_type", "image"));
                            photoURL = (String) uploadResult.get("secure_url");
                        } catch (Exception e) {
                            System.out.println("[updateUserInfo] Cloudinary upload error (client): " + e.getMessage());
                            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
                        }
                    }
                    existingUser.setClientImageUrl(photoURL);
                }
                if (userMap.containsKey("location")) {
                    Object locationObj = userMap.get("location");
                    if (locationObj != null) {
                        existingUser.setLocation(locationObj.toString());
                    }
                }
                // Save to Firestore
                userService.updateUser(existingUser);
                // Always read back from Firestore to get the true persisted state
                com.kwiyeh.back.model.UserClient savedUser = userService.getClientInfo(uid);
                // Build response from savedUser
                var resp = new java.util.LinkedHashMap<String, Object>();
                resp.put("uid", savedUser.getUid());
                resp.put("email", savedUser.getEmail());
                resp.put("fullName", savedUser.getFullName());
                resp.put("phoneNumber", savedUser.getPhoneNumber());
                resp.put("role", savedUser.getRole());
                resp.put("photoURL", savedUser.getClientImageUrl());
                resp.put("location", savedUser.getLocation());
                String json = objectMapper.writeValueAsString(resp);
                System.out.println("[updateUserInfo] Response (client): " + json);
                return ResponseEntity.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON).body(json);
            } else {
                return ResponseEntity.status(501).body("Only talent or client update supported");
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

    @GetMapping("/searchTalentsByService")
    @CrossOrigin(origins = {
        "http://localhost:8081", 
        "http://localhost:8082", 
        "http://localhost:19006",
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:4200",
        "http://localhost:5173"
    }, allowCredentials = "true")
    public ResponseEntity<String> searchTalentsByService(@RequestParam String service) {
        System.out.println("[searchTalentsByService] Request for service: " + service);
        try {
            List<com.kwiyeh.back.model.UserTalent> allTalents = userService.getAllTalents();
            List<com.kwiyeh.back.model.UserTalent> matchingTalents = new java.util.ArrayList<>();
            for (com.kwiyeh.back.model.UserTalent talent : allTalents) {
                if (talent.getServices() != null && talent.getServices().contains(service)) {
                    matchingTalents.add(talent);
                }
            }
            // Convert to JSON response with all fields
            String json = objectMapper.writeValueAsString(matchingTalents);
            System.out.println("[searchTalentsByService] Found " + matchingTalents.size() + " talents for service: " + service);
            System.out.println("[searchTalentsByService] Response: " + json);
            return ResponseEntity.ok(json);
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("[searchTalentsByService] Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error searching talents: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[searchTalentsByService] Unexpected Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
