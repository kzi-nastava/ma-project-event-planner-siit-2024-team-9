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
    SolutionCategoryService categoryService;
    ItemListAdapter adapter;
    UserSession userSession;

    BudgetService service;
    ArrayList<BudgetItem> items;
    boolean isEditMode = false;

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
        categoryService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(SolutionCategoryService.class);
        service = RetrofitClient.getClient(requireContext().getApplicationContext()).create(BudgetService.class);
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
        
        // Set up global edit/save button click listeners (like Angular)
        binding.globalEditButton.setOnClickListener(v -> editMode());
        binding.globalSaveButton.setOnClickListener(v -> save());
        
        if (eventName != null) {
            getEventAndBudget();
        }
        return binding.getRoot();
    }

    private void getEventAndBudget() {
        EventService eventService =RetrofitClient.getClient(requireContext()).create(EventService.class);
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
        
        // Ensure all items have proper IDs and unsaved state (like Angular app)
        for (BudgetItem item : items) {
            if (item.getId() == null) {
                // Set empty string for items without ID (like Angular app does)
                item.setId("");
            }
            // Mark existing items as saved (unsaved = false) like Angular
            item.setUnsaved(false);
        }
        
        adapter = new ItemListAdapter(requireContext(), items, getParentFragmentManager(), budget, BudgetFragment.this, isEditMode);
        binding.budgetItemsRecycler.setAdapter(adapter);
    }

    @Override
    public void onCategoryClick(SolutionCategory category) {
        if (!selected.contains(category))
            selected.add(category);
        getCategories(selected);
        
        // Enter edit mode when adding new category (like Angular app)
        isEditMode = true;
        
        // Create new budget item with empty string ID and unsaved=true (like Angular app)
        BudgetItem item = new BudgetItem("", category, 0, new HashSet<>());
        item.setUnsaved(true); // New items are unsaved by default
        items.add(item);
        
        binding.budgetItemsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ItemListAdapter(requireContext(), items, requireActivity().getSupportFragmentManager(), budget, BudgetFragment.this, isEditMode);
        binding.budgetItemsRecycler.setAdapter(adapter);
    }

    private void getBudgetByEvent() {
        EventService service =RetrofitClient.getClient(requireContext()).create(EventService.class);
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
        
        // Set event name
        if (event != null && event.getName() != null) {
            binding.eventName.setText(event.getName());
        } else if (eventName != null) {
            binding.eventName.setText(eventName);
        }
        
        categories.clear();
        for (BudgetItem item: budget.getItems()) {
            categories.add(item.getCategory());
            if (!deletedItem)
                selected.add(item.getCategory());
        }
        getCategories(categories);
        getItems();
        updateButtonVisibility();
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

    // Add methods to match Angular behavior
    public void save() {
        try {
            // Mark all unsaved items as saved
            if (items != null) {
                for (BudgetItem item : items) {
                    if (item != null && item.isUnsaved()) {
                        item.setUnsaved(false);
                    }
                }
            }
            
            isEditMode = false;
            updateBudget();
            updateButtonVisibility();
            
            // Update adapter to reflect the changes
            if (adapter != null) {
                adapter.setEditMode(isEditMode);
            }
        } catch (Exception e) {
            android.util.Log.e("BudgetFragment", "Error in save method", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Error saving budget", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void editMode() {
        isEditMode = true;
        // Mark all items as unsaved (like Angular)
        for (BudgetItem item : items) {
            item.setUnsaved(true);
        }
        if (adapter != null) {
            adapter.setEditMode(isEditMode);
        }
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (isEditMode) {
            binding.globalSaveButton.setVisibility(View.VISIBLE);
            binding.globalEditButton.setVisibility(View.GONE);
        } else {
            binding.globalSaveButton.setVisibility(View.GONE);
            binding.globalEditButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateBudget() {
        try {
            // Update budget with current items (like Angular updateBudget method)
            if (budget != null && items != null) {
                budget.setItems(new HashSet<>(items));
                service.update(UUID.fromString(budget.getId()), budget).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budget = response.body();
                    // Update the UI with the saved budget
                    binding.setBudget(budget);
                    
                    // Update the items list with the saved budget items
                    items.clear();
                    items.addAll(budget.getItems());
                    
                    // Mark all items as saved (unsaved = false)
                    for (BudgetItem item : items) {
                        item.setUnsaved(false);
                    }
                    
                    // Update the adapter on the main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                    
                    // Show success message
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Budget saved successfully", android.widget.Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Failed to save budget", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {
                // Handle network error
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(), "Network error while saving budget", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
            } else {
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(), "Budget or items are null", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            android.util.Log.e("BudgetFragment", "Error in updateBudget method", e);
            if (getContext() != null) {
                android.widget.Toast.makeText(getContext(), "Error updating budget", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshBudget() {
        service.get(UUID.fromString(budget.getId())).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                if (response.isSuccessful() && response.body() != null) {
                    budget = response.body();
                }
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {
                // Handle error
            }
        });
    }

    private void refreshItems() {
        service.getItems(UUID.fromString(budget.getId())).enqueue(new Callback<List<BudgetItem>>() {
            @Override
            public void onResponse(Call<List<BudgetItem>> call, Response<List<BudgetItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update the items list with fresh data from backend
                    items.clear();
                    items.addAll(response.body());
                    
                    // Mark all items as saved (unsaved = false)
                    for (BudgetItem item : items) {
                        item.setUnsaved(false);
                    }
                    
                    // Update the adapter with fresh data
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BudgetItem>> call, Throwable t) {
                // Handle error
                if (getContext() != null) {
                    android.widget.Toast.makeText(getContext(), "Error refreshing budget items", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}