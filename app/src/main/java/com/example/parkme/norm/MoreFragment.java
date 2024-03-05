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

import com.example.parkme.ProfileActivity;
import com.example.parkme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MoreFragment extends Fragment {

    // Other member variables...

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Initialize views and set up listeners
        setUpViewsAndListeners(view);
        return view;
    }

    private void setUpViewsAndListeners(View view) {
        TextView userNameTextView = view.findViewById(R.id.userName);
        fetchAndDisplayUserFirstName(userNameTextView);

        SwitchMaterial darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        // Set the switch based on the current theme mode
        darkModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDarkMode(isChecked));

        // Set up listeners
        view.findViewById(R.id.viewProfile).setOnClickListener(v -> viewProfile());
        view.findViewById(R.id.notificationSwitch).setOnClickListener(v -> toggleNotifications());
        view.findViewById(R.id.VehicleInfoText).setOnClickListener(v -> viewVehicleInfo());
        view.findViewById(R.id.paymentText).setOnClickListener(v -> viewPaymentMethod());
        view.findViewById(R.id.securityText).setOnClickListener(v -> viewSecurityPrivacy());
        view.findViewById(R.id.contactText).setOnClickListener(v -> contactUs());
        /*view.findViewById(R.id.aboutUsLayout).setOnClickListener(v -> aboutUs());*/
        view.findViewById(R.id.logoutText).setOnClickListener(v -> logOut());
    }



    private void fetchAndDisplayUserFirstName(TextView userNameTextView) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("user").document(currentUserId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String firstName = document.getString("fullName");
                            userNameTextView.setText(firstName);
                        } else {
                            userNameTextView.setText("Guest");
                        }
                    } else {
                        userNameTextView.setText("Guest");
                    }
                }
            });
        }
    }

    private void viewProfile() {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        startActivity(intent);
    }

    private void toggleDarkMode(boolean isEnabled) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void toggleNotifications() {
        // Code to toggle notifications
    }

    private void viewVehicleInfo() {
        Intent intent = new Intent(getActivity(), VehicleInfoActivity.class);
        startActivity(intent);
    }

    private void viewPaymentMethod() {
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_wallet);
    }

    private void viewSecurityPrivacy() {
        // Code to view security and privacy settings
    }

    private void contactUs() {
        // Code to handle contact us
    }

    private void aboutUs() {
        // Code to show about us information
    }

    private void logOut() {
        // Code to handle logout
    }
}