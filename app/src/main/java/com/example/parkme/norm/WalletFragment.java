package com.example.parkme.norm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.AddCardBottomSheetFragment;
import com.example.parkme.CardsAdapter;
import com.example.parkme.R;
import com.example.parkme.model.CardDetails;
import com.example.parkme.utils.FirebaseManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

/**
 * Fragment for managing and displaying saved payment cards in the user's wallet.
 */
public class WalletFragment extends Fragment {

    // RecyclerView for displaying the list of cards.
    private RecyclerView cardsRecyclerView;
    // Adapter for the RecyclerView to display card data.
    private CardsAdapter cardsAdapter;
    // Utility class for Firebase operations related to user cards.
    private FirebaseManager firebaseManager;
    // ProgressBar to indicate loading operations.
    private ProgressBar progressBar;
    // Button to trigger the addition of a new card.
    private MaterialButton addCardButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        // Initialize the FirebaseManager.
        firebaseManager = new FirebaseManager();
        // Reference to the ProgressBar in the layout.
        progressBar = view.findViewById(R.id.progressBar);

        // Set up the RecyclerView and the Add Card button.
        initializeRecyclerView(view);
        initializeAddCardButton(view);
        // Load existing card details from Firebase.
        loadCardDetails();

        return view;
    }

    /**
     * Sets up the RecyclerView using a LinearLayoutManager and a customized CardsAdapter.
     * @param view The parent view of the fragment.
     */
    private void initializeRecyclerView(View view) {
        cardsRecyclerView = view.findViewById(R.id.cardsRecyclerView);
        cardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cardsAdapter = new CardsAdapter(new ArrayList<>(), this::deleteCard);
        cardsRecyclerView.setAdapter(cardsAdapter);
    }


    /**
     * Sets up the Add Card button and assigns a click listener to it.
     * @param view The parent view of the fragment.
     */
    private void initializeAddCardButton(View view) {
        addCardButton = view.findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(v -> showAddCardDialog());
    }

    /**
     * A bottom sheet dialog is shown to add a new card.
     */
    private void showAddCardDialog() {
        AddCardBottomSheetFragment bottomSheet = new AddCardBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
    }

    /**
     * Retrieves the user's stored card information from Firebase.
     */
    private void loadCardDetails() {
        showLoading(true);
        firebaseManager.loadUserCardDetails(cards -> {
            showLoading(false);
            cardsAdapter.updateCardDetailsList(cards);
        }, error -> {
            showLoading(false);
            handleCardLoadingError(error);
        });
    }

    /**
     * Shows or hides the progress bar.
     * @param show Boolean indicating whether to show the progress bar.
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Begins the process of removing a designated card.
     * @param cardDetails The details of the card to delete.
     */
    private void deleteCard(CardDetails cardDetails) {
        firebaseManager.deleteUserCard(cardDetails, this::handleCardDeletionSuccess, this::handleCardDeletionFailure);
    }

    /**
     * Manages the deletion of a card and refreshes the list currently being displayed.
     */
    private void handleCardDeletionSuccess() {
        Toast.makeText(getContext(), "Card deleted successfully.", Toast.LENGTH_SHORT).show();
        loadCardDetails(); // Refresh the card list.
    }

    /**
     * Manages errors when attempting to delete a card, usually by showing a notification.
     * @param error The exception encountered during card deletion.
     */
    private void handleCardDeletionFailure(Exception error) {
        Toast.makeText(getContext(), "Failed to delete the card: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }

    /**
     * Manages errors that occur during the process of loading card information.
     * @param error The exception encountered during the loading process.
     */
    private void handleCardLoadingError(Exception error) {
        Toast.makeText(getContext(), "Error loading cards: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }
}
