package com.example.parkme.norm;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.parkme.R;
import com.example.parkme.NotificationAdapter;
import com.example.parkme.model.Notifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * NotificationFragment is responsible for displaying user notifications in a RecyclerView.
 * It retrieves notification data from Firestore, updates the UI accordingly, and handles user interactions.
 */
public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FirebaseFirestore fireStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment and initialize RecyclerView and Firestore.
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        initializeRecyclerView(view);
        initializeFirestore();
        loadNotifications();

        return view;
    }

    /**
     * Initializes the RecyclerView with a LinearLayoutManager and sets the adapter.
     *
     * @param view The current view of the fragment used to find the RecyclerView layout.
     */
    private void initializeRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Initializes the Firestore instance.
     */
    private void initializeFirestore() {
        fireStore = FirebaseFirestore.getInstance();
    }

    /**
     * Loads the notifications from Firestore and updates the adapter.
     * Filters notifications by the current user's ID and orders them by timestamp.
     */
    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Querying the Firestore database for notifications specific to the user.
            fireStore.collection("Notifications")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.w("NotificationFragment", "Listen failed.", error);
                            return;
                        }

                        List<Notifications> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            notifications.add(doc.toObject(Notifications.class));
                        }
                        adapter.setNotifications(notifications);
                    });
        } else {
            Log.e("NotificationFragment", "No user logged in, can't load notifications.");
        }
    }

    /**
     * Handles notification click events. Displays the notification details in an AlertDialog,
     * and marks the notification as read in Firestore.
     *
     * @param notification The notification that was clicked.
     */
    @Override
    public void onNotificationClicked(Notifications notification) {
        if (notification != null) {
            showNotificationDetails(notification);
        } else {
            Log.e("NotificationFragment", "Notification is null.");
        }
    }

    /**
     * Shows an AlertDialog with the details of the clicked notification.
     * If the notification is unread, it updates its status in Firestore.
     *
     * @param notification The clicked notification whose details are to be displayed.
     */
    private void showNotificationDetails(Notifications notification) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(notification.getTitle());
        builder.setMessage(notification.getMessage());
        builder.setPositiveButton("OK", (dialog, which) -> updateNotificationAsRead(notification));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Updates the notification's read status in Firestore and locally, then refreshes the UI.
     *
     * @param notification The notification to mark as read.
     */
    private void updateNotificationAsRead(Notifications notification) {
        if (!notification.isRead() && notification.getId() != null) {
            fireStore.collection("Notifications").document(notification.getId())
                    .update("isRead", true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("NotificationFragment", "DocumentSnapshot successfully updated!");
                        notification.setRead(true);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.w("NotificationFragment", "Error updating document", e));
        }
    }
}
