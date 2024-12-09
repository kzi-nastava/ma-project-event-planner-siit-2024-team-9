package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.adapters.EventListAdapter;
import com.example.eventify.adapters.SolutionListAdapter;
import com.example.eventify.models.Event;
import com.example.eventify.models.Solution;
import com.example.eventify.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExploreFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.explore_welcome_container, new WelcomeSearchFragment())
                .commit();

        setupRecyclerViews(view);

        return view;
    }

    private void setupRecyclerViews(View view) {
        RecyclerView topEventsRecyclerView = view.findViewById(R.id.event_recycler_view);
        topEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topEventsRecyclerView.setAdapter(new EventListAdapter(getContext(), generateDummyEvents()));

        RecyclerView allEventsRecyclerView = view.findViewById(R.id.all_event_recycler_view);
        allEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allEventsRecyclerView.setAdapter(new EventListAdapter(getContext(), generateDummyEvents()));

        RecyclerView topSolutionsRecyclerView = view.findViewById(R.id.top_solutions_recycler_view);
        topSolutionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topSolutionsRecyclerView.setAdapter(new SolutionListAdapter(getContext(), generateDummySolutions()));

        RecyclerView allSolutionsRecyclerView = view.findViewById(R.id.all_solutions_recycler_view);
        allSolutionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allSolutionsRecyclerView.setAdapter(new SolutionListAdapter(getContext(), generateDummySolutions()));
    }

    private List<Event> generateDummyEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event("1", "Concert", "Description", 100, null, new Date(), new Date(), 50, null));
        events.add(new Event("2", "Tech Meetup", "Description", 50, null, new Date(), new Date(), 30, null));
        return events;
    }

    private List<Solution> generateDummySolutions() {
        List<Solution> solutions = new ArrayList<>();
        /*
        ArrayList<EventType> types = new ArrayList<>();
        EventType type = new EventType("type", "event", true);
        types.add(type);
        solutions.add(new Solution(UUID.randomUUID(), null, "Solution 1", "Description", 100.0, 10.0, null, true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), null, "Solution 2", "Description", 200.0, 15.0, null, true, true, null, types));*/
        return solutions;
    }
}
