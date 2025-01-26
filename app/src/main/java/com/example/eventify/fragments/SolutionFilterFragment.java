package com.example.eventify.fragments;

import android.content.Context;
import android.os.Bundle;

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


public class SolutionFilterFragment extends Fragment {

    ServiceService service = RetrofitClient.getClient().create(ServiceService.class);
    Collection<Service> filtered;
    Collection<SolutionCategory> categories = new ArrayList<>();
    Collection<EventType> types = new ArrayList<>();
    ArrayList<String> categoryNames = new ArrayList<>();
    ArrayList<String> typeNames = new ArrayList<>();
    String search;

    FragmentServiceFilterBinding binding;


    public SolutionFilterFragment() {
        // Required empty public constructor
    }

    public static SolutionFilterFragment newInstance(String search) {
        SolutionFilterFragment fragment = new SolutionFilterFragment();
        Bundle args = new Bundle();
        args.putString("search", search);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentServiceFilterBinding.inflate(inflater, container, false);

        if (getArguments() != null)
            search = getArguments().getString("search");

        getCategories();
        getTypes();
        Button button = binding.filter;
        button.setOnClickListener(v -> applyFilter(search));

        return binding.getRoot();
    }

    public void getCategories() {
        SolutionCategoryService service = RetrofitClient.getClient().create(SolutionCategoryService.class);
        service.getActive().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                categories = response.body();
                categoryNames.clear();
                categoryNames.add("Select a category");
                if (categories != null) {
                    for (SolutionCategory category : categories) {
                        categoryNames.add(category.getName());
                    }
                }

                ComponentsSetup.spinnerSetup(binding.getRoot(), R.id.categorySpinner, categoryNames, getContext());
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {

            }
        });
    }

    public void getTypes() {
        EventTypeService service = RetrofitClient.getClient().create(EventTypeService.class);
        service.getAll().enqueue(new Callback<Collection<EventType>>() {
            @Override
            public void onResponse(Call<Collection<EventType>> call, Response<Collection<EventType>> response) {
                types = response.body();
                typeNames.clear();
                typeNames.add("Select a type");
                for (EventType type:types) {
                    typeNames.add(type.getName());
                }
                ComponentsSetup.spinnerSetup(binding.getRoot(), R.id.typeSpinner, typeNames, getContext());
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {

            }
        });
    }

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

    public void applyFilter(String search) {

        String category = "";
        String selectedCategory = binding.categorySpinner.getSelectedItem().toString();
        if (binding.categorySpinner.getSelectedItem() != null && !selectedCategory.equals("Select a category")) {
            category = binding.categorySpinner.getSelectedItem().toString();
        }

        String type = "";
        String selectedType = binding.typeSpinner.getSelectedItem().toString();
        if (binding.typeSpinner.getSelectedItem() != null && !selectedType.equals("Select a type")) {
            type = binding.typeSpinner.getSelectedItem().toString();
        }

        double price = 0.0;
        if (binding.priceFilter.getText() != null && !binding.priceFilter.getText().toString().trim().isEmpty()) {
            try {
                price = Double.parseDouble(binding.priceFilter.getText().toString().trim());
            } catch (NumberFormatException e) {
                price = 0.0;
            }
        }

        boolean availability = binding.availabilityFilter.isChecked();

        filter(search, category,  type, price, availability);
    }

    private void filter(String search, String category, String event, Double price, boolean availability) {
        service.filter(search, category, event, price, availability).enqueue(new Callback<Collection<Service>>() {
            @Override
            public void onResponse(Call<Collection<Service>> call, Response<Collection<Service>> response) {
                filtered = response.body();
                if (callback != null) {
                    callback.onFilterApplied(filtered);
                }
            }

            @Override
            public void onFailure(Call<Collection<Service>> call, Throwable t) {

            }
        });
    }
}