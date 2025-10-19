package com.example.eventify.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.eventify.R;
import com.example.eventify.adapters.ImageListAdapter;
import com.example.eventify.adapters.ReviewAdapter;
import com.example.eventify.databinding.FragmentServiceDetailsBinding;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.others.Purchase;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.solutions.Review;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.users.User;
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.solutions.ProductService;
import com.example.eventify.services.solutions.ReviewService;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.services.users.UserService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.example.eventify.utils.JwtUtils;
import com.example.eventify.activities.MainActivity;
import com.example.eventify.utils.NavigationManager;
import com.example.eventify.models.enums.UserRole;
import com.example.eventify.models.users.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsFragment extends Fragment {

    private ImageListAdapter adapter;

    private boolean isFavorite = false;
    private FragmentServiceDetailsBinding binding;

    private Solution showedSolution;
    private Service showedService;
    private Product showedProduct;

    String[] events;

    Event[] organizerEvents;

    private EventService eventService;
    private UserService userService;
    private ReviewService reviewService;
    private UserSession userSession;

    private boolean serviceDetails = true;

    private boolean isPurchased = false;
    private boolean isReserved = false;
    private Purchase purchase;

    private NavigationManager navigationManager;

    public ServiceDetailsFragment() {
        // Required empty public constructor
    }

    public static ServiceDetailsFragment newInstance(Solution solution) {
        ServiceDetailsFragment fragment = new ServiceDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("solution", solution);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context appCtx = requireContext().getApplicationContext();
        eventService  = RetrofitClient.getClient(appCtx).create(EventService.class);
        userService   = RetrofitClient.getClient(appCtx).create(UserService.class);
        reviewService = RetrofitClient.getClient(appCtx).create(ReviewService.class);

        userSession = new UserSession(requireContext());

        if (requireActivity() instanceof NavigationManager) {
            navigationManager = (NavigationManager) requireActivity();
        }

        getParentFragmentManager().setFragmentResultListener(
                BookServiceDialogFragment.TAG, this,
                (requestKey, bundle) -> {
                    String reservationId = bundle.getString("reservationId");
                    String action = bundle.getString("action");
                    if (reservationId != null) {
                        Toast.makeText(requireContext(), "Reservation created: " + reservationId, Toast.LENGTH_LONG).show();
                        // Mark service as reserved and enable review functionality
                        isReserved = true;
                        reviewPermission();
                    } else if ("CREATE_EVENT".equals(action)) {
                        if (navigationManager != null) {
                            // navigationManager.navigateTo(CreateEventFragment.newInstance());
                        } else {
                            Toast.makeText(requireContext(), "Open event creation screen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            showedSolution = getArguments().getParcelable("solution");
            binding.setService(showedSolution);
            binding.setLifecycleOwner(this);

            // Check visibility - if not visible, show message and return
            if (!showedSolution.isVisibility()) {
                Toast.makeText(requireContext(), "This item is no longer available", Toast.LENGTH_SHORT).show();
                if (navigationManager != null) {
                    navigationManager.navigateBack();
                }
                return binding.getRoot();
            }

            // Check availability and update UI
            if (!showedSolution.isAvailability()) {
                binding.availabilityStatus.setVisibility(View.VISIBLE);
                binding.availabilityStatus.setText("Currently Unavailable");
                binding.btnBook.setEnabled(false);
                binding.btnBook.setAlpha(0.5f);
            } else {
                binding.availabilityStatus.setVisibility(View.VISIBLE);
                binding.availabilityStatus.setText("Available");
            }
        }

        // Check if solution is already favorited
        checkFavoriteStatus();

        detailsBtnHandler();
        binding.right.setOnClickListener(v -> detailsBtnHandler());
        binding.left.setOnClickListener(v -> openChat());

        binding.favorite.setOnClickListener(v -> toggleFavorite());

//        samo zbog testiranja, da mogu da submit
//        binding.submitReview.setVisibility(View.GONE);

        adapter = new ImageListAdapter(requireContext(), showedSolution.getImages(), getParentFragmentManager());
        binding.recyclerView.setAdapter(adapter);

        binding.submitReview.setOnClickListener(v -> submitReview());

        setupCommentsSection();
        loadSolutionReviews();
        
        // Initialize review permission (will be updated when purchase/reservation status is checked)
        reviewPermission();

        return binding.getRoot();
    }

    private void checkFavoriteStatus() {
        if (userSession.isValidSession()) {
            UUID userId = userSession.getCurrentUserId();
            UUID solutionId = showedSolution.getId();
            
            userService.isFavorite(userId, solutionId).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        isFavorite = response.body();
                        binding.favorite.setSelected(isFavorite);
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    // Handle failure silently or show a message
                }
            });
        }
    }

    private void toggleFavorite() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        UUID userId = userSession.getCurrentUserId();
        UUID solutionId = showedSolution.getId();

        if (isFavorite) {
            // Remove from favorites
            userService.removeFromFavorites(userId, solutionId).enqueue(new Callback<com.example.eventify.models.users.User>() {
                @Override
                public void onResponse(Call<com.example.eventify.models.users.User> call, Response<com.example.eventify.models.users.User> response) {
                    if (response.isSuccessful()) {
                        isFavorite = false;
                        binding.favorite.setSelected(isFavorite);
                        Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.eventify.models.users.User> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error removing from favorites", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites
            userService.addToFavorites(userId, showedSolution).enqueue(new Callback<com.example.eventify.models.users.User>() {
                @Override
                public void onResponse(Call<com.example.eventify.models.users.User> call, Response<com.example.eventify.models.users.User> response) {
                    if (response.isSuccessful()) {
                        isFavorite = true;
                        binding.favorite.setSelected(isFavorite);
                        Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.example.eventify.models.users.User> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error adding to favorites", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void buyProductWithEventSelection() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to buy products", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if product is available
        if (!showedSolution.isAvailability()) {
            Toast.makeText(requireContext(), "This product is currently unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user's events using getByOwner
        UUID currentUserId = userSession.getCurrentUserId();
        
        eventService.getByOwner(currentUserId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                android.util.Log.d("ServiceDetails", "Response code: " + response.code());
                android.util.Log.d("ServiceDetails", "Response message: " + response.message());
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> userEvents = response.body();
                    android.util.Log.d("ServiceDetails", "Successfully loaded " + userEvents.size() + " events");
                    
                    if (userEvents.isEmpty()) {
                        Toast.makeText(requireContext(), "You need to create an event first to buy products", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // Update events arrays
                    int i = 0;
                    events = new String[userEvents.size()];
                    organizerEvents = new Event[userEvents.size()];
                    for (Event e : userEvents) {
                        events[i] = e.getName();
                        organizerEvents[i] = e;
                        i++;
                    }
                    
                    // Show event selection dialog
                    showEventSelectionForPurchase();
                } else {
                    android.util.Log.e("ServiceDetails", "Response not successful. Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            android.util.Log.e("ServiceDetails", "Error body: " + errorString);
                        } catch (Exception e) {
                            android.util.Log.e("ServiceDetails", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(requireContext(), "Failed to load your events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                android.util.Log.e("ServiceDetails", "Request failed with throwable: " + t.getClass().getSimpleName(), t);
                android.util.Log.e("ServiceDetails", "Full error: " + t.toString());
                android.util.Log.e("ServiceDetails", "Message: " + t.getMessage());
                
                String errorMsg = "";
                if (t instanceof java.net.ConnectException) {
                    errorMsg += "Cannot connect to server";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMsg += "Cannot resolve server address";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg += "Connection timeout";
                } else if (t instanceof retrofit2.HttpException) {
                    retrofit2.HttpException httpException = (retrofit2.HttpException) t;
                    errorMsg += "HTTP " + httpException.code() + " - " + httpException.message();
                } else if (t instanceof com.google.gson.JsonSyntaxException) {
                    com.google.gson.JsonSyntaxException jsonException = (com.google.gson.JsonSyntaxException) t;
                    errorMsg += "JSON Parse Error: " + jsonException.getMessage();
                    android.util.Log.e("ServiceDetails", "JSON parsing failed", jsonException);
                } else {
                    errorMsg += t.getClass().getSimpleName();
                    if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                        errorMsg += " - " + t.getMessage();
                    }
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEventSelectionForPurchase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select your event to buy this product");

        builder.setItems(events, (dialog, which) -> {
            Event selectedEvent = organizerEvents[which];
            executePurchaseForEvent(selectedEvent);
        });

        builder.show();
    }

    private void executePurchaseForEvent(Event selectedEvent) {
        eventService.getBudget(selectedEvent.getId()).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Budget budget = response.body();
                    BudgetService budgetService =RetrofitClient.getClient(requireContext()).create(BudgetService.class);
                    budgetService.buy(UUID.fromString(budget.getId()), showedProduct).enqueue(new Callback<Budget>() {
                        @Override
                        public void onResponse(Call<Budget> call, Response<Budget> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(requireContext(), "Product purchased successfully for event: " + selectedEvent.getName(), Toast.LENGTH_LONG).show();
                                isPurchased = true;
                                reviewPermission();
                            } else {
                                Toast.makeText(requireContext(), "Failed to purchase product", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Budget> call, Throwable t) {
                            Toast.makeText(requireContext(), "Error purchasing product", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "Failed to get event budget", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {
                Toast.makeText(requireContext(), "Error getting event budget", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void detailsBtnHandler() {

        FrameLayout details = binding.details;
        ViewGroup.LayoutParams params = details.getLayoutParams();

        if (serviceDetails) {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    600,
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("About us");
            details.setLayoutParams(params);
            if (showedSolution.isService()) {
                setService();
                binding.btnBook.setText("Book service");
                binding.btnBook.setOnClickListener(v -> openBookServiceDialog());
            }
            else {
                setProduct();
                binding.btnBook.setText("Buy Product");
                binding.btnBook.setOnClickListener(v -> buyProductWithEventSelection());
            }
            // Load solution-specific comments when showing solution details
            loadSolutionReviews();
        } else {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    630,
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("Solution");
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), PupDetailsForm.newInstance(showedSolution.getOwner()));
            serviceDetails = true;
            transaction.commit();
            // Load business owner comments when showing about us
            loadReviewsByOwner();
        }

    }

    private void openBookServiceDialog() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to book services", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!showedSolution.isAvailability()) {
            Toast.makeText(requireContext(), "This service is currently unavailable", Toast.LENGTH_SHORT).show();
            return;
        }
        if (showedService == null) {
            Toast.makeText(requireContext(), "Loading service…", Toast.LENGTH_SHORT).show();
            return;
        }
        if (showedSolution.getOwner() == null || showedSolution.getOwner().getId() == null) {
            Toast.makeText(requireContext(), "Missing provider", Toast.LENGTH_SHORT).show();
            return;
        }

        UUID serviceId  = showedService.getId();
        UUID providerId = UUID.fromString(showedSolution.getOwner().getId());
        UUID customerId = userSession.getCurrentUserId();

        BookServiceDialogFragment dialog = BookServiceDialogFragment.newInstance(
                serviceId,
                providerId,
                customerId,
                null,
                showedSolution.getName(),
                showedSolution.getOwner().getName()
        );
        dialog.show(getParentFragmentManager(), BookServiceDialogFragment.TAG);
    }


    private void reviewPermission() {
        boolean canReview = isPurchased || isReserved;
        binding.btnBook.setVisibility(canReview ? View.GONE:View.VISIBLE);
        binding.submitReview.setVisibility(canReview ? View.VISIBLE:View.GONE);
        
        // Disable rating bar and comment field if user cannot review
        binding.rbNewReview.setEnabled(canReview);
        binding.descriptionEditText.setEnabled(canReview);
        
        if (!canReview) {
            binding.rbNewReview.setRating(0);
            binding.descriptionEditText.setText("");
            binding.descriptionEditText.setHint("Purchase or reserve this solution to leave a review");
        } else {
            binding.descriptionEditText.setHint("Leave a comment");
        }
    }

    private void checkIfProductAlreadyPurchased() {
        if (!userSession.isValidSession() || showedProduct == null) {
            isPurchased = false;
            reviewPermission();
            return;
        }

        // Get current user's events and check if product is purchased using backend endpoint
        UUID currentUserId = userSession.getCurrentUserId();
        
        eventService.getByOwner(currentUserId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> userEvents = response.body();
                    
                    // Convert List<Event> to Event[] for Purchase object
                    Event[] eventsArray = userEvents.toArray(new Event[0]);
                    
                    // Create Purchase object for isPurchased check
                    Purchase purchase = new Purchase(eventsArray, showedProduct);
                    
                    // Call backend isPurchased endpoint
                    SolutionService solutionService =RetrofitClient.getClient(requireContext()).create(SolutionService.class);
                    solutionService.isPurchased(purchase).enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                isPurchased = response.body();
                                reviewPermission();
                            } else {
                                isPurchased = false;
                                reviewPermission();
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            isPurchased = false;
                            reviewPermission();
                        }
                    });
                } else {
                    isPurchased = false;
                    reviewPermission();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                isPurchased = false;
                reviewPermission();
            }
        });
    }

    private void checkIfServiceAlreadyReserved() {
        if (!userSession.isValidSession() || showedService == null) {
            isReserved = false;
            reviewPermission();
            return;
        }

        // For services, we need to check if the user has any reservations for this service
        // This would typically involve calling a service to check reservations
        // For now, we'll set it to false and let the user reserve first
        isReserved = false;
        reviewPermission();
    }

    private void loadReviewsByOwner() {
        // This method is now used for the "About Us" section to show all business owner reviews
        if (showedSolution != null && showedSolution.getOwner() != null) {
            reviewService.getByOwner(UUID.fromString(showedSolution.getOwner().getId())).enqueue(new Callback<Collection<Review>>() {
                @Override
                public void onResponse(Call<Collection<Review>> call, Response<Collection<Review>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Review> ownerReviews = new ArrayList<>(response.body());
                        displayOwnerReviews(ownerReviews);
                    } else {
                        Toast.makeText(requireContext(), "Failed to load owner reviews", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Collection<Review>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Error loading owner reviews", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void displayOwnerReviews(List<Review> reviews) {
        if (reviews.isEmpty()) {
            // Show empty state for business owner reviews in about us section
            List<Review> emptyReviews = new ArrayList<>();
            ReviewAdapter ownerReviewAdapter = new ReviewAdapter(requireContext(), emptyReviews);
            binding.commentsRecyclerView.setAdapter(ownerReviewAdapter);
            return;
        }

        // Display all business owner reviews in the RecyclerView below Reviews & Comments
        ReviewAdapter ownerReviewAdapter = new ReviewAdapter(requireContext(), reviews);
        binding.commentsRecyclerView.setAdapter(ownerReviewAdapter);
    }

    private void setupCommentsSection() {
        // Setup RecyclerView for comments
        List<Review> reviews = new ArrayList<>();
        ReviewAdapter reviewAdapter = new ReviewAdapter(requireContext(), reviews);
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.commentsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadSolutionReviews() {
        if (showedSolution != null) {
            reviewService.getBySolution(showedSolution.getId()).enqueue(new Callback<Collection<Review>>() {
                @Override
                public void onResponse(Call<Collection<Review>> call, Response<Collection<Review>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Review> reviews = new ArrayList<>(response.body());
                        ReviewAdapter reviewAdapter = new ReviewAdapter(requireContext(), reviews);
                        binding.commentsRecyclerView.setAdapter(reviewAdapter);
                    } else {
                        // Show empty state for solution-specific reviews
                        List<Review> emptyReviews = new ArrayList<>();
                        ReviewAdapter reviewAdapter = new ReviewAdapter(requireContext(), emptyReviews);
                        binding.commentsRecyclerView.setAdapter(reviewAdapter);
                    }
                }

                @Override
                public void onFailure(Call<Collection<Review>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Failed to load solution reviews", Toast.LENGTH_SHORT).show();
                    // Show empty state on failure
                    List<Review> emptyReviews = new ArrayList<>();
                    ReviewAdapter reviewAdapter = new ReviewAdapter(requireContext(), emptyReviews);
                    binding.commentsRecyclerView.setAdapter(reviewAdapter);
                }
            });
        }
    }

    private void setService() {
        ServiceService service =RetrofitClient.getClient(requireContext()).create(ServiceService.class);
        service.get(showedSolution.getId()).enqueue(new Callback<Service>() {
            @Override
            public void onResponse(Call<Service> call, Response<Service> response) {
                showedService = response.body();
                serviceDetails = false;
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(binding.details.getId(), ServiceDetailsForm.newInstance(showedService));
                transaction.commit();
                
                // Check if service is already reserved
                checkIfServiceAlreadyReserved();
            }

            @Override
            public void onFailure(Call<Service> call, Throwable t) {

            }
        });
    }

    private void setProduct() {
        ProductService service =RetrofitClient.getClient(requireContext()).create(ProductService.class);
        service.get(showedSolution.getId()).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                showedProduct = response.body();
                serviceDetails = false;
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(binding.details.getId(), ProductDetailsForm.newInstance(showedProduct));
                transaction.commit();
                
                // Check if product is already purchased
                checkIfProductAlreadyPurchased();
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {

            }
        });
    }

    private void submitReview() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to submit a review", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has purchased or reserved the solution
        if (!isPurchased && !isReserved) {
            Toast.makeText(requireContext(), "You must purchase or reserve this solution before leaving a review", Toast.LENGTH_SHORT).show();
            return;
        }

        int grade = Math.round(binding.rbNewReview.getRating());
        String commentText = binding.descriptionEditText.getText().toString().trim();

        if (grade <= 0 || commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a rating and comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review();
        review.setSolution(showedSolution);
        review.setComment(commentText);
        review.setGrade(grade);
        review.setStatus("PENDING");

        reviewService.add(review).enqueue(new Callback<Review>() {
            @Override public void onResponse(Call<Review> call, Response<Review> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Review submitted (pending approval)", Toast.LENGTH_SHORT).show();
                    binding.descriptionEditText.setText("");
                    binding.rbNewReview.setRating(0);
                    loadSolutionReviews();
                } else {
                    Toast.makeText(requireContext(), "Failed to submit review", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Review> call, Throwable t) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void openChat() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to start chatting", Toast.LENGTH_SHORT).show();
            return;
        }

        if (showedSolution == null || showedSolution.getOwner() == null) {
            Toast.makeText(requireContext(), "Unable to start chat", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create current user from JWT token information instead of making backend call
        try {
            String authToken = userSession.getAuthToken();
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(requireContext(), "Authentication token not found", Toast.LENGTH_SHORT).show();
                return;
            }

            JwtUtils.JwtClaims claims = JwtUtils.decodeToken(authToken);
            if (claims == null) {
                Toast.makeText(requireContext(), "Failed to decode authentication token", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create User object from JWT claims
            User currentUser = new User();
            currentUser.setId(claims.getUserId().toString());
            currentUser.setEmail(claims.getEmail());
            
            // Set role if available
            if (claims.getRole() != null) {
                try {
                    UserRole userRole = UserRole.valueOf(claims.getRole());
                    Role role = new Role(userRole);
                    currentUser.setRole(role);
                } catch (IllegalArgumentException e) {
                    // If role parsing fails, use default role
                    Role role = new Role(UserRole.AUTHENTICATED_USER);
                    currentUser.setRole(role);
                }
            } else {
                // Default role if not specified
                Role role = new Role(UserRole.AUTHENTICATED_USER);
                currentUser.setRole(role);
            }

            User chatPartner = showedSolution.getOwner();
            
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(binding.rootContainer.getId(), ChatFragment.newInstance(currentUser, chatPartner));
            transaction.addToBackStack("chat");
            transaction.commit();
            
        } catch (Exception e) {
            android.util.Log.e("ServiceDetails", "Error creating user from JWT: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing chat", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}