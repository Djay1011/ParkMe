package com.example.parkme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.parkme.model.CardDetails;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

/**
 * A fragment displayed as a bottomSheet for users to input a new payment card.
 * It uses Stripe for the process of card tokenization and uses Firestore to store card information.
 */
public class AddCardBottomSheetFragment extends BottomSheetDialogFragment {

    // A user interface component designed for entering card information.
    private CardInputWidget cardInputWidget;
    // The Stripe API client is used for converting card details into tokens.
    private Stripe stripe;
    // Uses Firebase Firestore to store user information.
    private FirebaseFirestore firestore;
    // Firebase Auth is used for verifying the identity of users.
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_add_card_bottom_sheet, container, false);

        // Initialize Firestore and Firebase Auth.
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set up Stripe with the public key.
        PaymentConfiguration.init(getContext(), "your_stripe_public_key");
        stripe = new Stripe(getContext(), PaymentConfiguration.getInstance(getContext()).getPublishableKey());

        // Setup the card input widget from Stripe.
        cardInputWidget = view.findViewById(R.id.cardInputWidget);

        // Creates the add card button and its associated click listener.
        Button addCardButton = view.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(v -> addCard());

        // Setup the dismiss icon to close the bottom sheet.
        ImageView dismissIcon = view.findViewById(R.id.remove);
        dismissIcon.setOnClickListener(v -> dismiss());

        return view;
    }

    /**
     * Begins the process of tokenizing card information in order to add a new payment card.
     */
    private void addCard() {
        CardParams cardParams = cardInputWidget.getCardParams();
        if (cardParams != null) {
            stripe.createCardToken(cardParams, new ApiResultCallback<Token>() {
                @Override
                public void onSuccess(@NonNull Token token) {
                    // After successfully converting the card details into tokens, proceed to securely store them.
                    saveCardDetails(token);
                }

                @Override
                public void onError(@NonNull Exception e) {
                    // Manage mistakes in the process of converting card information into tokens.
                    Toast.makeText(getContext(), "Error creating card token: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Invalid card data. Please check the details and try again.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verifies if the card is already present in Firestore and, if not, proceeds with saving it.
     * @param token The Stripe token representing card details.
     */
    private void saveCardDetails(Token token) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String last4 = token.getCard().getLast4();
            int expMonth = token.getCard().getExpMonth();
            int expYear = token.getCard().getExpYear();

            // Check Firestore to see if the card already exists.
            firestore.collection("user").document(userId).collection("cards")
                    .whereEqualTo("last4Digits", last4)
                    .whereEqualTo("expMonth", expMonth)
                    .whereEqualTo("expYear", expYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // If the card is not currently present in Firestore, proceed to add it.
                            addNewCard(userId, token, last4, expMonth, expYear);
                        } else {
                            // If the card is already in existence, it notify the user.
                            Toast.makeText(getActivity(), "Card already exists.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while querying Firestore.
                        Toast.makeText(getActivity(), "Error checking for existing card.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If the user is not logged in, display a message.
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the updated card information in Firestore.
     * @param userId The ID of the current user.
     * @param token The Stripe token representing card details.
     * @param last4 The last 4 digits of the card number.
     * @param expMonth The expiration month of the card.
     * @param expYear The expiration year of the card.
     */
    private void addNewCard(String userId, Token token, String last4, int expMonth, int expYear) {
        String cardId = firestore.collection("user").document(userId).collection("cards").document().getId();

        // Create a new card detail object.
        CardDetails cardDetails = new CardDetails(last4, expMonth, expYear, cardId);

        // Stores the card information in Firestore.
        firestore.collection("user").document(userId).collection("cards").document(cardId)
                .set(cardDetails)
                .addOnSuccessListener(aVoid -> {
                    // If the card details have been successfully it will stored in Firestore.
                    Toast.makeText(getActivity(), "Card added successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Error occurred while saving the card details.
                    Toast.makeText(getActivity(), "Failed to add card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
