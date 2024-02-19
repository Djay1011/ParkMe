package com.example.parkme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.parkme.utils.FirebaseManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.stripe.android.view.CardInputWidget;

public class BottomSheetManager {
    private Context context;
    private StripeService stripeService;
    private FirebaseManager firestoreService;
    private Runnable updateUIAfterCardOperation;
    private BottomSheetDialog bottomSheetDialog;

    public BottomSheetManager(Context context, StripeService stripeService, FirebaseManager firestoreService, Runnable updateUIAfterCardOperation) {
        this.context = context;
        this.stripeService = stripeService;
        this.firestoreService = firestoreService;
        this.updateUIAfterCardOperation = updateUIAfterCardOperation;
    }

    public void showAddCardDialog() {
        createBottomSheetDialog();
        View bottomSheetView = inflateBottomSheetView();
        setupCardInputWidget(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void createBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(context);
    }

    private View inflateBottomSheetView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View bottomSheetView = inflater.inflate(R.layout.bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        return bottomSheetView;
    }

    private void setupCardInputWidget(View bottomSheetView) {
        CardInputWidget cardInputWidget = bottomSheetView.findViewById(R.id.cardInputWidget);
        Button addCardButton = bottomSheetView.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(v -> handleAddCardClicked(cardInputWidget));
    }

    private void handleAddCardClicked(CardInputWidget cardInputWidget) {
        // Logic to handle the add card button click
    }
}
