package com.kwiyeh.back.controller;

import java.util.concurrent.ExecutionException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.service.UserService;

@RestController
public class UserController {
    public UserService userService= new UserService();

    @GetMapping("/getUserInfo")
    public ResponseEntity<String> getUserInfo(@RequestParam String uid) {
        try{
            AppUser user = userService.getUserInfo(uid);
            return ResponseEntity.ok(user.toJson());
        }
        catch (InterruptedException | ExecutionException e){
            return ResponseEntity.status(500).body("Error retrieving user information: " + e.getMessage());
        }
    }
    @DeleteMapping("/deleteAccount")
    public ResponseEntity<String> deleteAccount(@RequestParam String uid) {
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(500).body("Error deleting account: " + e.getMessage());
        }
    }

}
