package com.example.eventify;

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

public class RegisterFragment extends Fragment {


    private FragmentRegisterBinding binding;

    private static final int REQUEST_IMAGE_PICK = 1;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);

        binding.buttonSelectImage.setOnClickListener(v -> openImagePicker());
        binding.registerButton.setOnClickListener(v -> register());

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
    

}