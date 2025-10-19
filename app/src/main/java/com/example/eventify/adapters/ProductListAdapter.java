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
import com.example.eventify.databinding.FragmentProductFormBinding;
import com.example.eventify.fragments.ProductDetailsFragment;
import com.example.eventify.R;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.fragments.ProductFormFragment;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.NavigationManager;
import com.example.eventify.utils.ImageUrlUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
    private final ArrayList<Product> products;
    private final Context context;
    private final FragmentManager fragmentManager; // Add FragmentManager
    private NavigationManager navigationManager;

    // Bindings are not needed here since we are not using them in the adapter
    private FragmentCardBinding cardBinding;
    private FragmentProductFormBinding formBinding;

    // Modify constructor to accept FragmentManager
    public ProductListAdapter(Context context, ArrayList<Product> products, FragmentManager fragmentManager) {
        this.context = context;
        this.products = products;
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
        Product product = products.get(position);

        // Set the product title
        holder.productTitle.setText(product.getName());

        // Set the product description
        holder.productDescription.setText(product.getDescription());

        // Load the product image using Glide
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String filename = product.getImages().get(0); // Get the first image filename
            String imageUrl = ImageUrlUtils.getImageUrl(filename, "product");
            
            if (imageUrl != null) {
                Log.d("ProductListAdapter", "Loading local image from URL: " + imageUrl);
                
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.photos_holder) // Add a placeholder image
                        .error(R.drawable.photos_holder) // Add an error image
                        .into(holder.imageView);
            } else {
                Log.d("ProductListAdapter", "Ignoring web URL or invalid filename for product: " + product.getName() + ", filename: " + filename);
                holder.imageView.setImageResource(R.drawable.photos_holder);
            }
        } else {
            // Set a default image if no images are available
            Log.d("ProductListAdapter", "No images available for product: " + product.getName());
            holder.imageView.setImageResource(R.drawable.photos_holder);
        }

        // Set click listener for the details button
        holder.detailsBtn.setOnClickListener(v -> {
            // Navigate to ProductFormFragment for editing
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_content, ProductFormFragment.newInstance(product));
            transaction.addToBackStack("products");
            transaction.commit();
        });

        // Set click listener for the entire card
        holder.productCard.setOnClickListener(v -> {
            // Navigate to ProductDetailsFragment for viewing
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_content, ProductDetailsFragment.newInstance(product));
            transaction.addToBackStack("products");
            transaction.commit();
        });
    }

    // Return the total number of items in the dataset
    @Override
    public int getItemCount() {
        return products.size();
    }

}
