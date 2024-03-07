package com.example.parkme.norm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parkme.R;
import com.example.parkme.ReceiptActivity;
import com.example.parkme.model.Bookings;
import com.example.parkme.HistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that shows a collection of reservations presented in a RecyclerView.
 * This fragment of code communicates with Firestore in order to retrieve and present booking information.
 */
public class BookingFragment extends Fragment {
    // UI components
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView, errorStateTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HistoryAdapter adapter;


    // A collection for storing information about bookings.
    private final List<Bookings> bookingList = new ArrayList<>();
    // Using a Firestore instance to perform operations on the database.
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Initialize UI components and set up RecyclerView
        initializeViews(view);
        setupRecyclerView();

        // Load the booking data from Firestore
        loadBookings();

        return view;
    }

    /**
     * Sets up the refresh listener and initializes the views.
     * Binds all the user interface elements to the appropriate views in the design.
     * @param view The inflated view for this fragment.
     */
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.BookingHistory);
        progressBar = view.findViewById(R.id.progressBarLoading);
        emptyStateTextView = view.findViewById(R.id.emptyState);
        errorStateTextView = view.findViewById(R.id.tvErrorState);
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        // Setup pull-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
    }

    /**
     * Retrieves the booking information from Firestore and refreshes the user interface.
     * It arranges the bookings in descending order based on their starting time.
     */
    private void loadBookings() {
        showLoading(true);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            firestore.collection("Bookings")
                    .whereEqualTo("userId", currentUserId)
                    .orderBy("startTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showLoading(false);
                        if (task.isSuccessful() && task.getResult() != null) {
                            updateBookingList(task.getResult());
                        } else {
                            Log.e("BookingFragment", "Error loading bookings", task.getException());
                            showErrorState(true);
                        }
                    });
        } else {
            showErrorState(true);
            Log.e("BookingFragment", "User not authenticated");
        }
    }

    /**
     * Refreshes the UI by updating the booking list and notifying the adapter.
     * @param querySnapshot The snapshot containing the fetched data from Firestore.
     */
    private void updateBookingList(QuerySnapshot querySnapshot) {
        bookingList.clear();
        if (querySnapshot != null) {
            querySnapshot.forEach(document -> {
                Bookings booking = document.toObject(Bookings.class);
                if (booking != null) {
                    booking.setBookingId(document.getId());
                    bookingList.add(booking);
                }
            });
            // Inform the adapter to update the UI with the changes.
            adapter.notifyDataSetChanged();
        }
        // Adjust the display of UI elements according to the current dataset.t
        updateUIBasedOnData();
    }

    /**
     * Initialize the RecyclerView with its adapter and layout manager.
     * Sets up the adapter with a click listener to initiate a new activity for the chosen booking.
     */
    private void setupRecyclerView() {
        adapter = new HistoryAdapter(bookingList, booking -> {
            Intent intent = new Intent(getActivity(), ReceiptActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    /**
     * Displays or hides the loading indicator.
     * @param show Boolean to determine whether to show the progress bar or not.
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Displays or hides the error state TextView.
     * @param show Boolean to determine whether to show the error state or not.
     */
    private void showErrorState(boolean show) {
        errorStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Updates the user interface according to the status of the booking list.
     * Display an empty state message when the list is empty.
     */
    private void updateUIBasedOnData() {
        if (bookingList.isEmpty()) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("No bookings found");
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }
}
