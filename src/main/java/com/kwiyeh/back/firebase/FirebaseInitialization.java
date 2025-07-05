package com.kwiyeh.back.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseInitialization {

    @Value("${firebase.database.url}")
    private String firebaseDatabaseUrl;

    public FirebaseApp FireStoreInitialization(){
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("serviceAccountKey.json");
        } catch (FileNotFoundException e) {
            // TODO: Handle this exception properly.
            e.printStackTrace();
        }

        FirebaseOptions options = null;

        try {
            options = new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl(firebaseDatabaseUrl)
            .build();
        } catch (IOException e) {
            // TODO: Handle this exception properly.
            e.printStackTrace();
        }

        return FirebaseApp.initializeApp(options);
    }
}
