package com.example.eventify.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.fragments.ServiceDetailsFragment;
import com.example.eventify.models.events.BudgetItem;
import com.example.eventify.models.solutions.Solution;

import java.util.List;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ItemViewHolder> {

    private final Context context;
    private final List<BudgetItem> items;
    private final FragmentManager fragmentManager;

    public ItemListAdapter(Context context, List<BudgetItem> items, FragmentManager fragmentManager) {
        this.context = context;
        this.items = items;
        this.fragmentManager = fragmentManager;
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

        TableLayout table = holder.itemTable;
        LayoutInflater inflater = LayoutInflater.from(table.getContext());

        table.removeAllViews();

        for (Solution solution : item.getSelectedSolutions()) {
            TableRow tableRow = (TableRow) inflater.inflate(R.layout.budget_item_row, table, false);

            EditText nameCell = tableRow.findViewById(R.id.solutionTableName);
            nameCell.setText(solution.getName());

            tableRow.findViewById(R.id.solutionDetails).setOnClickListener(v -> {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_content, ServiceDetailsFragment.newInstance(solution));
                transaction.commit();
            });

            table.addView(tableRow);
        }

        table.setBackgroundColor(ContextCompat.getColor(context, R.color.white));


    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemValue;
        TableLayout itemTable;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemValue = itemView.findViewById(R.id.item_value);
            itemTable = itemView.findViewById(R.id.items);
        }
    }
}
