package com.example.eventify;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.eventify.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

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

        binding.addService.setOnClickListener(v -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(binding.frame.getId(), SolutionFormFragment.newInstance("gas","gas"));
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }
}
