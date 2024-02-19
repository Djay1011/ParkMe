package com.example.parkme;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.model.CardDetails;
import com.example.parkme.utils.FirebaseManager;

import java.util.ArrayList;

public class WalletFragment extends Fragment {

    private RecyclerView cardsRecyclerView;
    private CardsAdapter cardsAdapter;
    private FirebaseManager firebaseManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);

        firebaseManager = new FirebaseManager();

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
        firebaseManager.loadUserCardDetails(cards -> cardsAdapter.updateCardDetailsList(cards),
                error -> handleCardLoadingError(error));
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
