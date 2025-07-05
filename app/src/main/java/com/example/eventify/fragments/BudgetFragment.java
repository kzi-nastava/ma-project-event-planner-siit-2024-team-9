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
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BudgetFragment extends Fragment implements CategoryListAdapter.OnCategoryClickListener, ItemListAdapter.OnBudgetUpdatedListener {

    public BudgetFragment() {}

    Budget budget;
    Event event;
    String eventName;
    FragmentBudgetBinding binding;
    ArrayList<SolutionCategory> categories = new ArrayList<>();
    ArrayList<SolutionCategory> selected = new ArrayList<>();
    SolutionCategoryService categoryService = RetrofitClient.getClient().create(SolutionCategoryService.class);
    ItemListAdapter adapter;
    UserSession userSession;

    BudgetService service = RetrofitClient.getClient().create(BudgetService.class);
    ArrayList<BudgetItem> items;

    public static BudgetFragment newInstance(String eventName) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString("eventName", eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (eventName != null) {
            getEventAndBudget();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        if (eventName != null) {
            getEventAndBudget();
        }
        return binding.getRoot();
    }

    private void getEventAndBudget() {
        EventService eventService = RetrofitClient.getClient().create(EventService.class);
        // Get event by name using the backend getByName endpoint
        eventService.getByName(eventName).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    event = response.body();
                    getBudgetByEvent();
                } else {
                    // Handle error - event not found
                    if (binding != null) {
                        // You could show an error message or empty state
                    }
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                // Handle network error
                if (binding != null) {
                    // You could show an error message or retry option
                }
            }
        });
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

    private void getItems() {
        binding.budgetItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        items = new ArrayList<>(budget.getItems());
        adapter = new ItemListAdapter(requireContext(), items, getParentFragmentManager(), budget, BudgetFragment.this);
        binding.budgetItemsRecycler.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(SolutionCategory category) {
        if (!selected.contains(category))
            selected.add(category);
        getCategories(selected);
        BudgetItem item = new BudgetItem("",category,0, new HashSet<>());
        items.add(item);
        binding.budgetItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        ItemListAdapter adapter = new ItemListAdapter(requireContext(), items, requireActivity().getSupportFragmentManager(), budget, BudgetFragment.this);
        binding.budgetItemsRecycler.setAdapter(adapter);
    }

    private void getBudgetByEvent() {
        EventService service = RetrofitClient.getClient().create(EventService.class);
        service.getBudget(event.getId()).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                budget = response.body();
                categoriesReset(false);
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {

            }
        });
    }

    private void categoriesReset(boolean deletedItem) {
        binding.setBudget(budget);
        categories.clear();
        for (BudgetItem item: budget.getItems()) {
            categories.add(item.getCategory());
            if (!deletedItem)
                selected.add(item.getCategory());
        }
        getCategories(categories);
        getItems();
    }

    private void getBudget(boolean deletedItem) {
        service.get(UUID.fromString(budget.getId())).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                budget = response.body();
                categoriesReset(deletedItem);
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBudgetUpdated(boolean deletedItem) {
        getBudget(deletedItem);
    }
}