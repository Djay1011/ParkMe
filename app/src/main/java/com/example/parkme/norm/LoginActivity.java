package com.example.parkme.norm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parkme.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    // FirebaseAuth instance to handle user authentication.
    private FirebaseAuth mAuth;

    // EditText fields for user input.
    private EditText emailInput, passwordInput;

    // Counter for failed biometric attempts.
    private int biometricFailedAttempts = 0;

    // Components for biometric authentication.
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Inflate the layout for this activity.

        mAuth = FirebaseAuth.getInstance(); // Initialize the Firebase authentication instance.

        // If the user is already authenticated, direct them to the main activity.
        if (mAuth.getCurrentUser() != null) {
            navigateToMainActivity();
        }

        // Set up user interface components and event handlers.
        initializeViews();
        // Prepare biometric authentication, enhancing user experience with secure and quick access.
        setupBiometricAuthentication();
    }

    // Initialize UI components and attach event listeners.
    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Configure listeners for interactive elements.
        findViewById(R.id.forgotPassword).setOnClickListener(view -> resetPassword());
        findViewById(R.id.signUp).setOnClickListener(view -> goToSignUp());
        findViewById(R.id.loginButton).setOnClickListener(view -> loginUser());
        findViewById(R.id.biometricLoginButton).setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }

    // Ensure the device supports biometric authentication and prepare it if available.
    private void setupBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        // Assess the device's ability to facilitate biometric verification.
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Enables the biometric prompt if it is available.
                initBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Notify the user if biometric authentication is not accessible.
                showToast("Biometric authentication not available");
                break;
        }
    }

    // This Set up the biometric prompt with personalized configurations for user engagement.
    private void initBiometricPrompt() {
        // Create the authentication callback functions for biometric prompts.
        biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                //  provide feedback on authentication errors, except for user cancellations.
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    showToast("Authentication Error: " + errString);
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // After successfully verifying your identity, user may proceed to the main event.
                navigateToMainActivity();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Increment the failed attempts counter and provide feedback.
                biometricFailedAttempts++;
                if (biometricFailedAttempts >= 3) {
                    // closes the program following repeated unsuccessful tries..
                    showToast("Too many failed attempts. Closing application.");
                    finishAndRemoveTask();
                } else {
                    // Notify the user about the unsuccessful try and the number of attempts left.
                    showToast("Authentication Failed. Attempt " + biometricFailedAttempts + "/3");
                }
            }
        });

        // Create biometric prompt with custom title, subtitle, and negative button.
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for My App")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();
    }

    // Allows password reset for users who have forgotten their password.
    private void resetPassword() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            // Ask the user to input their email address if it is not already provided.
            showToast("Please enter your email to reset your password.");
            return;
        }
        // Uses Firebase Authentication to send an email requesting a password reset.
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Notify the user to look in their inbox for instructions on resetting their account.
                showToast("Reset link sent to your email.");
            } else {
                // Notify the user if there was an issue sending the reset email.
                showToast("Failed to send reset email.");
            }
        });
    }

    // Navigate to the signup activity for new users.
    private void goToSignUp() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    // Manage user authentication using the given email and password.
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            // Ensure both email and password fields are filled before attempting login.
            showToast("Email and Password cannot be empty.");
            return;
        }

        // Authenticate the user with Firebase and handle login success or failure.
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Direct the user to the main activity upon successful login.
                navigateToMainActivity();
            } else {
                // Display an error message if authentication fails.
                showToast(task.getException() != null ? task.getException().getMessage() : "Authentication failed.");
            }
        });
    }

    // Move  to the main activity  and remove all previous activities from the stack.
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Utility method to display toast messages.
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
