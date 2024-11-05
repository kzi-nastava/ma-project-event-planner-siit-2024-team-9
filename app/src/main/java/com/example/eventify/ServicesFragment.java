package com.example.eventify;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.eventify.databinding.ActivityMainBinding;
import com.example.eventify.databinding.FragmentServicesBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ServicesFragment extends Fragment {
    private ServiceListAdapter adapter;
    private ArrayList<Service> mProducts;
    private GridView gridView;

    private static final String ARG_PARAM = "param";

    public static ServicesFragment newInstance(ArrayList<Service> services) {
        ServicesFragment fragment = new ServicesFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, services);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ShopApp", "onCreate Services Fragment");
        if (getArguments() != null) {
            mProducts = getArguments().getParcelableArrayList(ARG_PARAM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i("ShopApp", "onCreateView Services Fragment");
        View root = inflater.inflate(R.layout.fragment_services, container, false);

        // Find and set up the GridView
        gridView = root.findViewById(R.id.grid_view);
        if (mProducts != null) {
            adapter = new ServiceListAdapter(getActivity(), mProducts);
            gridView.setAdapter(adapter);
        }

        // Optionally set up a listener for item clicks
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // Handle grid item click
            Service selectedService = mProducts.get(position);
            // Add your logic for item interaction
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        gridView = null;
    }
}
