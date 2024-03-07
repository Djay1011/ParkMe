package com.example.parkme.norm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.parkme.ContactUsActivity;
import com.example.parkme.norm.LoginActivity;
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
 * A fragment displaying a menu with a variety of options, offering users access to settings and extra features..
 */
public class MoreFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment with the "More" options
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Setup the UI components and their event listeners
        setUpViewsAndListeners(view);
        return view;
    }

    /**
     * Initializes the views and sets up the event listeners for the UI components.
     *
     * @param view The root view of the fragment.
     */
    private void setUpViewsAndListeners(View view) {
        // Initialize the TextView for displaying the user's name
        TextView userNameTextView = view.findViewById(R.id.userName);
        fetchAndDisplayUserFirstName(userNameTextView);

        // Setup the switch for toggling dark mode
        SwitchMaterial darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDarkMode(isChecked));

        // Initialize click listeners for various options in the fragment
        setupClickListeners(view);
    }

    /**
     * Retrieve the first name of the current user from Firestore and display it in the TextView.
     *
     * @param userNameTextView TextView where the user's first name will be displayed.
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
     * Sets up the click listeners for the user interface elements.
     * @param view The root view of the fragment.
     */
    private void setupClickListeners(View view) {
        // Listener for the profile view option
        view.findViewById(R.id.viewProfile).setOnClickListener(v -> viewProfile());

        // Placeholder for notification toggle
        view.findViewById(R.id.notificationSwitch).setOnClickListener(v -> toggleNotifications());

        // Listener for viewing vehicle information
        view.findViewById(R.id.VehicleInfoText).setOnClickListener(v -> viewVehicleInfo());

        // Listener for switching to the payment methods view
        view.findViewById(R.id.paymentText).setOnClickListener(v -> viewPaymentMethod());

        // Placeholder for viewing security and privacy settings
        view.findViewById(R.id.securityText).setOnClickListener(v -> viewSecurityPrivacy());

        // Listener for contacting support
        view.findViewById(R.id.contactText).setOnClickListener(v -> contactUs());

        // Placeholder for logging out the current user
        view.findViewById(R.id.logoutText).setOnClickListener(v -> logOut());
    }

    /**
     * Begins the process of accessing the user's profile.
     */
    private void viewProfile() {
        startActivity(new Intent(getActivity(), ProfileActivity.class));
    }

    /**
     * Switches the app's dark mode on or off.
     * @param isEnabled Whether dark mode should be enabled.
     */
    private void toggleDarkMode(boolean isEnabled) {
        AppCompatDelegate.setDefaultNightMode(isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    /**
     * Placeholder method for toggling notifications.
     */
    private void toggleNotifications() {
        // Implementation required
    }

    /**
     * Begins the process of accessing the vehicle's information.
     */
    private void viewVehicleInfo() {
        startActivity(new Intent(getActivity(), VehicleInfoActivity.class));
    }

    /**
     * Switches the current navigation view to the wallet section..
     */
    private void viewPaymentMethod() {
        ((BottomNavigationView) getActivity().findViewById(R.id.bottom_navigation)).setSelectedItemId(R.id.nav_wallet);
    }

    /**
     * Placeholder method for viewing security and privacy settings.
     */
    private void viewSecurityPrivacy() {
        // Implementation required
    }

    /**
     * S Starts the activity for contacting support.
     */
    private void contactUs() {
        startActivity(new Intent(getActivity(), ContactUsActivity.class));
    }

    /**
     * Ends the session for the current user and takes them back to the login page.
     */
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Close the current activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
