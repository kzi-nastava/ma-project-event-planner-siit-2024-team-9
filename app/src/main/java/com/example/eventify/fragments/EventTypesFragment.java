package com.example.eventify.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.eventify.R;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.services.events.EventTypeService;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventTypesFragment extends Fragment {

    private EventTypeService eventTypeService;
    private SolutionCategoryService categoryService;
    private List<EventType> eventTypes = new ArrayList<>();
    private List<SolutionCategory> allCategories = new ArrayList<>();
    private ArrayAdapter<EventType> adapter;
    private EventType editingEventType = null;
    private Set<String> selectedCategories = new HashSet<>();

    public EventTypesFragment() {
        // Required empty public constructor
    }

    public static EventTypesFragment newInstance() {
        return new EventTypesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Use a simple layout instead of the complex binding
        return inflater.inflate(R.layout.fragment_event_types_simple, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize services
        try {
                eventTypeService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(EventTypeService.class);
                categoryService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(SolutionCategoryService.class);
            setupListView();
        setupClickListeners();
        loadEventTypes();
        loadCategories();
        } catch (Exception e) {
            Log.e("EventTypesFragment", "Error initializing: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListView() {
        // Create a simple list view
        ListView listView = new ListView(requireContext());
        listView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        
        adapter = new ArrayAdapter<EventType>(requireContext(), R.layout.event_type_list_item, eventTypes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_type_list_item, parent, false);
                }
                
                EventType eventType = eventTypes.get(position);
                
                TextView nameText = convertView.findViewById(R.id.eventTypeName);
                TextView descriptionText = convertView.findViewById(R.id.eventTypeDescription);
                TextView categoriesText = convertView.findViewById(R.id.eventTypeCategories);
                CheckBox activeCheckbox = convertView.findViewById(R.id.activeCheckbox);
                Button editButton = convertView.findViewById(R.id.editButton);
                Button categoriesButton = convertView.findViewById(R.id.categoriesButton);
                
                // Set data
                nameText.setText(eventType.getName());
                descriptionText.setText(eventType.getDescription() != null ? eventType.getDescription() : "No description");
                activeCheckbox.setChecked(eventType.isActive());
                
                // Set categories text
                String categoriesStr = getCategoryNames(eventType);
                categoriesText.setText(categoriesStr);
                Log.d("EventTypesFragment", "Event type: " + eventType.getName() + ", Categories: " + categoriesStr);
                
                // Set up listeners
                activeCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked != eventType.isActive()) {
                        toggleActive(eventType);
                    }
                });
                
                editButton.setOnClickListener(v -> editEventType(eventType));
                categoriesButton.setOnClickListener(v -> openCategoryDialog(eventType));
                
                return convertView;
            }
        };
        
        listView.setAdapter(adapter);
        
        // Add the list view to the container
        ViewGroup container = getView().findViewById(R.id.eventTypesContainer);
        if (container != null) {
            container.addView(listView);
        }
    }

    private void setupClickListeners() {
        Button addButton = getView().findViewById(R.id.addEventTypeButton);
        if (addButton != null) {
            addButton.setOnClickListener(v -> addEventType());
        }
    }
    
    private void addEventType() {
        showAddEventTypeDialog();
    }
    
    private void showAddEventTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event_type, null);
        builder.setView(dialogView);
        
        EditText nameEdit = dialogView.findViewById(R.id.nameEdit);
        EditText descriptionEdit = dialogView.findViewById(R.id.descriptionEdit);
        Button categoriesButton = dialogView.findViewById(R.id.categoriesButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        
        // Initialize selected categories
        selectedCategories.clear();
        
        AlertDialog dialog = builder.create();
        
        categoriesButton.setOnClickListener(v -> {
            showCategorySelectionDialog();
        });
        
        saveButton.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String description = descriptionEdit.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Event type name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create new event type
            EventType newEventType = new EventType();
            newEventType.setName(name);
            newEventType.setDescription(description);
            newEventType.setActive(true);
            
            // Set selected categories - try without categories first if there are issues
            if (!selectedCategories.isEmpty()) {
                Set<SolutionCategory> selectedCategoryObjects = new HashSet<>();
                for (SolutionCategory category : allCategories) {
                    if (selectedCategories.contains(category.getId())) {
                        selectedCategoryObjects.add(category);
                    }
                }
                newEventType.setSuggestedCategories(selectedCategoryObjects);
            } else {
                newEventType.setSuggestedCategories(new HashSet<>());
            }
            
            createEventType(newEventType);
            dialog.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    private void createEventType(EventType eventType) {
        updateStatusText("Creating event type...");
        
        Log.d("EventTypesFragment", "Creating event type: " + eventType.getName());
        Log.d("EventTypesFragment", "Event type description: " + eventType.getDescription());
        Log.d("EventTypesFragment", "Event type isActive: " + eventType.isActive());
        Log.d("EventTypesFragment", "Event type categories count: " + (eventType.getSuggestedCategories() != null ? eventType.getSuggestedCategories().size() : 0));
        
       
        
        eventTypeService.create(eventType).enqueue(new Callback<EventType>() {
            @Override
            public void onResponse(Call<EventType> call, Response<EventType> response) {
                if (getContext() == null) return;
                
                Log.d("EventTypesFragment", "Create response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    updateStatusText("Event type created successfully!");
                    Log.d("EventTypesFragment", "Event type created successfully");
                    
                    // Log the response body to see what we got back
                    EventType createdEventType = response.body();
                    Log.d("EventTypesFragment", "Created event type name: " + createdEventType.getName());
                    Log.d("EventTypesFragment", "Created event type ID: " + createdEventType.getId());
                    Log.d("EventTypesFragment", "Created event type categories count: " + (createdEventType.getSuggestedCategories() != null ? createdEventType.getSuggestedCategories().size() : 0));
                    
                    if (createdEventType.getSuggestedCategories() != null) {
                        for (SolutionCategory category : createdEventType.getSuggestedCategories()) {
                            Log.d("EventTypesFragment", "Response category: " + category.getName() + " (ID: " + category.getId() + ")");
                        }
                    }
                    
                    // Add to local list
                    eventTypes.add(createdEventType);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    
                    // Refresh the list to get the latest data from backend
                    loadEventTypes();
                } else {
                    updateStatusText("Failed to create event type. Code: " + response.code());
                    Log.e("EventTypesFragment", "Failed to create event type. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("EventTypesFragment", "Error body: " + errorBody);
                            
                            // If it's a 500 error and we have categories, try without categories
                            if (response.code() == 500 && eventType.getSuggestedCategories() != null && !eventType.getSuggestedCategories().isEmpty()) {
                                Log.d("EventTypesFragment", "Trying to create event type without categories...");
                                EventType fallbackEventType = new EventType();
                                fallbackEventType.setName(eventType.getName());
                                fallbackEventType.setDescription(eventType.getDescription());
                                fallbackEventType.setActive(eventType.isActive());
                                fallbackEventType.setSuggestedCategories(new HashSet<>());
                                createEventType(fallbackEventType);
                                return;
                            }
                        } catch (Exception e) {
                            Log.e("EventTypesFragment", "Could not read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<EventType> call, Throwable t) {
                if (getContext() == null) return;
                updateStatusText("Failed to create event type: " + t.getMessage());
                Log.e("EventTypesFragment", "Failed to create event type", t);
                t.printStackTrace();
            }
        });
    }

    private void loadEventTypes() {
        if (eventTypeService == null) {
            updateStatusText("Service not initialized");
            return;
        }
        
        updateStatusText("Loading event types...");
        
        eventTypeService.getAll().enqueue(new Callback<Collection<EventType>>() {
            @Override
            public void onResponse(Call<Collection<EventType>> call, Response<Collection<EventType>> response) {
                if (getContext() == null) return; // Fragment might be detached
                
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    
                    // Debug: Log all loaded event types and their categories
                    Log.d("EventTypesFragment", "Loaded " + eventTypes.size() + " event types from backend");
                    for (EventType eventType : eventTypes) {
                        Log.d("EventTypesFragment", "Loaded event type: " + eventType.getName() + " (ID: " + eventType.getId() + ")");
                        Log.d("EventTypesFragment", "Categories count: " + (eventType.getSuggestedCategories() != null ? eventType.getSuggestedCategories().size() : 0));
                        if (eventType.getSuggestedCategories() != null) {
                            for (SolutionCategory category : eventType.getSuggestedCategories()) {
                                Log.d("EventTypesFragment", "  - Category: " + category.getName() + " (ID: " + category.getId() + ")");
                            }
                        }
                    }
                    
                    // Update adapter
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    
                    updateStatusText("Loaded " + eventTypes.size() + " event types");
                } else {
                    String errorMsg = "Failed to load event types. Code: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("EventTypesFragment", "Error reading error body", e);
                        }
                    }
                    updateStatusText(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {
                if (getContext() == null) return; // Fragment might be detached
                
                Log.e("EventTypesFragment", "Failed to load event types", t);
                updateStatusText("Failed to load event types: " + t.getMessage());
            }
        });
    }

    private void updateStatusText(String message) {
        if (getView() != null) {
            TextView statusText = getView().findViewById(R.id.statusText);
            if (statusText != null) {
                statusText.setText(message);
            }
        }
    }

    private void loadCategories() {
        if (categoryService == null) {
            Log.e("EventTypesFragment", "Category service is null");
            return;
        }
        
        Log.d("EventTypesFragment", "Loading categories...");
        categoryService.getActive().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                if (getContext() == null) return;
                
                Log.d("EventTypesFragment", "Categories response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    allCategories.clear();
                    allCategories.addAll(response.body());
                    Log.d("EventTypesFragment", "Loaded " + allCategories.size() + " categories");
                    for (SolutionCategory category : allCategories) {
                        Log.d("EventTypesFragment", "Category: " + category.getName() + " (ID: " + category.getId() + ")");
                    }
                } else {
                    Log.e("EventTypesFragment", "Failed to load categories. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("EventTypesFragment", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("EventTypesFragment", "Could not read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {
                if (getContext() == null) return;
                Log.e("EventTypesFragment", "Failed to load categories", t);
                t.printStackTrace();
            }
        });
    }

    private void toggleActive(EventType eventType) {
        eventType.setActive(!eventType.isActive());
        updateStatusText("Updating event type status...");
        
        eventTypeService.update(eventType.getId(), eventType).enqueue(new Callback<EventType>() {
            @Override
            public void onResponse(Call<EventType> call, Response<EventType> response) {
                if (getContext() == null) return;
                
                if (response.isSuccessful()) {
                    updateStatusText("Event type status updated successfully!");
                    // Update the local list
                    for (int i = 0; i < eventTypes.size(); i++) {
                        if (eventTypes.get(i).getId().equals(eventType.getId())) {
                            eventTypes.set(i, eventType);
                            break;
                        }
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    } else {
                    updateStatusText("Failed to update event type status. Code: " + response.code());
                    // Revert the change
                    eventType.setActive(!eventType.isActive());
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    }
            }

            @Override
            public void onFailure(Call<EventType> call, Throwable t) {
                if (getContext() == null) return;
                updateStatusText("Failed to update event type status: " + t.getMessage());
                // Revert the change
                eventType.setActive(!eventType.isActive());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    
    private void editEventType(EventType eventType) {
        editingEventType = eventType;
        showEditDialog(eventType);
    }
    
    private void showEditDialog(EventType eventType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_event_type, null);
        builder.setView(dialogView);
        
        EditText descriptionEdit = dialogView.findViewById(R.id.descriptionEdit);
        Button categoriesButton = dialogView.findViewById(R.id.categoriesButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        
        // Set current description
        descriptionEdit.setText(eventType.getDescription() != null ? eventType.getDescription() : "");
        
        // Initialize selected categories
        selectedCategories.clear();
        if (eventType.getSuggestedCategories() != null) {
            for (SolutionCategory category : eventType.getSuggestedCategories()) {
                selectedCategories.add(category.getId());
            }
        }
        
        AlertDialog dialog = builder.create();
        
        categoriesButton.setOnClickListener(v -> {
            showCategorySelectionDialog();
        });
        
        saveButton.setOnClickListener(v -> {
            String newDescription = descriptionEdit.getText().toString().trim();
            eventType.setDescription(newDescription);
            
            // Update suggested categories
            Set<SolutionCategory> selectedCategoryObjects = new HashSet<>();
            for (SolutionCategory category : allCategories) {
                if (selectedCategories.contains(category.getId())) {
                    selectedCategoryObjects.add(category);
                }
            }
            eventType.setSuggestedCategories(selectedCategoryObjects);
            
            saveEventType(eventType);
            dialog.dismiss();
        });
        
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void openCategoryDialog(EventType eventType) {
        editingEventType = eventType;
        selectedCategories.clear();
        
        // Initialize selected categories from the event type
        if (eventType.getSuggestedCategories() != null) {
            for (SolutionCategory category : eventType.getSuggestedCategories()) {
                selectedCategories.add(category.getId());
            }
        }
        
        showCategorySelectionDialog();
    }

    private void showCategorySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_select_categories, null);
        builder.setView(dialogView);

        LinearLayout categoryList = dialogView.findViewById(R.id.categoryList);
        categoryList.removeAllViews();

        // Create checkboxes for each category
        for (SolutionCategory category : allCategories) {
            CheckBox checkBox = new CheckBox(requireContext());
            checkBox.setText(category.getName());
            checkBox.setChecked(selectedCategories.contains(category.getId()));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategories.add(category.getId());
                } else {
                    selectedCategories.remove(category.getId());
                }
            });
            categoryList.addView(checkBox);
        }

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());
        
        dialogView.findViewById(R.id.saveButton).setOnClickListener(v -> {
            if (editingEventType != null) {
                // Update the event type with selected categories
                Set<SolutionCategory> selectedCategoryObjects = new HashSet<>();
                for (SolutionCategory category : allCategories) {
                    if (selectedCategories.contains(category.getId())) {
                        selectedCategoryObjects.add(category);
                    }
                }
                editingEventType.setSuggestedCategories(selectedCategoryObjects);
                
                // Update the local list
                for (int i = 0; i < eventTypes.size(); i++) {
                    if (eventTypes.get(i).getId().equals(editingEventType.getId())) {
                        eventTypes.set(i, editingEventType);
                        break;
                    }
                }
                
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveEventType(EventType eventType) {
        updateStatusText("Saving event type...");
        
        Log.d("EventTypesFragment", "Saving event type: " + eventType.getName());
        Log.d("EventTypesFragment", "Event type ID: " + eventType.getId());
        Log.d("EventTypesFragment", "Event type description: " + eventType.getDescription());
        Log.d("EventTypesFragment", "Event type categories count: " + (eventType.getSuggestedCategories() != null ? eventType.getSuggestedCategories().size() : 0));
        
        if (eventType.getSuggestedCategories() != null) {
            for (SolutionCategory category : eventType.getSuggestedCategories()) {
                Log.d("EventTypesFragment", "Category: " + category.getName() + " (ID: " + category.getId() + ")");
            }
        }
        
        eventTypeService.update(eventType.getId(), eventType).enqueue(new Callback<EventType>() {
            @Override
            public void onResponse(Call<EventType> call, Response<EventType> response) {
                if (getContext() == null) return;
                
                Log.d("EventTypesFragment", "Update response code: " + response.code());
                if (response.isSuccessful()) {
                    updateStatusText("Event type updated successfully!");
                    Log.d("EventTypesFragment", "Event type updated successfully");
                    // Update the local list
                    for (int i = 0; i < eventTypes.size(); i++) {
                        if (eventTypes.get(i).getId().equals(eventType.getId())) {
                            eventTypes.set(i, eventType);
                    break;
                }
            }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    updateStatusText("Failed to update event type. Code: " + response.code());
                    Log.e("EventTypesFragment", "Failed to update event type. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("EventTypesFragment", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("EventTypesFragment", "Could not read error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<EventType> call, Throwable t) {
                if (getContext() == null) return;
                updateStatusText("Failed to update event type: " + t.getMessage());
                Log.e("EventTypesFragment", "Failed to update event type", t);
                t.printStackTrace();
            }
        });
    }

    private String getCategoryNames(EventType eventType) {
        Log.d("EventTypesFragment", "Getting category names for: " + eventType.getName());
        Log.d("EventTypesFragment", "Suggested categories is null: " + (eventType.getSuggestedCategories() == null));
        if (eventType.getSuggestedCategories() != null) {
            Log.d("EventTypesFragment", "Suggested categories size: " + eventType.getSuggestedCategories().size());
        }
        
        if (eventType.getSuggestedCategories() == null || eventType.getSuggestedCategories().isEmpty()) {
            return "No categories";
        }
        StringBuilder names = new StringBuilder();
        boolean first = true;
        for (SolutionCategory category : eventType.getSuggestedCategories()) {
            Log.d("EventTypesFragment", "Category: " + category.getName() + " (ID: " + category.getId() + ")");
            if (!first) names.append(", ");
            names.append(category.getName());
            first = false;
        }
        String result = names.toString();
        Log.d("EventTypesFragment", "Final category names: " + result);
        return result;
    }
}
