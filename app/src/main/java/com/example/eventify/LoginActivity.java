package com.example.eventify;

import android.annotation.SuppressLint;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import com.example.eventify.databinding.ActivityLoginBinding;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    List<View> componentsToHide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.continueAsGuestTextView.setOnClickListener(v -> continueAsGuest());
        binding.loginButton.setOnClickListener(v -> openLoginFragment());
        binding.registerButton.setOnClickListener(v -> openRegisterFragment());

        componentsToHide = List.of(
                binding.titleTextView,
                binding.emailEditText,
                binding.passwordEditText,
                binding.forgotPasswordTextView,
                binding.loginButton,
                binding.registerButton,
                binding.continueAsGuestTextView
        );

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if there's a fragment in the container
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    for(View view: componentsToHide){
                        view.setVisibility(View.VISIBLE);
                    }

                    findViewById(R.id.fragment_container).setVisibility(View.GONE);
                } else {
                    // If no fragment in the back stack, just perform normal back press
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        // Add the callback to the back press dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void continueAsGuest(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void openLoginFragment(){
        String email = binding.emailEditText.getText().toString();
        final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if(email.matches(EMAIL_REGEX)){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else{
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRegisterFragment(){
        for(View view: componentsToHide){
            view.setVisibility(View.GONE);
        }
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        RegisterFragment registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,registerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}