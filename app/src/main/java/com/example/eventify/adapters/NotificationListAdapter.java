package com.example.eventify.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.others.Notification;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.VH> {

    public interface Listener {
        void onItemClick(Notification n, int position);
        void onDeleteClick(Notification n, int position);
    }

    private final List<Notification> data;
    private final Listener listener;
    private final DateFormat dfShort = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public NotificationListAdapter(List<Notification> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = data.get(position);
        h.title.setText(n.getTitle());
        h.message.setText(n.getMessage());
        h.date.setText(n.getCreatedAt() != null ? dfShort.format(n.getCreatedAt()) : "");

        h.unreadDot.setVisibility(n.isRead() ? View.GONE : View.VISIBLE);
        h.title.setEllipsize(TextUtils.TruncateAt.END);
        h.message.setEllipsize(TextUtils.TruncateAt.END);

        h.itemView.setAlpha(n.isRead() ? 0.75f : 1f);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(n, h.getBindingAdapterPosition());
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(n, h.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return data != null ? data.size() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        View unreadDot;
        TextView title, message, date;
        ImageButton btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            unreadDot = itemView.findViewById(R.id.unread_dot);
            title = itemView.findViewById(R.id.tv_title);
            message = itemView.findViewById(R.id.tv_message);
            date = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
