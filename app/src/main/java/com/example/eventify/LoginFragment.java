package com.example.eventify;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventify.databinding.FragmentLoginBinding;

import java.util.regex.Pattern;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater,container,false);

        View rootView = binding.getRoot();

        binding.loginButton2.setOnClickListener(v -> loginUser());



        return rootView;
    }

    private void loginUser() {
        String email = binding.email.getText().toString();
        final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if(Pattern.matches(EMAIL_REGEX,email)){
            Intent intent = new Intent(this.getActivity(), MainActivity.class);
            startActivity(intent);
        } else{
            Toast.makeText(getActivity(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding to prevent memory leaks
        binding = null;
    }

}