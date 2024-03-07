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

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private FirebaseFirestore fireStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fireStore = FirebaseFirestore.getInstance();
        adapter = new NotificationAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

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


    @Override
    public void onNotificationClicked(Notifications notification) {
        if (notification != null) {
            // Create an AlertDialog to show the notification details
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(notification.getTitle());
            builder.setMessage(notification.getMessage());

            // Add an OK button to the dialog
            builder.setPositiveButton("OK", (dialog, which) -> {
                // Check if the notification is not read and the ID is not null
                if (!notification.isRead() && notification.getId() != null) {
                    fireStore.collection("Notifications").document(notification.getId())
                            .update("isRead", true)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("NotificationFragment", "DocumentSnapshot successfully updated!");
                                // Set the local notification object as read
                                notification.setRead(true);
                                // Notify the adapter to refresh the RecyclerView
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> Log.w("NotificationFragment", "Error updating document", e));
                }
            });

            // Create and show the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Log.e("NotificationFragment", "Notification is null.");
        }
    }

}
