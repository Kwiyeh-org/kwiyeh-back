package com.kwiyeh.back.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
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
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.model.UserClient;
import com.kwiyeh.back.model.UserTalent;
import com.kwiyeh.back.service.UserService;
import com.kwiyeh.back.utils.TalentInfo;


@RestController
public class UserController {
    public UserService userService= new UserService();

    @GetMapping("/getUserInfo")
    public ResponseEntity<String> getUserInfo(@RequestHeader("Authorization") String token, @RequestParam String type) {
        token = token.replace("Bearer ", "");
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try{
            String uid = decodedToken.get().getUid();
            if("talent".equals(type)){
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
    public ResponseEntity<String> deleteAccount(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        ApiFuture<FirebaseToken> decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(token);
        try {
            String uid = decodedToken.get().getUid();
            FirebaseAuth.getInstance().deleteUser(uid);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body("Error deleting account: " + e.getMessage());
        }catch (InterruptedException | ExecutionException e){
            return ResponseEntity.status(500).body("Error retrieving user information: " + e.getMessage());
        }
    }
    @PostMapping("/updateUserInfo")
    public String updateUserInfo(@RequestBody AppUser user) {
        try {
            userService.addUserInfo(user);
            return "User information updated successfully ";
        } catch (InterruptedException | ExecutionException e) {
            return "Error updating user information: " + e.getMessage();
        }
    }

    @GetMapping("/getTalents")
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
