package com.example.eventify.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.adapters.EventListAdapter;
import com.example.eventify.models.Event;
import com.example.eventify.services.EventService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListFragment extends Fragment {

    private RecyclerView eventRecyclerView;
    private EventListAdapter eventListAdapter;
    private ProgressBar loadingIndicator;

    private EventService eventService;
    private List<Event> events = new ArrayList<>();
    private int totalEvents = 0;
    private int pageSize = 5;
    private int currentPage = 0;
    private boolean isLoading = false;

    private boolean showTop; // True for Top 5 events, False for All events

    public static EventListFragment newInstance(boolean showTop) {
        EventListFragment fragment = new EventListFragment();
        Bundle args = new Bundle();
        args.putBoolean("showTop", showTop);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        eventRecyclerView = view.findViewById(R.id.event_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        eventRecyclerView.setLayoutManager(layoutManager);
        eventListAdapter = new EventListAdapter(getContext(), events);
        eventRecyclerView.setAdapter(eventListAdapter);

        eventService = RetrofitClient.getClient(EventService.BASE_URL).create(EventService.class);

        if (getArguments() != null) {
            showTop = getArguments().getBoolean("showTop");
        }

        fetchEvents();
        setupScrollListener();

        return view;
    }

    private void fetchEvents() {
        if (isLoading) return; // Spreči višestruke pozive tokom učitavanja
        isLoading = true;

        loadingIndicator.setVisibility(currentPage == 0 ? View.VISIBLE : View.GONE);
        eventRecyclerView.setVisibility(currentPage == 0 ? View.GONE : View.VISIBLE);

        if (showTop) {
            eventService.getTop().enqueue(new Callback<List<Event>>() {
                @Override
                public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        events.clear();
                        events.addAll(response.body());
                        eventListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("EventListFragment", "Failed to load top events");
                    }
                    loadingIndicator.setVisibility(View.GONE);
                    eventRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<List<Event>> call, Throwable t) {
                    Log.e("EventListFragment", "Error loading top events", t);
                    loadingIndicator.setVisibility(View.GONE);
                    eventRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }
            });
        } else {
            eventService.getAllPaginated(currentPage, pageSize, "name", true).enqueue(new Callback<EventService.EventAllResponse>() {
                @Override
                public void onResponse(Call<EventService.EventAllResponse> call, Response<EventService.EventAllResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        events.addAll(response.body().content);
                        totalEvents = response.body().totalElements;
                        eventListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("EventListFragment", "Failed to load all events");
                    }
                    loadingIndicator.setVisibility(View.GONE);
                    eventRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<EventService.EventAllResponse> call, Throwable t) {
                    Log.e("EventListFragment", "Error loading all events", t);
                    loadingIndicator.setVisibility(View.GONE);
                    eventRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }
            });
        }
    }

    private void setupScrollListener() {
        eventRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !showTop &&
                        layoutManager.findLastCompletelyVisibleItemPosition() == events.size() - 1 &&
                        events.size() < totalEvents) {
                    currentPage++;
                    fetchEvents();
                }
            }
        });
    }
}
