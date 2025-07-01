package com.kwiyeh.back.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.model.UserClient;
import com.kwiyeh.back.model.UserTalent;
import com.kwiyeh.back.service.UserService;
import com.kwiyeh.back.utils.TalentInfo;


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
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try{
            String uid = decodedToken.get().getUid();
            if("talent".equals(role)){
                UserTalent user = userService.getTalentInfo(uid);
                return ResponseEntity.ok(user.toJson());
            }
            else{
                UserClient user = userService.getClientInfo(uid);
                return ResponseEntity.ok(user.toJson());
            }
        }
        catch (InterruptedException | ExecutionException e){
            return ResponseEntity.status(500).body("Error retrieving user information: " + e.getMessage());
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
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try {
            String uid = decodedToken.get().getUid();
            FirebaseAuth.getInstance().deleteUser(uid);
            // Delete user from Firestore (userInfo collection)
            try {
                FirestoreClient.getFirestore().collection("userInfo").document(uid).delete();
            } catch (Exception firestoreEx) {
                // Log and continue, since Auth deletion succeeded
                System.out.println("Firestore user deletion failed: " + firestoreEx.getMessage());
            }
            return ResponseEntity.ok("Account deleted successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body("Error deleting account: " + e.getMessage());
        }catch (InterruptedException | ExecutionException e){
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
    public ResponseEntity<String> updateUserInfo(@RequestBody AppUser user) {
        System.out.println("[DEBUG] /updateUserInfo endpoint hit. Payload: " + user);
        System.out.println("Received updateUserInfo: " + user);
        System.out.println("UID: " + user.getUid());
        System.out.println("UID length: " + (user.getUid() != null ? user.getUid().length() : "null"));
        try {
            userService.addUserInfo(user);
            return ResponseEntity.ok("User information updated successfully");
        } catch (InterruptedException | ExecutionException e) {
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
        try {
            List<TalentInfo> talentList= userService.getTalents();
            String talentsInJson= "";
            for (TalentInfo talent : talentList) {
                talentsInJson += talent.toJson() + ",";
            }
            return ResponseEntity.ok(talentsInJson.isEmpty() ? "No talents found" : "[" + talentsInJson.substring(0, talentsInJson.length() - 1) + "]");
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).body("Error retrieving talent information: " + e.getMessage());
        }
    }
}
