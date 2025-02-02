package com.example.eventify.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentDiscoverBinding;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {


    private List<Event> allEvents = new ArrayList<>();
    private List<Solution> allSolutions = new ArrayList<>();

    FragmentDiscoverBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentDiscoverBinding.inflate(inflater, container, false);

        initRecyclerViews();
//        setupDummyData();
//        setupPaginators(view);

//        setTopEvents();
//        setAllEvents();
//        setTopSolutions();
//        setAllSolutions();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        initRecyclerViews();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initRecyclerViews() {
        // Top 5 Events Fragment
        getChildFragmentManager().beginTransaction()
                .replace(binding.topEventsContainer.getId(), EventListFragment.newInstance(true))
                .commit();

        // All Events Fragment
        getChildFragmentManager().beginTransaction()
                .replace(binding.allEventsContainer.getId(), EventListFragment.newInstance(false))
                .commit();



        // Top 5 Solutions Fragment
        getChildFragmentManager().beginTransaction()
                .replace(binding.topSolutionsContainer.getId(), SolutionListFragment.newInstance(true))
                .commit();

        // All Solutions Fragment
        getChildFragmentManager().beginTransaction()
                .replace(binding.allSolutionsContainer.getId(), SolutionListFragment.newInstance(false))
                .commit();


//        topSolutionsRecyclerView = view.findViewById(R.id.top_solutions_recycler_view);
//        topSolutionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//
//        allSolutionsRecyclerView = view.findViewById(R.id.all_solutions_recycler_view);
//        allSolutionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

//    private void setupDummyData() {
//        for (int i = 1; i <= 50; i++) {
//            allEvents.add(new Event("e" + i, "Event " + i, "Description " + i, "dummy_image.png", 20, PrivacyType.PUBLIC, null, null, 10, new EventType("Type " + i, "Description " + i, true), new Location("City " + i, "Street " + i, 0.0, 0.0), 20.0*i, Set.of()));
//            allSolutions.add(new Solution("Solution " + i, null, null, Status.ACCEPTED, "Description " + i, i * 10, i, null, true, true));
//        }
//    }

//    private void setupPaginators(View view) {
//        MaterialButton eventsPrev = view.findViewById(R.id.events_prev_page);
//        MaterialButton eventsNext = view.findViewById(R.id.events_next_page);
//        TextView eventsPageNumber = view.findViewById(R.id.events_page_number);
//
//        eventsPrev.setEnabled(eventsPage > 1);
//        eventsPrev.setOnClickListener(v -> {
//            if (eventsPage > 1) {
//                eventsPage--;
//                eventsPageNumber.setText(String.valueOf(eventsPage));
//                setAllEvents();
//                eventsPrev.setEnabled(eventsPage > 1);
//            }
//        });
//
//        eventsNext.setOnClickListener(v -> {
//            if ((eventsPage - 1) * pageSize + pageSize < allEvents.size()) {
//                eventsPage++;
//                eventsPageNumber.setText(String.valueOf(eventsPage));
//                setAllEvents();
//                eventsPrev.setEnabled(eventsPage > 1);
//            }
//        });
//    }


//    private void setTopEvents() {
//        List<Event> topEvents = allEvents.subList(0, Math.min(5, allEvents.size()));
//        topEventsRecyclerView.setAdapter(new EventListAdapter(getContext(), topEvents));
//    }
//
//    private void setAllEvents() {
//        int start = (eventsPage - 1) * pageSize;
//        int end = Math.min(start + pageSize, allEvents.size());
//        List<Event> paginatedEvents = allEvents.subList(start, end);
//        allEventsRecyclerView.setAdapter(new EventListAdapter(getContext(), paginatedEvents));
//    }

//    private void setTopSolutions() {
//        List<Solution> topSolutions = allSolutions.subList(0, Math.min(5, allSolutions.size()));
//        topSolutionsRecyclerView.setAdapter(new SolutionListAdapter(getContext(), topSolutions));
//    }
//
//    private void setAllSolutions() {
//        int start = (solutionsPage - 1) * pageSize;
//        int end = Math.min(start + pageSize, allSolutions.size());
//        List<Solution> paginatedSolutions = allSolutions.subList(start, end);
//        allSolutionsRecyclerView.setAdapter(new SolutionListAdapter(getContext(), paginatedSolutions));
//    }
}
