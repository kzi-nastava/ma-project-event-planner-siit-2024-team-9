package com.example.eventify.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventify.databinding.FragmentServiceFormBinding;
import com.example.eventify.models.enums.Status;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.models.users.BusinessOwner;
import com.example.eventify.services.events.EventTypeService;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.services.users.BusinessOwnerService;
import com.example.eventify.utils.ComponentsSetup;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.R;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.utils.FileUtils;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServiceFormFragment extends Fragment {

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FragmentServiceFormBinding binding;

    public ServiceFormFragment() {
        // Required empty public constructor
    }

    Service selectedService;
    ServiceService service = RetrofitClient.getClient().create(ServiceService.class);
    Collection<SolutionCategory> categories = new ArrayList<>();
    Collection<EventType> types = new ArrayList<>();
    ArrayList<String> categoryNames = new ArrayList<>();
    ArrayList<String> typeNames = new ArrayList<>();
    private ArrayList<String> selectedList = new ArrayList<>();
    SolutionCategoryService categoryService = RetrofitClient.getClient().create(SolutionCategoryService.class);
    BusinessOwner owner;

    List<MultipartBody.Part> images = new ArrayList<>();


    public static ServiceFormFragment newInstance(Service service) {
        ServiceFormFragment fragment = new ServiceFormFragment();
        Bundle args = new Bundle();
        args.putParcelable("service", service);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setEdit() {
        setFormHeading();
        disableCategories();
        if (selectedService.getDuration()==0) {
            binding.duration.setVisibility(View.GONE);
            binding.durationInfo.setVisibility(View.GONE);
        }
        else {
            binding.min.setVisibility(View.GONE);
            binding.minInfo.setVisibility(View.GONE);
            binding.max.setVisibility(View.GONE);
            binding.maxInfo.setVisibility(View.GONE);
        }
    }

    private void setSwitchColor () {
        SwitchCompat visibilitySwitch = binding.visibility;
        visibilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                visibilitySwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red)));
                visibilitySwitch.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light_gray)));
            }});

        SwitchCompat availabilitySwitch = binding.availability;
        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                availabilitySwitch.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red)));
                availabilitySwitch.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.light_gray)));
            }});

    }

    private void setFormHeading() {
        TextView heading = binding.formHeading;
        heading.setText("Edit details");
    }

    private void disableCategories() {
        Spinner categories = binding.categorySpinner1;
        categories.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentServiceFormBinding.inflate(inflater, container, false);  // Use the generated binding class directly
        View view = binding.getRoot();

        // Initialize images list
        images = new ArrayList<>();

        getOwner();

        setSwitchColor();

        getCategories();
        getTypes();
        setupImagePicker(view);
        setupDeleteButton(view);
        binding.btnSubmit.setOnClickListener(v -> updateService());

        if (getArguments() != null) {
            selectedService = getArguments().getParcelable("service");
            binding.setService(selectedService);
            if (selectedService.getId() != null)
                setEdit();
        } else {
            selectedService = new Service();
            binding.setService(selectedService);
        }

        return binding.getRoot();
    }

    private void getOwner() {
        BusinessOwnerService businessOwnerService = RetrofitClient.getClient().create(BusinessOwnerService.class);
        UserSession userSession = new UserSession(requireContext());
        UUID currentUserId = userSession.getCurrentUserId();
        
        if (currentUserId == null) {
            showError("User session not found. Please log in again.");
            return;
        }
        
        businessOwnerService.get(currentUserId.toString()).enqueue(new Callback<BusinessOwner>() {
            @Override
            public void onResponse(Call<BusinessOwner> call, Response<BusinessOwner> response) {
                if (response.isSuccessful() && response.body() != null) {
                    owner = response.body();
                    Toast.makeText(requireContext(), "Owner set successfully: " + owner.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to get owner. Response: " + response.code(), Toast.LENGTH_LONG).show();
                    showError("Failed to get owner information. Please try again.");
                }
            }
            
            @Override
            public void onFailure(Call<BusinessOwner> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error getting owner: " + t.getMessage(), Toast.LENGTH_LONG).show();
                showError("Network error while getting owner information. Please check your connection.");
                t.printStackTrace();
            }
        });
    }

    private void setupImagePicker(View view) {
        view.findViewById(R.id.serviceImage).setOnClickListener(v -> openImagePicker());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<Uri> imageUris = new ArrayList<>();

                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                    imageUris.add(imageUri);
                                }
                            } else if (data.getData() != null) {
                                imageUris.add(data.getData());
                            }

                            Uri[] imageArray = imageUris.toArray(new Uri[0]);

                            if (!imageUris.isEmpty()) {
                                ImageView serviceImage = view.findViewById(R.id.serviceImage);
                                serviceImage.setImageURI(imageUris.get(0)); // Show first image
                            }

                            try {
                                images = FileUtils.prepareMultipleFiles(imageUris, requireContext());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
        );
    }


    private void setupDeleteButton(View view) {
        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Service")
                    .setMessage("Are you sure you want to delete this service?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        service.delete(selectedService.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                goBack();
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {

                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private SolutionCategory getCategory(String name) {
        for (SolutionCategory category: categories) {
            if (category.getName().equals(name))
                return category;
        }
        return null;
    }

    private void setTypes() {
        Set<EventType> set = selectedService.getEventTypes();
        if (set == null) {
            set = new HashSet<>();
            selectedService.setEventTypes(set);
        }
        set.clear(); // Clear existing types before adding new ones
        
        Toast.makeText(requireContext(), "Setting types for service...", Toast.LENGTH_SHORT).show();
        
        // Add existing EventType objects with their IDs
        for (String selectedName : selectedList) {
            for (EventType existingType : types) {
                if (existingType.getName().equals(selectedName)) {
                    // Use the existing EventType object that has an ID
                    set.add(existingType);
                    Toast.makeText(requireContext(), "Adding type: " + existingType.getName() + " with ID: " + existingType.getId(), Toast.LENGTH_SHORT).show();
                    break; // Found the match, no need to continue inner loop
                }
            }
        }

        // Debug log the final set
        for (EventType type : set) {
            Toast.makeText(requireContext(), "Final set type: " + type.getName() + " with ID: " + type.getId(), Toast.LENGTH_SHORT).show();
        }
    }


    private void updateService() {
        // Validate required fields
        if (!validateForm()) {
            return;
        }

        // Check if owner is set
        if (owner == null) {
            showError("Owner information not loaded yet. Please wait a moment and try again.");
            return;
        }

        Toast.makeText(requireContext(), "Starting service creation...", Toast.LENGTH_SHORT).show();

        // Update service fields from form
        selectedService.setVisibility(binding.visibility.isChecked());
        selectedService.setAvailability(binding.availability.isChecked());
        selectedService.setReservationDeadline(binding.automatic.isChecked() ? 0 : 1);

        // If this is an existing service (has ID)
        if (selectedService.getId() != null) {
            // Update event types if any were selected
            if (!selectedList.isEmpty()) {
                setTypes();
            }

            Toast.makeText(requireContext(), "Updating existing service...", Toast.LENGTH_SHORT).show();

            // Create service part and update with images
            service.update(
                selectedService.getId(), 
                FileUtils.createPartFromObject(selectedService), 
                images
            ).enqueue(new Callback<Service>() {
                @Override
                public void onResponse(Call<Service> call, Response<Service> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        goBack();
                    } else {
                        Toast.makeText(requireContext(), "Update failed with code: " + response.code(), Toast.LENGTH_LONG).show();
                        showError("Failed to update service. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<Service> call, Throwable t) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    showError("Network error. Please check your connection.");
                    t.printStackTrace();
                }
            });
        } else {
            // This is a new service
            setTypes();
            selectedService.setOwner(owner);

            Toast.makeText(requireContext(), "Creating new service with owner: " + owner.getEmail(), Toast.LENGTH_SHORT).show();

            // Handle category selection
            if (binding.newCategory.getVisibility() == View.VISIBLE) {
                // New category was entered
                SolutionCategory proposed = new SolutionCategory(
                    "",
                    binding.newCategory.getText().toString().trim(),
                    binding.newCategoryDescription.getText().toString().trim(),
                    false
                );
                selectedService.setCategory(proposed);
                selectedService.setStatus(Status.PENDING);
                Toast.makeText(requireContext(), "Using new category: " + proposed.getName(), Toast.LENGTH_SHORT).show();
            } else {
                // Existing category was selected
                String selectedCategory = binding.categorySpinner1.getSelectedItem().toString();
                if (selectedCategory.equals("Select a category")) {
                    showError("Please select a category");
                    return;
                }
                selectedService.setCategory(getCategory(selectedCategory));
                selectedService.setStatus(Status.ACCEPTED);
                Toast.makeText(requireContext(), "Using existing category: " + selectedCategory, Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(requireContext(), "Number of images to upload: " + images.size(), Toast.LENGTH_SHORT).show();

            // Create service
            service.add(
                FileUtils.createPartFromObject(selectedService), 
                images
            ).enqueue(new Callback<Service>() {
                @Override
                public void onResponse(Call<Service> call, Response<Service> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), "Service created successfully", Toast.LENGTH_SHORT).show();
                        goBack();
                    } else {
                        Toast.makeText(requireContext(), "Creation failed with code: " + response.code(), Toast.LENGTH_LONG).show();
                        try {
                            String errorBody = response.errorBody().string();
                            Toast.makeText(requireContext(), "Error: " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(requireContext(), "Could not read error details", Toast.LENGTH_SHORT).show();
                        }
                        showError("Failed to create service. Please try again.");
                    }
                }

                @Override
                public void onFailure(Call<Service> call, Throwable t) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    showError("Network error. Please check your connection.");
                    t.printStackTrace();
                }
            });
        }
    }

    private boolean validateForm() {
        // Validate name
        if (binding.nameEditText.getText().toString().trim().isEmpty()) {
            showError("Please enter a service name");
            return false;
        }

        // Validate image
        if (images.isEmpty()) {
            showError("Please select at least one image");
            return false;
        }

        // Validate description
        if (binding.descriptionEditText.getText().toString().trim().isEmpty()) {
            showError("Please enter a service description");
            return false;
        }

        // Validate price
        try {
            double price = Double.parseDouble(binding.priceEditText.getText().toString().trim());
            if (price <= 0) {
                showError("Please enter a valid price");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid price");
            return false;
        }

        // Validate duration or min/max time
        if (binding.duration.getVisibility() == View.VISIBLE) {
            try {
                int duration = Integer.parseInt(binding.durationInfo.getText().toString().trim());
            } catch (NumberFormatException e) {
                showError("Please enter a valid duration");
                return false;
            }
        } else {
            String minStr = binding.minInfo.getText().toString().trim();
            String maxStr = binding.maxInfo.getText().toString().trim();

            if (minStr.isEmpty() || maxStr.isEmpty()) {
                showError("Minimum and maximum values cannot be empty");
                return false;
            }

            try {
                int min = Integer.parseInt(minStr);
                int max = Integer.parseInt(maxStr);
                if (min <= 0 || max <= 0 || min >= max) {
                    showError("Please enter valid minimum and maximum times");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Please enter valid numeric values for minimum and maximum");
                return false;
            }
        }

        // Validate event types
        if (selectedList.isEmpty()) {
            showError("Please select at least one event type");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void goBack() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    public void getCategories() {
        categoryService.getActive().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Failed to load categories. Please try again.");
                    return;
                }
                categories = response.body();
                categoryNames.clear();
                categoryNames.add("Select a category");
                if (categories != null) {
                    for (SolutionCategory category : categories) {
                        categoryNames.add(category.getName());
                    }
                }
                categoryNames.add("Other");

                Spinner categorySpinner = binding.getRoot().findViewById(R.id.categorySpinner1);
                ComponentsSetup.spinnerSetup(binding.getRoot(), R.id.categorySpinner1, categoryNames, getContext());

                EditText newCategory = binding.getRoot().findViewById(R.id.newCategory);
                EditText newCategoryDescription = binding.getRoot().findViewById(R.id.newCategoryDescription);

                // Spinner item selection listener
                categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = categoryNames.get(position);
                        if ("Other".equals(selectedItem)) {
                            newCategory.setVisibility(View.VISIBLE);
                            newCategoryDescription.setVisibility(View.VISIBLE);
                        } else {
                            newCategory.setVisibility(View.GONE);
                            newCategoryDescription.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Optionally handle this case if needed
                    }
                });
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {
                showError("Network error while loading categories. Please check your connection.");
                t.printStackTrace();
            }
        });
    }


    public void getTypes() {
        EventTypeService service = RetrofitClient.getClient().create(EventTypeService.class);
        service.getAll().enqueue(new Callback<Collection<EventType>>() {
            @Override
            public void onResponse(Call<Collection<EventType>> call, Response<Collection<EventType>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Failed to load event types. Please try again.");
                    return;
                }
                types = response.body();
                typeNames.clear();

                if (types != null) {
                    for (EventType type : types) {
                        typeNames.add(type.getName());
                        Toast.makeText(requireContext(), "Loaded type: " + type.getName() + " with ID: " + type.getId(), Toast.LENGTH_SHORT).show();
                    }
                }

                setupMultiSelectSpinner(typeNames);
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {
                showError("Network error while loading event types. Please check your connection.");
                t.printStackTrace();
            }
        });
    }

    private void setupMultiSelectSpinner(ArrayList<String> items) {
        Spinner spinner = binding.typeSpinner1;
        boolean[] selectedItems = new boolean[items.size()];

        // Set the initial display value for the spinner
        ArrayAdapter<String> initialAdapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"Select a type"}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                // Set the text color for the initial value ("Select a type")
                ((TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }
        };
        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(initialAdapter);

        spinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Select Types")
                        .setMultiChoiceItems(items.toArray(new String[0]), selectedItems, (dialog, which, isChecked) -> {
                            if (isChecked) {
                                if (!selectedList.contains(items.get(which))) {
                                    selectedList.add(items.get(which));
                                }
                            } else {
                                selectedList.remove(items.get(which));
                            }
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Handle the selected items after user confirms the selection
                            if (selectedList.isEmpty()) {
                                // Reset spinner to initial value if no selection is made
                                ArrayAdapter<String> resetAdapter = new ArrayAdapter<String>(requireContext(),
                                        android.R.layout.simple_spinner_item, new String[]{"Select a type"}) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        // Ensure the initial value is black when resetting
                                        ((TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                                        return view;
                                    }
                                };
                                resetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(resetAdapter);
                            } else {
                                String selected = String.join(", ", selectedList);

                                // Create an adapter with black text for the selected items
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                                        android.R.layout.simple_spinner_item, new String[]{selected}) {
                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        View view = super.getView(position, convertView, parent);
                                        // Ensure selected items are black
                                        ((TextView) view).setTextColor(getResources().getColor(android.R.color.black));
                                        return view;
                                    }
                                };
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true; // Prevents default spinner behavior
        });
    }




}

