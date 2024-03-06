package com.example.parkme.norm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.parkme.BookingProcess;
import com.example.parkme.BookingProcessActivity;
import com.example.parkme.ParkingSpot;
import com.example.parkme.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM = 15;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SearchView searchView;
    private PlacesClient placesClient;
    private ListPopupWindow listPopupWindow;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDLQyr8Wnp_uxpefltVDlRsLyRTRvb3oDY");
        }
        placesClient = Places.createClient(requireContext());

        initializeMapFragment();
        setupSearchView(view);
        setupListPopupWindow(view);
        
        return view;
    }

    private void setupListPopupWindow(View view) {
        listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(searchView);
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        listPopupWindow.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            searchView.setQuery(selectedItem, true);
            listPopupWindow.dismiss();
        });
    }

    private void setupSearchView(View view) {
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    updateLiveSuggestions(newText);
                }
                return false;
            }
        });
    }

    private void searchForLocation(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());

                fetchPlaceDetails(prediction.getPlaceId());
                break;
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    private void fetchPlaceDetails(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            LatLng latLng = place.getLatLng();

            if (latLng != null) {
                updateMapLocation(latLng);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    private void updateLiveSuggestions(String newText) {
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(newText)
                .build();

        placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener(response -> {
            List<String> suggestions = new ArrayList<>();
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                suggestions.add(prediction.getFullText(null).toString());
            }
            updateSuggestionsUI(suggestions);
        }).addOnFailureListener(exception -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Error getting place predictions: " + apiException.getStatusCode());
            }
        });
    }

    private void updateSuggestionsUI(List<String> suggestions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, suggestions);
        listPopupWindow.setAdapter(adapter);

        if (!suggestions.isEmpty()) {
            listPopupWindow.show();
        } else {
            listPopupWindow.dismiss();
        }
    }

    private void updateMapLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
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

        // Inflate the custom layout
        View markerView = LayoutInflater.from(getContext()).inflate(R.layout.map_marker, null);
        TextView textView = markerView.findViewById(R.id.marker_text);
        textView.setText("£" + spot.getPrice());

        // Convert the layout to a Bitmap
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        // Add the marker with the custom icon
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title(spot.getName())
                .snippet("Tap to book"));
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
        bottomSheetView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        bottomSheetDialog.setContentView(bottomSheetView);

        setupBottomSheetView(bottomSheetView, spot, bottomSheetDialog);
        bottomSheetDialog.setOnDismissListener(dialog -> {
            // Set custom animation for hiding the bottom sheet
            bottomSheetView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
        });
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
            bottomSheetDialog.dismiss();
            showBookingBottomSheet(spot);
        });

        ImageView dismissIcon = bottomSheetView.findViewById(R.id.remove);
        dismissIcon.setOnClickListener(v -> bottomSheetDialog.dismiss());

    }

    private void showBookingBottomSheet(ParkingSpot spot) {
        Intent intent = new Intent(getActivity(), BookingProcessActivity.class);
        intent.putExtra("PARKING_SPOT", spot);
        startActivity(intent);
    }
}