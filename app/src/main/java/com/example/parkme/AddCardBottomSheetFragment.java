package com.example.parkme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.parkme.model.CardDetails;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

public class AddCardBottomSheetFragment extends BottomSheetDialogFragment {

    private CardInputWidget cardInputWidget;
    private Stripe stripe;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card_bottom_sheet, container, false);


        firestore = FirebaseFirestore.getInstance();

        // Initialize Stripe instance
        stripe = new Stripe(getContext(), "pk_test_51OjOHoCTdoQWDPpqUIigF81EkPKpeEeurB2aw3dfvWQmoXCai6XVsY8sYwm9LSwHdFiLXqusY0kYlD7B9QoGE8oh00kl9NL05E"); // Replace with your actual key

        auth = FirebaseAuth.getInstance();

        // Initialize the CardInputWidget
        cardInputWidget = view.findViewById(R.id.cardInputWidget);

        Button addCardButton = view.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(v -> addCard());

        return view;
    }

    private void addCard() {
        CardParams cardParams = cardInputWidget.getCardParams();
        if (cardParams != null) {
            stripe.createCardToken(cardParams, new ApiResultCallback<Token>() {
                @Override
                public void onSuccess(@NonNull Token token) {
                    saveCardDetails(token);
                }

                @Override
                public void onError(@NonNull Exception e) {
                    // Handle error (e.g., show an error message to the user)
                }
            });
        } else {
            // Card details are not valid (e.g., show an error message to the user)
        }
    }

    private void saveCardDetails(Token token) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            String last4 = token.getCard().getLast4();
            int expMonth = token.getCard().getExpMonth();
            int expYear = token.getCard().getExpYear();

            firestore.collection("user").document(userId).collection("cards")
                    .whereEqualTo("last4Digits", last4)
                    .whereEqualTo("expMonth", expMonth)
                    .whereEqualTo("expYear", expYear)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // Card not found, add new card
                            addNewCard(userId, token, last4, expMonth, expYear);
                        } else {
                            // Card already exists, notify user
                            Toast.makeText(getActivity(), "Card already exists.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Query failed, handle error
                        Toast.makeText(getActivity(), "Error checking for existing card.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // The user is not logged in. Handle this case appropriately.
            Toast.makeText(getActivity(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }

    }
    private void addNewCard(String userId, Token token, String last4, int expMonth, int expYear) {
        String cardId = firestore.collection("user").document(userId).collection("cards").document().getId();

        CardDetails cardDetails = new CardDetails(last4, expMonth, expYear, cardId);

        firestore.collection("user").document(userId).collection("cards").document(cardId)
                .set(cardDetails)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Card added successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to add card: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
