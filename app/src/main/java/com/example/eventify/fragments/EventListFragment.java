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
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventType;
import com.example.eventify.services.events.EventService;
import com.example.eventify.utils.RetrofitClient;
import android.widget.ImageButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.eventify.models.filters.EventFilterOptions;
import com.example.eventify.models.filters.EventFilterStatistics;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListFragment extends Fragment
        implements EventFilterDialogFragment.OnFiltersApplied {

    private RecyclerView eventRecyclerView;
    private EventListAdapter eventListAdapter;
    private ProgressBar loadingIndicator;

    private EventService eventService;
    private List<Event> events = new ArrayList<>();
    private int totalEvents = 0;
    private int pageSize = 5;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean showTop;

    private View topActionsContainer;
    private ImageButton btnFilter, btnSort;
    private TextInputEditText searchInput;

    private EventFilterOptions currentFilters = new EventFilterOptions();
    private boolean sortAscending = true;
    private String currentSortField = "name";

    private final String[] SORT_LABELS = {"Name", "Date", "Location", "Price"};
    private final String[] SORT_KEYS   = {"name", "date", "location", "price"};

    private EventFilterStatistics cachedStats = null;

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

        if (getArguments() != null) {
            showTop = getArguments().getBoolean("showTop");
        }

        eventRecyclerView = view.findViewById(R.id.event_recycler_view);
        loadingIndicator = view.findViewById(R.id.event_loading_indicator);

        topActionsContainer = view.findViewById(R.id.top_actions_container);
        searchInput = view.findViewById(R.id.search_input);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnSort = view.findViewById(R.id.btn_sort);

        if (!showTop) {
            topActionsContainer.setVisibility(View.VISIBLE);

            // Search
            searchInput.setOnEditorActionListener((v1, actionId, event) -> {
                currentFilters.search = String.valueOf(v1.getText());
                restartAndFetch();
                return true;
            });

            // Filter popup
            btnFilter.setOnClickListener(v12 -> openFilterDialog());

            // Sort
            btnSort.setOnClickListener(v -> {
                showSortFieldDialog();
            });

            btnSort.setOnLongClickListener(v -> {
                sortAscending = !sortAscending;
                updateSortIconTooltip();
                restartAndFetch();
                return true;
            });

        } else {
            topActionsContainer.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        eventRecyclerView.setLayoutManager(layoutManager);
        eventListAdapter = new EventListAdapter(getContext(), events);
        eventRecyclerView.setAdapter(eventListAdapter);

        eventService = RetrofitClient.getClient().create(EventService.class);



        fetchEvents();
        setupScrollListener();

        return view;
    }

    private void fetchEvents() {
        if (isLoading) return;
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
            java.util.Map<String, String> q = com.example.eventify.utils.EventQueryBuilder.fromFilters(currentFilters);

            eventService.filter(q, currentPage, pageSize, currentSortField, sortAscending)
                    .enqueue(new Callback<EventService.EventAllResponse>() {
                        @Override
                        public void onResponse(Call<EventService.EventAllResponse> call,
                                               Response<EventService.EventAllResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                events.addAll(response.body().content);
                                totalEvents = response.body().totalElements;
                                eventListAdapter.notifyDataSetChanged();
                            } else {
                                Log.e("EventListFragment", "Failed to load filtered events");
                            }
                            loadingIndicator.setVisibility(View.GONE);
                            eventRecyclerView.setVisibility(View.VISIBLE);
                            isLoading = false;
                        }

                        @Override
                        public void onFailure(Call<EventService.EventAllResponse> call, Throwable t) {
                            Log.e("EventListFragment", "Error loading filtered events", t);
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

    private void showSortFieldDialog() {
        int preselected = 0;
        for (int i = 0; i < SORT_KEYS.length; i++) {
            if (SORT_KEYS[i].equalsIgnoreCase(currentSortField)) {
                preselected = i;
                break;
            }
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_by)
                .setSingleChoiceItems(SORT_LABELS, preselected, (dialog, which) -> {
                    currentSortField = SORT_KEYS[which];
                })
                .setPositiveButton(R.string.apply, (d, w) -> {
                    restartAndFetch();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateSortIconTooltip() {
        btnSort.setContentDescription(getString(
                R.string.sort_direction_now, sortAscending ? getString(R.string.ascending) : getString(R.string.descending)
        ));
    }


    private void openFilterDialog() {
        if (cachedStats == null) {
            cachedStats = new EventFilterStatistics();
            cachedStats.eventTypes = new java.util.ArrayList<>();
            cachedStats.locations  = new java.util.ArrayList<>();
            for (Event e : events) {
                EventType et = e.getEventType();
                String typeName = (et != null) ? et.getName() : null;
                if (typeName != null && !typeName.isEmpty() && !cachedStats.eventTypes.contains(typeName)) {
                    cachedStats.eventTypes.add(typeName);
                }
                if (e.getLocation() != null && e.getLocation().getName() != null &&
                        !cachedStats.locations.contains(e.getLocation().getName()))
                    cachedStats.locations.add(e.getLocation().getName());
                if (cachedStats.maxPrice == null || e.getPrice() > cachedStats.maxPrice)
                    cachedStats.maxPrice = e.getPrice();
            }
            if (cachedStats.maxPrice == null) cachedStats.maxPrice = 0.0;
        }

        EventFilterDialogFragment dialog = EventFilterDialogFragment.newInstance(cachedStats, currentFilters);
        dialog.show(getChildFragmentManager(), "EventFilterDialog");
    }

    private void restartAndFetch() {
        currentPage = 0;
        events.clear();
        eventListAdapter.notifyDataSetChanged();
        fetchEvents();
    }

    private List<Event> applyClientFilters(List<Event> source, EventFilterOptions f) {
        List<Event> out = new ArrayList<>();
        for (Event e : source) {
            if (f.search != null && !f.search.isEmpty()) {
                String q = f.search.toLowerCase();
                if (!(e.getName().toLowerCase().contains(q) ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(q)))) {
                    continue;
                }
            }
            if (f.startDate != null && e.getEventStart().before(f.startDate)) continue;
            if (f.endDate != null && e.getEventEnd().after(f.endDate)) { /* ok */ }

            if (f.maxAttendees != null && f.maxAttendees > 0 && e.getMaxAttendees() > f.maxAttendees) continue;
            if (f.attendance != null && f.attendance > 0 && e.getAttendance() < f.attendance) continue;

            if (!f.eventTypes.isEmpty()) {
                com.example.eventify.models.events.EventType et = e.getEventType();
                String typeName = (et != null) ? et.getName() : null;
                if (typeName == null || !f.eventTypes.contains(typeName)) continue;
            }
            if (!f.locations.isEmpty()) {
                String loc = e.getLocation() != null ? e.getLocation().getName() : null;
                if (loc == null || !f.locations.contains(loc)) continue;
            }
            if (f.maxPrice != null && f.maxPrice > 0 && e.getPrice() > f.maxPrice) continue;

            out.add(e);
        }

        out.sort((a,b) -> {
            int cmp;
            switch (currentSortField) {
                case "price": cmp = Double.compare(a.getPrice(), b.getPrice()); break;
                case "date":  cmp = a.getEventStart().compareTo(b.getEventStart()); break;
                default:      cmp = a.getName().compareToIgnoreCase(b.getName());
            }
            return sortAscending ? cmp : -cmp;
        });

        return out;
    }

    @Override
    public void onApplied(EventFilterOptions filters) {
        this.currentFilters = filters;
        restartAndFetch();
    }


}
