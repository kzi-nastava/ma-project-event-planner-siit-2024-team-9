package com.example.eventify.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;

import com.example.eventify.fragments.BudgetFragment;
import com.example.eventify.fragments.CategoriesFragment;
import com.example.eventify.fragments.DiscoverFragment;
import com.example.eventify.fragments.NotificationsFragment;
import com.example.eventify.fragments.PriceListFragment;
import com.example.eventify.fragments.ProfileFragment;
import com.example.eventify.fragments.ReportsFragment;
import com.example.eventify.fragments.ReviewsFragment;
import com.example.eventify.fragments.ServicesFragment;
import com.example.eventify.models.events.Event;
import com.example.eventify.R;
import com.example.eventify.databinding.ActivityMainBinding;
import com.example.eventify.services.auth.LoginService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.utils.JwtUtils;
import com.example.eventify.utils.NavigationManager;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationManager {

    private ActivityMainBinding binding;
    private UserSession userSession;
    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;
    private static final String TAG = "MainActivity";
    private static final String HOME_FRAGMENT = "HOME_FRAGMENT";

    HashMap<Integer, Runnable> navigationActions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));

        userSession = new UserSession(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MaterialToolbar topBar = findViewById(R.id.top_app_bar);

        // toolbar
        LoginService ls = new LoginService(this);
        boolean isLoggedIn = ls.getToken() != null && ls.isTokenValid();
        topBar.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);

        topBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_notifications) {
                navigateToFragment(new NotificationsFragment(), false);
                return true;
            } else if (id == R.id.action_profile) {
                navigateToFragment(new ProfileFragment(), false);
                return true;
            }
            return false;
        });

//        setupNavigationActions();
        setupBottomNavigation();
        setupBackPressHandler();

        // Load initial fragment if no saved state
        if (savedInstanceState == null) {
            navigateToFragment(new DiscoverFragment(), false);
        }
    }

    private String resolveUserRole() {
        LoginService ls = new LoginService(this);
        String token = ls.getToken();
        if (token == null) return null;
        JwtUtils.JwtClaims claims = JwtUtils.decodeToken(token);
        if (claims == null) return null;
        return claims.getRole();
    }

    private void setupGuestNavigationActions() {
        navigationActions.clear();
        navigationActions.put(R.id.discover, () -> navigateToFragment(new DiscoverFragment(), false));
        navigationActions.put(R.id.services, () -> navigateToFragment(new ServicesFragment(), false));
        navigationActions.put(R.id.budget, this::getBudget);
        navigationActions.put(R.id.categories, () -> navigateToFragment(new CategoriesFragment(), false));
        navigationActions.put(R.id.priceList, () -> navigateToFragment(new PriceListFragment(), false));
    }

    private void setupAuthenticatedUserNavigationActions() {
        navigationActions.clear();
        navigationActions.put(R.id.discover, () -> navigateToFragment(new DiscoverFragment(), false));
        navigationActions.put(R.id.services, () -> navigateToFragment(new ServicesFragment(), false));
        navigationActions.put(R.id.budget, this::getBudget);
        navigationActions.put(R.id.categories, () -> navigateToFragment(new CategoriesFragment(), false));
        navigationActions.put(R.id.priceList, () -> navigateToFragment(new PriceListFragment(), false));
//        navigationActions.put(R.id.profile, () -> navigateToFragment(new ProfileFragment(), false));
    }

    private void setupBusinessOwnerNavigationActions() {
        navigationActions.clear();
        navigationActions.put(R.id.discover, () -> navigateToFragment(new DiscoverFragment(), false));
        navigationActions.put(R.id.services, () -> navigateToFragment(new ServicesFragment(), false));
        navigationActions.put(R.id.budget, this::getBudget);
        navigationActions.put(R.id.categories, () -> navigateToFragment(new CategoriesFragment(), false));
        navigationActions.put(R.id.priceList, () -> navigateToFragment(new PriceListFragment(), false));
//        navigationActions.put(R.id.profile, () -> navigateToFragment(new ProfileFragment(), false));
    }

    private void setupEventOrganizerNavigationActions() {
        navigationActions.clear();
        navigationActions.put(R.id.discover, () -> navigateToFragment(new DiscoverFragment(), false));
        navigationActions.put(R.id.services, () -> navigateToFragment(new ServicesFragment(), false));
        navigationActions.put(R.id.budget, this::getBudget);
        navigationActions.put(R.id.categories, () -> navigateToFragment(new CategoriesFragment(), false));
        navigationActions.put(R.id.priceList, () -> navigateToFragment(new PriceListFragment(), false));
//        navigationActions.put(R.id.profile, () -> navigateToFragment(new ProfileFragment(), false));
    }

    private void setupAdminNavigationActions() {
        navigationActions.clear();
        navigationActions.put(R.id.discover, () -> navigateToFragment(new DiscoverFragment(), false));
//        navigationActions.put(R.id.notifications, () -> navigateToFragment(new NotificationsFragment(), false));
        navigationActions.put(R.id.reviews, () -> navigateToFragment(new ReviewsFragment(), false));
        navigationActions.put(R.id.reports, () -> navigateToFragment(new ReportsFragment(), false));
//        navigationActions.put(R.id.profile, () -> navigateToFragment(new ProfileFragment(), false));
    }

//    private void setupBottomNavigation() {
//        bottomNavigationView = binding.bottomNavigation;
//        bottomNavigationView.getMenu().clear();
//        bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_business_owner);
//        bottomNavigationView.setOnItemSelectedListener(this::navigationLogic);
//    }

    private void setupBottomNavigation() {
        bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.getMenu().clear();

        String role = resolveUserRole(); // "ADMIN", "BUSINESS_OWNER", "AUTHENTICATED_USER"
        if ("ADMIN".equalsIgnoreCase(role)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            setupAdminNavigationActions();
        } else if ("BUSINESS_OWNER".equalsIgnoreCase(role)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_business_owner);
            setupBusinessOwnerNavigationActions();
        } else if ("EVENT_ORGANIZER".equalsIgnoreCase(role)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_event_organizer);
            setupEventOrganizerNavigationActions();
        } else if ("AUTHENTICATED_USER".equalsIgnoreCase(role)) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_auth_user);
            setupAuthenticatedUserNavigationActions();
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_guest);
            setupGuestNavigationActions();
        }

        bottomNavigationView.setOnItemSelectedListener(this::navigationLogic);
    }


    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (canNavigateBack()) {
                    navigateBack();
                } else {
                    // If we can't navigate back, exit the app
                    finish();
                }
            }
        });
    }

    
    private boolean navigationLogic(MenuItem item) {
        final int selectedItemId = item.getItemId();
        
        // Don't reload the same fragment
        if (bottomNavigationView.getSelectedItemId() == selectedItemId && currentFragment != null) {
            return true;
        }

        Runnable action = navigationActions.get(selectedItemId);
        if (action != null) {
            action.run();
            return true;
        }
        return false;
    }

    private void getBudget() {
        if (!userSession.isValidSession()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        EventService service = RetrofitClient.getClient(getApplicationContext()).create(EventService.class);
        // Get events by owner and navigate to the first event's budget
        service.getByOwner(userSession.getCurrentUserId()).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Event> events = response.body();
                    if (!events.isEmpty()) {
                        Event firstEvent = events.get(0);
                        // Navigate to BudgetFragment with the first event's name
                        navigateToFragment(BudgetFragment.newInstance(firstEvent.getName()), false);
                    } else {
                        Toast.makeText(MainActivity.this, "No events found. Please create an event first.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error loading events", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error response: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network error loading budget", t);
            }
        });
    }

    // NavigationManager interface implementation
    @Override
    public void navigateToFragment(Fragment fragment) {
        navigateToFragment(fragment, true);
    }

    @Override
    public void navigateToFragment(Fragment fragment, boolean addToBackStack) {
        if (fragment == null) return;

        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Add animation
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );

        transaction.replace(R.id.home_container, fragment);

        if (!(fragment instanceof DiscoverFragment)) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        

        transaction.commit();
    }

    @Override
    public void navigateBack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStackImmediate();
            Fragment fragment = fragmentManager.findFragmentById(R.id.home_container);
            if (fragment != null) {
                currentFragment = fragment;
            }

        } else {
            // If no fragments in back stack, navigate to default root fragment
            navigateToFragment(new DiscoverFragment(), false);
        }
    }

    @Override
    public boolean canNavigateBack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.getBackStackEntryCount() > 0;
    }

    private boolean isRootFragment(Fragment fragment) {
        return fragment instanceof DiscoverFragment ||
               fragment instanceof ServicesFragment ||
               fragment instanceof BudgetFragment ||
               fragment instanceof CategoriesFragment ||
               fragment instanceof PriceListFragment;
    }

    // Legacy method for backward compatibility
    public void loadFragment(Fragment fragment) {
        navigateToFragment(fragment, true);
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        navigateToFragment(fragment, addToBackStack);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current fragment state
        if (currentFragment != null) {
            outState.putString("current_fragment", currentFragment.getClass().getSimpleName());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore fragment state
        String currentFragmentName = savedInstanceState.getString("current_fragment");
        if (currentFragmentName != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.home_container);
            if (fragment != null) {
                currentFragment = fragment;
            }
        }
    }
}
