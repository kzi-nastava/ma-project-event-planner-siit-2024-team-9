package com.example.eventify.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.solutions.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        // Set comment text
        holder.commentText.setText(review.getComment());
        
        // Set status
        holder.statusText.setText("Status: " + review.getStatus());
        
        // Set rating stars
        setStarRating(holder, review.getGrade());
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    private void setStarRating(ReviewViewHolder holder, int rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setVisibility(View.VISIBLE);
                stars[i].setImageResource(R.drawable.check);
            } else {
                stars[i].setVisibility(View.VISIBLE);
                stars[i].setImageResource(R.drawable.back);
            }
        }
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView commentText, statusText;
        ImageView star1, star2, star3, star4, star5;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.commentText);
            statusText = itemView.findViewById(R.id.statusText);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
} 