package com.example.eventify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ServiceListAdapter extends RecyclerView.Adapter<ServiceListAdapter.ViewHolder> {
    private final ArrayList<Service> aProducts;
    private final Context context;

    public ServiceListAdapter(Context context, ArrayList<Service> products) {
        this.context = context;
        this.aProducts = products;
    }

    /*
     * ViewHolder class to hold references to each item view for efficient recycling.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout productCard;
        public ImageView imageView;
        public TextView productTitle;
        public TextView productDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            productCard = itemView.findViewById(R.id.product_card_item);
            imageView = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            productDescription = itemView.findViewById(R.id.product_description);
        }
    }

    /*
     * Inflate the item layout and create the ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_card, parent, false);
        return new ViewHolder(view);
    }

    /*
     * Bind data to the view components for each item in the RecyclerView
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Service product = aProducts.get(position);

        if (product != null) {
            holder.imageView.setImageResource(product.getImage());
            holder.productTitle.setText(product.getTitle());
            holder.productDescription.setText(product.getDescription());
        }
    }

    /*
     * Return the total count of items
     */
    @Override
    public int getItemCount() {
        return aProducts.size();
    }
}

