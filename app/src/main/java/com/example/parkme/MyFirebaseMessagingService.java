package com.example.parkme;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.parkme.model.Notifications;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Handle data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            saveNotification(remoteMessage);
        }

        // Handle notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            displayNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    private void displayNotification(String title, String messageBody) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.logo) // Set your own notification icon here.
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // ID for notification so we can update it later. Each notification needs a unique ID.
        int notificationId = (int) System.currentTimeMillis();

        notificationManager.notify(notificationId, builder.build());
    }

    private void saveNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        long timestampLong = System.currentTimeMillis(); // Or however you are getting the timestamp
        Timestamp timestamp = new Timestamp(timestampLong / 1000, (int) (timestampLong % 1000) * 1000000);
        // Generate a notification ID or use the FCM message ID.
        String notificationId = remoteMessage.getMessageId();

        Notifications notification = new Notifications(notificationId, title, message, timestamp, false);

        // Save the notification to Firestore.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (notificationId != null) {
            db.collection("notifications").document(notificationId).set(notification)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        } else {
            Log.w(TAG, "Notification ID is null, skipping Firestore save");
        }
    }
}
