package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentCategoriesBinding;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.services.solutions.SolutionCategoryService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment {

    SolutionCategoryService service;

    boolean isExistingEditMode = false;
    boolean isProposedEditMode = false;

    ArrayList<SolutionCategory> existingCategories = new ArrayList<>();
    ArrayList<SolutionCategory> proposedCategories = new ArrayList<>();

    public CategoriesFragment() {
        // Required empty public constructor
    }

    private FragmentCategoriesBinding binding;

    public static CategoriesFragment newInstance(String param1, String param2) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = RetrofitClient.getClient(requireContext().getApplicationContext()).create(SolutionCategoryService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentCategoriesBinding.inflate(inflater, container, false);

        fillTables();
        changeButtons();

        binding.editCategoryButton.setOnClickListener(v -> setEditMode(true));
        binding.editProposalButton.setOnClickListener(v -> setEditMode(false));

        binding.saveCategoryButton.setOnClickListener(v -> saveExisting());
        binding.saveProposalButton.setOnClickListener(v -> saveProposed());

        binding.addCategoryButton.setOnClickListener(v -> newCategory());

        return binding.getRoot();
    }

    private void setEditMode(boolean existing) {
        if (existing)
            isExistingEditMode = !isExistingEditMode;
        else
            isProposedEditMode = !isProposedEditMode;
        fillTables();
        changeButtons();
    }

    private void saveExisting() {
        boolean isNew = false;
        boolean isChanged = false;
        for (SolutionCategory category: existingCategories) {
            if (category.getId().isEmpty() && !category.getName().isEmpty() && !category.getDescription().isEmpty()) {
                isNew = true;
                isChanged = true;
                service.add(category).enqueue(new Callback<SolutionCategory>() {
                    @Override
                    public void onResponse(Call<SolutionCategory> call, Response<SolutionCategory> response) {
                        isExistingEditMode = !isExistingEditMode;
                        fillExisting();
                    }

                    @Override
                    public void onFailure(Call<SolutionCategory> call, Throwable t) {

                    }
                });
            }
            else if (!category.getId().isEmpty()) {
                isChanged = true;
                service.update(UUID.fromString(category.getId()), category).enqueue(new Callback<SolutionCategory>() {
                    @Override
                    public void onResponse(Call<SolutionCategory> call, Response<SolutionCategory> response) {
                        fillExisting();
                    }

                    @Override
                    public void onFailure(Call<SolutionCategory> call, Throwable t) {

                    }
                });
            }
        }
        if (!isNew)
            isExistingEditMode = !isExistingEditMode;
        if (!isChanged)
            fillTables();
        changeButtons();
    }


    private void saveProposed() {
        for (SolutionCategory category: proposedCategories) {
                service.update(UUID.fromString(category.getId()), category).enqueue(new Callback<SolutionCategory>() {
                    @Override
                    public void onResponse(Call<SolutionCategory> call, Response<SolutionCategory> response) {
                        fillProposed();
                    }

                    @Override
                    public void onFailure(Call<SolutionCategory> call, Throwable t) {

                    }
                });
            }
        isProposedEditMode = !isProposedEditMode;
        changeButtons();
    }


    private void newCategory() {

            TableLayout table = binding.existing;
            LayoutInflater inflater = LayoutInflater.from(table.getContext());

            SolutionCategory category = new SolutionCategory("","","",true);
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.existing_category_row, table, false);

            EditText nameCell = tableRow.findViewById(R.id.categoryName);
            EditText descCell = tableRow.findViewById(R.id.categoryDescription);

            nameCell.setText(category.getName());
            nameCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    category.setName(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            descCell.setText(category.getDescription());
            descCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    category.setDescription(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            tableRow.setEnabled(!isExistingEditMode);
            existingCategories.add(category);

            table.addView(tableRow);
    }

    private void changeButtons() {
        binding.editProposalButton.setVisibility(isProposedEditMode ? View.GONE:View.VISIBLE);
        binding.editCategoryButton.setVisibility(isExistingEditMode ? View.GONE:View.VISIBLE);

        binding.addCategoryButton.setVisibility(isExistingEditMode ? View.VISIBLE:View.GONE);

        binding.saveCategoryButton.setVisibility(isExistingEditMode ? View.VISIBLE:View.GONE);
        binding.saveProposalButton.setVisibility(isProposedEditMode ? View.VISIBLE:View.GONE);
    }

    private void fillExisting() {
        TableLayout existing = binding.existing;

        service.getActive().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                existingCategories.clear();
                existingCategories.addAll(response.body());
                fillTable(existing, existingCategories, true);
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {

            }
        });
    }

    private void fillProposed() {
        TableLayout proposed = binding.proposed;

        service.getProposed().enqueue(new Callback<Collection<SolutionCategory>>() {
            @Override
            public void onResponse(Call<Collection<SolutionCategory>> call, Response<Collection<SolutionCategory>> response) {
                proposedCategories.clear();
                proposedCategories.addAll(response.body());
                fillTable(proposed, proposedCategories, false);
            }

            @Override
            public void onFailure(Call<Collection<SolutionCategory>> call, Throwable t) {

            }
        });
    }

    private void removeCategory(SolutionCategory category) {
        service.delete(UUID.fromString(category.getId())).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                fillExisting();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }

    private void approveCategory(SolutionCategory category) {
        category.setActive(true);
        service.update(UUID.fromString(category.getId()), category).enqueue(new Callback<SolutionCategory>() {
            @Override
            public void onResponse(Call<SolutionCategory> call, Response<SolutionCategory> response) {
                fillTables();

            }

            @Override
            public void onFailure(Call<SolutionCategory> call, Throwable t) {

            }
        });
    }

    private void fillTable(TableLayout table, ArrayList<SolutionCategory> categories, boolean isExisting) {

        LayoutInflater inflater = LayoutInflater.from(table.getContext());
        table.removeAllViews();

        for (SolutionCategory category : categories) {
            // Inflate the TableRow layout (use the correct layout reference)
            TableRow tableRow;
            if (isExisting)
                tableRow = (TableRow) inflater.inflate(R.layout.existing_category_row, table, false);
            else
                tableRow = (TableRow) inflater.inflate(R.layout.proposed_category_row, table, false);

            // Find the name and description EditText within the inflated TableRow
            EditText nameCell = tableRow.findViewById(R.id.categoryName);
            EditText descCell = tableRow.findViewById(R.id.categoryDescription);

            // Set the text from the category object
            nameCell.setText(category.getName());
            nameCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    category.setName(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            descCell.setText(category.getDescription());
            descCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    category.setDescription(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            if (isExisting) {
                nameCell.setEnabled(isExistingEditMode);
                descCell.setEnabled(isExistingEditMode);
                ImageButton button = tableRow.findViewById(R.id.deleteCategory);
                button.setOnClickListener(v -> {removeCategory(category);
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);});
            }
            else {
                nameCell.setEnabled(isProposedEditMode);
                descCell.setEnabled(isProposedEditMode);
                ImageButton button = tableRow.findViewById(R.id.approveCategory);
                button.setOnClickListener(v -> {approveCategory(category);
                                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);});
            }


            // Add the TableRow to the TableLayout
            table.addView(tableRow);
        }
    }

    public void fillTables() {
        fillProposed();
        fillExisting();
    }



}