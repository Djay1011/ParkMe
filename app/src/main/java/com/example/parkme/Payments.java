package com.example.parkme;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;


public class Payments extends AppCompatActivity {

    private Stripe stripe;
    private CardInputWidget cardInputWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        PaymentConfiguration.init(this, getString(R.string.PublishableKey));
        stripe = new Stripe(getApplicationContext(), PaymentConfiguration.getInstance(getApplicationContext()).getPublishableKey());

        cardInputWidget = findViewById(R.id.cardInputWidget);

        Button addCardButton = findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPaymentMethod();
            }
        });

        ImageView closeIcon = findViewById(R.id.remove);
        closeIcon.setOnClickListener(v -> finish());
    }

    private void createPaymentMethod() {
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params != null) {
            stripe.createPaymentMethod(params, new ApiResultCallback<PaymentMethod>() {
                @Override
                public void onSuccess(PaymentMethod paymentMethod) {
                    // TODO: Handle the newly created payment method (e.g., attach it to a customer on your server)
                    Toast.makeText(Payments.this, "Payment Method Added", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull Exception e) {
                    Toast.makeText(Payments.this, "Error adding payment method: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(this, "Invalid Card Data", Toast.LENGTH_SHORT).show();
        }
    }
}