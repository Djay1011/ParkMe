package com.example.parkme.norm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkme.R;
import com.example.parkme.model.Vehicle;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class VehicleInfoActivity extends AppCompatActivity {

    private TextInputEditText editTextVehicleReg;
    private TextView textViewPlateNumber;
    private Button buttonAddVehicle;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private ImageView plateIcon, deleteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_info);

        editTextVehicleReg = findViewById(R.id.editTextVehicleReg);
        textViewPlateNumber = findViewById(R.id.textViewPlateNumber);
        buttonAddVehicle = findViewById(R.id.buttonAddVehicle); // Use the class member, not a local variable
        plateIcon = findViewById(R.id.plateIcon);
        deleteIcon = findViewById(R.id.deleteIcon);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        buttonAddVehicle.setEnabled(false);
        editTextVehicleReg.setEnabled(false);

        buttonAddVehicle.setOnClickListener(view -> addVehicle());
        deleteIcon.setOnClickListener(view -> deleteVehiclePlate());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        displayVehiclePlate();
    }

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
                                    break; // Assuming only one vehicle document is present
                                }
                            }
                            if (!plateExists) {
                                // No plate number, enable button and field
                                buttonAddVehicle.setEnabled(true);
                                editTextVehicleReg.setEnabled(true);
                                plateIcon.setVisibility(View.GONE);
                                deleteIcon.setVisibility(View.GONE);
                            }
                        } else {
                            textViewPlateNumber.setText("No Plate Number Added");
                            plateIcon.setVisibility(View.GONE);
                            deleteIcon.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(VehicleInfoActivity.this, "Error fetching vehicle info.", Toast.LENGTH_SHORT).show());
        } else {
            textViewPlateNumber.setText("User not signed in");
            plateIcon.setVisibility(View.GONE);
            deleteIcon.setVisibility(View.GONE);
        }
    }

    private void addVehicle() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String plateNumber = editTextVehicleReg.getText().toString().trim();

            if (plateNumber.matches("^[A-Z]{2}[0-9]{2} [A-Z]{3}$")) {
                firestore.collection("user").document(userId)
                        .collection("vehicles").add(new Vehicle(plateNumber))
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(VehicleInfoActivity.this, "Vehicle added successfully!", Toast.LENGTH_SHORT).show();
                            // Refresh the display after successful addition
                            displayVehiclePlate();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(VehicleInfoActivity.this, "Error adding vehicle", Toast.LENGTH_SHORT).show();
                        });
            } else {
                editTextVehicleReg.setError("Invalid UK plate format");
            }
        } else {
            Toast.makeText(this, "You must be signed in to add a vehicle", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteVehiclePlate() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            firestore.collection("user").document(userId).collection("vehicles")
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    // Delete the document
                                    firestore.collection("user").document(userId)
                                            .collection("vehicles").document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(VehicleInfoActivity.this, "Vehicle plate deleted successfully.", Toast.LENGTH_SHORT).show();
                                                // Reset UI elements after successful deletion
                                                buttonAddVehicle.setEnabled(true);
                                                editTextVehicleReg.setEnabled(true);
                                                editTextVehicleReg.setText("");
                                                textViewPlateNumber.setText("");
                                                plateIcon.setVisibility(View.GONE);
                                                deleteIcon.setVisibility(View.GONE);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(VehicleInfoActivity.this, "Error deleting vehicle plate.", Toast.LENGTH_SHORT).show());
                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(VehicleInfoActivity.this, "Error finding vehicle to delete.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(VehicleInfoActivity.this, "User not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Check if MoreFragment is in the back stack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed(); // This will finish the current activity
        }
    }
}