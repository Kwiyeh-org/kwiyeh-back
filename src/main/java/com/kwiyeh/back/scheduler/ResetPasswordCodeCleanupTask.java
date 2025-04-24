package com.kwiyeh.back.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.kwiyeh.back.service.UserService;

@Component
@Configuration
@EnableScheduling
public class ResetPasswordCodeCleanupTask {

    private final UserService userService = new UserService();
    private static final int EXPIRATION_MINUTES = 10;


    @Scheduled(fixedRate = 60000) // Run every minute
    public void deleteExpiredCodes() {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -EXPIRATION_MINUTES);
        Date createdDateOfExpired = calendar.getTime();

        ApiFuture<QuerySnapshot> querySnapshot = dbFirestore.collection("PasswordReset")
            .whereLessThan("createdAt", createdDateOfExpired)
            .get();
        try {
            for (QueryDocumentSnapshot i : querySnapshot.get().getDocuments() ) {
                userService.deletePasswordReset(i.getId());
                System.out.println("PasswordReset deleted");
            }
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
    }
}