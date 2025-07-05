package com.example.eventify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventify.fragments.ServiceDetailsFragment;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.R;
import com.example.eventify.utils.RetrofitClient;
import com.google.android.flexbox.FlexboxLayout;
import com.example.eventify.utils.NavigationManager;

import java.util.List;

public class SolutionListAdapter extends RecyclerView.Adapter<SolutionListAdapter.SolutionViewHolder> {

    private final Context context;
    private final List<Solution> solutions;
    private NavigationManager navigationManager;

    private final FragmentManager fragmentManager;

    public SolutionListAdapter(Context context, List<Solution> solutions, FragmentManager fragmentManager) {
        this.context = context;
        this.solutions = solutions;
        this.fragmentManager = fragmentManager;
        if (context instanceof NavigationManager) {
            this.navigationManager = (NavigationManager) context;
        }
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

        // Set Name and Description
        holder.solutionName.setText(solution.getName());
        holder.solutionDescription.setText(solution.getDescription());

        // Set Price and Discounted Price
        if (solution.getPrice() == 0) {
            holder.solutionPrice.setText(R.string.free);
            holder.solutionOriginalPrice.setVisibility(View.GONE);
        } else {
            double discountedPrice = solution.getPrice() * (100 - solution.getDiscount()) / 100.0;
            String formattedPrice = String.format("%.2f", discountedPrice);
            holder.solutionPrice.setText(context.getString(R.string.dollar_sign) + formattedPrice);
            holder.solutionOriginalPrice.setText(context.getString(R.string.dollar_sign) + solution.getPrice());
            holder.solutionOriginalPrice.setVisibility(solution.getDiscount() > 0 ? View.VISIBLE : View.GONE);
        }

        // Load image with proper URL construction
        if (solution.getImages() != null && !solution.getImages().isEmpty()) {
            String imageItem = solution.getImages().get(0); // Get first image
            
            // Construct the full URL for the image
            // Determine folder based on solution type (service or product)
            String imageFolder = solution.isService() ? "service" : "product";
            String baseUrl = RetrofitClient.BASE_URL.replace("api/", "");
            String imageUrl = baseUrl + "images/" + imageFolder + "/" + imageItem;
            
            Log.d("SolutionListAdapter", "Loading image from: " + imageUrl);
            
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.add) // Show placeholder while loading
                .error(R.drawable.add) // Show error image if loading fails
                .into(holder.solutionImage);
        } else {
            // No images available, show placeholder
            holder.solutionImage.setImageResource(R.drawable.add);
        }

        // Set Type Badge
        if (solution.isService()) {
            holder.solutionType.setText("Service");
            holder.solutionType.setBackground(context.getDrawable(R.drawable.service_badge_background));
        } else {
            holder.solutionType.setText("Product");
            holder.solutionType.setBackground(context.getDrawable(R.drawable.product_badge_background));
        }

        // Set Discount Badge
        if (solution.getDiscount() > 0) {
            holder.solutionDiscount.setVisibility(View.VISIBLE);
            holder.solutionDiscount.setText(solution.getDiscount() + "% OFF");
        } else {
            holder.solutionDiscount.setVisibility(View.GONE);
        }

        // Set Event Type Tags
        holder.solutionEventTypes.removeAllViews();
        if (solution.getEventTypes() != null) {
            for (EventType type : solution.getEventTypes()) {
                TextView tag = new TextView(context);
                tag.setText(type.getName());
                tag.setBackground(context.getDrawable(R.drawable.rounded_tile_event_card));
                tag.setPadding(12, 6, 12, 6);

                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        FlexboxLayout.LayoutParams.WRAP_CONTENT,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(6, 6, 6, 6); // Margine između pločica
                tag.setLayoutParams(params);

                tag.setTextColor(context.getColor(android.R.color.black));
                tag.setTextSize(12);
                holder.solutionEventTypes.addView(tag);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (navigationManager != null) {
                navigationManager.navigateToFragment(ServiceDetailsFragment.newInstance(solution));
            }
        });
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    static class SolutionViewHolder extends RecyclerView.ViewHolder {
        TextView solutionName, solutionDescription, solutionPrice, solutionOriginalPrice, solutionDiscount, solutionType;
        FlexboxLayout solutionEventTypes;
        ImageView solutionImage;

        public SolutionViewHolder(@NonNull View itemView) {
            super(itemView);

            solutionName = itemView.findViewById(R.id.solution_name);
            solutionDescription = itemView.findViewById(R.id.solution_description);
            solutionPrice = itemView.findViewById(R.id.solution_price);
            solutionOriginalPrice = itemView.findViewById(R.id.solution_original_price);
            solutionDiscount = itemView.findViewById(R.id.solution_discount);
            solutionType = itemView.findViewById(R.id.solution_type);
            solutionImage = itemView.findViewById(R.id.solution_image);
            solutionEventTypes = itemView.findViewById(R.id.solution_event_types);
        }
    }

}
