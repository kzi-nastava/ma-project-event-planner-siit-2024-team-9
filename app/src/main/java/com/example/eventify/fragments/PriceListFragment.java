package com.example.eventify.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.Toast;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentPriceListBinding;
import com.example.eventify.models.others.Discount;
import com.example.eventify.models.others.Price;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListFragment extends Fragment {

    private FragmentPriceListBinding binding;
    private SolutionService service = RetrofitClient.getClient().create(SolutionService.class);
    private List<Solution> solutions = new ArrayList<>();
    private boolean editMode = false;
    private UserSession userSession;

    public PriceListFragment() {}

    public static PriceListFragment newInstance() {
        PriceListFragment fragment = new PriceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentPriceListBinding.inflate(inflater, container, false);

        // Set initial button states
        binding.savePrices.setVisibility(View.GONE);
        binding.editPrices.setVisibility(View.VISIBLE);

        // Set click listeners
        binding.savePrices.setOnClickListener(v -> save());
        binding.editPrices.setOnClickListener(v -> setEditMode());

        // Load solutions
        loadSolutions();

        return binding.getRoot();
    }

    private void loadSolutions() {
        if (!userSession.isValidSession()) {
            showError("Please log in to view your solutions");
            return;
        }

        showLoading();
        UUID userId = userSession.getCurrentUserId();

        service.getByOwner(userId.toString()).enqueue(new Callback<List<Solution>>() {
            @Override
            public void onResponse(Call<List<Solution>> call, Response<List<Solution>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    solutions = response.body();
                    if (solutions.isEmpty()) {
                        showError("No solutions found");
                    } else {
                        hideError();
                        fillTable();
                    }
                } else {
                    showError("Failed to load solutions");
                }
            }

            @Override
            public void onFailure(Call<List<Solution>> call, Throwable t) {
                hideLoading();
                showError("Error loading solutions: " + t.getMessage());
            }
        });
    }

    private void setEditMode() {
        editMode = !editMode;
        changeButtons();
        fillTable();
    }

    private void save() {
        showLoading();
        int totalSolutions = solutions.size();
        final int[] savedCount = {0};
        final int[] errorCount = {0};

        for (Solution solution : solutions) {
            // First update price
            service.updatePrice(solution.getId().toString(), new Price(solution.getPrice()))
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                            // If price update successful, update discount
                            service.updateDiscount(solution.getId().toString(), new Discount(solution.getDiscount()))
                                .enqueue(new Callback<Boolean>() {
                                    @Override
                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                        if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                                            savedCount[0]++;
                                        } else {
                                            errorCount[0]++;
                                        }
                                        checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                                    }

                                    @Override
                                    public void onFailure(Call<Boolean> call, Throwable t) {
                                        errorCount[0]++;
                                        checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                                    }
                                });
                        } else {
                            errorCount[0]++;
                            checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        errorCount[0]++;
                        checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                    }
                });
        }
    }

    private void checkSaveCompletion(int total, int saved, int errors) {
        if (saved + errors == total) {
            hideLoading();
            if (errors > 0) {
                showError("Failed to save some changes");
            } else {
                Toast.makeText(requireContext(), "All changes saved successfully", Toast.LENGTH_SHORT).show();
                editMode = false;
                changeButtons();
                loadSolutions(); // Reload to get fresh data
            }
        }
    }

    private void changeButtons() {
        binding.editPrices.setVisibility(editMode ? View.GONE : View.VISIBLE);
        binding.savePrices.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private void fillTable() {
        binding.solutionPrices.removeAllViews();

        // Add header row
        View headerRow = LayoutInflater.from(requireContext()).inflate(R.layout.price_list_row, binding.solutionPrices, false);
        headerRow.setBackgroundColor(getResources().getColor(android.R.color.white));
        binding.solutionPrices.addView(headerRow);

        int solutionNumber = 0;
        for (Solution solution : solutions) {
            solutionNumber++;
            TableRow row = (TableRow) LayoutInflater.from(requireContext()).inflate(R.layout.price_list_row, binding.solutionPrices, false);

            // Set alternating row background
            row.setBackgroundColor(solutionNumber % 2 == 0 ? 
                getResources().getColor(android.R.color.white) : 
                Color.parseColor("#F5F5F5"));

            // Set up row views
            setupRowViews(row, solution, solutionNumber);

            binding.solutionPrices.addView(row);
        }
    }

    private void setupRowViews(TableRow row, Solution solution, int number) {
        // Number
        ((android.widget.TextView) row.findViewById(R.id.solutionNumber))
            .setText(String.valueOf(number));

        // Name
        ((android.widget.TextView) row.findViewById(R.id.solutionName))
            .setText(solution.getName());

        // Price
        EditText priceCell = row.findViewById(R.id.solutionPrice);
        priceCell.setText(String.format("%.2f", solution.getPrice()));
        priceCell.setEnabled(editMode);
        priceCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double price = Double.parseDouble(s.toString());
                    solution.setPrice(price);
                    updateDiscountedPrice(row, solution);
                } catch (NumberFormatException e) {
                    // Invalid number, ignore
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Discount
        EditText discountCell = row.findViewById(R.id.solutionDiscount);
        discountCell.setText(String.format("%.1f", solution.getDiscount()));
        discountCell.setEnabled(editMode);
        discountCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double discount = Double.parseDouble(s.toString());
                    solution.setDiscount(discount);
                    updateDiscountedPrice(row, solution);
                } catch (NumberFormatException e) {
                    // Invalid number, ignore
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Final price
        updateDiscountedPrice(row, solution);
    }

    private void updateDiscountedPrice(TableRow row, Solution solution) {
        double finalPrice = solution.getPrice() * (1 - solution.getDiscount() / 100);
        ((android.widget.TextView) row.findViewById(R.id.solutionDiscounted))
            .setText(String.format("%.2f", finalPrice));
    }

    private void showLoading() {
        binding.loadingProgress.setVisibility(View.VISIBLE);
        binding.errorText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.loadingProgress.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.errorText.setText(message);
        binding.errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.errorText.setVisibility(View.GONE);
    }
}