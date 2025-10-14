package com.example.eventify.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.solutions.Review;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.VH> {

    public interface Listener {
        void onApprove(Review r, int position);
        void onDelete(Review r, int position);
        void onClick(Review r, int position);
    }

    private final List<Review> data;
    private final Listener listener;
    private final DateFormat dfShort = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public ReviewListAdapter(List<Review> data, Listener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Review r = data.get(position);

        String title = (r.getSolution() != null && !TextUtils.isEmpty(r.getSolution().getName()))
                ? r.getSolution().getName()
                : h.itemView.getContext().getString(R.string.review);
        h.tvSolutionTitle.setText(title);

        h.tvComment.setText(r.getComment() != null ? r.getComment() : "");
        h.ratingBar.setRating(r.getGrade());

        h.chipStatus.setText(r.getStatus() != null ? r.getStatus() : "PENDING");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(r, h.getBindingAdapterPosition());
        });
        h.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(r, h.getBindingAdapterPosition());
        });
        h.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(r, h.getBindingAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return data != null ? data.size() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSolutionTitle, tvComment, tvDate;
        RatingBar ratingBar;
        Chip chipStatus;
        MaterialButton btnApprove, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvSolutionTitle = itemView.findViewById(R.id.tv_solution_title);
            tvComment = itemView.findViewById(R.id.tv_comment);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            chipStatus = itemView.findViewById(R.id.chip_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnApprove = itemView.findViewById(R.id.btn_approve);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
