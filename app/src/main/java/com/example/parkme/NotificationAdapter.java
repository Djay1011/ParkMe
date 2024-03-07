package com.example.parkme;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkme.model.Notifications;

import java.text.DateFormat;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notifications> notificationList;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClicked(Notifications notification);
    }

    public NotificationAdapter(List<Notifications> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    public void setNotifications(List<Notifications> notificationList) {
        this.notificationList = notificationList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_items, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifications notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;
        View readIndicator;

        public ViewHolder(View itemView, OnNotificationClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            timestamp = itemView.findViewById(R.id.notificationTimestamp);
            readIndicator = itemView.findViewById(R.id.readIndicator);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Notifications notification = notificationList.get(position);
                    listener.onNotificationClicked(notification);
                }
            });
        }

        public void bind(Notifications notification) {
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());
            timestamp.setText(DateFormat.getDateTimeInstance().format(notification.getTimestamp().toDate()));
            readIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        }
    }
}
