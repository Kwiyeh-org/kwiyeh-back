package com.kwiyeh.back.service;

import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
@Getter
public class UserService{

    private static final String DUPLICATE_ACCOUNT_ERROR = "EMAIL_EXISTS";

    // don't mind this

    /*public String createUser(AppUser user) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        System.out.println(dbFirestore.toString());
        ApiFuture<WriteResult> collectionApiFuture = (ApiFuture<WriteResult>) dbFirestore.collection("User").document(user.getDocument_id()).set(user);
        return collectionApiFuture.get().getUpdateTime().toString();
    }

    public AppUser getUser(String document_id) throws InterruptedException, ExecutionException {
        System.out.println(document_id);
        Firestore dbFirestore = FirestoreClient.getFirestore();
        System.out.println(dbFirestore.toString());
        DocumentReference documentReference = dbFirestore.collection("User").document(document_id);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        System.out.println(document.toString());
        AppUser user;
        if (document.exists()){
            user = document.toObject(AppUser.class);
            System.out.println(user.toString());
            return user;
        }
        return null;
    }

    public String updateUser(AppUser user) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionApiFuture = (ApiFuture<WriteResult>) dbFirestore.collection("User").document(user.getDocument_id()).set(user);
        
        return collectionApiFuture.get().getUpdateTime().toString();
    }

    public String deleteUser(String document_id){
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("User").document(document_id).delete();
        System.out.println(writeResult.toString());
        return "Successfully deleted " + document_id;
    }*/
}
