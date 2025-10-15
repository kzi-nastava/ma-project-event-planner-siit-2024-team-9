package com.example.eventify.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.eventify.databinding.FragmentRegisterBinding;
import com.example.eventify.models.auth.BusinessOwnerRegistrationRequest;
import com.example.eventify.models.auth.EventOrganizerRegistrationRequest;
import com.example.eventify.models.users.User;
import com.example.eventify.services.auth.RegistrationService;
import com.example.eventify.utils.RegistrationUtils;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private List<View> businessOwnerViews;
    private Uri selectedProfileImageUri;
    private List<Uri> selectedBusinessImageUris = new ArrayList<>();
    private RegistrationService registrationService;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        registrationService = RetrofitClient.getClient(requireContext()).create(RegistrationService.class);

        businessOwnerViews = List.of(
                binding.businessNameInputLayout,
                binding.descriptionInputLayout,
                binding.businessPhotosSection
        );

        binding.buttonSelectProfileImage.setOnClickListener(v -> openImagePicker());
        binding.buttonSelectBusinessImages.setOnClickListener(v -> openBusinessImagesPicker());
        binding.registerButton.setOnClickListener(v -> register());
        binding.loginButton.setOnClickListener(v -> aleradyHaveAccount());
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> radioButtonHandler());

        radioButtonHandler(); // Hide business owner views by default

        // Register the launcher with a callback to handle the result
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getClipData() != null) {
                                // Multiple images selected (business photos)
                                selectedBusinessImageUris.clear();
                                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                    selectedBusinessImageUris.add(imageUri);
                                }
                                // TODO: Update RecyclerView with business images
                            } else if (data.getData() != null) {
                                // Single image selected (profile photo)
                                selectedProfileImageUri = data.getData();
                                binding.profileImageView.setImageURI(selectedProfileImageUri);
                                binding.profileImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
        );

        return binding.getRoot();
    }

    private void openImagePicker(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openBusinessImagesPicker(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }

    private void aleradyHaveAccount(){
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
    
    private void register(){
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString();
        String address = binding.addressEditText.getText().toString().trim();
        String phone = binding.phoneEditText.getText().toString().trim();

        if (binding.eventOrganizerRadioButton.isChecked()) {
            registerEventOrganizer(email, password, confirmPassword, address, phone);
        } else if (binding.businessOwnerRadioButton.isChecked()) {
            registerBusinessOwner(email, password, confirmPassword, address, phone);
        }
    }

    private void registerEventOrganizer(String email, String password, String confirmPassword, 
                                      String address, String phone) {
        String firstName = binding.firstNameEditText.getText().toString().trim();
        String lastName = binding.lastNameEditText.getText().toString().trim();

        if (!RegistrationUtils.validateEventOrganizerInput(email, password, confirmPassword, 
                address, phone, firstName, lastName)) {
            Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        EventOrganizerRegistrationRequest request = new EventOrganizerRegistrationRequest(
                email, password, address, phone, firstName, lastName
        );

        RequestBody dto = RegistrationUtils.createJsonRequestBody(request);
        MultipartBody.Part profileImagePart = null;
        
        if (selectedProfileImageUri != null) {
            profileImagePart = RegistrationUtils.createImagePart(requireContext(), 
                    selectedProfileImageUri, "profileImageFile");
        }

        Call<User> call = registrationService.registerEventOrganizer(dto, profileImagePart);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                } else {
                    String errorMessage = "Registration failed";
                    if (response.code() == 409) {
                        errorMessage = "User already exists";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Registration", "Registration failed", t);
                Toast.makeText(requireContext(), "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerBusinessOwner(String email, String password, String confirmPassword, 
                                     String address, String phone) {
        String name = binding.businessNameEditText.getText().toString().trim();
        String description = binding.descriptionEditText.getText().toString().trim();

        if (!RegistrationUtils.validateBusinessOwnerInput(email, password, confirmPassword, 
                address, phone, name, description)) {
            Toast.makeText(requireContext(), "Please fill in all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        BusinessOwnerRegistrationRequest request = new BusinessOwnerRegistrationRequest(
                email, password, address, phone, name, description
        );

        RequestBody dto = RegistrationUtils.createJsonRequestBody(request);
        MultipartBody.Part profileImagePart = null;
        
        if (selectedProfileImageUri != null) {
            profileImagePart = RegistrationUtils.createImagePart(requireContext(), 
                    selectedProfileImageUri, "profileImageFile");
        }

        // For now, we'll handle business images later
        MultipartBody.Part[] businessImageParts = new MultipartBody.Part[0];

        Call<User> call = registrationService.registerBusinessOwner(dto, profileImagePart, businessImageParts);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                } else {
                    String errorMessage = "Registration failed";
                    if (response.code() == 409) {
                        errorMessage = "User already exists";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Registration", "Registration failed", t);
                Toast.makeText(requireContext(), "Registration failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void radioButtonHandler(){
        if(binding.businessOwnerRadioButton.isChecked()){
            // Show business owner fields
            binding.businessNameInputLayout.setVisibility(View.VISIBLE);
            binding.descriptionInputLayout.setVisibility(View.VISIBLE);
            binding.businessPhotosSection.setVisibility(View.VISIBLE);
            
            // Hide event organizer fields
            binding.nameInputLayout.setVisibility(View.GONE);
            binding.surnameInputLayout.setVisibility(View.GONE);
        } else {
            // Show event organizer fields
            binding.nameInputLayout.setVisibility(View.VISIBLE);
            binding.surnameInputLayout.setVisibility(View.VISIBLE);
            
            // Hide business owner fields
            binding.businessNameInputLayout.setVisibility(View.GONE);
            binding.descriptionInputLayout.setVisibility(View.GONE);
            binding.businessPhotosSection.setVisibility(View.GONE);
        }
    }
}