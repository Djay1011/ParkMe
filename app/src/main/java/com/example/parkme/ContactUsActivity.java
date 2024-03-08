package com.example.parkme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity that enables users to reach out to customer support or provide feedback through email.
 * Users fill in their personal information such as their name and email,
 * along with their message, and the application streamlines the process of sending it through an email platform.
 */
public class ContactUsActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        initializeViews();
        setupToolbar();
    }

    /**
     * Sets up the visual elements of the activity.
     */
    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> validateInputAndSendEmail());
    }

    /**
     * Organizes the toolbar and specifies how the back button should function.
     */
    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Confirms the user's input and, if it meets the criteria, proceeds to send an email.
     */
    private void validateInputAndSendEmail() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
        } else {
            sendEmail(email, name, message);
        }
    }

    /**
     * Generates a intent to send the email and initiates the email application.
     *
     * @param email   The email address entered by the user.
     * @param name    The name entered by the user.
     * @param message The message entered by the user.
     */
    private void sendEmail(String email, String name, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email}); // Recipient's email.
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us Message from " + name);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "There are no email applications installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
