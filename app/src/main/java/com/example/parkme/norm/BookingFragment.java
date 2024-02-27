package com.example.parkme.norm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.parkme.model.Bookings; // Ensure this is the correct import for your Bookings class
import com.example.parkme.HistoryAdapter;
import com.example.parkme.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView, errorStateTextView;
    private HistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Bookings> bookingList;

    // Firebase Firestore instance
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        recyclerView = view.findViewById(R.id.BookingHistory);
        progressBar = view.findViewById(R.id.progressBarLoading);
        emptyStateTextView = view.findViewById(R.id.emptyState);
        errorStateTextView = view.findViewById(R.id.tvErrorState);
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        firestore = FirebaseFirestore.getInstance();

        setupRecyclerView();
        loadBookings();

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);

        return view;
    }

    private void loadBookings() {
        showLoading(true);
        errorStateTextView.setVisibility(View.GONE);

        firestore.collection("Bookings")
                .orderBy("startTime") // Consider adjusting if your booking logic has changed
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            querySnapshot.forEach(doc -> {
                                Bookings booking = doc.toObject(Bookings.class);
                                bookingList.add(booking);
                            });
                            adapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        showLoading(false);
                        updateUIBasedOnData();
                    } else {
                        showErrorState(true);
                        swipeRefreshLayout.setRefreshing(false);
                        showLoading(false);
                    }
                });
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new HistoryAdapter(bookingList); // Make sure HistoryAdapter accepts List<Bookings>
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorState(boolean show) {
        errorStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateUIBasedOnData() {
        emptyStateTextView.setVisibility(bookingList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
