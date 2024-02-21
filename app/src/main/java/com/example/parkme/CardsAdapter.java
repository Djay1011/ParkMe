package com.example.parkme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.model.CardDetails;
import java.util.List;
import java.util.ArrayList;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {
    public interface OnCardSelectListener {
        void onCardSelect(CardDetails card);
    }

    public interface OnCardDeleteListener {
        void onCardDelete(CardDetails card);
    }

    private List<CardDetails> cardDetailsList;
    private OnCardSelectListener selectListener;
    private OnCardDeleteListener deleteListener;

    public CardsAdapter(List<CardDetails> cardDetailsList,
                        OnCardSelectListener selectListener,
                        OnCardDeleteListener deleteListener) {
        this.cardDetailsList = cardDetailsList != null ? cardDetailsList : new ArrayList<>();
        this.selectListener = selectListener;
        this.deleteListener = deleteListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CardDetails card = cardDetailsList.get(position);
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        return cardDetailsList.size();
    }

    public void updateCardDetailsList(List<CardDetails> newCardDetailsList) {
        cardDetailsList.clear();
        cardDetailsList.addAll(newCardDetailsList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardInfoTextView;
        ImageView deleteButton; // Changed to ImageView

        ViewHolder(View itemView) {
            super(itemView);
            cardInfoTextView = itemView.findViewById(R.id.textViewCardDetails);
            deleteButton = itemView.findViewById(R.id.imageViewDeleteCard); // Make sure this ID corresponds to an ImageView

            itemView.setOnClickListener(v -> {
                if (selectListener != null) {
                    selectListener.onCardSelect(cardDetailsList.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onCardDelete(cardDetailsList.get(getAdapterPosition()));
                }
            });
        }

        void bind(CardDetails card) {
            cardInfoTextView.setText("Card ending in " + card.getLast4Digits());
        }
    }
}