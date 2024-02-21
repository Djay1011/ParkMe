package com.example.parkme;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkme.model.CardDetails;

public class Activity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private ParkingSpot parkingSpot; // Field for parkingSpot

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView and other setup...

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.getSelectedCard().observe(this, this::reopenBookingProcess);
    }

    private void reopenBookingProcess(CardDetails card) {
        BookingProcess bookingFragments = (BookingProcess) getSupportFragmentManager()
                .findFragmentByTag("BookingProcess");

        if (bookingFragments != null) {
            bookingFragments.updateCardDetails(card);
        } else {
            bookingFragments = BookingProcess.newInstance(parkingSpot, card);
            bookingFragments.show(getSupportFragmentManager(), "BookingProcess");
        }
    }
}