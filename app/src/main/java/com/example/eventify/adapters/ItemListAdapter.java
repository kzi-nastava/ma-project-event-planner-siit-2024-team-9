package com.example.eventify.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private boolean editMode=false;
    private Budget budget;
    private BudgetService service = RetrofitClient.getClient().create(BudgetService.class);

    public ItemListAdapter(Context context, List<BudgetItem> items, FragmentManager fragmentManager, Budget budget, OnBudgetUpdatedListener budgetUpdatedListener) {
        this.context = context;
        this.items = items;
        this.fragmentManager = fragmentManager;
        this.budget = budget;
        this.budgetUpdatedListener = budgetUpdatedListener;
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
        holder.itemValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(holder.itemValue, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        holder.itemValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty())
                    item.setPlannedValue(Double.valueOf(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        TableLayout table = holder.itemTable;
        LayoutInflater inflater = LayoutInflater.from(table.getContext());

        for (Solution solution : item.getSelectedSolutions()) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.budget_item_row, table, false);

            TextView nameCell = tableRow.findViewById(R.id.solutionTableName);
            nameCell.setText(solution.getName());

            tableRow.findViewById(R.id.solutionDetails).setOnClickListener(v -> {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.home_container, ServiceDetailsFragment.newInstance(solution));
                transaction.addToBackStack(null);
                transaction.commit();
            });

            table.addView(tableRow);
        }

        btnHandler(holder);

        holder.save.setOnClickListener(v -> saveItem(holder, item));
        holder.edit.setOnClickListener(v -> editItem(holder));
        holder.delete.setOnClickListener(v -> deleteItem(holder, item));

    }

    private void btnHandler(ItemViewHolder holder) {
        holder.delete.setVisibility(editMode ? View.GONE:View.VISIBLE);
        holder.edit.setVisibility(editMode ? View.GONE:View.VISIBLE);
        holder.itemValue.setEnabled(editMode);
    }

    private void saveItem(ItemViewHolder holder, BudgetItem item) {
        holder.save.setVisibility(View.GONE);
        Set<BudgetItem> items = budget.getItems();
        items.add(item);
        budget.setItems(items);
        updateBudget(holder, false);
    }

    private void editItem(ItemViewHolder holder) {
        editMode = true;
        btnHandler(holder);
    }

    private void deleteItem(ItemViewHolder holder, BudgetItem item) {
        editMode = false;
        Set<BudgetItem> items = budget.getItems();
        items.remove(item);
        budget.setItems(items);
        updateBudget(holder, true);
    }

    private void updateBudget(ItemViewHolder holder, boolean deleted) {
        service.update(UUID.fromString(budget.getId()), budget).enqueue(new Callback<Budget>() {
            @Override
            public void onResponse(Call<Budget> call, Response<Budget> response) {
                budgetUpdatedListener.onBudgetUpdated(deleted);
                editMode = false;
                btnHandler(holder);
            }

            @Override
            public void onFailure(Call<Budget> call, Throwable t) {

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
