package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.parkme.model.Bookings;
import com.example.parkme.model.CardDetails;
import com.example.parkme.norm.VehicleInfoActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
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
import java.util.Locale;
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

        parkingSpot = getIntent().getParcelableExtra("PARKING_SPOT");
        if (parkingSpot != null) {
            parkingSpaceName.setText(parkingSpot.getName());
            fetchParkingRate();
        } else {
            // Handle the case where parkingSpot is null
            parkingSpaceName.setText("No Parking Spot Selected");
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Finish this activity and go back to the previous one
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
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference spotRef = firestore.collection("ParkingSpot").document(parkingSpot.getId());

    firestore.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(spotRef);
                    long newBookedSpots = snapshot.getLong("bookedSpots") != null ? snapshot.getLong("bookedSpots") + 1 : 1;
                    if (newBookedSpots > parkingSpot.getCapacity()) {
                        runOnUiThread(() -> {
                            bookButton.setText("Unavailable");
                            bookButton.setEnabled(false);
                            showError("Parking spot is full.");
                        });
                        throw new FirebaseFirestoreException("Parking spot capacity exceeded", FirebaseFirestoreException.Code.ABORTED);
                    }
                    transaction.update(spotRef, "bookedSpots", newBookedSpots);
                    return null;
                }).addOnSuccessListener(aVoid -> createBookingAndUpdateUI())
                .addOnFailureListener(e -> showError("Booking failed. Please try again. Error: " + e.getMessage()));
    }

    private void createBookingAndUpdateUI() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date startTime, endTime;

        try {
            startTime = sdf.parse(dateEditText.getText().toString() + " " + startTimeEditText.getText().toString());
            endTime = sdf.parse(dateEditText.getText().toString() + " " + endTimeEditText.getText().toString());
        } catch (ParseException e) {
            showError("Invalid date or time format.");
            return;
        }

        long durationInMillis = endTime.getTime() - startTime.getTime();
        int durationInHours = (int) (durationInMillis / (1000 * 60 * 60));
        double ratePerHour = parkingSpot.getPrice();
        double totalPrice = durationInHours * ratePerHour;

        double vatPercentage = 20; // Assuming a 20% VAT rate
        double serviceFee = 0.40; // Assuming there's a fixed service fee
        double vatAmount = totalPrice * vatPercentage / 100;
        double finalTotalPrice = totalPrice + vatAmount + serviceFee;

        Bookings booking = new Bookings();
        booking.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        booking.setParkingSpotId(parkingSpot.getId()); // Assuming getId() gets the Firestore document ID or unique identifier.
        booking.setParkingSpotName(parkingSpot.getName()); // Setting the name of the parking spot.
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setDuration(durationInHours);
        booking.setTotalPrice(finalTotalPrice);
        booking.setStatus(determineBookingStatus(startTime, endTime)); // Ensure this method is correctly implemented.

        // Ensure this method is implemented correctly

        Map<String, Object> bookingMap = booking.toMap(); // Convert booking object to a map for Firestore upload

        firestore.collection("Bookings").add(bookingMap)
                .addOnSuccessListener(documentReference -> {
                    // Update the UI to show the booking confirmation
                    displayConfirmation(booking);
                })
                .addOnFailureListener(e -> {
                    showError("Failed to save booking. Please try again.");
                });
    }


    private String determineBookingStatus(Date startTime, Date endTime) {
        Date now = new Date();
        if (now.before(startTime)) {
            return Bookings.BookingStatus.UPCOMING.name();
        } else if (now.after(startTime) && now.before(endTime)) {
            return Bookings.BookingStatus.INPROGRESS.name();
        } else if (now.after(endTime)) {
            return Bookings.BookingStatus.COMPLETED.name();
        } else {
            return "UNKNOWN"; // Consider using an appropriate status or handling this case differently
        }
    }

    private void displayConfirmation(Bookings booking) {
        Dialog confirmationDialog = new Dialog(this); // Use 'getContext()' if this is a fragment
        confirmationDialog.setContentView(R.layout.receipt);

        // Initialize the views from the layout
        TextView bookingDetailsTextView = confirmationDialog.findViewById(R.id.bookingDetails);
        ImageView qrCodeImageView = confirmationDialog.findViewById(R.id.qrCode);

        // Format the booking details and set them to the TextView
        String bookingDetails = formatBookingDetails(booking);
        bookingDetailsTextView.setText(bookingDetails);

        // Generate the QR code (assuming you have a method to do this)
        Bitmap qrCodeBitmap = generateQRCode(bookingDetails);
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        } else {
            // Handle the case where the QR code generation fails
            qrCodeImageView.setVisibility(View.GONE); // Or show a default image
        }

        // Set the button click listener
        Button confirmButton = confirmationDialog.findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            confirmationDialog.dismiss();
            finish();
            // Handle the confirmation action (e.g., navigate to another activity or close the current one)
        });

        // Display the dialog
        confirmationDialog.show();
    }

    private String formatBookingDetails(Bookings booking) {
        // Format your booking details into a human-readable string
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return "ID: " + booking.getBookingId() + "\n"
                + "Spot Name: " + booking.getParkingSpotName() + "\n"  // Including the parking spot name in the details
                + "Start: " + dateFormat.format(booking.getStartTime()) + "\n"
                + "End: " + dateFormat.format(booking.getEndTime()) + "\n"
                + "Price: $" + String.format("%.2f", booking.getTotalPrice());  // Ensuring the price is formatted to two decimal places
    }


    private Bitmap generateQRCode(String text) {
        // Implement QR code generation logic here
        // This is a placeholder for the actual QR code generation logic
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}