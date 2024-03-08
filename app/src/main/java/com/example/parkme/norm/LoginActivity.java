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

/**
 * The LoginActivity allows users to authenticate using their email/password or biometric data.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Firebase Authentication instance.
    private EditText emailInput, passwordInput; // Fields where users can enter their email and password.

    private int biometricFailedAttempts = 0; // Count of unsuccessful attempts at biometric authentication.

    // Components of BiometricPrompt used for managing biometric authentication.
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Placing the view content based on the layout file.


        mAuth = FirebaseAuth.getInstance(); // Initializing Firebase Authentication instance.

        if (mAuth.getCurrentUser() != null) {
            navigateToMainActivity(); // Go to the MainActivity if the user is already signed in.

        }

        initializeViews(); // Initializing UI components.
        setupBiometricAuthentication(); // Setting up biometric authentication if available.
    }

    /**
     * Start the user interface elements and establish event listeners.
     */
    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        findViewById(R.id.forgotPassword).setOnClickListener(view -> resetPassword());
        findViewById(R.id.signUp).setOnClickListener(view -> goToSignUp());
        findViewById(R.id.loginButton).setOnClickListener(view -> loginUser());
        findViewById(R.id.biometricLoginButton).setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }

    /**
     * Enable biometric authentication if the device is compatible with it.
     */
    private void setupBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                initBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast("Biometric authentication not available");
                break;
        }
    }

    /**
     * Initialize the biometric prompt.
     */
    private void initBiometricPrompt() {
        biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            showToast("Authentication Error: " + errString);
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        navigateToMainActivity();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        biometricFailedAttempts++;
                        if (biometricFailedAttempts >= 3) {
                            showToast("Too many failed attempts. Closing application.");
                            finishAndRemoveTask();
                        } else {
                            showToast("Authentication Failed. Attempt " + biometricFailedAttempts + "/3");
                        }
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for My App")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();
    }

    /**
     * Assist users in resetting their password if they have forgotten it.
     */
    private void resetPassword() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            showToast("Please enter your email to reset your password.");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Reset link sent to your email.");
                    } else {
                        showToast("Failed to send reset email.");
                    }
                });
    }

    /**
     * Navigate to the SignUp activity.
     */
    private void goToSignUp() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    /**
     * Verify the user's identity by using their email and password.
     */
    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Email and Password cannot be empty.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToMainActivity();
                    } else {
                        showToast(task.getException() != null ? task.getException().getMessage() : "Authentication failed.");
                    }
                });
    }

    /**
     * Go to the MainActivity and remove all the activities from the stack.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Display a Toast message.
     *
     * @param message The message to display.
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
