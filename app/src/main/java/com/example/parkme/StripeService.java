package com.example.parkme;


import android.content.Context;

import com.stripe.android.Stripe;

public class StripeService {
    private Context context;
    private Stripe stripe;

    public StripeService(Context context) {
        this.context = context;
        initializeStripe();
    }

    private void initializeStripe() {
        // Initialize Stripe
    }
}
