package com.example.eventify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.fragments.EventDetailsFragment;
import com.example.eventify.models.events.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CalendarEventAdapter extends RecyclerView.Adapter<CalendarEventAdapter.EventViewHolder> {

    private static final String TAG = "CalendarEventAdapter";
    private final Context context;
    private final List<Event> events;
    private final FragmentManager fragmentManager;
    private final android.app.Dialog dialog;
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public CalendarEventAdapter(Context context, List<Event> events, FragmentManager fragmentManager, android.app.Dialog dialog) {
        this.context = context;
        this.events = events;
        this.fragmentManager = fragmentManager;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        
        holder.eventTitle.setText(event.getName());
        
        // Format time
        String eventTime = "Time not available";
        if (event.getEventStart() != null && event.getEventEnd() != null) {
            eventTime = timeFormatter.format(event.getEventStart()) + " - " + timeFormatter.format(event.getEventEnd());
        } else if (event.getEventStart() != null) {
            eventTime = timeFormatter.format(event.getEventStart());
        }
        holder.eventTime.setText(eventTime);
        
        // Format location
        String location = "Location not available";
        if (event.getLocation() != null && event.getLocation().getName() != null) {
            location = event.getLocation().getName();
        }
        holder.eventLocation.setText(location);
        
            // Set click listener
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "Calendar event card clicked for: " + event.getName());
                try {
                    // Close the dialog first
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    
                    EventDetailsFragment eventDetailsFragment = EventDetailsFragment.newInstance(event);
                    fragmentManager.beginTransaction()
                            .replace(R.id.home_container, eventDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                    Log.d(TAG, "Navigation to EventDetailsFragment initiated from calendar dialog");
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating to EventDetailsFragment from calendar dialog", e);
                }
            });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle, eventTime, eventLocation;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.textViewEventTitle);
            eventTime = itemView.findViewById(R.id.textViewEventTime);
            eventLocation = itemView.findViewById(R.id.textViewEventLocation);
        }
    }
}
