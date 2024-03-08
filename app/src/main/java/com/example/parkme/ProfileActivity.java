package com.example.parkme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.parkme.norm.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for  overseeing and handling user profile data.
 * Users have the option to both update their information and terminate their account.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private TextInputEditText fullNameEditText, emailEditText, phoneEditText;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupToolbar();
        fetchUserDetails();
    }

    /**
     * Start up the views and configure event listeners.
     */
    private void initializeViews() {
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);

        findViewById(R.id.buttonUpdateDetails).setOnClickListener(v -> updateDetails());
        findViewById(R.id.buttonCloseAccount).setOnClickListener(v -> confirmAccountDeletion());
    }

    /**
     * Sets up the Material Toolbar and enables functionality for the back button.
     */
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Retrieves the latest information of the logged-in user from Firestore and displays it.
     */
    private void fetchUserDetails() {
        if (currentUser != null) {
            DocumentReference userDoc = firestore.collection("user").document(currentUser.getUid());
            userDoc.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    fullNameEditText.setText(documentSnapshot.getString("fullName"));
                    emailEditText.setText(documentSnapshot.getString("email"));
                    phoneEditText.setText(documentSnapshot.getString("phone"));
                } else {
                    Toast.makeText(this, "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error fetching user details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    /**
     * Verifies the removal of the user's account.
     */
    private void confirmAccountDeletion() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> closeUserAccount())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Deactivates the account of the current user by erasing their data and disabling the account.
     */
    private void closeUserAccount() {
        if (currentUser != null) {
            DocumentReference userDoc = firestore.collection("user").document(currentUser.getUid());
            userDoc.delete().addOnSuccessListener(aVoid -> {
                currentUser.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().signOut();
                        redirectToLogin();
                    } else {
                        Toast.makeText(this, "Failed to delete user account.", Toast.LENGTH_LONG).show();
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error deleting user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    /**
     * Send the user to the login page.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Updates the user's information with the newly entered data.
     */
    private void updateDetails() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullNameEditText.getText().toString());
        updates.put("email", emailEditText.getText().toString());
        updates.put("phone", phoneEditText.getText().toString());

        if (currentUser != null) {
            DocumentReference userDoc = firestore.collection("user").document(currentUser.getUid());
            userDoc.update(updates).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "User details updated successfully.", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update user details.", Toast.LENGTH_LONG).show();
            });
        }
    }
}
