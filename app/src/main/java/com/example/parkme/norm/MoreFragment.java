package com.example.parkme.norm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.parkme.ContactUsActivity;
import com.example.parkme.ProfileActivity;
import com.example.parkme.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment representing the "More" menu in the application, providing users with various settings and options.
 */
public class MoreFragment extends Fragment {

    private SwitchMaterial notificationSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment and initialize UI components.
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        setUpViewsAndListeners(view);
        return view;
    }

    /**
     * Initializes the UI components and sets up listeners.
     *
     * @param view The root view of the fragment.
     */
    private void setUpViewsAndListeners(View view) {
        // Initialize and set up the user name display.
        TextView userNameTextView = view.findViewById(R.id.userName);
        fetchAndDisplayUserFirstName(userNameTextView);

        // Initialize and set up the dark mode switch.
        SwitchMaterial darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDarkMode(isChecked));

        // Initialize and set up the notification switch.
        setupNotificationSwitch(view);

        // Setup click listeners for the profile, vehicle info, payment, contact, and logout options.
        setupClickListeners(view);
    }

    /**
     * Fetches the user's first name from Firebase and displays it.
     *
     * @param userNameTextView The TextView where the user name is displayed.
     */
    private void fetchAndDisplayUserFirstName(TextView userNameTextView) {
        // Retrieve the existing user from Firebase.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Continue only if the user is not nonexistent.
        if (user != null) {
            String currentUserId = user.getUid();
            // Referencing the Firestore document represent the user
            DocumentReference docRef = FirebaseFirestore.getInstance().collection("user").document(currentUserId);

            // Fetch the user document in an asynchronous manner.
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    // Assign the user's name to the TextView. If the name is not available, set it to "Guest" by default.
                    String firstName = document != null ? document.getString("fullName") : "Guest";
                    userNameTextView.setText(firstName);
                } else {
                    // Alternative text in the event of a failure.
                    userNameTextView.setText("Guest");
                }
            });
        }
    }

    /**
     * Sets up the notification switch to enable/disable notifications.
     *
     * @param view The root view of the fragment.
     */
    private void setupNotificationSwitch(View view) {
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        notificationSwitch.setChecked(NotificationManagerCompat.from(requireContext()).areNotificationsEnabled());

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()) {
                // If notifications are disabled in system settings, guide the user to enable them.
                Toast.makeText(getContext(), "Please enable notifications in system settings.", Toast.LENGTH_LONG).show();
                getContext().startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName()));
            }
            toggleNotifications(isChecked);
        });
    }

    /**
     * Toggles the app notifications based on the switch state.
     *
     * @param enable Indicates whether notifications should be enabled or disabled.
     */
    private void toggleNotifications(boolean enable) {
        // Here you should integrate with your notification logic
        if (enable) {
            Toast.makeText(getContext(), "Notifications Enabled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Notifications Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sets up click listeners for different layout components like profile, vehicle info, etc.
     *
     * @param view The root view of the fragment.
     */
    private void setupClickListeners(View view) {
        view.findViewById(R.id.viewProfile).setOnClickListener(v -> viewProfile());
        view.findViewById(R.id.VehicleInfoText).setOnClickListener(v -> viewVehicleInfo());
        view.findViewById(R.id.paymentText).setOnClickListener(v -> viewPaymentMethod());
        view.findViewById(R.id.contactText).setOnClickListener(v -> contactUs());
        view.findViewById(R.id.logoutText).setOnClickListener(v -> logOut());
    }

    private void viewProfile() {
        startActivity(new Intent(getActivity(), ProfileActivity.class));
    }

    private void toggleDarkMode(boolean isEnabled) {
        AppCompatDelegate.setDefaultNightMode(isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void viewVehicleInfo() {
        startActivity(new Intent(getActivity(), VehicleInfoActivity.class));
    }

    private void viewPaymentMethod() {
        ((BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.nav_wallet);
    }

    private void contactUs() {
        startActivity(new Intent(getActivity(), ContactUsActivity.class));
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
