package com.example.eventify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.events.Event;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventStatsEventAdapter extends RecyclerView.Adapter<EventStatsEventAdapter.EventStatsViewHolder> {

    public interface Listener {
        void onEventSelected(@NonNull Event event);
    }

    private final LayoutInflater inflater;
    private final Listener listener;
    private final List<Event> items = new ArrayList<>();
    private String selectedEventId;

    public EventStatsEventAdapter(Context context, Listener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setItems(@NonNull List<Event> events) {
        items.clear();
        items.addAll(events);
        notifyDataSetChanged();
    }

    public void setSelectedEventId(String selectedId) {
        this.selectedEventId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_event_stats, parent, false);
        return new EventStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventStatsViewHolder holder, int position) {
        Event event = items.get(position);
        holder.bind(event, event.getId() != null && event.getId().equals(selectedEventId));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class EventStatsViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView textName;
        private final TextView textDescription;
        private final TextView textAttendance;

        EventStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textName = itemView.findViewById(R.id.text_event_name);
            textDescription = itemView.findViewById(R.id.text_event_description);
            textAttendance = itemView.findViewById(R.id.text_event_attendance);
        }

        void bind(Event event, boolean isSelected) {
            textName.setText(event.getName());
            textDescription.setText(event.getDescription() != null ? event.getDescription() : "");
            String attendanceText = itemView.getResources().getString(
                    R.string.event_stats_attendance_format,
                    event.getAttendance(),
                    event.getMaxAttendees()
            );
            textAttendance.setText(attendanceText);

            int strokeColor = ContextCompat.getColor(itemView.getContext(),
                    isSelected ? R.color.orange : R.color.light_gray);
            card.setStrokeColor(strokeColor);

            itemView.setOnClickListener(v -> listener.onEventSelected(event));
        }
    }
}
