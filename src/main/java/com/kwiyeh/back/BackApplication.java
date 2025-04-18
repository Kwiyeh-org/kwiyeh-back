package com.kwiyeh.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.kwiyeh.back.firebase.FirebaseInitialization;

@SpringBootApplication
public class BackApplication {

	public static FirebaseAuth firebaseAuth;

	private static void runFirebase (){
		FirebaseInitialization firebaseInitialization = new FirebaseInitialization();
		FirebaseApp firebaseApp = firebaseInitialization.FireStoreInitialisation();
		firebaseAuth = firebaseInitialization.firebaseAuth(firebaseApp);
	}

	public static void main(String[] args) {
		runFirebase();
		SpringApplication.run(BackApplication.class, args);
	}

}
