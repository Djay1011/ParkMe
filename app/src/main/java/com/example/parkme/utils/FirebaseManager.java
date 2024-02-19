package com.example.parkme.utils;

import com.example.parkme.model.CardDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FirebaseManager {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void loadUserCardDetails(Consumer<List<CardDetails>> onSuccess,
                                    Consumer<Exception> onFailure) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            onFailure.accept(new IllegalStateException("User not logged in"));
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("users").document(userId).collection("cards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CardDetails> cardDetailsList = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        CardDetails card = document.toObject(CardDetails.class);
                        cardDetailsList.add(card);
                    });
                    onSuccess.accept(cardDetailsList);
                })
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    public void deleteUserCard(CardDetails cardDetails, Runnable onSuccess, Consumer<Exception> onFailure) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || cardDetails == null) {
            onFailure.accept(new IllegalStateException("Invalid operation"));
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("users").document(userId).collection("cards")
                .document(cardDetails.getCardId())
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    // Additional Firebase methods
}
