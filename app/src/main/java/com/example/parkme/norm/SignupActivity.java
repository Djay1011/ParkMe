package com.example.parkme.norm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkme.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    // Firebase Authentication and Firestore instances for handling sign-up and data storage.
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    // EditText fields for user input.
    private EditText fullNameInput, emailInput, phoneInput, passwordInput, retypePasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);  // Set the XML layout for the activity.

        // Starts up the Firebase services and set up the user interface components.
        initializeFirebase();
        initializeViews();
    }

    // Starts Firebase Authentication and Firestore.
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    // Setup the user interface and attach event listeners.
    private void initializeViews() {
        // Bind UI elements to their respective IDs.
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        retypePasswordInput = findViewById(R.id.retypePasswordInput);

        // Event listener for the registration button.
        findViewById(R.id.signUpButton).setOnClickListener(view -> attemptRegistration());
        // Listener for switching to the login activity.
        findViewById(R.id.signIn).setOnClickListener(view -> navigateToLogin());
    }

    // Manages user registration process
    private void attemptRegistration() {
        if (!validateInputs()) {
            // Exit if inputs are not valid.
            return;
        }
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Proceed with the user registration.
        createUserAccount(email, password);
    }

    // Validates user input before registration.
    private boolean validateInputs() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String retypePassword = retypePasswordInput.getText().toString().trim();

        // Validate email, phone, and password formats.
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

    // Creates a user account with Firebase Authentication.
    private void createUserAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User is successfully registered and signed in, proceed with email verification and Firestore update.
                        sendEmailVerification();
                        addUserToFirestore();
                    } else {
                        // Shows a message to the user regarding the unsuccessful registration process.
                        showToast("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    // Registers the newly signed-up user in the Firestore database.
    private void addUserToFirestore() {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullNameInput.getText().toString().trim());
        user.put("email", emailInput.getText().toString().trim());
        user.put("phone", phoneInput.getText().toString().trim());

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firestore.collection("user").document(firebaseUser.getUid()).set(user)
                    .addOnSuccessListener(aVoid -> showToast("User added to Firestore"))
                    .addOnFailureListener(e -> showToast("Error adding user to Firestore: " + e.getMessage()));
        }
    }

    // Sends an email confirmation to the user's provided email address.
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

    // Returns back to the login page once the registration process is complete.
    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // Verifies if the password adheres to the specified guidelines.
    private boolean isValidPassword(String password) {
        return password.length() >= 6 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    // Presents a short message in the style of a toast.
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
