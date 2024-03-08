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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

/**
 * A Fragment representing a map view, that enables users to search, filter, and engage with markers for parking spots.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // Tag employed for recording information, aiding in the process of troubleshooting.
    private static final String TAG = "MapFragment";
    // The standard level of zoom for the camera on the map.
    private static final float DEFAULT_ZOOM = 15;

    // The GoogleMap object is a representation of the map.
    private GoogleMap mMap;
    // Client ffor receiving updates on location.
    private FusedLocationProviderClient fusedLocationProviderClient;
    // The search interface for inputting location inquiries.
    private SearchView searchView;
    // Client for interacting with the Places API.
    private PlacesClient placesClient;
    // A window will appear to display search suggestions.
    private ListPopupWindow listPopupWindow;
    // The filters have been put on the parking spaces.
    private boolean isFilterDisabledAccessApplied = false;
    private boolean isFilterCCTVApplied = false;
    private boolean isFilterElectricChargingApplied = false;

    // List to hold all the markers added to the map.
    private List<Marker> allMarkers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Google Places API.
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());

        // Set up the map fragment, search view, and the filter button.
        initializeMapFragment();
        setupSearchView(view);
        setupListPopupWindow(view);
        ImageButton filterButton = view.findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> showFilterDialog());

        return view;
    }

    /**
     * Sets up a ListPopupWindow to display suggestions for autocompletion as the user enters text.
     */
    private void setupListPopupWindow(View view) {
        // Initialize and configure the list popup window for showing suggestions.
        listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(searchView); // Anchor to the search view.
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
        listPopupWindow.setOnItemClickListener((parent, view1, position, id) -> {
            // Handle click events on search suggestions.
            String selectedItem = (String) parent.getItemAtPosition(position);
            searchView.setQuery(selectedItem, true);
            listPopupWindow.dismiss();
        });
    }


    /**
     * Configures the search interface to react to user input and show suggestions for search results.
     *
     * @param view The current view of the fragment.
     */
    private void setupSearchView(View view) {
        // Configure the search view to handle search queries and suggestions.
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform the search when the user submits a query.
                searchForLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update the suggestions as the query text changes.
                if (!newText.isEmpty()) {
                    updateLiveSuggestions(newText);
                }
                return false;
            }
        });
    }

    /**
     * Utilizing the Google Places API to search for a location and subsequently updating the map with the obtained result.
     *
     * @param query The search query submitted by the user.
     */
    private void searchForLocation(String query) {
        // Create a request to retrieve autocomplete predictions from the Places API.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        // Fetch predictions asynchronously.
        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                // Log details and fetch place details for the first prediction.
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

    /**
     * Retrieves comprehensive data about a location through its unique identifier.
     *
     * @param placeId The place ID for which details are requested.
     */
    private void fetchPlaceDetails(String placeId) {
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        // Construct a request object, specifying the place ID and fields to be returned.
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            LatLng latLng = place.getLatLng();

            if (latLng != null) {
                // Update the map's camera position if the place has a LatLng.
                updateMapLocation(latLng);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    /**
     * Modifies the autocomplete suggestions displayed in the user interface according to the text entered by the user.
     *
     * @param newText The new text entered by the user in the search view.
     */
    private void updateLiveSuggestions(String newText) {
        // Generate autocomplete predictions based on the user's input.
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                .setQuery(newText)
                .build();

        placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener(response -> {
            // Update the UI with the received predictions.
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

    /**
     * Refreshes the user interface with the provided suggestions.
     *
     * @param suggestions The list of strings representing the suggestions to be displayed.
     */
    private void updateSuggestionsUI(List<String> suggestions) {
        // Display the suggestions in the list popup window.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, suggestions);
        listPopupWindow.setAdapter(adapter);

        if (!suggestions.isEmpty()) {
            listPopupWindow.show();
        } else {
            listPopupWindow.dismiss();
        }
    }

    private void updateMapLocation(LatLng latLng) {
        // Move the camera to the specified LatLng.
        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
    }


    /**
     * The map fragment is initialized in an asynchronous manner, with the map being set up once it is ready.
     */
    private void initializeMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Configure the map once it's ready to be used.
        mMap = googleMap;
        setupMap();
        loadParkingSpots();
        updateMapStyle();
    }


    /**
     * Applies the suitable map appearance depending on the current theme (dark/light mode).
     */
    private void updateMapStyle() {
        int nightModeFlags = requireContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                applyMapStyle(R.raw.dark_map);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                applyMapStyle(R.raw.light_map);
                break;
        }
    }

    /**
     * Applies a style to the map.
     *
     * @param styleResId The resource ID of the style to apply.
     */
    private void applyMapStyle(int styleResId) {
        // Try to apply the specified style to the map.
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), styleResId));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    /**
     * It selects the suitable map design according to the current theme (dark/light mode).
     */
    private void setupMap() {
        // Basic configuration for the map. Set listeners, enable zoom controls, etc.
        mMap.setOnInfoWindowClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        enableMyLocation();
    }

    /**
     * Retrieves parking spot data from Firestore and displays them as markers on the map.
     */
    private void loadParkingSpots() {
        FirebaseFirestore.getInstance().collection("ParkingSpot").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkingSpot spot = document.toObject(ParkingSpot.class);
                            addMarkerForParkingSpot(spot);  // Add markers without considering the filters
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    /**
     * Utilizes the filters chosen by the user to display the designated parking spaces on the map.
     *
     * @param filterDisabledAccess Whether to filter by disabled access.
     * @param filterCCTV Whether to filter by CCTV availability.
     * @param filterElectricCharging Whether to filter by electric charging availability.
     */
    private void applyFilters(boolean filterDisabledAccess, boolean filterCCTV, boolean filterElectricCharging) {
        for (Marker marker : allMarkers) {
            ParkingSpot spot = (ParkingSpot) marker.getTag();
            boolean isVisible = (!filterDisabledAccess || spot.isHasDisabledAccess()) &&
                    (!filterCCTV || spot.isHasCCTV()) &&
                    (!filterElectricCharging || spot.isHasElectricCharger());
            marker.setVisible(isVisible);
        }
    }

    /**
     * Displays a dialogue box for the user to select and apply filters to the map.
     */
    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.filter, null);
        builder.setView(view);

        CheckBox disabledAccess = view.findViewById(R.id.disabledAccess);
        CheckBox CCTV = view.findViewById(R.id.cctv);
        CheckBox electricCharging = view.findViewById(R.id.electricCharging);

        // Initialize checkboxes based on the current filter states.
        disabledAccess.setChecked(isFilterDisabledAccessApplied);
        CCTV.setChecked(isFilterCCTVApplied);
        electricCharging.setChecked(isFilterElectricChargingApplied);

        Button buttonApplyFilters = view.findViewById(R.id.button_apply_filters);
        AlertDialog dialog = builder.create();

        buttonApplyFilters.setOnClickListener(v -> {
            isFilterDisabledAccessApplied = disabledAccess.isChecked();
            isFilterCCTVApplied = CCTV.isChecked();
            isFilterElectricChargingApplied = electricCharging.isChecked();

            dialog.dismiss();
            applyFilters(isFilterDisabledAccessApplied, isFilterCCTVApplied, isFilterElectricChargingApplied);
        });

        dialog.show();
    }

    /**
     * Places a marker on the map to show the location of a parking spot.
     *
     * @param spot The ParkingSpot object containing the data to be marked.
     */
    private void addMarkerForParkingSpot(ParkingSpot spot) {
        // Add a custom marker for each parking spot on the map.
        if (mMap == null || spot == null || getContext() == null || !isAdded()) {
            Log.w(TAG, "Map is not ready, Spot is null, or Fragment is not added to an activity.");
            return;
        }

        LatLng location = new LatLng(spot.getLatitude(), spot.getLongitude());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View markerView = inflater.inflate(R.layout.map_marker, null);
        TextView textView = markerView.findViewById(R.id.marker_text);
        textView.setText("£" + spot.getPrice());

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                .title(spot.getName())
                .snippet("Tap to book"));
        if (marker != null) {
            marker.setTag(spot);
            allMarkers.add(marker); // Add the marker to the list.
        } else {
            Log.w(TAG, "Marker could not be added on the map");
        }
    }

    /**
     * Seeks permission to access the device's location and activates the My Location layer if permission is given.
     */
    private void enableMyLocation() {
        // Request permissions to enable the device's current location on the map.
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle the result of the permission request.
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            handlePermissionDenied();
        }
    }

    /**
     * This addresses the situation in which the user declines to grant permission for access to their location.
     */
    private void handlePermissionDenied() {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, "Location permission is needed to show your current location on the map.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant", v -> ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1))
                    .show();
        }
    }

    /**
     * Respond to clicks on the info windows of markers.
     *
     */
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof ParkingSpot) {
            showBottomSheetDialog((ParkingSpot) tag);
            moveCameraToSpot((ParkingSpot) tag);
        }
    }

    /**
     * Move the camera to the selected parking spot.
     *
     */
    private void moveCameraToSpot(ParkingSpot spot) {
        LatLng spotLatLng = new LatLng(spot.getLatitude(), spot.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spotLatLng, DEFAULT_ZOOM));
    }


    /**
     * Show a bottom sheet dialog with details about the parking spot.
     */
    private void showBottomSheetDialog(ParkingSpot spot) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireActivity());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.parkingspot_bottom_sheet, null);
        bottomSheetView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
        bottomSheetDialog.setContentView(bottomSheetView);

        setupBottomSheetView(bottomSheetView, spot, bottomSheetDialog);
        bottomSheetDialog.setOnDismissListener(dialog -> {
            // Custom animation for hiding the bottom sheet.
            bottomSheetView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
        });
        bottomSheetDialog.show();
    }

    /**
     * Configure the content of the bottom sheet dialog, displaying the parking spot's details.
     */
    private void setupBottomSheetView(View bottomSheetView, ParkingSpot spot, BottomSheetDialog bottomSheetDialog) {
        //
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

    /**
     * Start the booking process activity, passing the selected parking spot as an extra.
     */
    private void showBookingBottomSheet(ParkingSpot spot) {
        Intent intent = new Intent(getActivity(), BookingProcessActivity.class);
        intent.putExtra("PARKING_SPOT", spot);
        startActivity(intent);
    }
}
