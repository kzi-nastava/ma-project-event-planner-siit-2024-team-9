package com.example.eventify.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;


import com.example.eventify.Fragments.SolutionListFragment;
import com.example.eventify.R;
import com.example.eventify.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

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

        binding.serviceButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SolutionsActivity.class);
            startActivity(intent);

        });



        navigationActions.put(R.id.discover, this::setDiscoverFragment);
        navigationActions.put(R.id.services, this::setServicesFragment);
        navigationActions.put(R.id.events, this::setEventsFragment);
        navigationActions.put(R.id.calendar, this::setCalendarFragment);
        navigationActions.put(R.id.profile, this::setProfileFragment);
        navigationActions.put(R.id.chats, this::setChatsFragment);


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

    private boolean navigationLogic(MenuItem item){
        final int selectedItemId = item.getItemId();

        Runnable action = navigationActions.get(selectedItemId);
        if (action != null){
            action.run();
            return true;
        }
        return false;
    }

    private void setDiscoverFragment(){
        Toast.makeText(this, "Discover", Toast.LENGTH_SHORT).show();
    }

    private void setServicesFragment(){
        loadFragment(new SolutionListFragment());
    }

    private void setProfileFragment(){
        Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
    }

    private void setEventsFragment(){
        Toast.makeText(this, "Events", Toast.LENGTH_SHORT).show();
    }

    private void setChatsFragment(){
        Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show();
    }

    private void setCalendarFragment(){
        Toast.makeText(this, "Calendar", Toast.LENGTH_SHORT).show();
    }



    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace with your container ID
                .commit();
    }

    private String getUser(){
        return "businessOwner";
        // return "auth_user";
        // return "eventOrganizer";
    }
}
