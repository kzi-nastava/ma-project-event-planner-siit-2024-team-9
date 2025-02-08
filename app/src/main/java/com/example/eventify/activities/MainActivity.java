package com.example.eventify.activities;

import android.os.Bundle;
import android.util.Log;
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
import com.example.eventify.fragments.PriceListFragment;
import com.example.eventify.fragments.SolutionFilterFragment;
import com.example.eventify.models.enums.PrivacyType;
import com.example.eventify.fragments.ServicesFragment;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.R;
import com.example.eventify.databinding.ActivityMainBinding;
import com.example.eventify.services.events.EventService;
import com.example.eventify.utils.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    BottomNavigationView bottomNavigationView;

    HashMap<Integer, Runnable> navigationActions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        navigationActions.put(R.id.discover, this::setDiscoverFragment);
        navigationActions.put(R.id.services, this::setServicesFragment);
        navigationActions.put(R.id.budget, this::setBudgetFragment);
        navigationActions.put(R.id.events, this::setEventsFragment);
        navigationActions.put(R.id.categories, this::setCategoriesFragment);
        navigationActions.put(R.id.profile, this::setProfileFragment);
        navigationActions.put(R.id.chats, this::setChatsFragment);
        navigationActions.put(R.id.priceList, this::setPricesFragment);



        bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.getMenu().clear();
        switch (getUser()){
            case "businessOwner":
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_business_owner);
                break;
            case "auth_user":
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_auth_user);
                break;
            case "eventOrganizer":
                bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_event_organizer);
                break;
        }

        bottomNavigationView.setOnItemSelectedListener(this::navigationLogic);
        bottomNavigationView.setSelectedItemId(R.id.discover);

    }

    private void setPricesFragment() {
        loadFragment(new PriceListFragment());
    }

    private boolean navigationLogic(MenuItem item){
        final int selectedItemId = item.getItemId();

        Runnable action = navigationActions.get(selectedItemId);
        if (action != null){
            action.run();
            return true;
        }
        return false;
    }


    private void setDiscoverFragment() { loadFragment(new DiscoverFragment());}
    private void setServicesFragment(){
        loadFragment(new ServicesFragment());
    }

    private void getBudget() {
        EventService service = RetrofitClient.getClient().create(EventService.class);
        service.getAllPaginated(0, 5, "name", true).enqueue(new Callback<EventService.EventAllResponse>() {
            @Override
            public void onResponse(Call<EventService.EventAllResponse> call, Response<EventService.EventAllResponse> response) {
                List<Event> events = response.body().content;
                loadFragment(new BudgetFragment().newInstance(events.get(0)));
            }

            @Override
            public void onFailure(Call<EventService.EventAllResponse> call, Throwable t) {

            }
        });
    }

    private void setBudgetFragment() {getBudget();}

    private void setProfileFragment(){
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
    }

    private void setEventsFragment(){
        Toast.makeText(this, "Events", Toast.LENGTH_SHORT).show();
    }

    private void setChatsFragment(){
        Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show();
    }

    private void setCategoriesFragment(){
        loadFragment(new CategoriesFragment());
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.home_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private String getUser(){
        return "businessOwner";
        // return "auth_user";
        // return "eventOrganizer";
    }

//    private List<Event> generateDummyEvents() {
//        List<Event> events = new ArrayList<>();
//        events.add(new Event("a", "Concert", "Live music concert with popular bands", 100, PrivacyType.PUBLIC, new Date(), new Date(), 50,
//                new Location("Novi Sad", "Main Square", 19.85, 45.25)));
//        events.add(new Event("b", "Tech Meetup", "Networking and tech talks", 50, PrivacyType.PRIVATE, new Date(), new Date(), 30,
//                new Location("Novi Sad", "Tech Park", 19.83, 45.27)));
//        events.add(new Event("b", "Art Exhibition", "Showcasing local artists and their work", 75, PrivacyType.PUBLIC, new Date(), new Date(), 40,
//                new Location("Novi Sad", "Gallery Center", 19.82, 45.29)));
//        events.add(new Event("c", "Cooking Class", "Learn to cook gourmet dishes", 20, PrivacyType.PUBLIC, new Date(), new Date(), 10,
//                new Location("Novi Sad", "Culinary School", 19.80, 45.26)));
//        events.add(new Event("d", "Startup Pitch Night", "Watch startups pitch their ideas", 200, PrivacyType.PUBLIC, new Date(), new Date(), 80,
//                new Location("Novi Sad", "Startup Incubator", 19.84, 45.28)));
//        events.add(new Event("e", "Yoga Workshop", "Relax and unwind with expert yoga trainers", 15, PrivacyType.PRIVATE, new Date(), new Date(), 12,
//                new Location("Novi Sad", "Wellness Center", 19.81, 45.22)));
//        events.add(new Event("f", "Science Fair", "Discover innovative projects from students", 500, PrivacyType.PUBLIC, new Date(), new Date(), 300,
//                new Location("Novi Sad", "University Hall", 19.89, 45.30)));
//        events.add(new Event("g", "Photography Walk", "Learn photography on a scenic walk", 25, PrivacyType.PRIVATE, new Date(), new Date(), 15,
//                new Location("Novi Sad", "City Park", 19.88, 45.32)));
//        events.add(new Event("h", "Movie Screening", "Watch classic movies under the stars", 100, PrivacyType.PUBLIC, new Date(), new Date(), 70,
//                new Location("Novi Sad", "Open Air Theater", 19.86, 45.24)));
//        events.add(new Event("i", "Gaming Tournament", "Compete in the latest video games", 150, PrivacyType.PUBLIC, new Date(), new Date(), 100,
//                new Location("Novi Sad", "Gaming Arena", 19.87, 45.23)));
//        return events;
//    }


    private List<Event> filterPublicEvents(List<Event> events) {
        List<Event> publicEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getPrivacyType() == PrivacyType.PUBLIC) {
                publicEvents.add(event);
            }
        }
        return publicEvents;
    }

    private List<Event> getTop5OpenEvents(List<Event> events) {
        List<Event> openEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getPrivacyType() == PrivacyType.PUBLIC) {
                openEvents.add(event);
            }
        }
        return openEvents.size() > 5 ? openEvents.subList(0, 5) : openEvents;
    }

    private List<Event> getAllOpenEvents(List<Event> events) {
        List<Event> openEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getPrivacyType() == PrivacyType.PUBLIC && "Novi Sad".equals(event.getLocation().getName())) {
                openEvents.add(event);
            }
        }
        return openEvents;
    }


    private List<Solution> generateDummySolutions() {
        List<Solution> solutions = new ArrayList<>();
        /*
        ArrayList<EventType> types = new ArrayList<>();
        EventType type = new EventType("type", "event", true);
        types.add(type);
        solutions.add(new Solution(UUID.randomUUID(), Status.ACCEPTED, "Solution 1", "Description 1", 100.0, 10.0, new ArrayList<>(), true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), Status.PENDING, "Solution 2", "Description 2", 200.0, 15.0, new ArrayList<>(), true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), Status.ACCEPTED, "Solution 3", "Description 3", 300.0, 20.0, new ArrayList<>(), true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), Status.ACCEPTED, "Solution 4", "Description 4", 400.0, 25.0, new ArrayList<>(), true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), Status.DENIED, "Solution 5", "Description 5", 500.0, 30.0, new ArrayList<>(), true, true, null, types));
        solutions.add(new Solution(UUID.randomUUID(), Status.ACCEPTED, "Solution 6", "Description 6", 600.0, 35.0, new ArrayList<>(), true, true, null, types));*/
        return solutions;
    }

    private List<Solution> getTop5Solutions(List<Solution> solutions) {
        return solutions.size() > 5 ? solutions.subList(0, 5) : solutions;
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        // handler.removeCallbacksAndMessages(null); // Primer za Handler
        // unregisterReceiver(yourReceiver); // Primer za BroadcastReceiver
    }


}
