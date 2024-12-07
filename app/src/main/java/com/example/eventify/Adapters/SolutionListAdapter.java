package com.example.eventify.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.models.Solution;
import com.example.eventify.R;

import java.util.List;

public class SolutionListAdapter extends RecyclerView.Adapter<SolutionListAdapter.SolutionViewHolder> {

    private final Context context;
    private final List<Solution> solutions;

    public SolutionListAdapter(Context context, List<Solution> solutions) {
        this.context = context;
        this.solutions = solutions;
    }

    @NonNull
    @Override
    public SolutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_solution_card, parent, false);
        return new SolutionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolutionViewHolder holder, int position) {
        Solution solution = solutions.get(position);

        holder.solutionName.setText(solution.getName());
        holder.solutionPrice.setText("Price: $" + solution.getPrice());
        holder.solutionDiscount.setText("Discount: " + solution.getDiscount() + "%");
        holder.solutionImage.setImageResource(R.drawable.dummy_event_image);
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    static class SolutionViewHolder extends RecyclerView.ViewHolder {
        TextView solutionName, solutionPrice, solutionDiscount;
        ImageView solutionImage;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);

            solutionName = itemView.findViewById(R.id.solution_name);
            solutionPrice = itemView.findViewById(R.id.solution_price);
            solutionDiscount = itemView.findViewById(R.id.solution_discount);
            solutionImage = itemView.findViewById(R.id.solution_image);
        }
    }
}
