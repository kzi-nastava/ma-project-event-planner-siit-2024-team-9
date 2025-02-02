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
import com.example.eventify.utils.RetrofitClient;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        }

        return binding.getRoot();
    }

    private void getOwner() {
        service.getAll().enqueue(new Callback<Collection<Service>>() {
            @Override
            public void onResponse(Call<Collection<Service>> call, Response<Collection<Service>> response) {
                Service firstService = response.body().iterator().next();
                owner = firstService.getOwner();
            }

            @Override
            public void onFailure(Call<Collection<Service>> call, Throwable t) {

            }
        });
    }

    private void setupImagePicker(View view) {
        // Set click listener for service image
        view.findViewById(R.id.serviceImage).setOnClickListener(v -> openImagePicker());

        // Register image picker activity result callback
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getClipData() != null ?
                                    data.getClipData().getItemAt(0).getUri() : data.getData();
                            ImageView serviceImage = view.findViewById(R.id.serviceImage);
                            serviceImage.setImageURI(selectedImageUri);
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
        for (String name: selectedList) {
            for (EventType type: types) {
                if (type.getName().equals(name))
                    set.add(type);
            }
        }
        selectedService.setEventTypes(set);
    }


    private void updateService() {
        selectedService.setVisibility(binding.visibility.isChecked());
        selectedService.setAvailability(binding.availability.isChecked());
        int reservationMethod = binding.automatic.isChecked() ? 0:1;
        selectedService.setReservationDeadline(reservationMethod);
        if (selectedService.getId() != null) {
            if (!selectedList.isEmpty())
                setTypes();
            service.update(selectedService.getId(), selectedService).enqueue(new Callback<Service>() {
                @Override
                public void onResponse(Call<Service> call, Response<Service> response) {
                    goBack();
                }

                @Override
                public void onFailure(Call<Service> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        } else {
            setTypes();
            selectedService.setOwner(owner);
            if (binding.newCategory.getVisibility() == View.VISIBLE) {
                SolutionCategory proposed = new SolutionCategory("",binding.newCategory.getText().toString(),binding.newCategoryDescription.toString(),false);
                selectedService.setCategory(proposed);
                selectedService.setStatus(Status.PENDING);
            }
            else {
                selectedService.setCategory(getCategory(binding.categorySpinner1.getSelectedItem().toString()));
                selectedService.setStatus(Status.ACCEPTED);
            }
            ArrayList<String> images = new ArrayList<>();
            images.add("https://cdn.shopify.com/s/files/1/2026/7451/files/blog_w150_outdoor-party-1.jpg?1846900428569813131");
            selectedService.setImages(images);
            service.add(selectedService).enqueue(new Callback<Service>() {
                @Override
                public void onResponse(Call<Service> call, Response<Service> response) {
                    goBack();
                }

                @Override
                public void onFailure(Call<Service> call, Throwable t) {

                }
            });
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
                // Handle failure here
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

                if (types != null) {
                    for (EventType type : types) {
                        typeNames.add(type.getName());
                    }
                }

                setupMultiSelectSpinner(typeNames);
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {
                // Handle error
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

