package com.example.parkme.norm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkme.R;
import com.example.parkme.model.Vehicle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The activity to display and manage vehicle information.
 * Users have the ability to either add or remove their vehicle's license plate number.
 */
public class VehicleInfoActivity extends AppCompatActivity {

    // Declaration of UI elements and Firebase instances.
    private TextInputEditText editTextVehicleReg;
    private TextView textViewPlateNumber;
    private Button buttonAddVehicle;
    private ImageView plateIcon, deleteIcon;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info); // Sets the view of the content to our layout.

        // Initializing UI components.
        editTextVehicleReg = findViewById(R.id.editTextVehicleReg);
        textViewPlateNumber = findViewById(R.id.textViewPlateNumber);
        buttonAddVehicle = findViewById(R.id.buttonAddVehicle);
        plateIcon = findViewById(R.id.plateIcon);
        deleteIcon = findViewById(R.id.deleteIcon);

        // Initializing Firebase instances.
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Initially, set up the state by disabling the button and editText until the user's plate status is determined.
        buttonAddVehicle.setEnabled(false);
        editTextVehicleReg.setEnabled(false);

        // Event listeners for adding and deleting a vehicle.
        buttonAddVehicle.setOnClickListener(view -> addVehicle());
        deleteIcon.setOnClickListener(view -> deleteVehiclePlate());

        // Setup for the custom toolbar.
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Display the user's vehicle plate if it exists.
        displayVehiclePlate();
    }

    /**
     * Retrieves and shows the car's license plate number if it is associated with the current user.
     */
    private void displayVehiclePlate() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            firestore.collection("user").document(userId).collection("vehicles").limit(1).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            boolean plateExists = false;
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String plateNumber = document.getString("plateNumber");
                                    textViewPlateNumber.setText(plateNumber);
                                    plateIcon.setVisibility(View.VISIBLE);
                                    deleteIcon.setVisibility(View.VISIBLE);
                                    plateExists = true;
                                    break; // Each user is assumed to have one vehicle.

                                }
                            }
                            if (!plateExists) {
                                enableVehicleAddition();
                            }
                        } else {
                            showErrorState("No Plate Number Added");
                        }
                    })
                    .addOnFailureListener(e -> showErrorState("Error fetching vehicle info."));
        } else {
            showErrorState("User not signed in");
        }
    }

    /**
     * Enables the UI for adding a vehicle plate.
     */
    private void enableVehicleAddition() {
        buttonAddVehicle.setEnabled(true);
        editTextVehicleReg.setEnabled(true);
        plateIcon.setVisibility(View.GONE);
        deleteIcon.setVisibility(View.GONE);
    }

    /**
     * Shows a message indicating an error state.
     * @param message The error message to display.
     */
    private void showErrorState(String message) {
        textViewPlateNumber.setText(message);
        plateIcon.setVisibility(View.GONE);
        deleteIcon.setVisibility(View.GONE);
    }

    /**
     * Saves a car's license plate number to the Firestore database, linked to the user's current document.
     */
    private void addVehicle() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String plateNumber = editTextVehicleReg.getText().toString().trim();

            // Validate plate number format here (e.g., UK plate format).
            if (plateNumber.matches("^[A-Z]{2}[0-9]{2} [A-Z]{3}$")) {
                Vehicle newVehicle = new Vehicle(plateNumber);
                firestore.collection("user").document(userId)
                        .collection("vehicles").add(newVehicle)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(VehicleInfoActivity.this, "Vehicle added successfully!", Toast.LENGTH_SHORT).show();
                            displayVehiclePlate(); // Refresh display.
                        })
                        .addOnFailureListener(e -> Toast.makeText(VehicleInfoActivity.this, "Error adding vehicle", Toast.LENGTH_SHORT).show());
            } else {
                editTextVehicleReg.setError("Invalid UK plate format");
            }
        } else {
            Toast.makeText(this, "You must be signed in to add a vehicle", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This action removes the vehicle plate of the logged-in user from Firestore.
     */
    private void deleteVehiclePlate() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Fetch and delete the first vehicle document found.
            firestore.collection("user").document(userId).collection("vehicles")
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            task.getResult().forEach(document -> {
                                deleteDocument(userId, document.getId());
                            });
                        } else {
                            Toast.makeText(VehicleInfoActivity.this, "Error finding vehicle to delete.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(VehicleInfoActivity.this, "User not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A method that assists in deleting a document based on the user's ID and the document's ID.
     * @param userId The user's ID.
     * @param documentId The document's ID to be deleted.
     */
    private void deleteDocument(String userId, String documentId) {
        firestore.collection("user").document(userId)
                .collection("vehicles").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(VehicleInfoActivity.this, "Vehicle plate deleted successfully.", Toast.LENGTH_SHORT).show();
                    displayVehiclePlate(); // Reset UI elements after deletion.
                })
                .addOnFailureListener(e -> Toast.makeText(VehicleInfoActivity.this, "Error deleting vehicle plate.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        // Override to handle back navigation specifically if there are fragments in the stack.
        super.onBackPressed();
    }
}
