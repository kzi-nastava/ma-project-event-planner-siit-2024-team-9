package com.example.eventify.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.adapters.ActivityCreationAdapter;
import com.example.eventify.databinding.FragmentEventCreationBinding;
import com.example.eventify.models.events.Activity;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventDTO;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.others.Location;
import com.example.eventify.models.enums.PrivacyType;
import com.example.eventify.models.users.EventOrganizer;
import com.example.eventify.models.users.EventOrganizerDTO;
import com.example.eventify.models.users.RoleDTO;
import com.example.eventify.models.users.User;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.events.EventTypeService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventCreationFragment extends Fragment {
    private FragmentEventCreationBinding binding;
    private EventService eventService;
    private EventTypeService eventTypeService;
    private UserSession userSession;
    private UUID userId;
    private User organizer;
    private Collection<EventType> eventTypes = new ArrayList<>();
    private List<Activity> activities = new ArrayList<>();
    private ActivityCreationAdapter activityAdapter;
    
    // Form data
    private String eventName = "";
    private String eventDescription = "";
    private PrivacyType privacyType = PrivacyType.PUBLIC;
    private int maxAttendees = 0;
    private String eventStartDate = "";
    private String eventEndDate = "";
    private String locationName = "";
    private String locationAddress = "";
    private String locationCity = "";
    private String locationCountry = "";
    private double locationLongitude = 0.0;
    private double locationLatitude = 0.0;
    private EventType selectedEventType = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventService = RetrofitClient.getClient(requireContext()).create(EventService.class);
        eventTypeService = RetrofitClient.getClient(requireContext()).create(EventTypeService.class);
        userSession = new UserSession(requireContext());
        userId = userSession.getCurrentUserId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventCreationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews();
        loadEventTypes();
        loadUserData();
        setupRecyclerView();
        
        // Show the first step initially
        goToStep(1);
    }

    private void setupViews() {
        // Set up step navigation
        binding.btnNextStep1.setOnClickListener(v -> goToStep(2));
        binding.btnNextStep2.setOnClickListener(v -> goToStep(3));
        binding.btnNextStep3.setOnClickListener(v -> goToStep(4));
        binding.btnBackStep2.setOnClickListener(v -> goToStep(1));
        binding.btnBackStep3.setOnClickListener(v -> goToStep(2));
        binding.btnBackStep4.setOnClickListener(v -> goToStep(3));
        
        // Set up action buttons
        binding.btnAddActivity.setOnClickListener(v -> addActivity());
        binding.btnSubmitEvent.setOnClickListener(v -> submitEvent());
        binding.btnCancelEvent.setOnClickListener(v -> cancelEvent());
        
        // Set up privacy type spinner
        ArrayAdapter<PrivacyType> privacyAdapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, new PrivacyType[]{PrivacyType.PUBLIC, PrivacyType.PRIVATE});
        privacyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerPrivacyType.setAdapter(privacyAdapter);
        
        // Set up date pickers
        binding.etEventStartDate.setOnClickListener(v -> showDatePicker("start"));
        binding.etEventEndDate.setOnClickListener(v -> showDatePicker("end"));
    }

    private void loadEventTypes() {
        eventTypeService.getAll().enqueue(new Callback<Collection<EventType>>() {
            @Override
            public void onResponse(Call<Collection<EventType>> call, Response<Collection<EventType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes = response.body();
                    setupEventTypeSpinner();
                } else {
                    Log.e("EventCreation", "Failed to load event types: " + response.code());
                    // Fallback to hardcoded types if API fails
                    eventTypes = new ArrayList<>();
                    setupEventTypeSpinner();
                }
            }

            @Override
            public void onFailure(Call<Collection<EventType>> call, Throwable t) {
                Log.e("EventCreation", "Error loading event types", t);
                // Fallback to hardcoded types if API fails
                eventTypes = new ArrayList<>();
                setupEventTypeSpinner();
            }
        });
    }

    private void setupEventTypeSpinner() {
        List<String> eventTypeNames = new ArrayList<>();
        eventTypeNames.add("Select Event Type");
        for (EventType type : eventTypes) {
            eventTypeNames.add(type.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, eventTypeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerEventType.setAdapter(adapter);
        
        // Add selection listener to update selectedEventType
        binding.spinnerEventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Select Event Type" option
                    selectedEventType = new ArrayList<>(eventTypes).get(position - 1);
                    Log.d("EventCreation", "Selected event type: " + selectedEventType.getName());
                } else {
                    selectedEventType = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEventType = null;
            }
        });
    }

    private void loadUserData() {
        // Load current user data to get organizer info
        // This would typically come from UserSession or a separate API call
        // For now, we'll create a placeholder organizer
        organizer = new User();
        organizer.setId(userId.toString());
        organizer.setFirstName("Event");
        organizer.setLastName("Organizer");
    }

    private void setupRecyclerView() {
        activityAdapter = new ActivityCreationAdapter(activities, new ActivityCreationAdapter.OnActivityClickListener() {
            @Override
            public void onRemoveActivity(int position) {
                activities.remove(position);
                activityAdapter.notifyItemRemoved(position);
            }
        });
        
        // Set context for date/time pickers
        activityAdapter.setContext(requireContext());
        
        binding.recyclerViewActivities.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewActivities.setAdapter(activityAdapter);
        
        // Add initial empty activity to show the RecyclerView
        if (activities.isEmpty()) {
            addActivity();
        }
    }

    private void addActivity() {
        Activity newActivity = new Activity();
        newActivity.setName("");
        newActivity.setDescription("");
        newActivity.setStartDate("");
        newActivity.setStartTime("");
        newActivity.setEndDate("");
        newActivity.setEndTime("");
        newActivity.setLocation(new Location("", "", "", "", 0.0, 0.0));
        
        activities.add(newActivity);
        activityAdapter.notifyItemInserted(activities.size() - 1);
    }

    private void goToStep(int step) {
        binding.step1Layout.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        binding.step2Layout.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        binding.step3Layout.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        binding.step4Layout.setVisibility(step == 4 ? View.VISIBLE : View.GONE);
        
        // Update step indicator
        updateStepIndicator(step);
    }

    private void updateStepIndicator(int currentStep) {
        binding.stepIndicator1.setSelected(currentStep >= 1);
        binding.stepIndicator2.setSelected(currentStep >= 2);
        binding.stepIndicator3.setSelected(currentStep >= 3);
        binding.stepIndicator4.setSelected(currentStep >= 4);
    }

    private void showDatePicker(String type) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    if ("start".equals(type)) {
                        binding.etEventStartDate.setText(selectedDate);
                    } else if ("end".equals(type)) {
                        binding.etEventEndDate.setText(selectedDate);
                    }
                }, year, month, day);

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        
        datePickerDialog.show();
    }

    private void submitEvent() {
        if (!validateForm()) {
            return;
        }
        
        // Collect form data
        collectFormData();
        
        // Create event DTO
        EventDTO eventDTO = createEventFromFormData();
        
        // Debug: Log the JSON being sent
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String json = gson.toJson(eventDTO);
            Log.d("EventCreation", "Sending JSON: " + json);
        } catch (Exception e) {
            Log.e("EventCreation", "Error serializing JSON", e);
        }
        
        // Submit to API
        eventService.createEvent(eventDTO).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    // Navigate back or to event details
                    requireActivity().onBackPressed();
                } else {
                    // Log detailed error information
                    Log.e("EventCreation", "API Error - Code: " + response.code());
                    Log.e("EventCreation", "Error Headers: " + response.headers());
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("EventCreation", "Error Response Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e("EventCreation", "Error reading error body", e);
                    }
                    Log.e("EventCreation", "Request URL: " + call.request().url());
                    Log.e("EventCreation", "Request Headers: " + call.request().headers());
                    Log.e("EventCreation", "Request Method: " + call.request().method());
                    Log.e("EventCreation", "Request Body Size: " + (call.request().body() != null ? "Present" : "Null"));
                    Toast.makeText(requireContext(), "Failed to create event: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Log.e("EventCreation", "Network Error", t);
                Toast.makeText(requireContext(), "Error creating event: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateForm() {
        // Basic validation
        if (binding.etEventName.getText().toString().trim().isEmpty()) {
            binding.etEventName.setError("Event name is required");
            return false;
        }
        
        if (binding.etEventDescription.getText().toString().trim().isEmpty()) {
            binding.etEventDescription.setError("Event description is required");
            return false;
        }
        
        if (selectedEventType == null) {
            Toast.makeText(requireContext(), "Please select an event type", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (binding.etEventStartDate.getText().toString().trim().isEmpty()) {
            binding.etEventStartDate.setError("Start date is required");
            return false;
        }
        
        if (binding.etEventEndDate.getText().toString().trim().isEmpty()) {
            binding.etEventEndDate.setError("End date is required");
            return false;
        }
        
        if (binding.etLocationName.getText().toString().trim().isEmpty()) {
            binding.etLocationName.setError("Location name is required");
            return false;
        }
        
        return true;
    }

    private void collectFormData() {
        eventName = binding.etEventName.getText().toString().trim();
        eventDescription = binding.etEventDescription.getText().toString().trim();
        privacyType = (PrivacyType) binding.spinnerPrivacyType.getSelectedItem();
        maxAttendees = Integer.parseInt(binding.etMaxAttendees.getText().toString().trim());
        eventStartDate = binding.etEventStartDate.getText().toString().trim();
        eventEndDate = binding.etEventEndDate.getText().toString().trim();
        locationName = binding.etLocationName.getText().toString().trim();
        locationAddress = binding.etLocationAddress.getText().toString().trim();
        locationCity = binding.etLocationCity.getText().toString().trim();
        locationCountry = binding.etLocationCountry.getText().toString().trim();
        
        try {
            locationLongitude = Double.parseDouble(binding.etLocationLongitude.getText().toString().trim());
            locationLatitude = Double.parseDouble(binding.etLocationLatitude.getText().toString().trim());
        } catch (NumberFormatException e) {
            locationLongitude = 0.0;
            locationLatitude = 0.0;
        }
        
        // Get selected event type
        int selectedPosition = binding.spinnerEventType.getSelectedItemPosition();
        if (selectedPosition > 0) {
            selectedEventType = new ArrayList<>(eventTypes).get(selectedPosition - 1);
        }
    }

    private EventDTO createEventFromFormData() {
        // Create location
        Location location = new Location(locationName, locationAddress, locationCity, locationCountry, locationLongitude, locationLatitude);
        
        // Parse dates to Date objects for the API
        Date startDate = null;
        Date endDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            startDate = sdf.parse(eventStartDate);
            endDate = sdf.parse(eventEndDate);
        } catch (Exception e) {
            // Use current date as fallback
            startDate = new Date();
            endDate = new Date();
        }
        
        // Create EventOrganizerDTO with proper role
        RoleDTO role = new RoleDTO();
        role.setId(java.util.UUID.randomUUID().toString()); // Set a proper UUID
        role.setName("EVENT_ORGANIZER");
        
        EventOrganizerDTO organizerDTO = new EventOrganizerDTO();
        // Ensure the organizer ID is a proper UUID format
        String organizerId = organizer.getId();
        try {
            // Try to parse as UUID to validate format
            java.util.UUID.fromString(organizerId);
            organizerDTO.setId(organizerId);
        } catch (IllegalArgumentException e) {
            // If not a valid UUID, generate a new one
            organizerDTO.setId(java.util.UUID.randomUUID().toString());
        }
        organizerDTO.setEmail(organizer.getEmail() != null ? organizer.getEmail() : "organizer@eventify.com");
        organizerDTO.setFirstName(organizer.getFirstName() != null ? organizer.getFirstName() : "Event");
        organizerDTO.setLastName(organizer.getLastName() != null ? organizer.getLastName() : "Organizer");
        organizerDTO.setAddress(organizer.getAddress() != null ? organizer.getAddress() : "Default Address");
        organizerDTO.setPhoneNumber(organizer.getPhoneNumber() != null ? organizer.getPhoneNumber() : "000-000-0000");
        organizerDTO.setProfileImage(organizer.getProfileImage() != null ? organizer.getProfileImage() : "");
        organizerDTO.setRole(role);
        organizerDTO.setRoleName("EVENT_ORGANIZER"); // Set the role.name field for polymorphic deserialization
        
        // Create event DTO
        EventDTO eventDTO = new EventDTO();
        eventDTO.setName(eventName);
        eventDTO.setDescription(eventDescription);
        eventDTO.setImage(""); // Set empty image for now
        eventDTO.setPrivacyType(privacyType);
        eventDTO.setMaxAttendees(maxAttendees);
        eventDTO.setEventStart(startDate);
        eventDTO.setEventEnd(endDate);
        eventDTO.setLocation(location);
        eventDTO.setEventType(selectedEventType);
        eventDTO.setOrganizer(organizerDTO);
        eventDTO.setActivities(activities);
        eventDTO.setPrice(0.0);
        eventDTO.setAttendance(0); // Set initial attendance to 0
        
        return eventDTO;
    }

    private void cancelEvent() {
        requireActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
