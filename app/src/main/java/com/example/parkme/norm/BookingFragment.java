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

import com.example.parkme.Booking;
import com.example.parkme.HistoryAdapter;
import com.example.parkme.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BookingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookingFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView, errorStateTextView;
    private HistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Booking> bookingList;
    private DatabaseReference databaseReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        recyclerView = view.findViewById(R.id.BookingHistory);
        progressBar = view.findViewById(R.id.progressBarLoading);
        emptyStateTextView = view.findViewById(R.id.emptyState);
        errorStateTextView = view.findViewById(R.id.tvErrorState);
        swipeRefreshLayout = view.findViewById(R.id.refreshLayout);

        databaseReference = FirebaseDatabase.getInstance().getReference("bookings");

        setupRecyclerView();
        loadBookings();

        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);

        return view;
    }

    private void loadBookings() {
        showLoading(true);
        errorStateTextView.setVisibility(View.GONE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    bookingList.add(booking);
                }
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                updateUIBasedOnData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showErrorState(true);
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
            }
        });
    }


    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new HistoryAdapter(bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showErrorState(boolean show) {
        errorStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    };

    private void updateUIBasedOnData() {
        if (bookingList.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }

    private void showEmptyState(boolean show) {
        emptyStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
    }


}