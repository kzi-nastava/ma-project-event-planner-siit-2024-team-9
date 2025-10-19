package com.example.eventify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.solutions.Solution;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class BudgetSolutionListAdapter extends RecyclerView.Adapter<BudgetSolutionListAdapter.SolutionViewHolder> {

    public interface OnSolutionClickListener {
        void onSolutionClick(Solution solution);
    }

    private final Context context;
    private final List<Solution> solutions;
    private final OnSolutionClickListener clickListener;

    public BudgetSolutionListAdapter(Context context, List<Solution> solutions, OnSolutionClickListener clickListener) {
        this.context = context;
        this.solutions = solutions;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.solution_item_row, parent, false);
        return new SolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        Solution solution = solutions.get(position);
        
        holder.solutionName.setText(solution.getName());
        
        holder.detailsButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSolutionClick(solution);
            }
        });
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    static class SolutionViewHolder extends RecyclerView.ViewHolder {
        TextView solutionName;
        MaterialButton detailsButton;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);
            solutionName = itemView.findViewById(R.id.solution_name);
            detailsButton = itemView.findViewById(R.id.details_button);
        }
    }
}