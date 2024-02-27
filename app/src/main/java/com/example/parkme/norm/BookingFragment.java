package com.example.parkme.norm;

import android.content.Intent;
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

import com.example.parkme.R;
import com.example.parkme.ReceiptActivity;
import com.example.parkme.model.Bookings;
import com.example.parkme.HistoryAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
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

    private final List<Bookings> bookingList = new ArrayList<>();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);
        initializeViews(view);
        setupRecyclerView();
        loadBookings();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.BookingHistory);
        progressBar = view.findViewById(R.id.progressBarLoading);
        emptyStateTextView = view.findViewById(R.id.emptyState);
        errorStateTextView = view.findViewById(R.id.tvErrorState);
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
    }

    private void loadBookings() {
        showLoading(true);
        firestore.collection("Bookings")
                .orderBy("startTime")
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showLoading(false);
                    if (task.isSuccessful()) {
                        updateBookingList(task.getResult());
                    } else {
                        showErrorState(true);
                    }
                });
    }

    private void updateBookingList(QuerySnapshot querySnapshot) {
        bookingList.clear();
        if (querySnapshot != null) {
            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                Bookings booking = documentSnapshot.toObject(Bookings.class);
                if (booking != null) {
                    booking.setBookingId(documentSnapshot.getId()); // Assuming a setBookingId method exists
                }
                bookingList.add(booking);
            }
            adapter.notifyDataSetChanged();
        }
        updateUIBasedOnData();
    }
    private void setupRecyclerView() {
        adapter = new HistoryAdapter(bookingList, booking -> {
            Intent intent = new Intent(getActivity(), ReceiptActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            startActivity(intent);
        });
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