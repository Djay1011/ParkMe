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

/**
 * The SignupActivity oversees the process of user registration, which includes checking and validating the input.
 * The process of verifying a user's identity using Firebase and saving their information in Firestore.
 */
public class SignupActivity extends AppCompatActivity {

    //Firebase instances can be used for authentication and performing operations on the Firestore database.
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    // EditText fields for user inputs.
    private EditText fullNameInput, emailInput, phoneInput, passwordInput, retypePasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Set the layout for the activity.

        initializeFirebase(); // Start Firebase Auth and Firestore.

        initializeViews();    // Setup UI elements and event handlers.
    }

    /**
     * Starts up the Firebase Authentication and Firestore services.
     */
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Sets up event listeners and initializes views.
     */
    private void initializeViews() {
        // Binding UI elements to properties.
        fullNameInput = findViewById(R.id.fullNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        retypePasswordInput = findViewById(R.id.retypePasswordInput);

        // Setting click listeners for the registration and sign-in redirection.
        findViewById(R.id.signUpButton).setOnClickListener(view -> attemptRegistration());
        findViewById(R.id.signIn).setOnClickListener(view -> navigateToLogin());
    }

    /**
     * Attempts o sign up the user using the given login details.
     */
    private void attemptRegistration() {
        if (validateInputs()) { // Proceed only if the inputs are valid.
            createUserAccount(emailInput.getText().toString().trim(),
                    passwordInput.getText().toString().trim());
        }
    }

    /**
     * Checks the input fields to ensure they are in the proper formats.
     *
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        // Extracting text from EditText inputs and trimming whitespace.
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String retypePassword = retypePasswordInput.getText().toString().trim();

        // Email validation.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address.");
            return false;
        }

        // Phone number validation.
        if (!Patterns.PHONE.matcher(phone).matches()) {
            showToast("Please enter a valid phone number.");
            return false;
        }

        // Password validation.
        if (!isValidPassword(password)) {
            showToast("Invalid password format. Password must be at least 6 characters long and include an uppercase letter, a lowercase letter, a number, and a special character.");
            return false;
        }

        // Check if passwords match.
        if (!password.equals(retypePassword)) {
            showToast("Passwords do not match.");
            return false;
        }

        return true;
    }

    /**
     * Creates a new user account by utilizing Firebase Authentication.
     *
     * @param email    The user's email.
     * @param password The user's chosen password.
     */
    private void createUserAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        sendEmailVerification();
                        addUserToFirestore();
                    } else {
                        showToast("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Saves the information of the logged-in user to Firestore.
     */
    private void addUserToFirestore() {
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullNameInput.getText().toString().trim());
        user.put("email", emailInput.getText().toString().trim());
        user.put("phone", phoneInput.getText().toString().trim());

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firestore.collection("users").document(firebaseUser.getUid()).set(user)
                    .addOnSuccessListener(aVoid -> showToast("User added to Firestore"))
                    .addOnFailureListener(e -> showToast("Error adding user to Firestore: " + e.getMessage()));
        }
    }

    /**
     * Sends an email confirmation to the user's email address.
     */
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

    /**
     * Navigates back to the LoginActivity.
     */
    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Verifies whether the given password meets the security requirements of the application.
     *
     * @param password The password to validate.
     * @return true if the password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 6 && password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") && password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    /**
     * Displays a short toast message.
     *
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
