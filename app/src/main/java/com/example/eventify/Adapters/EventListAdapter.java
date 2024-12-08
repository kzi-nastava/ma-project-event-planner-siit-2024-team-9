package com.example.eventify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.models.Event;
import com.example.eventify.R;

import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    private final Context context;
    private final List<Event> events;

    public EventListAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.eventTitle.setText(event.getName());
        holder.eventRating.setText("4.5");
        holder.eventLocation.setText(event.getLocation().getName());
        holder.eventPrice.setText("Price: $" + event.getMaxAttendees());
        holder.eventImage.setImageResource(R.drawable.dummy_event_image);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventRating, eventLocation, eventPrice;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventTitle = itemView.findViewById(R.id.event_title);
            eventRating = itemView.findViewById(R.id.event_rating);
            eventLocation = itemView.findViewById(R.id.event_location);
            eventPrice = itemView.findViewById(R.id.event_price);
            eventImage = itemView.findViewById(R.id.event_image);
        }
    }
}
