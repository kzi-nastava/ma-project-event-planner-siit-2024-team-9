package com.example.eventify.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventify.R;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.utils.ImageUrlUtils;

public class ProductDetailsFragment extends Fragment {

    private Product product;

    public ProductDetailsFragment() {
        // Required empty public constructor
    }

    public static ProductDetailsFragment newInstance(Product product) {
        ProductDetailsFragment fragment = new ProductDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable("product");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (product != null) {
            TextView nameTextView = view.findViewById(R.id.product_name);
            TextView descriptionTextView = view.findViewById(R.id.product_description);
            TextView priceTextView = view.findViewById(R.id.product_price);
            ImageView productImageView = view.findViewById(R.id.product_image);

            nameTextView.setText(product.getName());
            descriptionTextView.setText(product.getDescription());
            priceTextView.setText("$" + String.format("%.2f", product.getPrice()));

            // Load the product image using Glide
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String filename = product.getImages().get(0); // Get first image
                
                // Use ImageUrlUtils to get the proper URL (ignores web URLs)
                String imageUrl = ImageUrlUtils.getImageUrl(filename, "product");
                
                // Debug logging
                android.util.Log.d("ProductDetails", "Product: " + product.getName());
                android.util.Log.d("ProductDetails", "Filename: " + filename);
                android.util.Log.d("ProductDetails", "Image URL: " + imageUrl);
                
                if (imageUrl != null) {
                    // Load local image
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.photos_holder) // Show placeholder while loading
                        .error(R.drawable.photos_holder) // Show error image if loading fails
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                android.util.Log.e("ProductDetails", "Image load failed: " + (e != null ? e.getMessage() : "Unknown error"));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                android.util.Log.d("ProductDetails", "Image load successful");
                                return false;
                            }
                        })
                        .into(productImageView);
                } else {
                    // Web URL or invalid filename - show placeholder
                    android.util.Log.d("ProductDetails", "Image URL is null, showing placeholder");
                    productImageView.setImageResource(R.drawable.photos_holder);
                }
            } else {
                // No images available, show placeholder
                android.util.Log.d("ProductDetails", "No images available, showing placeholder");
                productImageView.setImageResource(R.drawable.photos_holder);
            }
        }
    }

}
