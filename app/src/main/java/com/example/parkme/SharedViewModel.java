package com.example.parkme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkme.model.CardDetails;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<CardDetails> selectedCard = new MutableLiveData<>();

    public void selectCard(CardDetails card) {
        selectedCard.setValue(card);
    }

    public LiveData<CardDetails> getSelectedCard() {
        return selectedCard;
    }
}