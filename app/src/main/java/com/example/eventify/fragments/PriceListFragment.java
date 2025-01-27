package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.example.eventify.R;
import com.example.eventify.databinding.FragmentPriceListBinding;
import com.example.eventify.models.others.Discount;
import com.example.eventify.models.others.Price;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.models.solutions.SolutionCategory;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PriceListFragment extends Fragment {

    FragmentPriceListBinding binding;
    SolutionService service = RetrofitClient.getClient().create(SolutionService.class);
    List<Solution> solutions = new ArrayList<>();
    boolean editMode = false;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentPriceListBinding.inflate(inflater, container, false);

        getSolutions();
        binding.savePrices.setOnClickListener(v -> save());
        binding.editPrices.setOnClickListener(v -> setEditMode());

        return binding.getRoot();
    }

    private void getSolutions() {
        service.getAllPaginated(0, 5, "name", true).enqueue(new Callback<SolutionService.SolutionAllResponse>() {
                                                                @Override
                                                                public void onResponse(Call<SolutionService.SolutionAllResponse> call, Response<SolutionService.SolutionAllResponse> response) {
                                                                    solutions = response.body().content;
                                                                    fillTable();
                                                                    changeButtons();
                                                                }

                                                                @Override
                                                                public void onFailure(Call<SolutionService.SolutionAllResponse> call, Throwable t) {

                                                                }
                                                            }
        );
    }

    private void setEditMode() {
        editMode = !editMode;
        changeButtons();
        fillTable();
    }

    private void save() {
        saveSolutions();
        editMode = false;
        changeButtons();
        fillTable();
    }

    private void saveSolutions() {
        for (Solution solution:solutions) {
            service.updatePrice(solution.getId().toString(), new Price(solution.getPrice())).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    service.updateDiscount(solution.getId().toString(), new Discount(solution.getDiscount())).enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {

                        }
                    });
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {

                }
            });
        }
    }

    private void changeButtons() {
        binding.editPrices.setVisibility(editMode ? View.GONE:View.VISIBLE);
        binding.savePrices.setVisibility(editMode ? View.VISIBLE:View.GONE);
    }

    private void fillTable() {

        TableLayout table = binding.solutionPrices;
        LayoutInflater inflater = LayoutInflater.from(table.getContext());
        table.removeAllViews();
        int solutionsNumber=0;

        for (Solution solution : solutions) {
            solutionsNumber++;
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.price_list_row, table, false);

            EditText numberCell = tableRow.findViewById(R.id.solutionNumber);
            EditText nameCell = tableRow.findViewById(R.id.solutionName);
            EditText priceCell = tableRow.findViewById(R.id.solutionPrice);
            EditText discountCell = tableRow.findViewById(R.id.solutionDiscount);
            EditText discountedCell = tableRow.findViewById(R.id.solutionDiscounted);

            numberCell.setEnabled(false);
            nameCell.setEnabled(false);
            priceCell.setEnabled(editMode);
            discountedCell.setEnabled(false);
            discountCell.setEnabled(editMode);

            numberCell.setText(String.valueOf(solutionsNumber));

            nameCell.setText(solution.getName());

            priceCell.setText(String.valueOf(solution.getPrice()));
            priceCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    solution.setPrice(Double.parseDouble(s.toString()));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            discountCell.setText(String.valueOf(solution.getDiscount()));
            discountCell.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    solution.setDiscount(Double.parseDouble(s.toString()));
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            discountedCell.setText(String.valueOf(solution.getPrice()*(1-solution.getDiscount()/100)));

            table.addView(tableRow);
        }
    }
}