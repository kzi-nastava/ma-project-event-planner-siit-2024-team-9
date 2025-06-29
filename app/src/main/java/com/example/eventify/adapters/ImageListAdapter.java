package com.example.eventify.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventify.R;
import com.example.eventify.models.others.ImageItem;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {
    private final ArrayList<String> images;
    private final Context context;
    private final FragmentManager fragmentManager;

    public ImageListAdapter(Context context, ArrayList<String> images, FragmentManager fragmentManager) {
        this.context = context;
        this.images = images;
        this.fragmentManager = fragmentManager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.scrollable_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageItem = images.get(position);
        
        // Construct the full URL for the image
        // Spring Boot serves static resources directly from the root
        String baseUrl = RetrofitClient.BASE_URL.replace("api/", "");
        String imageUrl = baseUrl + "images/service/" + imageItem;
        
        Log.d("ImageListAdapter", "Loading image from: " + imageUrl);
        
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.add) // Show placeholder while loading
            .error(R.drawable.add) // Show error image if loading fails
            .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
