package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private TextView parkingSpotNameTextView, totalTextView, dateTextView, startTimeTextView, endTimeTextView, durationTextView, statusTextView;
    private Button cancelBookingButton;
    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        parkingSpotNameTextView = findViewById(R.id.parkingSpotNameTextView);
        dateTextView = findViewById(R.id.dateTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        durationTextView = findViewById(R.id.durationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        totalTextView = findViewById(R.id.totalTextView); // New TextView for Total
        cancelBookingButton = findViewById(R.id.cancelBookingButton);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> finish());

        String bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId != null) {
            loadBookingDetails(bookingId);
        }

        cancelBookingButton.setOnClickListener(v -> {
            if (bookingId != null) {
                updateBookingStatusToCancelled(bookingId);
            }
        });
    }

    private void updateBookingStatusToCancelled(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookingRef = db.collection("Bookings").document(bookingId);

        bookingRef.update("status", "Cancelled")
                .addOnSuccessListener(aVoid -> {
                    statusTextView.setText("Cancelled");
                    Toast.makeText(ReceiptActivity.this, "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                    qrCodeImageView.setImageBitmap(null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ReceiptActivity.this, "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
                });
    }


    private void loadBookingDetails(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Bookings").document(bookingId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    parkingSpotNameTextView.setText(document.getString("parkingSpotName"));
                    statusTextView.setText(document.getString("status"));

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    Timestamp startDateTimestamp = document.getTimestamp("startTime");
                    if (startDateTimestamp != null) {
                        Date startDate = startDateTimestamp.toDate();
                        dateTextView.setText(dateFormat.format(startDate));
                    }

                    Timestamp startTimeTimestamp = document.getTimestamp("startTime");
                    if (startTimeTimestamp != null) {
                        Date startTime = startTimeTimestamp.toDate();
                        startTimeTextView.setText(timeFormat.format(startTime));
                    }

                    Timestamp endTimeTimestamp = document.getTimestamp("endTime");
                    if (endTimeTimestamp != null) {
                        Date endTime = endTimeTimestamp.toDate();
                        endTimeTextView.setText(timeFormat.format(endTime));
                    }

                    // Retrieve and display the duration in hours
                    Long duration = document.getLong("duration");
                    if (duration != null) {
                        durationTextView.setText(String.format(Locale.getDefault(), "%d hours", duration));
                    } else {
                        durationTextView.setText("Duration unavailable");
                    }

                    // Retrieve and display the total price
                    Double price = document.getDouble("totalPrice");
                    if (price != null) {
                        totalTextView.setText(String.format(Locale.getDefault(), "£%.2f", price));
                    } else {
                        totalTextView.setText("Price unavailable");
                    }

                    generateAndSetQRCode(bookingId);
                } else {
                    // Handle case where no such document exists.
                    Toast.makeText(ReceiptActivity.this, "Booking not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the failure to retrieve the document
                Toast.makeText(ReceiptActivity.this, "Failed to load booking details. Please try again later.", Toast.LENGTH_LONG).show();
                Log.e("ReceiptActivity", "Error loading booking details", task.getException());
            }
        });
    }

    private void generateAndSetQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrCodeImageView.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}