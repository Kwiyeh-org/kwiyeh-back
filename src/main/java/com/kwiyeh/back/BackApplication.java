package com.kwiyeh.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kwiyeh.back.firebase.FirebaseInitialization;

@SpringBootApplication
public class BackApplication {

	private static void runFirebase (){
		FirebaseInitialization firebaseInitialization = new FirebaseInitialization();
		firebaseInitialization.FireStoreInitialization();
	}

	public static void main(String[] args) {
		runFirebase();
		SpringApplication.run(BackApplication.class, args);
	}

}
