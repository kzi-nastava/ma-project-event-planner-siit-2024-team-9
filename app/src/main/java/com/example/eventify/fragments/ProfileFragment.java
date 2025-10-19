package com.example.eventify.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventify.R;
import com.example.eventify.activities.LoginActivity;
import com.example.eventify.adapters.CalendarEventAdapter;
import com.example.eventify.adapters.EventListAdapter;
import com.example.eventify.adapters.SolutionListAdapter;
import com.example.eventify.databinding.FragmentProfileBinding;
import com.example.eventify.fragments.EventDetailsFragment;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.users.User;
import com.example.eventify.models.users.EventOrganizerDTO;
import com.example.eventify.models.users.BusinessOwnerDTO;
import com.example.eventify.models.enums.UserRole;
import com.example.eventify.services.auth.LoginService;
import com.example.eventify.services.users.UserService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    
    private FragmentProfileBinding binding;
    private User currentUser;
    private List<Event> favoriteEvents = new ArrayList<>();
    private List<Solution> favoriteSolutions = new ArrayList<>();
    
    private EventListAdapter eventAdapter;
    private SolutionListAdapter solutionAdapter;
    
    private UserService userService;
    
    // Calendar related fields
    private Calendar currentCalendar;
    private List<Event> calendarEvents = new ArrayList<>();
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
    
    // Image handling
    private static final int PICK_IMAGE_REQUEST = 1001;
    private String selectedImageBase64 = null;

    public ProfileFragment() { }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = RetrofitClient.getClient(requireContext()).create(UserService.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        setupRecyclerViews();
        setupCalendar();
        loadUserData();
        setupLogoutButton();
        setupProfileEditing();
        setupFieldVisibility();
    }

    private void setupRecyclerViews() {
        // Setup favorite events RecyclerView
        eventAdapter = new EventListAdapter(requireContext(), favoriteEvents, event -> {
            Log.d(TAG, "Event click callback received for: " + event.getName());
            Log.d(TAG, "Event ID: " + event.getId());
            Log.d(TAG, "Event name: " + event.getName());
            try {
                // Navigate to EventDetailsFragment
                Log.d(TAG, "Creating EventDetailsFragment...");
                EventDetailsFragment eventDetailsFragment = EventDetailsFragment.newInstance(event);
                Log.d(TAG, "EventDetailsFragment created successfully");
                
                Log.d(TAG, "Getting FragmentManager...");
                androidx.fragment.app.FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                Log.d(TAG, "FragmentManager obtained: " + (fragmentManager != null ? "SUCCESS" : "NULL"));
                
                Log.d(TAG, "Starting fragment transaction...");
                fragmentManager.beginTransaction()
                        .replace(R.id.home_container, eventDetailsFragment)
                        .addToBackStack(null)
                        .commit();
                Log.d(TAG, "Fragment transaction committed successfully");
                Log.d(TAG, "Navigation to EventDetailsFragment initiated");
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to EventDetailsFragment", e);
                e.printStackTrace();
                Toast.makeText(requireContext(), "Error opening event details: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        binding.recyclerViewFavoriteEvents.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFavoriteEvents.setAdapter(eventAdapter);

        // Setup favorite solutions RecyclerView
        solutionAdapter = new SolutionListAdapter(requireContext(), favoriteSolutions, getParentFragmentManager());
        binding.recyclerViewFavoriteSolutions.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewFavoriteSolutions.setAdapter(solutionAdapter);
    }

    private void setupCalendar() {
        currentCalendar = Calendar.getInstance();
        updateCalendarDisplay();
        
        // Setup navigation buttons
        binding.btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarDisplay();
            // Reload calendar events for the new month
            if (currentUser != null) {
                UserSession userSession = new UserSession(requireContext());
                String userId = userSession.getCurrentUserId() != null ? userSession.getCurrentUserId().toString() : null;
                if (userId != null) {
                    loadCalendarEvents(userId);
                }
            }
        });
        
        binding.btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarDisplay();
            // Reload calendar events for the new month
            if (currentUser != null) {
                UserSession userSession = new UserSession(requireContext());
                String userId = userSession.getCurrentUserId() != null ? userSession.getCurrentUserId().toString() : null;
                if (userId != null) {
                    loadCalendarEvents(userId);
                }
            }
        });
    }

    private void updateCalendarDisplay() {
        binding.textViewCurrentMonth.setText(monthYearFormat.format(currentCalendar.getTime()));
        populateCalendarGrid();
    }

    private void populateCalendarGrid() {
        binding.calendarGrid.removeAllViews();
        
        // Add day headers
        String[] dayHeaders = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : dayHeaders) {
            TextView dayHeader = new TextView(requireContext());
            dayHeader.setText(day);
            dayHeader.setTextSize(12);
            dayHeader.setTextColor(getResources().getColor(android.R.color.black));
            dayHeader.setGravity(android.view.Gravity.CENTER);
            dayHeader.setPadding(8, 12, 8, 12);
            dayHeader.setBackgroundResource(R.drawable.calendar_day_header);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayHeader.setLayoutParams(params);
            binding.calendarGrid.addView(dayHeader);
        }
        
        // Get first day of month and number of days
        Calendar firstDay = Calendar.getInstance();
        firstDay.setTime(currentCalendar.getTime());
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Add empty cells for days before the first day of the month
        for (int i = 1; i < firstDayOfWeek; i++) {
            TextView emptyDay = new TextView(requireContext());
            emptyDay.setText("");
            emptyDay.setPadding(8, 8, 8, 8);
            emptyDay.setBackgroundResource(R.drawable.calendar_day_cell);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 80;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emptyDay.setLayoutParams(params);
            binding.calendarGrid.addView(emptyDay);
        }
        
        // Add day cells
        for (int day = 1; day <= daysInMonth; day++) {
            TextView dayCell = createDayCell(day);
            binding.calendarGrid.addView(dayCell);
        }
    }

    private TextView createDayCell(int day) {
        TextView dayCell = new TextView(requireContext());
        dayCell.setText(String.valueOf(day));
        dayCell.setTextSize(14);
        dayCell.setGravity(android.view.Gravity.CENTER);
        dayCell.setPadding(8, 8, 8, 8);
        dayCell.setMinHeight(80);
        
        // Check if this day has events
        Calendar dayCalendar = Calendar.getInstance();
        dayCalendar.setTime(currentCalendar.getTime());
        dayCalendar.set(Calendar.DAY_OF_MONTH, day);
        
        boolean hasEvents = hasEventsOnDay(dayCalendar);
        if (hasEvents) {
            dayCell.setBackgroundResource(R.drawable.calendar_day_cell_with_events);
            dayCell.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            dayCell.setBackgroundResource(R.drawable.calendar_day_cell);
            dayCell.setTextColor(getResources().getColor(android.R.color.black));
        }
        
        // Add hover effect
        dayCell.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.setAlpha(0.7f);
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP || 
                      event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.setAlpha(1.0f);
            }
            return false;
        });
        
        // Add click listener
        dayCell.setOnClickListener(v -> {
            showEventsForDay(dayCalendar);
        });
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 80;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        dayCell.setLayoutParams(params);
        
        return dayCell;
    }

    private boolean hasEventsOnDay(Calendar day) {
        for (Event event : calendarEvents) {
            if (event.getEventStart() != null) {
                Calendar eventDate = Calendar.getInstance();
                eventDate.setTime(event.getEventStart());
                
                if (eventDate.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                    eventDate.get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                    eventDate.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showEventsForDay(Calendar day) {
        List<Event> dayEvents = new ArrayList<>();
        for (Event event : calendarEvents) {
            if (event.getEventStart() != null) {
                Calendar eventDate = Calendar.getInstance();
                eventDate.setTime(event.getEventStart());
                
                if (eventDate.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                    eventDate.get(Calendar.MONTH) == day.get(Calendar.MONTH) &&
                    eventDate.get(Calendar.DAY_OF_MONTH) == day.get(Calendar.DAY_OF_MONTH)) {
                    dayEvents.add(event);
                }
            }
        }
        
        showEventsDialog(day, dayEvents);
    }

        private void showEventsDialog(Calendar day, List<Event> dayEvents) {
            Dialog dialog = new Dialog(requireContext());
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calendar_events, null);
            dialog.setContentView(dialogView);
            
            // Set dialog date
            TextView dialogDate = dialogView.findViewById(R.id.textViewDialogDate);
            SimpleDateFormat dialogDateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
            dialogDate.setText("Events for " + dialogDateFormat.format(day.getTime()));
            
            // Setup RecyclerView
            androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDialogEvents);
            TextView noEventsText = dialogView.findViewById(R.id.textViewNoEventsDialog);
            
            if (dayEvents.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                noEventsText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noEventsText.setVisibility(View.GONE);
                
                CalendarEventAdapter adapter = new CalendarEventAdapter(requireContext(), dayEvents, getParentFragmentManager(), dialog);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
            }
            
            // Close button
            Button closeButton = dialogView.findViewById(R.id.btnCloseDialog);
            closeButton.setOnClickListener(v -> dialog.dismiss());
            
            dialog.show();
        }

    private void loadUserData() {
        UserSession userSession = new UserSession(requireContext());
        String userId = userSession.getCurrentUserId() != null ? userSession.getCurrentUserId().toString() : null;
        
        Log.d(TAG, "Loading user data...");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Is logged in: " + userSession.isLoggedIn());
        Log.d(TAG, "User email: " + userSession.getUserEmail());
        Log.d(TAG, "Auth token: " + (userSession.getAuthToken() != null ? "Present" : "Missing"));
        
        if (userId == null) {
            Log.e(TAG, "User ID not found in session");
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Making API call to: users/" + userId);
        
        // Load user profile
        userService.get(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d(TAG, "API Response - Code: " + response.code());
                Log.d(TAG, "API Response - Success: " + response.isSuccessful());
                Log.d(TAG, "API Response - Body: " + (response.body() != null ? "Present" : "Null"));
                
                    if (response.isSuccessful() && response.body() != null) {
                        currentUser = response.body();
                        Log.d(TAG, "User loaded successfully: " + currentUser.getEmail());
                        updateUserProfile();
                        setupFieldVisibility(); // Set field visibility after user is loaded
                        loadFavoriteEvents(userId);
                        loadFavoriteSolutions(userId);
                        loadCalendarEvents(userId);
                } else {
                    Log.e(TAG, "Failed to load user profile: " + response.code());
                    Log.e(TAG, "Error body: " + response.errorBody());
                    Log.e(TAG, "Response headers: " + response.headers());
                    
                    if (response.code() == 404) {
                        Toast.makeText(requireContext(), "User not found. Please logout and login again.", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "User ID " + userId + " not found in backend. This might be an invalid session.");
                    } else {
                        Toast.makeText(requireContext(), "Failed to load profile: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error loading user profile", t);
                Log.e(TAG, "Request URL: " + call.request().url());
                Log.e(TAG, "Request headers: " + call.request().headers());
                Toast.makeText(requireContext(), "Error loading profile: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUserProfile() {
        if (currentUser == null) {
            Log.e(TAG, "currentUser is null, cannot update profile");
            return;
        }

        Log.d(TAG, "Updating user profile UI...");
        Log.d(TAG, "User data - Name: " + getDisplayName());
        Log.d(TAG, "User data - Email: " + currentUser.getEmail());
        Log.d(TAG, "User data - Phone: " + currentUser.getPhoneNumber());
        Log.d(TAG, "User data - Address: " + currentUser.getAddress());
        Log.d(TAG, "User data - Role: " + currentUser.getRoleName());

        try {
            // Update form fields
            binding.editTextFirstName.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
            binding.editTextLastName.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
            binding.editTextEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            binding.editTextPhone.setText(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
            binding.editTextAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
            binding.textViewUserRole.setText(currentUser.getRoleName() != null ? currentUser.getRoleName() : "");
            
            // Update profile image
            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(currentUser.getProfileImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    binding.imageViewProfile.setImageBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading profile image", e);
                    // Keep default image if loading fails
                }
            }
            
            Log.d(TAG, "Profile UI updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile UI", e);
        }
    }

    private String getDisplayName() {
        if (currentUser.getFirstName() != null && currentUser.getLastName() != null) {
            return currentUser.getFirstName() + " " + currentUser.getLastName();
        }
        return currentUser.getEmail();
    }

    private void loadFavoriteEvents(String userId) {
        Log.d(TAG, "Loading favorite events for user: " + userId);
        userService.getFavoriteEvents(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                Log.d(TAG, "Favorite events API response - Code: " + response.code());
                Log.d(TAG, "Favorite events API response - Success: " + response.isSuccessful());
                Log.d(TAG, "Favorite events API response - Body: " + (response.body() != null ? "Present" : "Null"));
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Favorite events count: " + response.body().size());
                    for (int i = 0; i < response.body().size(); i++) {
                        Event event = response.body().get(i);
                        Log.d(TAG, "Event " + i + " - Name: " + event.getName());
                        Log.d(TAG, "Event " + i + " - EventStart: " + (event.getEventStart() != null ? event.getEventStart().toString() : "NULL"));
                        Log.d(TAG, "Event " + i + " - EventEnd: " + (event.getEventEnd() != null ? event.getEventEnd().toString() : "NULL"));
                        Log.d(TAG, "Event " + i + " - Location: " + (event.getLocation() != null ? event.getLocation().getName() : "NULL"));
                        if (event.getLocation() != null) {
                            Log.d(TAG, "Event " + i + " - Location Latitude: " + event.getLocation().getLatitude());
                            Log.d(TAG, "Event " + i + " - Location Longitude: " + event.getLocation().getLongitude());
                        }
                    }
                    
                    favoriteEvents.clear();
                    favoriteEvents.addAll(response.body());
                    
                    // Try to catch the crash here
                    try {
                        Log.d(TAG, "About to call eventAdapter.notifyDataSetChanged()");
                        eventAdapter.notifyDataSetChanged();
                        Log.d(TAG, "eventAdapter.notifyDataSetChanged() completed successfully");
                    } catch (Exception e) {
                        Log.e(TAG, "Error in eventAdapter.notifyDataSetChanged()", e);
                        e.printStackTrace();
                    }
                    
                    if (favoriteEvents.isEmpty()) {
                        binding.textViewNoFavoriteEvents.setVisibility(View.VISIBLE);
                        binding.recyclerViewFavoriteEvents.setVisibility(View.GONE);
                    } else {
                        binding.textViewNoFavoriteEvents.setVisibility(View.GONE);
                        binding.recyclerViewFavoriteEvents.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to load favorite events: " + response.code());
                    binding.textViewNoFavoriteEvents.setVisibility(View.VISIBLE);
                    binding.recyclerViewFavoriteEvents.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e(TAG, "Error loading favorite events", t);
                binding.textViewNoFavoriteEvents.setVisibility(View.VISIBLE);
                binding.recyclerViewFavoriteEvents.setVisibility(View.GONE);
            }
        });
    }

    private void loadFavoriteSolutions(String userId) {
        userService.getFavorites(userId).enqueue(new Callback<List<Solution>>() {
            @Override
            public void onResponse(Call<List<Solution>> call, Response<List<Solution>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    favoriteSolutions.clear();
                    favoriteSolutions.addAll(response.body());
                    solutionAdapter.notifyDataSetChanged();
                    
                    if (favoriteSolutions.isEmpty()) {
                        binding.textViewNoFavoriteSolutions.setVisibility(View.VISIBLE);
                        binding.recyclerViewFavoriteSolutions.setVisibility(View.GONE);
                    } else {
                        binding.textViewNoFavoriteSolutions.setVisibility(View.GONE);
                        binding.recyclerViewFavoriteSolutions.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "Failed to load favorite solutions: " + response.code());
                    binding.textViewNoFavoriteSolutions.setVisibility(View.VISIBLE);
                    binding.recyclerViewFavoriteSolutions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Solution>> call, Throwable t) {
                Log.e(TAG, "Error loading favorite solutions", t);
                binding.textViewNoFavoriteSolutions.setVisibility(View.VISIBLE);
                binding.recyclerViewFavoriteSolutions.setVisibility(View.GONE);
            }
        });
    }

    private void loadCalendarEvents(String userId) {
        Log.d(TAG, "Loading calendar events for user: " + userId);
        userService.getCalendarEvents(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                Log.d(TAG, "Calendar events API response - Code: " + response.code());
                Log.d(TAG, "Calendar events API response - Success: " + response.isSuccessful());
                Log.d(TAG, "Calendar events API response - Body: " + (response.body() != null ? "Present" : "Null"));
                
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Calendar events count: " + response.body().size());
                    calendarEvents.clear();
                    calendarEvents.addAll(response.body());
                    
                    // Update calendar display with events
                    updateCalendarDisplay();
                    
                    if (calendarEvents.isEmpty()) {
                        binding.textViewNoCalendarEvents.setVisibility(View.VISIBLE);
                    } else {
                        binding.textViewNoCalendarEvents.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(TAG, "Failed to load calendar events: " + response.code());
                    binding.textViewNoCalendarEvents.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e(TAG, "Error loading calendar events", t);
                binding.textViewNoCalendarEvents.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupFieldVisibility() {
        // Set initial field visibility based on user type
        if (currentUser != null) {
            boolean isBusinessOwner = currentUser.getRole().getName() == UserRole.BUSINESS_OWNER;
            boolean isAdmin = currentUser.getRole().getName() == UserRole.ADMIN;
            
            if (isBusinessOwner) {
                // Business owners: hide first/last name fields completely
                ((View) binding.editTextFirstName.getParent()).setVisibility(View.GONE);
                ((View) binding.editTextLastName.getParent()).setVisibility(View.GONE);
            } else {
                // Event organizers: show first/last name fields
                ((View) binding.editTextFirstName.getParent()).setVisibility(View.VISIBLE);
                ((View) binding.editTextLastName.getParent()).setVisibility(View.VISIBLE);
            }
            
            // Hide edit button for admins
            if (isAdmin) {
                binding.btnEditProfile.setVisibility(View.GONE);
            } else {
                binding.btnEditProfile.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupProfileEditing() {
        // Edit button click listener
        binding.btnEditProfile.setOnClickListener(v -> {
            enableEditing(true);
        });

        // Save button click listener
        binding.btnSaveProfile.setOnClickListener(v -> {
            saveProfile();
        });

        // Cancel button click listener
        binding.btnCancelEdit.setOnClickListener(v -> {
            enableEditing(false);
            // Reset form to original values
            updateUserProfile();
            // Reset image selection
            selectedImageBase64 = null;
        });

        // Profile image click listener
        binding.imageViewProfile.setOnClickListener(v -> {
            boolean isAdmin = currentUser != null && currentUser.getRole().getName() == UserRole.ADMIN;
            if (!isAdmin && binding.editTextPhone.isEnabled()) {
                // Only allow image selection in edit mode and not for admins
                selectImage();
            }
        });
    }

    private void enableEditing(boolean enabled) {
        // Check user role to determine which fields can be edited
        boolean isBusinessOwner = currentUser != null && currentUser.getRole().getName() == UserRole.BUSINESS_OWNER;
        boolean isAdmin = currentUser != null && currentUser.getRole().getName() == UserRole.ADMIN;
        
        // Admins cannot edit their profile at all
        if (isAdmin) {
            return;
        }
        
        // Show/hide first/last name fields based on user type
        if (isBusinessOwner) {
            // Business owners: hide first/last name fields completely
            ((View) binding.editTextFirstName.getParent()).setVisibility(View.GONE);
            ((View) binding.editTextLastName.getParent()).setVisibility(View.GONE);
        } else {
            // Event organizers: show first/last name fields and enable/disable based on edit mode
            ((View) binding.editTextFirstName.getParent()).setVisibility(View.VISIBLE);
            ((View) binding.editTextLastName.getParent()).setVisibility(View.VISIBLE);
            binding.editTextFirstName.setEnabled(enabled);
            binding.editTextLastName.setEnabled(enabled);
        }
        
        // Phone and address can be edited by both types (except admins)
        binding.editTextPhone.setEnabled(enabled);
        binding.editTextAddress.setEnabled(enabled);
        
        // Email is always disabled (cannot be changed)
        binding.editTextEmail.setEnabled(false);

        // Show/hide action buttons
        binding.layoutActionButtons.setVisibility(enabled ? View.VISIBLE : View.GONE);
        
        // Change edit button text
        binding.btnEditProfile.setText(enabled ? "Cancel" : "Edit");
    }

    private void saveProfile() {
        // Get form values
        String firstName = binding.editTextFirstName.getText().toString().trim();
        String lastName = binding.editTextLastName.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String address = binding.editTextAddress.getText().toString().trim();

        // Basic validation based on user type
        boolean isBusinessOwner = currentUser != null && currentUser.getRole().getName() == UserRole.BUSINESS_OWNER;
        if (!isBusinessOwner && (firstName.isEmpty() || lastName.isEmpty())) {
            Toast.makeText(requireContext(), "First name and last name are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update user object
        if (currentUser != null) {
            currentUser.setPhoneNumber(phone);
            currentUser.setAddress(address);
            
            // Update profile image if selected
            if (selectedImageBase64 != null) {
                currentUser.setProfileImage(selectedImageBase64);
            }

            // Update role-specific fields
            if (!isBusinessOwner) {
                // Event organizers can update first/last name
                currentUser.setFirstName(firstName);
                currentUser.setLastName(lastName);
            }

            // Make API call to update user profile
            UserSession userSession = new UserSession(requireContext());
            String userId = userSession.getCurrentUserId() != null ? userSession.getCurrentUserId().toString() : null;
            
            if (userId != null) {
                // Use appropriate DTO based on user type
                if (isBusinessOwner) {
                    BusinessOwnerDTO businessOwnerDTO = new BusinessOwnerDTO(currentUser);
                    userService.updateBusinessOwner(userId, businessOwnerDTO).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            handleUpdateResponse(response);
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            handleUpdateFailure(t);
                        }
                    });
                } else {
                    EventOrganizerDTO eventOrganizerDTO = new EventOrganizerDTO(currentUser);
                    userService.updateEventOrganizer(userId, eventOrganizerDTO).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            handleUpdateResponse(response);
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            handleUpdateFailure(t);
                        }
                    });
                }
            } else {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleUpdateResponse(Response<User> response) {
        if (response.isSuccessful() && response.body() != null) {
            currentUser = response.body();
            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            enableEditing(false);
            selectedImageBase64 = null;
        } else {
            Log.e(TAG, "Failed to update profile: " + response.code());
            Toast.makeText(requireContext(), "Failed to update profile: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdateFailure(Throwable t) {
        Log.e(TAG, "Error updating profile", t);
        Toast.makeText(requireContext(), "Error updating profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                
                // Resize image to reasonable size
                Bitmap resizedBitmap = resizeBitmap(bitmap, 300, 300);
                
                // Convert to base64
                selectedImageBase64 = bitmapToBase64(resizedBitmap);
                
                // Update image view
                binding.imageViewProfile.setImageBitmap(resizedBitmap);
                
                Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error loading image", e);
                Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void setupLogoutButton() {
        binding.btnLogout.setOnClickListener(view -> {
            LoginService loginService = new LoginService(requireContext());
            loginService.logout();

            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
