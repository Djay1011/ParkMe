package com.example.parkme;

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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<Bookings> bookingList;

    public HistoryAdapter(List<Bookings> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        Bookings booking = bookingList.get(position);

        // Format the date for display
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        holder.parkingSpotIdView.setText(booking.getParkingSpotId());
        holder.dateView.setText(dateFormat.format(booking.getStartTime())); // Assuming startTime has the date
        holder.totalPriceView.setText(String.format(Locale.getDefault(), "£%.2f", booking.getTotalPrice())); // Format the price to two decimal places
        holder.statusView.setText(booking.getStatus());

        switch (booking.getStatus().toUpperCase()) {
            case "UPCOMING":
                holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorUpcoming));
                break;
            case "INPROGRESS":
                holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorInProgress));
                break;
            case "COMPLETED":
                holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorCompleted));
                break;
            case "CANCELLATION":
                holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorCancellation));
                break;
            default:
                // Default color if status is not recognized
                holder.statusView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

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
