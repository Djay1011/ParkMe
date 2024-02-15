package com.example.parkme;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        /*fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);*/
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        enableMyLocation();
        loadParkingSpots(); // Load and display parking spots
    }



    private void loadParkingSpots() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parkingSpots").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                // Handle the error
                return;
            }
            if (snapshots != null && !snapshots.isEmpty()) {
                updateMapMarkers(snapshots.getDocuments());
            }
        });
    }

    private void updateMapMarkers(List<DocumentSnapshot> documents) {
        mMap.clear(); // Clear existing markers
        for (DocumentSnapshot document : documents) {
            ParkingSpot spot = document.toObject(ParkingSpot.class);
            if (spot != null) {
                addMarkerForParkingSpot(spot);
            }
        }
    }


    private void addMarkerForParkingSpot(ParkingSpot spot) {
        LatLng location = new LatLng(spot.getLatitude(), spot.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(spot.getName()).snippet("Tap to book"));
        marker.setTag(spot);
    }


    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                if (getActivity() == null) return; // Check if activity is still around

                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showLocationPermissionExplanation();
                } else {
                    informUserNavigateToSettings();
                }
            }
        }
    }

    private void showLocationPermissionExplanation() {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, "Location permission is needed to show your current location on the map.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant", v -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                    .show();
        }
    }

    private void informUserNavigateToSettings() {
        new AlertDialog.Builder(getContext())
                .setMessage("Location permission is needed for core functionality. Please enable it in the app settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    if (getActivity() == null) return; // Check if activity is still around
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getActivity().getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        ParkingSpot spot = (ParkingSpot) marker.getTag();
        if (spot != null) {
            showBottomSheetDialog(spot);
        }
    }

    private void showBottomSheetDialog(ParkingSpot spot) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.parkingspot_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView tvTitle = bottomSheetView.findViewById(R.id.bottom_sheet_title);
        TextView tvDetails = bottomSheetView.findViewById(R.id.bottom_sheet_details);
        ImageView imageView = bottomSheetView.findViewById(R.id.imageParkingSpot);
        TextView tvPrice = bottomSheetView.findViewById(R.id.tvPrice);
        TextView tvRating = bottomSheetView.findViewById(R.id.tvRating);
        Button btnBookNow = bottomSheetView.findViewById(R.id.bottom_sheet_book_now);

        tvTitle.setText(spot.getName());
        tvDetails.setText(spot.getDetails());
        tvPrice.setText("Price: $" + spot.getPrice() + "/hour");
        tvRating.setText("Rating: " + spot.getRating() + " ★");

        // Load image using a library like Glide or Picasso
        /*Glide.with(this).load(spot.getImageUrl()).into(imageView);*/

        btnBookNow.setOnClickListener(v -> {
            // Navigate to the booking page or handle booking logic
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
