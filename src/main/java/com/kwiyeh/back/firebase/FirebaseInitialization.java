package com.kwiyeh.back.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseInitialization {

    @Value("${firebase.database.url}")
    private String firebaseDatabaseUrl;

    public FirebaseApp FireStoreInitialisation(){
        FileInputStream serviceAccount = null;
            try {
                serviceAccount = new FileInputStream("serviceAccountKey.json");
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            FirebaseOptions options = null;

            try {
                options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(firebaseDatabaseUrl)
                .build();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
}
}
