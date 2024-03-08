package com.example.parkme.utils;

import com.example.parkme.model.CardDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A utility class responsible for handling Firebase operations, particularly managing user credit card information.
 */
public class FirebaseManager {

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    /**
     * Instantiating Firebase Firestore and Auth instances in the constructor.
     */
    public FirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Retrieves the stored card information for the current user from Firestore.
     *
     * @param onSuccess A consumer that takes the list of CardDetails objects on successful data retrieval.
     * @param onFailure A consumer that handles the exception if the operation fails.
     */
    public void loadUserCardDetails(Consumer<List<CardDetails>> onSuccess,
                                    Consumer<Exception> onFailure) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            onFailure.accept(new IllegalStateException("User not logged in"));
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("user").document(userId).collection("cards")
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

    /**
     * Deletes a specific card from the Firestore database.
     *
     * @param cardDetails The card details to delete.
     * @param onSuccess A runnable that is executed on successful deletion.
     * @param onFailure A consumer that handles the exception if the deletion fails.
     */
    public void deleteUserCard(CardDetails cardDetails, Runnable onSuccess, Consumer<Exception> onFailure) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || cardDetails == null) {
            onFailure.accept(new IllegalStateException("Invalid operation"));
            return;
        }

        String userId = currentUser.getUid();
        firestore.collection("user").document(userId).collection("cards")
                .document(cardDetails.getCardId())
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e));
    }

}
