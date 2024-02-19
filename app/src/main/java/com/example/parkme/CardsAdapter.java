package com.example.parkme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.model.CardDetails;

import java.util.ArrayList;
import java.util.List;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {
    private List<CardDetails> cardDetailsList;

    public interface OnCardDeleteListener {
        void onCardDelete(CardDetails cardDetails);
    }

    private final OnCardDeleteListener deleteListener;

    public CardsAdapter(List<CardDetails> cardDetailsList, OnCardDeleteListener deleteListener) {
        this.cardDetailsList = cardDetailsList != null ? cardDetailsList : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    public void updateCardDetailsList(List<CardDetails> newCardDetailsList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CardDetailsDiffCallback(this.cardDetailsList, newCardDetailsList));
        this.cardDetailsList.clear();
        this.cardDetailsList.addAll(newCardDetailsList);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view, deleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        CardDetails cardDetails = cardDetailsList.get(position);
        holder.bind(cardDetails);
    }

    @Override
    public int getItemCount() {
        return cardDetailsList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCardDetails;
        ImageView imageViewDeleteCard;
        CardDetails cardDetails;

        CardViewHolder(View itemView, OnCardDeleteListener deleteListener) {
            super(itemView);
            textViewCardDetails = itemView.findViewById(R.id.textViewCardDetails);
            imageViewDeleteCard = itemView.findViewById(R.id.imageViewDeleteCard);

            imageViewDeleteCard.setOnClickListener(view -> {
                if (deleteListener != null && cardDetails != null) {
                    deleteListener.onCardDelete(cardDetails);
                }
            });
        }

        void bind(CardDetails cardDetails) {
            this.cardDetails = cardDetails;
            textViewCardDetails.setText("•••• " + cardDetails.getLast4Digits() +
                    " Exp: " + cardDetails.getExpMonth() + "/" + cardDetails.getExpYear());
        }
    }

    static class CardDetailsDiffCallback extends DiffUtil.Callback {

        private final List<CardDetails> oldList;
        private final List<CardDetails> newList;

        CardDetailsDiffCallback(List<CardDetails> oldList, List<CardDetails> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getCardId().equals(newList.get(newItemPosition).getCardId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
