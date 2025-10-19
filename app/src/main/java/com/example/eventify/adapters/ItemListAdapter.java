package com.example.eventify.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
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
    private Budget budget;
    private BudgetService service;
    private NavigationManager navigationManager;
    private boolean isEditMode;

    public ItemListAdapter(Context context, List<BudgetItem> items, FragmentManager fragmentManager, Budget budget, OnBudgetUpdatedListener budgetUpdatedListener, boolean isEditMode) {
        this.context = context;
        this.items = items;
        this.fragmentManager = fragmentManager;
        this.budget = budget;
        this.budgetUpdatedListener = budgetUpdatedListener;
        this.isEditMode = isEditMode;

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
        
        // Set category name
        holder.itemName.setText(item.getCategory().getName());
        
        // Set planned value
        holder.itemValue.setText(String.valueOf(item.getPlannedValue()));
        
        // Set up solutions RecyclerView
        setupSolutionsRecyclerView(holder, item);
        
        // Set up button states based on unsaved flag
        updateButtonStates(holder, item);
        
        // Set up click listeners
        setupClickListeners(holder, item, position);
    }
    
    private void setupSolutionsRecyclerView(ItemViewHolder holder, BudgetItem item) {
        List<Solution> solutions = new ArrayList<>(item.getSelectedSolutions());
        BudgetSolutionListAdapter solutionAdapter = new BudgetSolutionListAdapter(context, solutions, solution -> {
            if (navigationManager != null) {
                navigationManager.navigateToFragment(ServiceDetailsFragment.newInstance(solution));
            }
        });
        
        holder.solutionsRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.solutionsRecycler.setAdapter(solutionAdapter);
    }
    
    private void updateButtonStates(ItemViewHolder holder, BudgetItem item) {
        if (item.isUnsaved()) {
            // Edit mode - show save button, hide edit/delete (like Angular)
            holder.saveItem.setVisibility(View.VISIBLE);
            holder.editItem.setVisibility(View.GONE);
            holder.deleteItem.setVisibility(View.GONE);
            holder.itemValue.setEnabled(true);
        } else {
            // View mode - show edit/delete buttons, hide save (like Angular)
            holder.saveItem.setVisibility(View.GONE);
            holder.editItem.setVisibility(View.VISIBLE);
            holder.deleteItem.setVisibility(View.VISIBLE);
            holder.itemValue.setEnabled(false);
        }
    }
    
    private void setupClickListeners(ItemViewHolder holder, BudgetItem item, int position) {
        // Save button
        holder.saveItem.setOnClickListener(v -> saveItem(holder, item, position));
        
        // Edit button
        holder.editItem.setOnClickListener(v -> editItem(item));
        
        // Delete button
        holder.deleteItem.setOnClickListener(v -> deleteItem(item, position));
        
        // Input field done action
        holder.itemValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveItem(holder, item, position);
                return true;
            }
            return false;
        });
    }

    private void saveItem(ItemViewHolder holder, BudgetItem item, int position) {
        try {
            // Get the value from the input field
            String valueText = holder.itemValue.getText().toString().trim();
            if (valueText.isEmpty()) {
                Toast.makeText(context, "Please enter a planned value", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double value;
            try {
                value = Double.parseDouble(valueText);
                if (value < 0) {
                    Toast.makeText(context, "Planned value cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }
            
            item.setPlannedValue(value);
            
            // Mark as saved (like Angular saveItem method)
            item.setUnsaved(false);
            
            // Update budget immediately (like Angular)
            updateBudget();
            
            Toast.makeText(context, "Budget item saved", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e("ItemListAdapter", "Error saving budget item", e);
            Toast.makeText(context, "Error saving budget item", Toast.LENGTH_SHORT).show();
        }
    }

    private void editItem(BudgetItem item) {
        // Mark as unsaved to enter edit mode (like Angular editItem method)
        item.setUnsaved(true);
        
        // Notify adapter to refresh this item
        int position = items.indexOf(item);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    private void deleteItem(BudgetItem item, int position) {
        try {
            // Only allow deletion if no selected solutions (like Angular deleteItem method)
            if (item.getSelectedSolutions() == null || item.getSelectedSolutions().isEmpty()) {
                // Remove from items list
                items.remove(item);
                
                // Update budget immediately (like Angular)
                updateBudget();
                
                // Notify adapter
                notifyItemRemoved(position);
                
                Toast.makeText(context, "Budget item deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Cannot delete item with selected solutions", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ItemListAdapter", "Error deleting budget item", e);
            Toast.makeText(context, "Error deleting budget item", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBudget() {
        updateBudgetWithRetry(0);
    }
    
    private void updateBudgetWithRetry(int retryCount) {
        Log.d("ItemListAdapter", "Updating budget with ID: " + budget.getId() + " (attempt " + (retryCount + 1) + ")");
        
        // Validate items before sending
        for (BudgetItem item : items) {
            if (item.getCategory() == null) {
                Toast.makeText(context, "Invalid category for budget item", Toast.LENGTH_SHORT).show();
                return;
            }
            if (item.getPlannedValue() < 0) {
                Toast.makeText(context, "Planned value cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        // Update budget items
        budget.setItems(new java.util.HashSet<>(items));
        
        service.update(UUID.fromString(budget.getId()), budget).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ItemListAdapter", "Budget updated successfully");
                    // Update the budget object with the response
                    budget = response.body();
                    // Update the items list with the saved budget items
                    items.clear();
                    items.addAll(budget.getItems());
                    // Mark all items as saved
                    for (BudgetItem item : items) {
                        item.setUnsaved(false);
                    }
                    // Notify the adapter of changes
                    notifyDataSetChanged();
                    budgetUpdatedListener.onBudgetUpdated(false);
                } else {
                    Log.e("ItemListAdapter", "Failed to update budget: " + response.code());
                    String errorMessage = "Failed to save budget";
                    if (response.code() == 400) {
                        errorMessage = "Invalid budget data. Please check your inputs.";
                    } else if (response.code() == 404) {
                        errorMessage = "Budget not found. Please refresh and try again.";
                    } else if (response.code() == 409) {
                        errorMessage = "Budget was modified by another user. Please refresh and try again.";
                    } else if (response.code() >= 500) {
                        errorMessage = "Server error. Please try again later.";
                    }
                    
                    // Retry for concurrency conflicts (409) or server errors (5xx) up to 2 times
                    if ((response.code() == 409 || response.code() >= 500) && retryCount < 2) {
                        Log.d("ItemListAdapter", "Retrying budget update due to error: " + response.code());
                        // Wait a bit before retrying
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            updateBudgetWithRetry(retryCount + 1);
                        }, 1000 * (retryCount + 1)); // Exponential backoff
                        return;
                    }
                    
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    
                    // Revert unsaved items back to unsaved state on failure
                    for (BudgetItem item : items) {
                        if (item.isUnsaved()) {
                            // Keep as unsaved since save failed
                        }
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {
                Log.e("ItemListAdapter", "Error updating budget", t);
                Toast.makeText(context, "Network error while saving budget. Please check your connection.", Toast.LENGTH_LONG).show();
                
                // Revert unsaved items back to unsaved state on failure
                for (BudgetItem item : items) {
                    if (item.isUnsaved()) {
                        // Keep as unsaved since save failed
                    }
                }
                notifyDataSetChanged();
            }
        });
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        notifyDataSetChanged();
    }

    private void refreshItems() {
        service.getItems(UUID.fromString(budget.getId())).enqueue(new Callback<List<BudgetItem>>() {
            @Override
            public void onResponse(Call<List<BudgetItem>> call, Response<List<BudgetItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Update the items list with fresh data from backend
                    items.clear();
                    items.addAll(response.body());
                    // Mark all items as saved (unsaved = false)
                    for (BudgetItem item : items) {
                        item.setUnsaved(false);
                    }
                    // Notify the adapter of changes
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<BudgetItem>> call, Throwable t) {
                Log.e("ItemListAdapter", "Error refreshing items", t);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName;
        TextInputEditText itemValue;
        RecyclerView solutionsRecycler;
        MaterialButton editItem, saveItem, deleteItem;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemValue = itemView.findViewById(R.id.item_value);
            solutionsRecycler = itemView.findViewById(R.id.solutions_recycler);
            editItem = itemView.findViewById(R.id.editItem);
            saveItem = itemView.findViewById(R.id.saveItem);
            deleteItem = itemView.findViewById(R.id.deleteItem);
        }
    }
}
