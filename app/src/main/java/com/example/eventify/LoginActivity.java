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
import com.example.eventify.databinding.ActivityLoginBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.continueAsGuestTextView.setOnClickListener(v -> continueAsGuest());
        binding.loginButton.setOnClickListener(v -> openLoginFragment());
        binding.registerButton.setOnClickListener(v -> openRegisterFragment());

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if there's a fragment in the container
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.registerButton).setVisibility(View.VISIBLE);
                    findViewById(R.id.continueAsGuestTextView).setVisibility(View.VISIBLE);
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
        findViewById(R.id.loginButton).setVisibility(View.GONE);
        findViewById(R.id.registerButton).setVisibility(View.GONE);
        findViewById(R.id.continueAsGuestTextView).setVisibility(View.GONE);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, loginFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openRegisterFragment(){

    }

}