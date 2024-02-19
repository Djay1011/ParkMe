package com.example.parkme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.example.parkme.model.CardDetails;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class BookingProcess extends BottomSheetDialogFragment {

    private static final String ARG_PARKING_SPOT = "parking_spot";
    private static final String ARG_CARD_DETAILS = "card_details";
    private TextView parkingSpaceName, vatFeeValue, serviceFeeValue, totalPrice, errorTextView;
    private EditText dateEditText, timeEditText, durationEditText;
    private MaterialButton bookButton, addPaymentMethodButton;
    private FirebaseFirestore firestore;
    private ParkingSpot parkingSpot;
    private CardDetails currentCard;
    private TextView currentPaymentMethod;

    public static BookingProcess newInstance(ParkingSpot spot, @Nullable CardDetails cardDetails) {
        BookingProcess fragment = new BookingProcess();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARKING_SPOT, spot);
        args.putParcelable(ARG_CARD_DETAILS, cardDetails);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            parkingSpot = getArguments().getParcelable(ARG_PARKING_SPOT);
            currentCard = getArguments().getParcelable(ARG_CARD_DETAILS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_process, container, false);
        initializeViews(view);
        setupListeners();
        return view;
    }

    private void initializeViews(View view) {
        parkingSpaceName = view.findViewById(R.id.parkingSpaceName);
        dateEditText = view.findViewById(R.id.date);
        timeEditText = view.findViewById(R.id.time);
        durationEditText = view.findViewById(R.id.duration);
        bookButton = view.findViewById(R.id.bookBtn);
        vatFeeValue = view.findViewById(R.id.vatFeeValue);
        serviceFeeValue = view.findViewById(R.id.serviceFeeValue);
        totalPrice = view.findViewById(R.id.totalPrice);
        errorTextView = view.findViewById(R.id.errorTextView);
        addPaymentMethodButton = view.findViewById(R.id.addPaymentMethodButton);
        currentPaymentMethod = view.findViewById(R.id.currentPaymentMethod);
        updatePaymentMethodDisplay();


        loadData();
    }

    private void updatePaymentMethodDisplay() {
        if (currentCard != null) {
            currentPaymentMethod.setText("Card ending with " + currentCard.getLast4Digits());
        } else {
            currentPaymentMethod.setText("No payment method added");
        }
    }

    private void setupListeners() {
        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        durationEditText.setOnClickListener(v -> showDurationPickerDialog());
        bookButton.setOnClickListener(v -> processBooking());
        durationEditText.addTextChangedListener(new CostTextWatcher());

        addPaymentMethodButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Payments.class);
            startActivity(intent);
        });
    }

    private class CostTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not used
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not used
        }

        @Override
        public void afterTextChanged(Editable s) {
            calculateAndDisplayTotalCost();
        }
    }


    private void loadData() {
        parkingSpot = getArguments().getParcelable(ARG_PARKING_SPOT);
        if (parkingSpot != null) {
            parkingSpaceName.setText(parkingSpot.getName());
        }
    }

    private void processBooking() {
        // Handle the booking process
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this::onDateSet, year, month, day);
        datePickerDialog.show();
    }

    private void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String selectedDate = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
        dateEditText.setText(selectedDate);
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this::onTimeSet, hour, minute, DateFormat.is24HourFormat(getActivity()));
        timePickerDialog.show();
    }

    private void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
        timeEditText.setText(selectedTime);
    }

    private void showDurationPickerDialog() {
        NumberPicker numberPicker = new NumberPicker(getContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(24);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_duration)
                .setView(numberPicker)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    if (durationEditText != null) {
                        durationEditText.setText(String.valueOf(numberPicker.getValue()));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }



    private void fetchParkingSpaceName(String parkingSpotId) {
         firestore.collection("ParkingSpot").document(parkingSpotId)
                 .get() // Changed to get() for a one-time read
                 .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                     @Override
                     public void onSuccess(DocumentSnapshot documentSnapshot) {
                         if (documentSnapshot.exists()) {
                             String name = documentSnapshot.getString("name");
                             updateParkingSpaceName(name);
                         } else {
                             // Handle the case where the document does not exist.
                         }
                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         // Handle the error here
                     }
                 });
     }

     private void updateParkingSpaceName(String name) {
         FragmentActivity activity = getActivity();
         if (activity != null) {
             activity.runOnUiThread(() -> parkingSpaceName.setText(name));
         }
     }

    private void calculateAndDisplayTotalCost() {
        String durationInput = durationEditText.getText().toString();
        if (durationInput.isEmpty()) {
            displayErrorMessage("Please enter a duration");
            return;
        }

        try {
            int duration = Integer.parseInt(durationInput);
            if (duration <= 0) {
                displayErrorMessage("Duration must be a positive number");
                return;
            }
            calculateCosts(duration);
        } catch (NumberFormatException e) {
            displayErrorMessage("Invalid duration format");
        }
    }

    private void calculateCosts(int duration) {
        if (parkingSpot == null) {
            displayErrorMessage("Parking spot information is not available.");
            return;
        }
        double baseCost = duration * parkingSpot.getPrice();
        double vat = baseCost * 0.20;
        double serviceFee = 0.50;
        double totalCost = baseCost + vat + serviceFee;

        vatFeeValue.setText(String.format(Locale.getDefault(), "£%.2f", vat));
        serviceFeeValue.setText(String.format(Locale.getDefault(), "£%.2f", serviceFee));
        totalPrice.setText(String.format(Locale.getDefault(), "£%.2f", totalCost));
    }

    private void displayErrorMessage(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        vatFeeValue.setText("£0.00");
        serviceFeeValue.setText("£0.00");
        totalPrice.setText("£0.00");
    }


    // ... other methods and logic
}
