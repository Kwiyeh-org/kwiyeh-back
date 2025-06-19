package com.kwiyeh.back.service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.kwiyeh.back.model.AppUser;
import com.kwiyeh.back.model.PasswordReset;
import com.kwiyeh.back.utils.TalentInfo;

@Service
public class UserService{

    public String createPasswordReset(String email, PasswordReset passwordReset) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = (ApiFuture<WriteResult>) dbFirestore.collection("PasswordReset").document(email).set(passwordReset);
        return writeResult.get().getUpdateTime().toString();
    }

    public String deletePasswordReset(String email) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = dbFirestore.collection("PasswordReset").document(email).delete();
        return writeResult.get().getUpdateTime().toString();
    }

    public PasswordReset getPasswordReset(String email) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("PasswordReset").document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        PasswordReset passwordReset;
        if (document.exists()){
            passwordReset = document.toObject(PasswordReset.class);
            return passwordReset;
        }
        return null;
    }

    public String addUserInfo(AppUser user) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> writeResult = (ApiFuture<WriteResult>) dbFirestore.collection("userInfo").document(user.getUid()).set(user);
        return writeResult.get().getUpdateTime().toString();
    }

    public AppUser getUserInfo(String uid) throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFirestore.collection("userInfo").document(uid);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        AppUser user;
        if (document.exists()){
            user = document.toObject(AppUser.class);
            return user;
        }
        return null;
    }

    public String updateUser(AppUser user) throws InterruptedException, ExecutionException{
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionApiFuture = (ApiFuture<WriteResult>) dbFirestore.collection("userInfo").document(user.getUid()).set(user);
        return collectionApiFuture.get().getUpdateTime().toString();
    }

    public List<TalentInfo> getTalents() throws InterruptedException, ExecutionException {
        Firestore dbFirestore = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> querySnapshot = (ApiFuture<QuerySnapshot>) dbFirestore.collection("PasswordReset")
        .select("uid", "email", "fullName", "phoneNumber", "type")
        .whereEqualTo("type", "talent")
        .get();
        QuerySnapshot document = querySnapshot.get();
        List<TalentInfo> talents;
        if (!document.isEmpty()) {
            talents = document.toObjects(TalentInfo.class);
            if (!talents.isEmpty()) {
                return talents;
            }
        }
        return null;
    }

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
