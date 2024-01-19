package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private TextView forgotPassword, signUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            // User is already logged in, navigate to the main activity
            navigateToMainActivity();
        }
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        forgotPassword = findViewById(R.id.forgotPassword);
        signUpText = findViewById(R.id.signUp);

        findViewById(R.id.loginButton).setOnClickListener(view -> loginUser());
        findViewById(R.id.signUp).setOnClickListener(view -> goToSignUp());

        findViewById(R.id.forgotPassword).setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {
        // TODO: Implement password reset functionality
    }

    private void goToSignUp() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                navigateToMainActivity();
            }else {
                String message = "Authentication failed.";
                if (task.getException()!=null){
                    message = task.getException().getMessage();
                }
                Toast.makeText(LoginActivity.this,message,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        // TODO: Replace MainActivity.class with the actual class of your main activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}