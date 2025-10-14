package com.example.eventify.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.eventify.R;
import com.example.eventify.adapters.ReviewListAdapter;
import com.example.eventify.models.solutions.Review;
import com.example.eventify.services.solutions.ReviewService;
import com.example.eventify.utils.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsFragment extends Fragment {

    private RecyclerView rv;
    private View emptyState;
    private ProgressBar progress;
    private SwipeRefreshLayout swipe;

    private ReviewService reviewService;
    private ReviewListAdapter adapter;
    private final List<Review> data = new ArrayList<>();

    public static ReviewsFragment newInstance() { return new ReviewsFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reviews, container, false);

        rv = v.findViewById(R.id.rv_reviews);
        emptyState = v.findViewById(R.id.empty_state);
        progress = v.findViewById(R.id.progress);
        swipe = v.findViewById(R.id.swipe_refresh);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReviewListAdapter(data, new ReviewListAdapter.Listener() {
            @Override public void onApprove(Review r, int position) { approve(r, position); }
            @Override public void onDelete(Review r, int position) { delete(r, position); }
            @Override public void onClick(Review r, int position) { /* optional details */ }
        });
        rv.setAdapter(adapter);

        reviewService = RetrofitClient.getClient().create(ReviewService.class);

        swipe.setOnRefreshListener(this::fetch);
        fetch();

        return v;
    }

    private void fetch() {
        if (!swipe.isRefreshing()) progress.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        reviewService.getPending().enqueue(new Callback<Collection<Review>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<Review>> call,
                                   @NonNull Response<Collection<Review>> resp) {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);

                if (resp.isSuccessful() && resp.body() != null) {
                    data.clear();
                    data.addAll(resp.body());
                    adapter.notifyDataSetChanged();
                    emptyState.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    showMsg(getString(R.string.failed_to_load));
                    if (data.isEmpty()) emptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<Review>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                showMsg(getString(R.string.network_error));
                if (data.isEmpty()) emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void approve(Review r, int position) {
        UUID reviewId = safeUuid(r.getId());
        if (reviewId == null) {
            showMsg(getString(R.string.action_failed));
            return;
        }

        progress.setVisibility(View.VISIBLE);
        reviewService.approve(reviewId).enqueue(new retrofit2.Callback<Boolean>() {
            @Override public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> resp) {
                progress.setVisibility(View.GONE);
                if (resp.isSuccessful() && Boolean.TRUE.equals(resp.body())) {
                    int pos = position >= 0 ? position : data.indexOf(r);
                    if (pos >= 0 && pos < data.size()) {
                        data.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    } else {
                        fetch();
                    }
                    showMsg(getString(R.string.review_approved));
                    toggleEmpty();
                } else {
                    showMsg(getString(R.string.action_failed));
                }
            }
            @Override public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                showMsg(getString(R.string.network_error));
            }
        });
    }


    private void delete(Review r, int position) {
        UUID reviewId = safeUuid(r.getId());
        if (reviewId == null) {
            showMsg(getString(R.string.action_failed));
            return;
        }

        progress.setVisibility(View.VISIBLE);
        reviewService.delete(reviewId).enqueue(new Callback<Boolean>() {
            @Override public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> resp) {
                progress.setVisibility(View.GONE);
                if (resp.isSuccessful() && Boolean.TRUE.equals(resp.body())) {
                    int pos = position >= 0 ? position : data.indexOf(r);
                    if (pos >= 0 && pos < data.size()) {
                        data.remove(pos);
                        adapter.notifyItemRemoved(pos);
                    } else {
                        fetch();
                    }
                    showMsg(getString(R.string.review_deleted));
                    toggleEmpty();
                } else {
                    showMsg(getString(R.string.action_failed));
                }
            }
            @Override public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                showMsg(getString(R.string.network_error));
            }
        });
    }

    private void toggleEmpty() {
        emptyState.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showMsg(String msg) {
        View root = getView();
        if (root != null) Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
    }

    private @Nullable UUID safeUuid(@Nullable String id) {
        try { return id != null ? UUID.fromString(id) : null; }
        catch (IllegalArgumentException e) { return null; }
    }
}
