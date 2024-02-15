/**package com.example.parkme;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.annotations.Nullable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookingProcess#newInstance} factory method to
 * create an instance of this fragment.
 *
public class BookingProcess extends BottomSheetDialogFragment {

    // You can pass data to the fragment using a static newInstance method
    public static BookingProcess newInstance(ParkingSpot spot) {
        BookingProcess fragment = new BookingProcess();
        Bundle args = new Bundle();
        args.putSerializable("parking_spot", spot); // Ensure ParkingSpot implements Serializable
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_booking_process, container, false);
    }

    // Add methods for handling the booking logic here
}**/