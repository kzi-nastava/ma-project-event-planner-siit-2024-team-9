package com.example.eventify.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.eventify.Helpers.ComponentsSetup;
import com.example.eventify.R;
import com.example.eventify.databinding.FragmentSolutionFormBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SolutionFormFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SolutionFormFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FragmentSolutionFormBinding binding;

    public SolutionFormFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ServicesFormFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SolutionFormFragment newInstance(String param1, String param2) {
        SolutionFormFragment fragment = new SolutionFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    public void setEdit () {
        setFormHeading();
        disableCategories();
    }

    private void setFormHeading() {
        // Make sure the view is ready before accessing it
        if (getView() != null) {
            TextView heading = getView().findViewById(R.id.formHeading);
            heading.setText("Edit details");
        }
    }

    private void disableCategories() {
        if (getView() != null) {
            Spinner categories= getView().findViewById(R.id.categorySpinner);
            categories.setVisibility(View.GONE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_solution_form, container, false);

        // Setup categories for Spinner
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Service category 1");
        categories.add("Service category 2");
        categories.add("Service category 3");
        categories.add("Other");

        TextView newCategory = view.findViewById(R.id.newCategory);

        Spinner categorySpinner = ComponentsSetup.spinnerOtherSetup(view, R.id.categorySpinner, categories, getContext(), newCategory);

        ChipGroup chipGroup = view.findViewById(R.id.eventTypes);

        String[] options = {"Event type 1", "Event type 2", "Event type 3", "Event type 4", "Event type 5"};

        for (String option : options) {
            Chip chip = new Chip(getContext());
            chip.setText(option);
            chip.setCheckable(true);
            chip.setChecked(false);

            chip.setCheckedIconResource(R.drawable.check);  // Use your check icon
            chip.setCheckedIconVisible(true);  // Make the check icon visible when checked

            // Optionally, remove the default close icon if it appears
            chip.setCloseIconVisible(false);

            chip.setChipBackgroundColorResource(R.color.white);  // Optional styling
            chip.setTextColor(getResources().getColorStateList(R.color.black)); // Optional text color

            chipGroup.addView(chip);
        }

        // Set click listener for service image
        view.findViewById(R.id.serviceImage).setOnClickListener(v -> openImagePicker());

        // Register image picker activity result callback
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // If multiple images are selected
                            if (data.getClipData() != null) {
                                int count = data.getClipData().getItemCount();
                                Uri selectedImageUri = data.getClipData().getItemAt(0).getUri();
                                ImageView serviceImage = view.findViewById(R.id.serviceImage);
                                serviceImage.setImageURI(selectedImageUri); // Update image with selected URI
                                // Optionally store the selected image URIs in a list
                            }
                        } else if (data.getData() != null) {
                            // Single image selection (fallback case)
                            Uri selectedImageUri = data.getData();
                            ImageView serviceImage = view.findViewById(R.id.serviceImage);
                            serviceImage.setImageURI(selectedImageUri); // Display the selected image
                        }
                    }
                }
        );

        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Service")
                    .setMessage("Are you sure you want to delete this service?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Deletion logic here
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        return view;
    }

    // Method to open the image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); // Set MIME type for images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple image selection
        imagePickerLauncher.launch(intent); // Launch the image picker
    }

}