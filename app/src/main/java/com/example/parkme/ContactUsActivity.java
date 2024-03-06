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

public class ContactUsActivity extends AppCompatActivity {

    TextInputEditText nameEditText, emailEditText, messageEditText;
    Button sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract the text from TextInputEditText
                String name = nameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String message = messageEditText.getText().toString().trim();

                // Check if the fields are not empty
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(message)) {
                    sendEmail(email, name, message);
                } else {
                    // Show a message if any field is empty
                    Toast.makeText(ContactUsActivity.this, "Please fill all the fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmail(String email, String name, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email}); // Recipient's email
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us Message from " + name);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        // Attempt to launch the email application
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Notify the user if no email app is installed
            Toast.makeText(ContactUsActivity.this, "There are no email applications installed.", Toast.LENGTH_SHORT).show();
        }
    }
}