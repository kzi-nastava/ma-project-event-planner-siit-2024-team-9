package com.example.eventify.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.eventify.fragments.RegisterFragment;
import com.example.eventify.R;
import com.example.eventify.databinding.ActivityLoginBinding;
import com.example.eventify.services.auth.LoginService;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    List<View> componentsToHide;

    private View fragmentContainer;

    private Color primaryColor;

    private LoginService loginService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.medium_gray));

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.continueAsGuestTextView.setOnClickListener(v -> continueAsGuest());
        binding.loginButton.setOnClickListener(v -> openLoginFragment());
        binding.registerButton.setOnClickListener(v -> openRegisterFragment());

        fragmentContainer = binding.fragmentContainer;

        loginService = new LoginService(this);

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

                    fragmentContainer.setVisibility(View.GONE);
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
        //finish();
    }

    private void openLoginFragment(){
        String email = binding.emailEditText.getText().toString();
        final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        if(email.matches(EMAIL_REGEX)){
            login(email, binding.passwordEditText.getText().toString());
        } else{
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String email, String password) {
        try {
            loginService.login(email, password);
            Log.i("Login", "Login successful "+loginService.getToken());


            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void openRegisterFragment(){
        for(View view: componentsToHide){
            view.setVisibility(View.GONE);
        }
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        RegisterFragment registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(fragmentContainer.getId(),registerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
    }


}