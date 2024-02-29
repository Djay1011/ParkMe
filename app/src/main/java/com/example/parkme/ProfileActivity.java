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

public class ProfileActivity extends AppCompatActivity {

    private FirebaseFirestore firesStore;
    private TextInputEditText fullNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.buttonUpdateDetails).setOnClickListener(v -> updateDetails());
        findViewById(R.id.buttonCloseAccount).setOnClickListener(v -> confirmAccountDeletion());

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        fetchUserDetails();
    }

    private void confirmAccountDeletion() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialogInterface, i) -> closeUserAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void closeUserAccount() {
        if (currentUser != null) {
            firesStore.collection("user").document(currentUser.getUid()).delete()
                    .addOnSuccessListener(aVoid -> {
                        currentUser.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseAuth.getInstance().signOut();
                                redirectToLogin();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to delete user account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void updateDetails() {
        String newFullName = fullNameEditText.getText().toString();
        String newEmail = emailEditText.getText().toString();
        String newPhone = phoneEditText.getText().toString();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("fullName", newFullName);
        userUpdates.put("email", newEmail);
        userUpdates.put("phoneNumber", newPhone);

        if (currentUser != null) {
            firesStore.collection("users").document(currentUser.getUid())
                    .update(userUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "User details updated successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to update user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void fetchUserDetails() {
       /* FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            DocumentReference docRef = firesStore.collection("user").document(currentUser.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fullName = documentSnapshot.getString("fullName");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phoneNumber");

                    TextInputEditText fullNameEditText = findViewById(R.id.fullNameEditText);
                    TextInputEditText emailEditText = findViewById(R.id.emailEditText);
                    TextInputEditText phoneEditText = findViewById(R.id.phoneEditText);

                    fullNameEditText.setText(fullName);
                    emailEditText.setText(email);
                    phoneEditText.setText(phone);
                } else {
                    Toast.makeText(ProfileActivity.this, "No user details found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve user detail: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }*/
    }
}