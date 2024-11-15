package com.example.eventify;

import android.content.Context;
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

import com.example.eventify.Fragments.SolutionDetailsFragment;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.databinding.FragmentSolutionFormBinding;

import java.util.ArrayList;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {
    private final ArrayList<Service> aProducts;
    private final Context context;
    private final FragmentManager fragmentManager; // Add FragmentManager

    // Bindings are not needed here since we are not using them in the adapter
    private FragmentCardBinding cardBinding;
    private FragmentSolutionFormBinding formBinding;

    // Modify constructor to accept FragmentManager
    public ServiceListAdapter(Context context, ArrayList<Service> products, FragmentManager fragmentManager) {
        this.context = context;
        this.aProducts = products;
        this.fragmentManager = fragmentManager; // Assign the FragmentManager
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
        Service product = aProducts.get(position);

        if (product != null) {
            holder.imageView.setImageResource(product.getImage());
            holder.productTitle.setText(product.getTitle());
            holder.productDescription.setText(product.getDescription());

            // Set up the button click listener for each item
            holder.itemView.setOnClickListener(v -> {
                // Use the FragmentManager to perform the transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_content, SolutionDetailsFragment.newInstance("gas", "gas"));
                transaction.addToBackStack(null);
                transaction.commit();
            });

            holder.detailsBtn.setOnClickListener(v -> {
                // Use the FragmentManager to perform the transaction
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_content, SolutionFormFragment.newInstance("gas", "gas"));
                transaction.addToBackStack(null);
                transaction.commit();
            });


        }
    }

    // Return the total count of items
    @Override
    public int getItemCount() {
        return aProducts.size();
    }
}

