package com.example.eventify.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentProductDetailsFormBinding;
import com.example.eventify.databinding.FragmentServiceDetailsFormBinding;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.users.User;
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.users.UserService;
import com.example.eventify.utils.NavigationManager;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailsForm extends Fragment {

    Product product;
    private UserSession userSession;
    private UserService userService;
    private NavigationManager navigationManager;

    public ProductDetailsForm() {
        // Required empty public constructor
    }

    FragmentProductDetailsFormBinding binding;

    public static ProductDetailsForm newInstance(Product product) {
        ProductDetailsForm fragment = new ProductDetailsForm();
        Bundle args = new Bundle();
        args.putParcelable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
        userService = RetrofitClient.getClient().create(UserService.class);
        
        // Get NavigationManager from activity
        if (requireActivity() instanceof NavigationManager) {
            navigationManager = (NavigationManager) requireActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductDetailsFormBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            product = getArguments().getParcelable("product");
            binding.setService(product);
        }

        setTypes();
        
        binding.btnChat.setOnClickListener(v -> openChat());

        return binding.getRoot();
    }

    private void setTypes() {
        StringBuilder typesInfo = new StringBuilder();
        for (EventType type: product.getEventTypes()) {
            typesInfo.append(type.getName()).append(", ");
        }
        binding.eventTypesInfo.setText(typesInfo.toString());
    }

    private void openChat() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to start chatting", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product == null || product.getOwner() == null) {
            Toast.makeText(requireContext(), "Unable to start chat", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        UUID currentUserId = userSession.getCurrentUserId();
        userService.get(currentUserId.toString()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User currentUser = response.body();
                    User chatPartner = product.getOwner();
                    
                    // Navigate to ChatFragment using NavigationManager
                    if (navigationManager != null) {
                        navigationManager.navigateToFragment(ChatFragment.newInstance(currentUser, chatPartner));
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to get user information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(requireContext(), "Error getting user information", Toast.LENGTH_SHORT).show();
            }
        });
    }
}