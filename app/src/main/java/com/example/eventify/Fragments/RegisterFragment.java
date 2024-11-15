package com.example.eventify.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eventify.databinding.FragmentRegisterBinding;

import java.util.List;

public class RegisterFragment extends Fragment {


    private FragmentRegisterBinding binding;

    private static final int REQUEST_IMAGE_PICK = 1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private List<View> businessOwnerViews;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        businessOwnerViews = List.of(
                binding.businessNameEditText,
                binding.descriptionInputLayout
        );

        binding.buttonSelectImage.setOnClickListener(v -> openImagePicker());
        binding.registerButton.setOnClickListener(v -> register());
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> radioButtonHandler());

        radioButtonHandler(); // Hide business owner views by default

        // Register the launcher with a callback to handle the result
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            binding.profileImageView.setImageURI(selectedImageUri); // Display the selected image
                        }
                    }
                }
        );

        return binding.getRoot();
    }
    private void openImagePicker(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/");
        imagePickerLauncher.launch(intent);
    }
    
    private void register(){
        Toast.makeText(requireContext(), "Validate inputs", Toast.LENGTH_SHORT).show();
        Toast.makeText(requireContext(), "Please activate your account in the email we sent you", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void radioButtonHandler(){
        if(binding.businessOwnerRadioButton.isChecked()){
            businessOwnerViews.forEach(view -> view.setVisibility(View.VISIBLE));
        } else {
            businessOwnerViews.forEach(view -> view.setVisibility(View.GONE));
        }
    }
    

}