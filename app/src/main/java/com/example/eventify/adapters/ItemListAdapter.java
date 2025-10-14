package com.example.eventify.adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.fragments.ServiceDetailsFragment;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.BudgetItem;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.activities.MainActivity;
import com.example.eventify.utils.NavigationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    public interface OnBudgetUpdatedListener {
        void onBudgetUpdated(boolean deleted);
    }

    private final Context context;
    private final List<BudgetItem> items;
    private final FragmentManager fragmentManager;
    private final OnBudgetUpdatedListener budgetUpdatedListener;
    private Map<Integer, Boolean> editModeMap = new HashMap<>();
    private Budget budget;
    private BudgetService service;
    private NavigationManager navigationManager;

    public ItemListAdapter(Context context, List<BudgetItem> items, FragmentManager fragmentManager, Budget budget, OnBudgetUpdatedListener budgetUpdatedListener) {
        this.context = context;
        this.items = items;
        this.fragmentManager = fragmentManager;
        this.budget = budget;
        this.budgetUpdatedListener = budgetUpdatedListener;

        this.service = RetrofitClient
                .getClient(context.getApplicationContext())
                .create(BudgetService.class);

        if (context instanceof NavigationManager) {
            this.navigationManager = (NavigationManager) context;
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_budget_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        BudgetItem item = items.get(position);
        holder.itemName.setText(item.getCategory().getName());
        holder.itemValue.setText(String.valueOf(item.getPlannedValue()));
        
        // Set initial state based on edit mode
        boolean isInEditMode = editModeMap.getOrDefault(position, false);
        holder.itemValue.setEnabled(isInEditMode);
        
        // Handle keyboard done action
        holder.itemValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                try {
                    String value = holder.itemValue.getText().toString();
                    if (!value.isEmpty()) {
                        item.setPlannedValue(Double.parseDouble(value));
                        saveItem(holder, item, position);
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid number
                }
                return true;
            }
            return false;
        });

        // Handle focus changes
        holder.itemValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isInEditMode) {
                // If getting focus while not in edit mode, enter edit mode
                editItem(holder, position);
            }
        });
        
        // Clear any existing TextWatcher to avoid duplicates
        TextWatcher existingWatcher = (TextWatcher) holder.itemValue.getTag();
        if (existingWatcher != null) {
            holder.itemValue.removeTextChangedListener(existingWatcher);
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (!s.toString().isEmpty()) {
                        double value = Double.parseDouble(s.toString());
                        item.setPlannedValue(value);
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid number format
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        
        holder.itemValue.setTag(watcher);
        holder.itemValue.addTextChangedListener(watcher);

        TableLayout table = holder.itemTable;
        table.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(table.getContext());

        for (Solution solution : item.getSelectedSolutions()) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.budget_item_row, table, false);

            TextView nameCell = tableRow.findViewById(R.id.solutionTableName);
            nameCell.setText(solution.getName());

            tableRow.findViewById(R.id.solutionDetails).setOnClickListener(v -> {
                if (navigationManager != null) {
                    navigationManager.navigateToFragment(ServiceDetailsFragment.newInstance(solution));
                }
            });

            table.addView(tableRow);
        }

        btnHandler(holder, position);

        holder.save.setOnClickListener(v -> saveItem(holder, item, position));
        holder.edit.setOnClickListener(v -> editItem(holder, position));
        holder.delete.setOnClickListener(v -> deleteItem(holder, item, position));
    }

    private void btnHandler(ItemViewHolder holder, int position) {
        boolean isInEditMode = editModeMap.getOrDefault(position, false);
        
        if (isInEditMode) {
            // In edit mode
            holder.delete.setVisibility(View.GONE);
            holder.edit.setVisibility(View.GONE);
            holder.save.setVisibility(View.VISIBLE);
            holder.itemValue.setEnabled(true);
            
            // Post the focus and keyboard show to ensure the window has focus
            holder.itemValue.post(() -> {
                holder.itemValue.requestFocus();
                if (context instanceof Activity && !((Activity) context).isFinishing()) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(holder.itemValue, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            });
        } else {
            // Not in edit mode
            holder.delete.setVisibility(View.VISIBLE);
            holder.edit.setVisibility(View.VISIBLE);
            holder.save.setVisibility(View.GONE);
            holder.itemValue.setEnabled(false);
            
            // Hide keyboard safely
            if (holder.itemValue.hasFocus()) {
                holder.itemValue.clearFocus();
                if (context instanceof Activity && !((Activity) context).isFinishing()) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(holder.itemValue.getWindowToken(), 0);
                    }
                }
            }
        }
    }

    private void saveItem(ItemViewHolder holder, BudgetItem item, int position) {
        editModeMap.put(position, false);
        Set<BudgetItem> items = budget.getItems();
        items.add(item);
        budget.setItems(items);
        
        // Hide keyboard safely
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && holder.itemValue.getWindowToken() != null) {
                imm.hideSoftInputFromWindow(holder.itemValue.getWindowToken(), 0);
            }
        }
        
        updateBudget(holder, false, position);
        btnHandler(holder, position);
    }

    private void editItem(ItemViewHolder holder, int position) {
        editModeMap.put(position, true);
        btnHandler(holder, position);
    }

    private void deleteItem(ItemViewHolder holder, BudgetItem item, int position) {
        if (item.getSelectedSolutions().isEmpty()) {
            editModeMap.put(position, false);
            Set<BudgetItem> items = budget.getItems();
            items.remove(item);
            budget.setItems(items);
            updateBudget(holder, true, position);
        }
    }

    private void updateBudget(ItemViewHolder holder, boolean deleted, int position) {
        service.update(UUID.fromString(budget.getId()), budget).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                budgetUpdatedListener.onBudgetUpdated(deleted);
                editModeMap.put(position, false);
                btnHandler(holder, position);
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {
                // Handle error
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        EditText itemValue;
        TableLayout itemTable;
        ImageButton edit, save, delete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemValue = itemView.findViewById(R.id.item_value);
            itemTable = itemView.findViewById(R.id.items);
            edit = itemView.findViewById(R.id.editItem);
            save = itemView.findViewById(R.id.saveItem);
            delete = itemView.findViewById(R.id.deleteItem);
        }
    }
}
