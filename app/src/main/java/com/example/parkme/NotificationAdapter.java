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

/**
 * A RecyclerView adapter designed to showcase notifications.
 * Manages the binding between notification data and the view, and also handles item click events.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notifications> notificationList;
    private final OnNotificationClickListener listener;

    /**
     * An interface designed to manage the handling of clicks on notification items.
     */
    public interface OnNotificationClickListener {
        void onNotificationClicked(Notifications notification);
    }

    /**
     * Constructor for the NotificationAdapter.
     *
     * @param notificationList A list of Notifications to be displayed.
     * @param listener         Listener for notification click events.
     */
    public NotificationAdapter(List<Notifications> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    /**
     * updates the RecyclerView by updating the adapter's dataset.
     *
     * @param notificationList A new list of Notifications to be displayed.
     */
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

    /**
     * A holder for notification items within the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, timestamp;
        View readIndicator;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView The view of the notification item.
         * @param listener Listener for click events on the notification item.
         */
        public ViewHolder(View itemView, OnNotificationClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            message = itemView.findViewById(R.id.notificationMessage);
            timestamp = itemView.findViewById(R.id.notificationTimestamp);
            readIndicator = itemView.findViewById(R.id.readIndicator);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClicked(notificationList.get(position));
                }
            });
        }

        /**
         * Binds the notification data with the view components.
         *
         * @param notification The notification item to be displayed.
         */
        public void bind(Notifications notification) {
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());
            timestamp.setText(DateFormat.getDateTimeInstance().format(notification.getTimestamp().toDate()));
            readIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);
        }
    }
}
