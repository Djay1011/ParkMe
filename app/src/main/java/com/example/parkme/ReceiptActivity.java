package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkme.model.Bookings;
import com.example.parkme.norm.MainActivity;
import com.google.android.material.appbar.MaterialToolbar;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity displaying the booking receipt details along with the option to cancel the booking.
 * It also provides a feature to generate a QR code related to the booking details.
 */
public class ReceiptActivity extends AppCompatActivity {

    // UI components to display booking details
    private TextView parkingSpotNameTextView, totalTextView, dateTextView, startTimeTextView, endTimeTextView, durationTextView, statusTextView;
    private Button cancelBookingButton; // Button to trigger the cancellation of the booking
    private ImageView qrCodeImageView; // ImageView to display the generated QR code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Binding the UI components to their respective views in the layout
        parkingSpotNameTextView = findViewById(R.id.parkingSpotNameTextView);
        dateTextView = findViewById(R.id.dateTextView);
        startTimeTextView = findViewById(R.id.startTimeTextView);
        endTimeTextView = findViewById(R.id.endTimeTextView);
        durationTextView = findViewById(R.id.durationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        totalTextView = findViewById(R.id.totalTextView);
        cancelBookingButton = findViewById(R.id.cancelBookingButton);
        qrCodeImageView = findViewById(R.id.qrCodeImageView);

        // Setting up the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Extracting booking ID passed from the previous activity
        String bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId != null) {
            loadBookingDetails(bookingId);
        }

        // Setup the cancellation button's onClick listener
        cancelBookingButton.setOnClickListener(v -> {
            if (bookingId != null) {
                updateBookingStatusToCancelled(bookingId);
            }
        });
    }

    /**
     * Updates the booking status to 'Cancelled' in the database and reflects the change in the UI.
     *
     * @param bookingId The ID of the booking to be cancelled.
     */
    private void updateBookingStatusToCancelled(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference bookingRef = db.collection("Bookings").document(bookingId);

        // Retrieving the booking document and updating its status
        bookingRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                bookingRef.update("status", "Cancelled")
                        .addOnSuccessListener(aVoid -> {
                            statusTextView.setText("Cancelled");
                            Toast.makeText(ReceiptActivity.this, "Booking cancelled successfully.", Toast.LENGTH_SHORT).show();
                            qrCodeImageView.setImageBitmap(null); // Clear the QR code image
                            sendCancellationNotification(documentSnapshot.toObject(Bookings.class));
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ReceiptActivity.this, "Failed to cancel booking.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    /**
     * Sends a local notification and updates Firestore to reflect the cancellation of the booking.
     *
     * @param booking The booking object associated with the cancellation.
     */
    private void sendCancellationNotification(Bookings booking) {
        if (booking != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = booking.getUserId();
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", userId);
            notificationData.put("title", "Booking Cancelled");
            notificationData.put("message", "Your booking at " + booking.getParkingSpotName() + " has been cancelled.");
            notificationData.put("timestamp", new Timestamp(new Date()));
            notificationData.put("isRead", false);

            // Adding the cancellation notification to Firestore
            db.collection("Notifications").add(notificationData)
                    .addOnSuccessListener(documentReference -> receiveFcmMessage("Booking Cancelled", "Your booking at " + booking.getParkingSpotName() + " has been cancelled."))
                    .addOnFailureListener(e -> Log.e("ReceiptActivity", "Error sending cancellation notification", e));
        }
    }

    /**
     * Triggers the local notification to inform the user about the booking cancellation.
     *
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    private void receiveFcmMessage(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "cancel_notification_channel";

        // Creating notification channel for Android Oreo and above
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Booking Cancellations", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Creating the intent that will fire when the user taps on the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Building the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Issuing the notification
        notificationManager.notify(1, builder.build());
    }

    /**
     * Loads the details of the booking from Firestore and displays them in the UI.
     *
     * @param bookingId The ID of the booking whose details are to be loaded.
     */
    private void loadBookingDetails(String bookingId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Bookings").document(bookingId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    updateUIWithBookingDetails(document);
                } else {
                    Toast.makeText(ReceiptActivity.this, "Booking not found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ReceiptActivity.this, "Failed to load booking details. Please try again later.", Toast.LENGTH_LONG).show();
                Log.e("ReceiptActivity", "Error loading booking details", task.getException());
            }
        });
    }

    /**
     * Updates the UI elements with the data from the booking document.
     *
     * @param document The Firestore document snapshot containing booking details.
     */
    private void updateUIWithBookingDetails(DocumentSnapshot document) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Display booking details
        parkingSpotNameTextView.setText(document.getString("parkingSpotName"));
        statusTextView.setText(document.getString("status"));
        Timestamp startDateTimestamp = document.getTimestamp("startTime");
        dateTextView.setText(dateFormat.format(startDateTimestamp.toDate()));
        startTimeTextView.setText(timeFormat.format(startDateTimestamp.toDate()));
        Timestamp endTimeTimestamp = document.getTimestamp("endTime");
        endTimeTextView.setText(timeFormat.format(endTimeTimestamp.toDate()));
        Long duration = document.getLong("duration");
        durationTextView.setText(String.format(Locale.getDefault(), "%d hours", duration));
        Double price = document.getDouble("totalPrice");
        totalTextView.setText(String.format(Locale.getDefault(), "£%.2f", price));

        // Generate and display QR code
        generateAndSetQRCode(document.getId());
    }

    /**
     * Generates a QR code based on the given data and sets it to the ImageView.
     *
     * @param data The data to encode in the QR code.
     */
    private void generateAndSetQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            qrCodeImageView.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.e("ReceiptActivity", "Error generating QR code: ", e);
        }
    }
}
