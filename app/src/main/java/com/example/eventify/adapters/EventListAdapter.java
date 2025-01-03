package com.example.eventify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventify.models.events.Event;
import com.example.eventify.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

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
        String eventTime = dateFormatter.format(event.getEventStart()) + " | " +
                timeFormatter.format(event.getEventStart()) + " - " +
                timeFormatter.format(event.getEventEnd());
        holder.eventDateTime.setText(eventTime);
        holder.eventDescription.setText(event.getDescription());
        holder.eventLocation.setText(event.getLocation().getName());
        if (event.getPrice() == 0) {
            holder.eventPrice.setText(R.string.free);
        } else {
            holder.eventPrice.setText(context.getString(R.string.dollar_sign) + event.getPrice());
        }
        Glide.with(context)
                .load(event.getImage())
                .placeholder(R.drawable.dummy_event_image)
                .error(R.drawable.dummy_event_image)
                .into(holder.eventImage);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventDateTime, eventLocation, eventPrice, eventDescription;
        ImageView eventImage;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            eventTitle = itemView.findViewById(R.id.event_title);
            eventDateTime = itemView.findViewById(R.id.event_date_time);
            eventDescription = itemView.findViewById(R.id.event_description);
            eventLocation = itemView.findViewById(R.id.event_location);
            eventPrice = itemView.findViewById(R.id.event_price);
            eventImage = itemView.findViewById(R.id.event_image);
        }
    }
}
