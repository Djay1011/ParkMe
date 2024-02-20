package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private EditText fullNameInput, emailInput, phoneInput, passwordInput, retypePasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFirebase();
        initializeViews();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        retypePasswordInput = findViewById(R.id.retypePasswordInput);

        findViewById(R.id.signUpButton).setOnClickListener(view -> attemptRegistration());
        findViewById(R.id.signIn).setOnClickListener(view -> navigateToLogin());
    }

    private void attemptRegistration() {
        if (!validateInputs()) {
            return;
        }
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        createUserAccount(email, password);
    }

    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String retypePassword = retypePasswordInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }
        if (!Patterns.PHONE.matcher(phone).matches()) {
            showToast("Please enter a valid phone number.");
            return false;
        }
        if (!isValidPassword(password)) {
            showToast("Password must be at least 6 characters long and include an uppercase letter, a lowercase letter, a number, and a special character.");
            return false;
        }
        if (!password.equals(retypePassword)) {
            showToast("Passwords do not match.");
            return false;
        }
        return true;
    }

    private void createUserAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sendEmailVerification();
                        addUserToFirestore();
                    } else {
                        showToast("Registration failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }

    private void addUserToFirestore() {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullNameInput.getText().toString().trim());
        user.put("email", emailInput.getText().toString().trim());
        user.put("phone", phoneInput.getText().toString().trim());

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firestore.collection("user")
                    .document(firebaseUser.getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> showToast("User added to Firestore"))
                    .addOnFailureListener(e -> showToast("Error adding user to Firestore: " + e.getMessage()));
        }
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Verification email sent to " + user.getEmail());
                            navigateToLogin();
                        } else {
                            showToast("Failed to send verification email.");
                        }
                    });
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}