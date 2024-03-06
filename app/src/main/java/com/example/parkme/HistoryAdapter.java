package com.example.parkme;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.model.Bookings;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * An adapter for showing a list of recent and previous bookings in a RecyclerView.
 * Each booking is shown as a separate entry in the list.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<Bookings> bookingList;
    private final OnBookingClickListener listener;

    /**
     * An interface designed to manage and process clicks on booking requests.
     */
    public interface OnBookingClickListener {
        void onBookingClick(Bookings booking);
    }

    /**
     * Constructor for the HistoryAdapter.
     * @param bookingList List of bookings to be displayed.
     * @param listener Listener for booking click events.
     */
    public HistoryAdapter(List<Bookings> bookingList, OnBookingClickListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bookings booking = bookingList.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.parkingSpotIdView.setText(booking.getParkingSpotName());
        holder.dateView.setText(dateFormat.format(booking.getStartTime()));
        holder.totalPriceView.setText(String.format(Locale.getDefault(), "£%.2f", booking.getTotalPrice()));
        holder.statusView.setText(booking.getStatus());

        // Determine the status text color according to the booking status.
        updateStatusColor(holder, booking);

        // A click listener to trigger the launch of ReceiptActivity when an item is clicked.
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ReceiptActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            context.startActivity(intent);
        });
    }

    /**
     * Changes the color of the status text view to reflect the booking's current status.
     * @param holder The ViewHolder for the booking item.
     * @param booking The booking whose status is being updated.
     */
    private void updateStatusColor(ViewHolder holder, Bookings booking) {
        int colorRes;
        switch (booking.getStatus().toUpperCase()) {
            case "UPCOMING":
                colorRes = R.color.colorUpcoming;
                break;
            case "INPROGRESS":
                colorRes = R.color.colorInProgress;
                break;
            case "COMPLETED":
                colorRes = R.color.colorCompleted;
                break;
            case "CANCELLED":
                colorRes = R.color.colorCancellation;
                break;
            default:
                colorRes = android.R.color.black; // Set to the default color for an unknown status.

                break;
        }
        holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    /**
     * A class that holds views for the RecyclerView.
     * Manages the opinions regarding the booking of individual items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView parkingSpotIdView;
        TextView dateView;
        TextView totalPriceView;
        TextView statusView;

        public ViewHolder(View itemView) {
            super(itemView);
            parkingSpotIdView = itemView.findViewById(R.id.parkingSpotId);
            dateView = itemView.findViewById(R.id.date);
            totalPriceView = itemView.findViewById(R.id.totalPrice);
            statusView = itemView.findViewById(R.id.status);
        }
    }
}
