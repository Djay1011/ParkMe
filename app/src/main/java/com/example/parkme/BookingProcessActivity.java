package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.parkme.model.CardDetails;
import com.example.parkme.norm.VehicleInfoActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookingProcessActivity extends AppCompatActivity {
    private TextInputEditText dateEditText;
    private TextInputEditText startTimeEditText;
    private TextInputEditText endTimeEditText;
    private TextInputEditText durationEditText;
    private TextView vatFeeValueTextView;
    private TextView serviceFeeValueTextView;
    private TextView totalPriceTextView;
    private TextView vehicleDetails;
    private TextView parkingSpaceName;
    private TextView currentPaymentMethod;
    private TextView errorTextView;
    private MaterialButton bookButton;
    private CardDetails currentCard;
    private FirebaseFirestore firestore;
    private ParkingSpot parkingSpot;
    private double parkingRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_process);

        firestore = FirebaseFirestore.getInstance();

        dateEditText = findViewById(R.id.date);
        startTimeEditText = findViewById(R.id.startTime);
        endTimeEditText = findViewById(R.id.endTime);
        durationEditText = findViewById(R.id.duration);
        vatFeeValueTextView = findViewById(R.id.vatFeeValue);
        serviceFeeValueTextView = findViewById(R.id.serviceFeeValue);
        vehicleDetails = findViewById(R.id.vehicleDetails);
        currentPaymentMethod = findViewById(R.id.currentPaymentMethod);
        totalPriceTextView = findViewById(R.id.totalPrice);
        parkingSpaceName = findViewById(R.id.parkingSpaceName);
        errorTextView = findViewById(R.id.errorTextView);
        bookButton = findViewById(R.id.bookBtn);
        ImageView backButton = findViewById(R.id.backButton);

        parkingSpot = getIntent().getParcelableExtra("PARKING_SPOT");
        if (parkingSpot != null) {
            parkingSpaceName.setText(parkingSpot.getName());
            fetchParkingRate();
        } else {
            // Handle the case where parkingSpot is null
            parkingSpaceName.setText("No Parking Spot Selected");
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish the activity, going back to the previous screen
            }
        });

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimePickerDialog();
            }
        });

        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePickerDialog();
            }
        });

        startTimeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateDuration();
            }
        });

        endTimeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateDuration();
            }
        });

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    verifyAvailability();
                    // Further processing like saving booking details
                    confirmBooking();
                }
            }
        });
        fetchVehicleInfo();
        fetchCardDetails();
    }

    private boolean validateInputs() {
        if (dateEditText.getText().toString().isEmpty()) {
            dateEditText.setError("Please select a date");
            return false;
        }
        if (startTimeEditText.getText().toString().isEmpty()) {
            startTimeEditText.setError("Please select a start time");
            return false;
        }
        // Add similar checks for endTime, duration, and vehicleInfo

        return true;
    }

    private void showDatePickerDialog() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Format the date and set it to the date EditText
                        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        dateEditText.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showStartTimePickerDialog() {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format the time and set it to the startTime EditText
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        startTimeEditText.setText(selectedTime);
                    }
                }, hour, minute, true); // true for 24 hour time format

        timePickerDialog.show();
    }

    private void showEndTimePickerDialog() {
        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format the time and set it to the endTime EditText
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        endTimeEditText.setText(selectedTime);
                    }
                }, hour, minute, true); // true for 24 hour time format

        timePickerDialog.show();
    }

    private void calculateDuration() {
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");

        try {
            Date startDate = format.parse(startTime);
            Date endDate = format.parse(endTime);

            if (startDate != null && endDate != null) {
                long difference = endDate.getTime() - startDate.getTime();
                if (difference < 0) {
                    durationEditText.setText("Invalid time range");
                    return;
                }

                int hours = (int) (difference / (1000 * 60 * 60));
                int minutes = (int) (difference / (1000 * 60) % 60);

                String duration = (hours > 0 ? hours + " hours " : "") + minutes + " minutes";
                durationEditText.setText(duration);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calculateTotalCost();
    }

    private void verifyAvailability() {
        String selectedDate = dateEditText.getText().toString();
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();

        firestore.collection("bookings")
                .whereEqualTo("parkingSpotId", parkingSpot.getId())
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int currentBookedSpots = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String bookedStartTime = document.getString("startTime");
                            String bookedEndTime = document.getString("endTime");
                            if (timeOverlaps(startTime, endTime, bookedStartTime, bookedEndTime)) {
                                currentBookedSpots++;
                            }
                        }
                        // Now use the isAvailable method
                        parkingSpot.setBookedSpots(currentBookedSpots);
                        if (parkingSpot.isAvailable()) {
                            // Proceed with booking process
                        } else {
                            showError("Parking spot is not available for the selected time.");
                        }
                    } else {
                        showError("Error checking availability. Please try again.");
                    }
                })
                .addOnFailureListener(e -> showError("Failed to check availability. Please try again."));
    }

    private boolean timeOverlaps(String startTime, String endTime, String bookedStartTime, String bookedEndTime) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        try {
            Date start = format.parse(startTime);
            Date end = format.parse(endTime);
            Date bookedStart = format.parse(bookedStartTime);
            Date bookedEnd = format.parse(bookedEndTime);

            // Check for overlap
            if ((start.before(bookedEnd) || start.equals(bookedEnd)) &&
                    (end.after(bookedStart) || end.equals(bookedStart))) {
                return true; // There is an overlap
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // No overlap
    }


    private void fetchParkingRate() {
        if (parkingSpot != null) {
            parkingRate = parkingSpot.getPrice();
            // Optionally, you might want to update the UI or calculations that depend on the parkingRate here
        } else {
            // Handle the case where the parkingSpot object is null
        }
    }
    private void calculateTotalCost() {
        String durationStr = durationEditText.getText().toString();
        // Parse the duration string to extract hours and minutes
        int hours = 0, minutes = 0;
        if (durationStr.contains("hours")) {
            String[] parts = durationStr.split(" hours ");
            hours = Integer.parseInt(parts[0]);
            minutes = Integer.parseInt(parts[1].split(" ")[0]);
        } else if (durationStr.contains("minutes")) {
            minutes = Integer.parseInt(durationStr.split(" ")[0]);
        }

        double totalHours = hours + minutes / 60.0;
        double totalCost = totalHours * parkingRate;

        double vat = totalCost * 0.20;
        double serviceFee = 0.40;
        double finalCost = totalCost + vat + serviceFee;

        // Update the TextViews
        vatFeeValueTextView.setText(String.format("£%.2f", vat));
        serviceFeeValueTextView.setText(String.format("£%.2f", serviceFee));
        totalPriceTextView.setText(String.format("£%.2f", finalCost));
    }

    private void fetchVehicleInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("user") // Replace with your collection name
                    .document(userId)
                    .collection("vehicles")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming you want to display the first vehicle's info
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            String vehicleInfo = documentSnapshot.getString("plateNumber"); // Replace with your field name
                            vehicleDetails.setText(vehicleInfo);
                        } else {
                            vehicleDetails.setText("Add Vehicle Details");
                            setAddVehicleDetailsListener();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors here
                        vehicleDetails.setText("Error fetching vehicle info");
                    });
        } else {
            // Handle the case where there is no logged-in user
            vehicleDetails.setText("User not logged in");
        }
    }

    private void setAddVehicleDetailsListener() {
        vehicleDetails.setOnClickListener(view -> {
            Intent intent = new Intent(BookingProcessActivity.this, VehicleInfoActivity.class);
            startActivity(intent);
        });
    }

    private void fetchCardDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get current user's ID
            firestore.collection("user").document(userId).collection("cards")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming you want to use the first card
                            CardDetails card = queryDocumentSnapshots.getDocuments().get(0).toObject(CardDetails.class);
                            if (card != null) {
                                currentCard = card;
                                updatePaymentMethodDisplay();
                            }
                        } else {
                            // Handle case where no card details are found
                            currentPaymentMethod.setText("No payment method added");
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors here
                    });
        } else {
            // Handle case where no user is logged in
            currentPaymentMethod.setText("No user logged in");
        }
    }

    private void updatePaymentMethodDisplay() {
        if (currentCard != null) {
            currentPaymentMethod.setText("Card ending with " + currentCard.getLast4Digits());
        } else {
            currentPaymentMethod.setText("No payment method added");
        }
    }

    private void showError(String errorMessage) {
        if (errorTextView != null) {
            errorTextView.setText(errorMessage);
            errorTextView.setVisibility(View.VISIBLE); // Show the error TextView
        }
    }

    private void confirmBooking() {
        // Increment the booked spots and update the parking spot in Firestore
        int newBookedSpots = parkingSpot.getBookedSpots() + 1;
        parkingSpot.setBookedSpots(newBookedSpots);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("bookedSpots", newBookedSpots);

        firestore.collection("ParkingSpot").document(parkingSpot.getName())
                .update(updateMap)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated the parking spot information
                })
                .addOnFailureListener(e -> {
                    // Handle failure here
                });

        // Create booking details
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("parkingSpotId", parkingSpot.getName());
        bookingDetails.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        bookingDetails.put("date", dateEditText.getText().toString());
        bookingDetails.put("startTime", startTimeEditText.getText().toString());
        bookingDetails.put("endTime", endTimeEditText.getText().toString());
        bookingDetails.put("duration", durationEditText.getText().toString());
        // Add other necessary fields

        // Save booking details to Firestore
        firestore.collection("Bookings").add(bookingDetails)
                .addOnSuccessListener(documentReference -> {
                    // Generate and display the booking confirmation receipt
                    String confirmationReceipt = generateConfirmationReceipt(bookingDetails);
                    Bitmap qrCodeBitmap = generateQRCode(confirmationReceipt);
                    displayConfirmation(confirmationReceipt, qrCodeBitmap);

                    // Optionally, send the confirmation receipt via email or another method
                })
                .addOnFailureListener(e -> {
                    showError("Booking failed. Please try again.");
                });
    }

    private String generateConfirmationReceipt(Map<String, Object> bookingDetails) {
        // Generate a confirmation receipt based on bookingDetails
        return "Booking Confirmed: " + bookingDetails.get("parkingSpotId") +
                " on " + bookingDetails.get("date") +
                " from " + bookingDetails.get("startTime") +
                " to " + bookingDetails.get("endTime");
    }

    private Bitmap generateQRCode(String text) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayConfirmation(String confirmationReceipt, Bitmap qrCodeBitmap) {
        TextView confirmationTextView = findViewById(R.id.confirmationDetailsTextView);
        ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);

        confirmationTextView.setText(confirmationReceipt);
        confirmationTextView.setVisibility(View.VISIBLE);

        qrCodeImageView.setImageBitmap(qrCodeBitmap);
        qrCodeImageView.setVisibility(View.VISIBLE);
    }


}