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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.AddCardBottomSheetFragment;
import com.example.parkme.BookingProcess;
import com.example.parkme.CardsAdapter;
import com.example.parkme.ParkingSpot;
import com.example.parkme.R;
import com.example.parkme.model.CardDetails;
import com.example.parkme.utils.FirebaseManager;

import java.util.ArrayList;

public class WalletFragment extends Fragment {

    private RecyclerView cardsRecyclerView;
    private CardsAdapter cardsAdapter;
    private FirebaseManager firebaseManager;
    private ProgressBar progressBar;
    private CardDetails preferredCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        firebaseManager = new FirebaseManager();

        progressBar = view.findViewById(R.id.progressBar);
        initializeRecyclerView(view);
        initializeAddCardButton(view);
        loadCardDetails();

        return view;
    }

    private void initializeRecyclerView(View view) {
        cardsRecyclerView = view.findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cardsAdapter = new CardsAdapter(new ArrayList<>(), this::deleteCard);
        cardsRecyclerView.setAdapter(cardsAdapter);
    }

    private void initializeAddCardButton(View view) {
        ImageView addCardButton = view.findViewById(R.id.iconAddCard);
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
            if (!cards.isEmpty()) {
                preferredCard = cards.get(0);
            }
        }, error -> {
            showLoading(false);  // Hide loading on failure
            handleCardLoadingError(error);
        });
    }

    public void openBookingProcess(ParkingSpot parkingSpot) {
        BookingProcess bookingFragment = BookingProcess.newInstance(parkingSpot, preferredCard);
        bookingFragment.show(getParentFragmentManager(), "booking");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void handleCardLoadingError(Exception error) {
        // Handle the error, such as displaying a message to the user
    }

    private void deleteCard(CardDetails cardDetails) {
        firebaseManager.deleteUserCard(cardDetails,
                () -> handleCardDeletionSuccess(),
                error -> handleCardDeletionFailure(error));
    }

    private void handleCardDeletionSuccess() {
        // Handle successful deletion, such as updating UI or notifying the user
    }

    private void handleCardDeletionFailure(Exception error) {
        // Handle deletion failure, such as displaying an error message
    }

    // Additional methods as needed
}
