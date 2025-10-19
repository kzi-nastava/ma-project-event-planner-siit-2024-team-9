package com.example.eventify.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.eventify.databinding.FragmentServiceFilterBinding;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.services.events.EventTypeService;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.utils.ComponentsSetup;
import com.example.eventify.R;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ServiceFilterFragment extends Fragment {

    private ServiceService serviceService;
    Collection<Service> filtered;
    Collection<SolutionCategory> categories = new ArrayList<>();
    Collection<EventType> types = new ArrayList<>();
    ArrayList<String> categoryNames = new ArrayList<>();
    ArrayList<String> typeNames = new ArrayList<>();
    String search;

    FragmentServiceFilterBinding binding;

    public interface OnFilterAppliedListener {
        void onFilterApplied(Collection<Service> filteredCollection);
    }

    private OnFilterAppliedListener callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnFilterAppliedListener) {
            callback = (OnFilterAppliedListener) parentFragment;
        } else {
            throw new RuntimeException(parentFragment + " must implement OnFilterAppliedListener");
        }
    }

    public static ServiceFilterFragment newInstance(String search) {
        ServiceFilterFragment fragment = new ServiceFilterFragment();
        Bundle args = new Bundle();
        args.putString("search", search);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            search = getArguments().getString("search");
        }
        serviceService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(ServiceService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServiceFilterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        getCategories();
        getTypes();

        binding.filter.setOnClickListener(v -> applyFilter());

        // Add test button for debugging (remove in production)
        // binding.getRoot().findViewById(R.id.testFilter).setOnClickListener(v -> testFilter());

        return view;
    }

    private void getCategories() {
        SolutionCategoryService categoryService = RetrofitClient.getClient(requireContext()).create(SolutionCategoryService.class);
        categoryService.getActive().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                Log.d("ServiceFilter", "Categories response - success: " + response.isSuccessful() + ", code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    categoryNames.clear();
                    categoryNames.add("Select a category");
                    if (categories != null) {
                        Log.d("ServiceFilter", "Loaded " + categories.size() + " categories");
                        for (SolutionCategory category : categories) {
                            Log.d("ServiceFilter", "Loading category: " + category.getName() + " (ID: " + category.getId() + ")");
                            categoryNames.add(category.getName());
                        }
                    }
                    ComponentsSetup.spinnerSetup(binding.getRoot(), R.id.categorySpinner, categoryNames, getContext());
                } else {
                    Log.e("ServiceFilter", "Failed to load categories - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {
                Log.e("ServiceFilter", "Failed to load categories: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void getTypes() {
        EventTypeService service = RetrofitClient.getClient(requireContext()).create(EventTypeService.class);
        service.getAll().enqueue(new Callback<Collection<EventType>>() {
            @Override
            public void onResponse(Call<Collection<EventType>> call, Response<Collection<EventType>> response) {
                Log.d("ServiceFilter", "Event types response - success: " + response.isSuccessful() + ", code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    types = response.body();
                    typeNames.clear();
                    typeNames.add("Select a type");
                    if (types != null) {
                        Log.d("ServiceFilter", "Loaded " + types.size() + " event types");
                        for (EventType type : types) {
                            Log.d("ServiceFilter", "Loading event type: " + type.getName() + " (ID: " + type.getId() + ")");
                            typeNames.add(type.getName());
                        }
                    }
                    ComponentsSetup.spinnerSetup(binding.getRoot(), R.id.typeSpinner, typeNames, getContext());
                } else {
                    Log.e("ServiceFilter", "Failed to load event types - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {
                Log.e("ServiceFilter", "Failed to load event types: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void applyFilter() {
        String category = null;
        if (binding.categorySpinner.getSelectedItem() != null) {
            String selectedCategory = binding.categorySpinner.getSelectedItem().toString();
            Log.d("ServiceFilter", "Selected category: " + selectedCategory);
            if (!selectedCategory.equals("Select a category") && !selectedCategory.isEmpty() && !selectedCategory.equals("null")) {
                category = selectedCategory.trim();
            }
        }

        String eventType = null;
        if (binding.typeSpinner.getSelectedItem() != null) {
            String selectedType = binding.typeSpinner.getSelectedItem().toString();
            Log.d("ServiceFilter", "Selected event type: " + selectedType);
            if (!selectedType.equals("Select a type") && !selectedType.isEmpty() && !selectedType.equals("null")) {
                eventType = selectedType.trim();
            }
        }

        Double price = null;
        if (binding.priceFilter.getText() != null && !binding.priceFilter.getText().toString().trim().isEmpty()) {
            try {
                price = Double.parseDouble(binding.priceFilter.getText().toString().trim());
            } catch (NumberFormatException e) {
                Log.e("ServiceFilter", "Invalid price format: " + binding.priceFilter.getText().toString());
                price = null;
            }
        }

        Boolean availability = binding.availabilityFilter.isChecked();

        Log.d("ServiceFilter", "About to filter with - search: " + search + ", category: " + category + ", eventType: " + eventType + ", price: " + price + ", availability: " + availability);
        
        // Ensure we don't send "null" strings
        String finalKeyword = (search != null && !search.trim().isEmpty()) ? search : null;
        String finalCategory = (category != null && !category.trim().isEmpty()) ? category : null;
        String finalEventType = (eventType != null && !eventType.trim().isEmpty()) ? eventType : null;
        
        Log.d("ServiceFilter", "Final parameters - keyword: " + finalKeyword + ", category: " + finalCategory + ", eventType: " + finalEventType);
        filter(finalKeyword, finalCategory, finalEventType, price, availability);
    }

    private void filter(String keyword, String category, String eventType, Double price, Boolean availability) {
        Log.d("ServiceFilter", "=== FILTER REQUEST ===");
        Log.d("ServiceFilter", "keyword: '" + keyword + "' (null: " + (keyword == null) + ")");
        Log.d("ServiceFilter", "category: '" + category + "' (null: " + (category == null) + ")");
        Log.d("ServiceFilter", "eventType: '" + eventType + "' (null: " + (eventType == null) + ")");
        Log.d("ServiceFilter", "price: " + price + " (null: " + (price == null) + ")");
        Log.d("ServiceFilter", "availability: " + availability + " (null: " + (availability == null) + ")");
        
        // Debug: Log available categories and types for comparison
        Log.d("ServiceFilter", "Available categories: " + categoryNames);
        Log.d("ServiceFilter", "Available event types: " + typeNames);
        
        serviceService.filter(keyword, category, eventType, price, availability).enqueue(new Callback<Collection<Service>>() {
            @Override
            public void onResponse(Call<Collection<Service>> call, Response<Collection<Service>> response) {
                Log.d("ServiceFilter", "Response received - success: " + response.isSuccessful() + ", code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    filtered = response.body();
                    Log.d("ServiceFilter", "Filtered results count: " + filtered.size());
                    // Log first few results for debugging
                    int count = 0;
                    for (Service service : filtered) {
                        if (count < 3) {
                            Log.d("ServiceFilter", "Result " + count + ": " + service.getName() + 
                                  " - Category: " + (service.getCategory() != null ? service.getCategory().getName() : "null") +
                                  " - EventTypes: " + (service.getEventTypes() != null ? service.getEventTypes().toString() : "null"));
                            count++;
                        }
                    }
                    if (callback != null) {
                        callback.onFilterApplied(filtered);
                    }
                } else {
                    Log.d("ServiceFilter", "Unsuccessful response or null body - Code: " + response.code() + ", Message: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("ServiceFilter", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("ServiceFilter", "Could not read error body", e);
                        }
                    }
                    // Handle unsuccessful response
                    if (callback != null) {
                        callback.onFilterApplied(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<Collection<Service>> call, Throwable t) {
                Log.e("ServiceFilter", "Filter request failed: " + t.getMessage());
                t.printStackTrace();
                // Handle error
                if (callback != null) {
                    callback.onFilterApplied(new ArrayList<>());
                }
            }
        });
    }

    private void resetFilter() {
        binding.categorySpinner.setSelection(0);
        binding.typeSpinner.setSelection(0);
        binding.priceFilter.setText("");
        binding.availabilityFilter.setChecked(false);
    }
}
