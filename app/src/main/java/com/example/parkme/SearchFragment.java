package com.example.parkme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;

public class SearchFragment extends Fragment {

    private BottomSheetDialog bottomSheetDialog;
    private ConstraintLayout addCardLayout;
    private ConstraintLayout cardDetailsLayout; // This should be ConstraintLayout, not TextView.
    private TextView cardDetailsTextView; // We need a separate TextView for card details.
    private ImageView deleteCardButton;
    String SECRET_KEY="pk_test_51OjOHoCTdoQWDPpqUIigF81EkPKpeEeurB2aw3dfvWQmoXCai6XVsY8sYwm9LSwHdFiLXqusY0kYlD7B9QoGE8oh00kl9NL05E";
    String PUBLISH_KEY="sk_test_51OjOHoCTdoQWDPpqEterXHfQMnrOxluO593psmQ6tLy16kvIXaeYV98zmJmfEIJ2PaC38VTkRT17DRjrnYmB4MyX00l1RtIIWE";
    private PaymentSheet paymentSheet;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        addCardLayout = view.findViewById(R.id.addCard);
        cardDetailsLayout = view.findViewById(R.id.cardDetailsLayout); // This is the layout holding the card details.
        cardDetailsTextView = view.findViewById(R.id.cardDetails); // This is the actual TextView for card details.
        deleteCardButton = view.findViewById(R.id.deleteCardButton); // Get the delete button.

        PaymentConfiguration.init(getContext(), PUBLISH_KEY);

        paymentSheet = new PaymentSheet(this, paymentSheetResult -> {
        });

        addCardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

        deleteCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCardDetails();
            }
        });

        return view;
    }

    private void showBottomSheetDialog() {
        if (bottomSheetDialog == null) {
            View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
            bottomSheetDialog = new BottomSheetDialog(requireContext());
            bottomSheetDialog.setContentView(bottomSheetView);

            Button confirmButton = bottomSheetView.findViewById(R.id.AddCardButton);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText cardNumberEditText = bottomSheetView.findViewById(R.id.cardNumber);
                    // Validate and process card details
                    // Update UI with card details
                    updateCardDetailsUI(cardNumberEditText.getText().toString());
                    bottomSheetDialog.dismiss();
                }
            });
        }
        bottomSheetDialog.show();
    }

    private void updateCardDetailsUI(String cardNumber) {
        // Extract last four digits and expiry date
        // For demonstration, using static values
        String lastFourDigits = cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : cardNumber;
        cardDetailsTextView.setText("•••• " + lastFourDigits + " Exp: 12/24");
        cardDetailsLayout.setVisibility(View.VISIBLE); // Make the layout visible.
    }

    private void deleteCardDetails() {
        cardDetailsLayout.setVisibility(View.GONE); // Hide the card details layout.
    }
}
