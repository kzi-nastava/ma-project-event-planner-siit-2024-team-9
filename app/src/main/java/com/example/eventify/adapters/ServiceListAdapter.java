package com.example.eventify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventify.databinding.FragmentServiceFormBinding;
import com.example.eventify.fragments.ServiceDetailsFragment;
import com.example.eventify.R;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.fragments.ServiceFormFragment;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.NavigationManager;

import java.util.ArrayList;
import java.util.List;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {
    private final ArrayList<Service> services;
    private final Context context;
    private final FragmentManager fragmentManager; // Add FragmentManager
    private NavigationManager navigationManager;

    // Bindings are not needed here since we are not using them in the adapter
    private FragmentCardBinding cardBinding;
    private FragmentServiceFormBinding formBinding;

    // Modify constructor to accept FragmentManager
    public ServiceListAdapter(Context context, ArrayList<Service> services, FragmentManager fragmentManager) {
        this.context = context;
        this.services = services;
        this.fragmentManager = fragmentManager; // Assign the FragmentManager
        if (context instanceof NavigationManager) {
            this.navigationManager = (NavigationManager) context;
        }
    }

    // ViewHolder class to hold references to each item view for efficient recycling.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout productCard;
        public ImageView imageView;
        public TextView productTitle;
        public TextView productDescription;
        public Button detailsBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            productCard = itemView.findViewById(R.id.product_card_item);
            imageView = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            productDescription = itemView.findViewById(R.id.product_description);
            detailsBtn = itemView.findViewById(R.id.detailsBtn); // Access the button
        }
    }

    // Inflate the item layout and create the ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_card, parent, false);
        return new ViewHolder(view);
    }

    // Bind data to the view components for each item in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service service = services.get(position);

        if (service != null) {
            // Load image with proper URL construction
            if (service.getImages() != null && !service.getImages().isEmpty()) {
                String imageItem = service.getImages().get(0); // Get first image
                
                // Construct the full URL for the image
                String baseUrl = RetrofitClient.BASE_URL.replace("api/", "");
                String imageUrl = baseUrl + "images/service/" + imageItem;
                
                Log.d("ServiceListAdapter", "Loading image from: " + imageUrl);
                
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.add) // Show placeholder while loading
                    .error(R.drawable.add) // Show error image if loading fails
                    .into(holder.imageView);
            } else {
                // No images available, show placeholder
                holder.imageView.setImageResource(R.drawable.add);
            }
            
            holder.productTitle.setText(service.getName());
            holder.productDescription.setText(service.getSpecifity());

            // Set up the button click listener for each item
            holder.detailsBtn.setOnClickListener(v -> {
                // Use the FragmentManager to perform the transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                ServiceFormFragment fragment = ServiceFormFragment.newInstance(service);
                transaction.replace(R.id.home_container, fragment);
                transaction.addToBackStack("details");
                transaction.commit();
                fragmentManager.executePendingTransactions();
            });

            holder.itemView.setOnClickListener(v -> {
                if (navigationManager != null) {
                    navigationManager.navigateToFragment(ServiceDetailsFragment.newInstance(service));
                }
            });
        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return services.size();
    }
}

