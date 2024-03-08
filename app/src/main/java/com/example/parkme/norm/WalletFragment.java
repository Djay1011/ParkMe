package com.example.parkme.norm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.AddCardBottomSheetFragment;
import com.example.parkme.BookingProcess;
import com.example.parkme.CardsAdapter;
import com.example.parkme.ParkingSpot;
import com.example.parkme.R;
import com.example.parkme.SharedViewModel;
import com.example.parkme.model.CardDetails;
import com.example.parkme.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class WalletFragment extends Fragment {

    private RecyclerView cardsRecyclerView;
    private CardsAdapter cardsAdapter;
    private FirebaseManager firebaseManager;
    private ProgressBar progressBar;
    private SharedViewModel sharedViewModel;
    private MaterialButton addCardButton;
    private ParkingSpot parkingSpot; // This should be set or retrieved from somewhere

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        firebaseManager = new FirebaseManager();
        progressBar = view.findViewById(R.id.progressBar);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        initializeRecyclerView(view);
        initializeAddCardButton(view);
        loadCardDetails();

        return view;
    }

    private void initializeRecyclerView(View view) {
        cardsRecyclerView = view.findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cardsAdapter = new CardsAdapter(new ArrayList<>(), this::onCardSelected, this::deleteCard);
        cardsRecyclerView.setAdapter(cardsAdapter);
    }

    private void onCardSelected(CardDetails card) {
        sharedViewModel.selectCard(card);
        openBookingProcessWithCard(card);
    }

    private void openBookingProcessWithCard(CardDetails card) {
        BookingProcess bookingFragment = BookingProcess.newInstance(parkingSpot, card);
        bookingFragment.show(getParentFragmentManager(), "BookingProcess");
    }

    private void initializeAddCardButton(View view) {
        addCardButton = view.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(v -> showAddCardDialog());
    }

    private void showAddCardDialog() {
        AddCardBottomSheetFragment bottomSheet = new AddCardBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    private void loadCardDetails() {
        showLoading(true);
        firebaseManager.loadUserCardDetails(cards -> {
            showLoading(false);  // Hide loading on success
            cardsAdapter.updateCardDetailsList(cards);
        }, error -> {
            showLoading(false);  // Hide loading on failure
            handleCardLoadingError(error);
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void handleCardLoadingError(Exception error) {
        // Handle the error
    }

    private void deleteCard(CardDetails cardDetails) {
        firebaseManager.deleteUserCard(cardDetails,
                this::handleCardDeletionSuccess,
                this::handleCardDeletionFailure);
    }

    private void handleCardDeletionSuccess() {
        // Handle successful deletion
    }

    private void handleCardDeletionFailure(Exception error) {
        // Handle deletion failure
    }

}