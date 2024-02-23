package com.example.parkme.norm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.parkme.BookingProcess;
import com.example.parkme.BookingProcessActivity;
import com.example.parkme.ParkingSpot;
import com.example.parkme.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initializeMapFragment();
        return view;
    }

    private void initializeMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        loadParkingSpots();
    }

    private void setupMap() {
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
    }

    private void loadParkingSpots() {
        FirebaseFirestore.getInstance().collection("ParkingSpot").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        task.getResult().forEach(document ->
                                addMarkerForParkingSpot(document.toObject(ParkingSpot.class)));
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void addMarkerForParkingSpot(ParkingSpot spot) {
        if (mMap == null || spot == null) {
            Log.w(TAG, "Map is not ready or Spot is null");
            return;
        }

        LatLng location = new LatLng(spot.getLatitude(), spot.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(spot.getName()).snippet("Tap to book"));
        if (marker != null) {
            marker.setTag(spot);
        } else {
            Log.w(TAG, "Marker could not be added on the map");
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            handlePermissionDenied();
        }
    }

    private void handlePermissionDenied() {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, "Location permission is needed to show your current location on the map.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant", v -> ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                    .show();
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof ParkingSpot) {
            showBottomSheetDialog((ParkingSpot) tag);
            moveCameraToSpot((ParkingSpot) tag);
        }
    }

    private void moveCameraToSpot(ParkingSpot spot) {
        LatLng spotLatLng = new LatLng(spot.getLatitude(), spot.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spotLatLng, DEFAULT_ZOOM));
    }

    private void showBottomSheetDialog(ParkingSpot spot) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.parkingspot_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        setupBottomSheetView(bottomSheetView, spot, bottomSheetDialog);
        bottomSheetDialog.show();
    }

    private void setupBottomSheetView(View bottomSheetView, ParkingSpot spot, BottomSheetDialog bottomSheetDialog) {
        TextView tvTitle = bottomSheetView.findViewById(R.id.bottom_sheet_title);
        tvTitle.setText(spot.getName());

        TextView tvDetails = bottomSheetView.findViewById(R.id.bottom_sheet_details);
        tvDetails.setText(spot.getDetails());

        ImageView iconCCTV = bottomSheetView.findViewById(R.id.iconCCTV);
        ImageView iconDisabledAccess = bottomSheetView.findViewById(R.id.iconDisabledAccess);
        ImageView iconElectricCharger = bottomSheetView.findViewById(R.id.iconElectricCharger);


        iconCCTV.setVisibility(spot.isHasCCTV() ? View.VISIBLE : View.GONE);
        iconDisabledAccess.setVisibility(spot.isHasDisabledAccess() ? View.VISIBLE : View.GONE);
        iconElectricCharger.setVisibility(spot.isHasElectricCharger() ? View.VISIBLE : View.GONE);


        TextView tvSizeType = bottomSheetView.findViewById(R.id.SizeType);
        tvSizeType.setText(spot.getSizeType());

        TextView tvPrice = bottomSheetView.findViewById(R.id.Price);
        tvPrice.setText("£" + spot.getPrice() + "/hour");

        Button btnBookNow = bottomSheetView.findViewById(R.id.bottom_sheet_book_now);
        btnBookNow.setOnClickListener(v -> {
            // Handle booking logic
            bottomSheetDialog.dismiss();
            showBookingBottomSheet(spot);
        });
    }

    private void showBookingBottomSheet(ParkingSpot spot) {
        Intent intent = new Intent(getActivity(), BookingProcessActivity.class);
        intent.putExtra("PARKING_SPOT", spot);
        startActivity(intent);
    }
}