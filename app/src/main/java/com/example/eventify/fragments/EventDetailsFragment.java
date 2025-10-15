package com.example.eventify.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventify.databinding.FragmentEventDetailsBinding;
import com.example.eventify.models.events.Activity;
import com.example.eventify.models.events.Event;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.events.EventFavoritesService;
import com.example.eventify.services.pdf.PdfService;
import com.example.eventify.services.users.UserService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.example.eventify.R;

// OpenStreetMap imports
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsFragment extends Fragment {

    private FragmentEventDetailsBinding binding;
    private Event event;
    private boolean isFavorite = false;
    private UUID userId;
    private EventService eventService;
    private UserService userService;
    private EventFavoritesService eventFavoritesService;
    private PdfService pdfService;
    private UserSession userSession;
    private ActivityAdapter activityAdapter;
    private List<Activity> activities = new ArrayList<>();
    
    // Permission launcher for storage access
    private ActivityResultLauncher<String> storagePermissionLauncher;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    public static EventDetailsFragment newInstance(Event event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
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
        }
        
        // Initialize services
        eventService = RetrofitClient.getClient(requireContext()).create(EventService.class);
        userService = RetrofitClient.getClient(requireContext()).create(UserService.class);
        eventFavoritesService = RetrofitClient.getClient(requireContext()).create(EventFavoritesService.class);
        pdfService = RetrofitClient.getClient(requireContext()).create(PdfService.class);
        userSession = new UserSession(requireContext());
        
        // Initialize permission launcher
        storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                Log.d("EventDetails", "Permission result received: " + isGranted);
                if (isGranted) {
                    Log.d("EventDetails", "Storage permission granted, proceeding with PDF download");
                    performPdfDownload();
                } else {
                    Log.e("EventDetails", "Storage permission denied");
                    Toast.makeText(requireContext(), "Storage permission required to download PDF", Toast.LENGTH_LONG).show();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false);
        binding.setEvent(event);
        
        setupViews();
        setupRecyclerView();
        loadEventDetails();
        checkFavoriteStatus();
        initializeMap();
        
        return binding.getRoot();
    }

    private void setupViews() {
        Log.d("EventDetails", "Setting up views");
        // Set up click listeners
        binding.favoriteButton.setOnClickListener(v -> toggleFavorite());
        binding.attendButton.setOnClickListener(v -> attendEvent());
        binding.inviteButton.setOnClickListener(v -> openInvitationDialog());
        
        // Debug export button
        if (binding.exportButton != null) {
            Log.d("EventDetails", "Export button found, setting up click listener");
            binding.exportButton.setClickable(true);
            binding.exportButton.setEnabled(true);
            binding.exportButton.setOnClickListener(v -> {
                Log.d("EventDetails", "Export button click listener triggered");
                Toast.makeText(requireContext(), "Export button clicked!", Toast.LENGTH_SHORT).show();
                exportEvent();
            });
        } else {
            Log.e("EventDetails", "Export button is null!");
        }
        
        binding.chatButton.setOnClickListener(v -> openChat());
    }

    private void setupRecyclerView() {
        activityAdapter = new ActivityAdapter(activities);
        binding.agendaRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.agendaRecyclerView.setAdapter(activityAdapter);
    }

    private void loadEventDetails() {
        if (event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load full event details from API
        eventService.get(event.getId()).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Event fullEvent = response.body();
                    event = fullEvent;
                    binding.setEvent(fullEvent);
                    
                    // Update fields manually since we removed some data binding expressions
                    if (fullEvent.getOrganizer() != null) {
                        String firstName = fullEvent.getOrganizer().getFirstName();
                        String lastName = fullEvent.getOrganizer().getLastName();
                        if (firstName != null && lastName != null) {
                            binding.organizerNameTextView.setText(firstName + " " + lastName);
                        } else if (firstName != null) {
                            binding.organizerNameTextView.setText(firstName);
                        } else if (lastName != null) {
                            binding.organizerNameTextView.setText(lastName);
                        } else {
                            binding.organizerNameTextView.setText("Unknown Organizer");
                        }
                    }
                    
                    if (fullEvent.getPrivacyType() != null) {
                        binding.privacyTextView.setText(fullEvent.getPrivacyType().name());
                    }
                    
                    if (fullEvent.getEventType() != null) {
                        binding.eventTypeTextView.setText(fullEvent.getEventType().getName());
                    }
                    
                    if (fullEvent.getLocation() != null) {
                        binding.locationTextView.setText(fullEvent.getLocation().getAddress());
                    }
                    
                    // Load activities if available
                    if (fullEvent.getActivities() != null) {
                        activities.clear();
                        activities.addAll(fullEvent.getActivities());
                        sortActivities();
                        activityAdapter.notifyDataSetChanged();
                        
                        // Show/hide no agenda message
                        if (activities.isEmpty()) {
                            binding.noAgendaTextView.setVisibility(View.VISIBLE);
                            binding.agendaRecyclerView.setVisibility(View.GONE);
                        } else {
                            binding.noAgendaTextView.setVisibility(View.GONE);
                            binding.agendaRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                    
                    // Load event image
                    loadEventImage();
                } else {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Log.e("EventDetails", "Failed to load event details", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventImage() {
        if (event != null && event.getImage() != null && !event.getImage().isEmpty()) {
            Glide.with(requireContext())
                    .load(event.getImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.eventImageView);
        }
    }


    private void checkFavoriteStatus() {
        userId = userSession.getCurrentUserId();
        if (userId != null && event != null && event.getId() != null) {
            eventFavoritesService.isFavoriteEvent(userId.toString(), event.getId()).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isFavorite = response.body();
                        updateFavoriteButton();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.e("EventDetails", "Failed to check favorite status", t);
                    isFavorite = false;
                    updateFavoriteButton();
                }
            });
        }
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            binding.favoriteButton.setIcon(getResources().getDrawable(R.drawable.ic_favorite, null));
        } else {
            binding.favoriteButton.setIcon(getResources().getDrawable(R.drawable.ic_favorite_border, null));
        }
    }

    private void toggleFavorite() {
        if (userId == null || event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            // Remove from favorites
            eventFavoritesService.removeFromFavoriteEvents(userId.toString(), event.getId()).enqueue(new Callback<com.example.eventify.models.users.User>() {
                @Override
                public void onResponse(Call<com.example.eventify.models.users.User> call, Response<com.example.eventify.models.users.User> response) {
                    if (response.isSuccessful()) {
                        isFavorite = false;
                        updateFavoriteButton();
                        Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.eventify.models.users.User> call, Throwable t) {
                    Log.e("EventDetails", "Failed to remove from favorites", t);
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites
            EventFavoritesService.EventFavoriteRequest request = new EventFavoritesService.EventFavoriteRequest(event.getId());
            eventFavoritesService.addToFavoriteEvents(userId.toString(), request).enqueue(new Callback<com.example.eventify.models.users.User>() {
                @Override
                public void onResponse(Call<com.example.eventify.models.users.User> call, Response<com.example.eventify.models.users.User> response) {
                    if (response.isSuccessful()) {
                        isFavorite = true;
                        updateFavoriteButton();
                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.eventify.models.users.User> call, Throwable t) {
                    Log.e("EventDetails", "Failed to add to favorites", t);
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void attendEvent() {
        if (event == null || event.getId() == null) {
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        eventService.joinEvent(event.getId(), new java.util.HashMap<>()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null && response.body()) {
                    Toast.makeText(requireContext(), "Successfully joined the event!", Toast.LENGTH_SHORT).show();
                    // Update attendance count
                    if (event.getAttendance() < event.getMaxAttendees()) {
                        event.setAttendance(event.getAttendance() + 1);
                        binding.setEvent(event);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to join event", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("EventDetails", "Failed to join event", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openInvitationDialog() {
        // TODO: Implement invitation dialog
        Toast.makeText(requireContext(), "Invitation feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void exportEvent() {
        Log.d("EventDetails", "Export button clicked");
        if (event == null || event.getId() == null) {
            Log.e("EventDetails", "Event or event ID is null");
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        if (!userSession.isLoggedIn()) {
            Log.e("EventDetails", "User not logged in, cannot download PDF");
            Toast.makeText(requireContext(), "Please log in to download PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check storage permission (for Android 10 and below)
        int permissionStatus = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Log.d("EventDetails", "Storage permission status: " + permissionStatus + " (GRANTED=" + PackageManager.PERMISSION_GRANTED + ")");
        Log.d("EventDetails", "Android API level: " + android.os.Build.VERSION.SDK_INT);
        
        // For Android 11+ (API 30+), WRITE_EXTERNAL_STORAGE is automatically granted
        // For Android 10 and below, we need to request it
        if (android.os.Build.VERSION.SDK_INT < 30 && permissionStatus != PackageManager.PERMISSION_GRANTED) {
            Log.d("EventDetails", "Android < 30, requesting storage permission");
            try {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                Log.d("EventDetails", "Permission launcher launched successfully");
            } catch (Exception e) {
                Log.e("EventDetails", "Failed to launch permission request", e);
                Toast.makeText(requireContext(), "Failed to request permission: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        } else if (android.os.Build.VERSION.SDK_INT >= 30) {
            Log.d("EventDetails", "Android >= 30, storage permission automatically granted");
        }
        
        // Permission granted, proceed with download
        performPdfDownload();
    }
    
    private void performPdfDownload() {
        Log.d("EventDetails", "Proceeding with PDF download (using external storage)");

        // Show loading toast
        Toast.makeText(requireContext(), "Generating PDF...", Toast.LENGTH_SHORT).show();
        
        String authToken = userSession.getAuthToken();
        Log.d("EventDetails", "User logged in, auth token present: " + (authToken != null && !authToken.isEmpty()));
        if (authToken != null) {
            Log.d("EventDetails", "Auth token: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");
        }
        Log.d("EventDetails", "Calling PDF service for event ID: " + event.getId());
        pdfService.getEventPdf(event.getId(), "application/pdf").enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                Log.d("EventDetails", "PDF response received. Success: " + response.isSuccessful() + ", Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Log.d("EventDetails", "Saving PDF to Downloads folder");
                        // Save to external Downloads directory (like browser downloads)
                        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Eventify");
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs();
                        }
                        String fileName = event.getName().replaceAll("[^a-zA-Z0-9]", "_") + "_event_details.pdf";
                        File pdfFile = new File(downloadsDir, fileName);

                        Log.d("EventDetails", "PDF file path: " + pdfFile.getAbsolutePath());
                        
                        FileOutputStream fos = new FileOutputStream(pdfFile);
                        byte[] pdfBytes = response.body().bytes();
                        Log.d("EventDetails", "PDF bytes received: " + pdfBytes.length);
                        fos.write(pdfBytes);
                        fos.close();
                        
                        Log.d("EventDetails", "PDF saved successfully");
                        // Share the PDF file
                        sharePdfFile(pdfFile, fileName);
                    } catch (IOException e) {
                        Log.e("EventDetails", "Failed to save PDF", e);
                        Toast.makeText(requireContext(), "Failed to save PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("EventDetails", "Failed to generate PDF. Response code: " + response.code() + ", Message: " + response.message());
                    if (response.body() != null) {
                        try {
                            String errorBody = response.body().string();
                            Log.e("EventDetails", "Error response body: " + errorBody);
                        } catch (IOException e) {
                            Log.e("EventDetails", "Could not read error response body", e);
                        }
                    }
                    
                    // Log request details for debugging
                    Log.e("EventDetails", "Request URL: " + call.request().url());
                    Log.e("EventDetails", "Request headers: " + call.request().headers());
                    
                    String errorMessage = "Failed to generate PDF";
                    if (response.code() == 406) {
                        errorMessage = "PDF service not available or unsupported format";
                    } else if (response.code() == 401) {
                        errorMessage = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMessage = "PDF endpoint not found";
                    }
                    
                    Toast.makeText(requireContext(), errorMessage + " (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.e("EventDetails", "Failed to generate PDF", t);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openChat() {
        // TODO: Implement chat functionality
        Toast.makeText(requireContext(), "Chat feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void sortActivities() {
        Collections.sort(activities, new Comparator<Activity>() {
            @Override
            public int compare(Activity a, Activity b) {
                // Sort by start date and time
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String dateTimeA = a.getStartDate() + " " + a.getStartTime();
                    String dateTimeB = b.getStartDate() + " " + b.getStartTime();
                    return dateFormat.parse(dateTimeA).compareTo(dateFormat.parse(dateTimeB));
                } catch (Exception e) {
                    return 0;
                }
            }
        });
    }

    // Simple Activity Adapter for RecyclerView
    private static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
        private List<Activity> activities;

        public ActivityAdapter(List<Activity> activities) {
            this.activities = activities;
        }

        @NonNull
        @Override
        public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ActivityViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
            Activity activity = activities.get(position);
            holder.title.setText(activity.getName());
            holder.subtitle.setText(activity.getDescription());
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        static class ActivityViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView title;
            android.widget.TextView subtitle;

            ActivityViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(android.R.id.text1);
                subtitle = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void initializeMap() {
        if (event == null || event.getLocation() == null) {
            return;
        }

        // Initialize osmdroid configuration
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", 0));

        MapView mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Set the map center to the event location
        double latitude = event.getLocation().getLatitude();
        double longitude = event.getLocation().getLongitude();
        
        if (latitude != 0.0 && longitude != 0.0) {
            GeoPoint eventLocation = new GeoPoint(latitude, longitude);
            mapView.getController().setCenter(eventLocation);
            mapView.getController().setZoom(15);

            // Add a marker for the event location
            Marker eventMarker = new Marker(mapView);
            eventMarker.setPosition(eventLocation);
            eventMarker.setTitle(event.getName());
            eventMarker.setSnippet(event.getLocation().getAddress());
            mapView.getOverlays().add(eventMarker);
        }
    }

    private void sharePdfFile(File pdfFile, String fileName) {
        try {
            Log.d("EventDetails", "Attempting to open PDF: " + pdfFile.getAbsolutePath());
            Log.d("EventDetails", "File exists: " + pdfFile.exists() + ", Size: " + pdfFile.length());
            
            // Use FileProvider for proper URI handling on all Android versions
            Uri fileUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                pdfFile
            );
            
            Log.d("EventDetails", "File URI: " + fileUri);

            // Create intent to open PDF (like file manager does)
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, "application/pdf");
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Try to launch the intent directly first
            try {
                Log.d("EventDetails", "Attempting to launch PDF viewer directly");
                startActivity(openIntent);
                Toast.makeText(requireContext(), "PDF opened: " + fileName, Toast.LENGTH_SHORT).show();
                return;
            } catch (Exception e) {
                Log.d("EventDetails", "Direct launch failed, trying chooser: " + e.getMessage());
            }

            // If direct launch fails, try with chooser
            try {
                Intent chooserIntent = Intent.createChooser(openIntent, "Open PDF with:");
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                Log.d("EventDetails", "Launching PDF chooser");
                startActivity(chooserIntent);
                Toast.makeText(requireContext(), "PDF downloaded and opening: " + fileName, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e("EventDetails", "Both direct and chooser launch failed", e);
                Toast.makeText(requireContext(), "PDF downloaded to Downloads: " + fileName, Toast.LENGTH_LONG).show();
                Log.d("EventDetails", "PDF downloaded to: " + pdfFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e("EventDetails", "Failed to open PDF", e);
            Toast.makeText(requireContext(), "PDF downloaded but couldn't open: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
