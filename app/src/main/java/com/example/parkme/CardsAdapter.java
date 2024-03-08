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

/**
 * An adapter class for RecyclerView to display a list of saved payment card details.
 */
public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder> {

    // Interface to handle the deletion of a card from the list.
    public interface OnCardDeleteListener {
        void onCardDelete(CardDetails card);
    }

    // List to hold the card details.
    private List<CardDetails> cardDetailsList;
    // Listener to handle delete action.
    private OnCardDeleteListener deleteListener;

    /**
     * Constructor for the CardsAdapter.
     *
     * @param cardDetailsList A list of CardDetails objects to be displayed.
     * @param deleteListener  A listener for handling the card deletion events.
     */
    public CardsAdapter(List<CardDetails> cardDetailsList, OnCardDeleteListener deleteListener) {
        this.cardDetailsList = cardDetailsList != null ? cardDetailsList : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind the card details to the view
        CardDetails card = cardDetailsList.get(position);
        holder.bind(card);
    }

    @Override
    public int getItemCount() {
        // Return the size of the dataset (cardDetailsList)
        return cardDetailsList.size();
    }

    /**
     * Updates the dataset of the adapter and refreshes the RecyclerView.
     *
     * @param newCardDetailsList A new list of CardDetails objects.
     */
    public void updateCardDetailsList(List<CardDetails> newCardDetailsList) {
        cardDetailsList.clear();
        cardDetailsList.addAll(newCardDetailsList);
        notifyDataSetChanged();
    }

    /**
     * The ViewHolder class holds a reference to the views for each individual data item.
     * sophisticated data elements may require multiple perspectives for each element, and
     * you grant the view holder access to all data item views.
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView cardInfoTextView;
        ImageView deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardInfoTextView = itemView.findViewById(R.id.textViewCardDetails);
            deleteButton = itemView.findViewById(R.id.imageViewDeleteCard);

            // Set a click listener on the delete button.
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onCardDelete(cardDetailsList.get(getAdapterPosition()));
                }
            });
        }

        /**
         * Binds the card details to the TextView and ImageView of the ViewHolder.
         *
         * @param card The CardDetails object containing the data to be displayed.
         */
        void bind(CardDetails card) {
            cardInfoTextView.setText("Card ending in " + card.getLast4Digits());
        }
    }
}
