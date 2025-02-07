package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.R;
import com.example.eventify.adapters.CategoryListAdapter;
import com.example.eventify.adapters.ItemListAdapter;
import com.example.eventify.databinding.FragmentBudgetBinding;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.BudgetItem;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetFragment extends Fragment implements CategoryListAdapter.OnCategoryClickListener {

    public BudgetFragment() {}

    Budget budget;
    Event event;
    FragmentBudgetBinding binding;
    ArrayList<SolutionCategory> categories = new ArrayList<>();
    ArrayList<SolutionCategory> selected = new ArrayList<>();
    SolutionCategoryService categoryService = RetrofitClient.getClient().create(SolutionCategoryService.class);
    ArrayList<BudgetItem> items = new ArrayList<>();

    public static BudgetFragment newInstance(Event event) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putParcelable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = getArguments().getParcelable("event");
            budget = event.getBudget();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBudgetBinding.inflate(inflater, container, false);

        getCategories(categories);

        return binding.getRoot();
    }

    private void getCategories(ArrayList<SolutionCategory> selected) {
        StringBuilder ids = new StringBuilder();
        for (SolutionCategory category: selected)
            ids.append(category.getId()+",");
        categoryService.getSpecific(ids.toString(), event.getEventType().getId().toString()).enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                categories.clear();
                categories = (ArrayList<SolutionCategory>) response.body();
                binding.categoriesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
                CategoryListAdapter adapter = new CategoryListAdapter(requireContext(), categories, BudgetFragment.this);
                binding.categoriesRecycler.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onCategoryClick(SolutionCategory category) {
        if (!selected.contains(category))
            selected.add(category);
        getCategories(selected);
        BudgetItem item = new BudgetItem("",category,0, new HashSet<>());
        items.add(item);
        binding.budgetItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        ItemListAdapter adapter = new ItemListAdapter(requireContext(), items, getParentFragmentManager());
        binding.budgetItemsRecycler.setAdapter(adapter);
    }
}