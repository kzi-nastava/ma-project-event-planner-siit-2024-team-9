package com.example.eventify.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventify.Helpers.ComponentsSetup;
import com.example.eventify.models.Service;
import com.example.eventify.R;
import com.example.eventify.databinding.FragmentSolutionFormBinding;
import com.example.eventify.services.ServiceService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class SolutionFormFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private FragmentSolutionFormBinding binding;

    public SolutionFormFragment() {
        // Required empty public constructor
    }

    ServiceService service = ServiceService.getInstance();

    public static SolutionFormFragment newInstance(Service service) {
        SolutionFormFragment fragment = new SolutionFormFragment();
        Bundle args = new Bundle();
        args.putParcelable("service", service);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setEdit() {
        setFormHeading();
        disableCategories();
    }

    private void setFormHeading() {
        if (getView() != null) {
            TextView heading = getView().findViewById(R.id.formHeading);
            heading.setText("Edit details");
        }
    }

    private void disableCategories() {
        if (getView() != null) {
            Spinner categories = getView().findViewById(R.id.categorySpinner);
            categories.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSolutionFormBinding.inflate(inflater, container, false);  // Use the generated binding class directly
        View view = binding.getRoot();

        setupCategories(view);
        setupChips(view);
        setupImagePicker(view);
        setupDeleteButton(view);
        setupSubmitButton(view);

        if (getArguments() != null) {
            Service service = getArguments().getParcelable("service");
            binding.setService(service);
            binding.setLifecycleOwner(this);
        }

        return binding.getRoot();
    }

    private void setupCategories(View view) {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Service category 1");
        categories.add("Service category 2");
        categories.add("Service category 3");
        categories.add("Other");

        TextView newCategory = view.findViewById(R.id.newCategory);

        // Setup category spinner with an external method
        Spinner categorySpinner = ComponentsSetup.spinnerOtherSetup(view, R.id.categorySpinner, categories, getContext(), newCategory);
    }

    private void setupChips(View view) {
        ChipGroup chipGroup = view.findViewById(R.id.eventTypes);

        String[] options = {"Event type 1", "Event type 2", "Event type 3", "Event type 4", "Event type 5"};

        for (String option : options) {
            Chip chip = new Chip(getContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChecked(false);
            chip.setCheckedIconResource(R.drawable.check);  // Use your check icon
            chip.setCheckedIconVisible(true);
            chip.setCloseIconVisible(false);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setTextColor(getResources().getColorStateList(R.color.black));

            chipGroup.addView(chip);
        }
    }

    private void setupImagePicker(View view) {
        // Set click listener for service image
        view.findViewById(R.id.serviceImage).setOnClickListener(v -> openImagePicker());

        // Register image picker activity result callback
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getClipData() != null ?
                                    data.getClipData().getItemAt(0).getUri() : data.getData();
                            ImageView serviceImage = view.findViewById(R.id.serviceImage);
                            serviceImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void setupDeleteButton(View view) {
        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Service")
                    .setMessage("Are you sure you want to delete this service?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Service deleted = binding.getService();
                        service.delete(deleted.getId());
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStack();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void setupSubmitButton(View view) {
        Button submitBtn = view.findViewById(R.id.btnSubmit);
        submitBtn.setOnClickListener(v -> {
            hideKeyboard(binding.nameEditText);
            Service updated = binding.getService();
            boolean isUpdate=service.update(updated.getId(), updated);
            if (!isUpdate) {
                service.create(updated);
            }
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        });

    }

    public void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && editText != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(intent);
    }
}

