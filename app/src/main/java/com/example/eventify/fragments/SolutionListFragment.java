package com.example.eventify.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.adapters.SolutionListAdapter;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionListFragment extends Fragment {

    private RecyclerView solutionRecyclerView;
    private SolutionListAdapter solutionListAdapter;
    private ProgressBar solutionLoadingIndicator;

    private SolutionService solutionService;
    private List<Solution> solutions = new ArrayList<>();
    private int totalSolutions = 0;
    private int pageSize = 5;
    private int currentPage = 0;
    private boolean isLoading = false;

    private boolean showTop;

    public static SolutionListFragment newInstance(boolean showTop) {
        SolutionListFragment fragment = new SolutionListFragment();
        Bundle args = new Bundle();
        args.putBoolean("showTop", showTop);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solution_list, container, false);

        solutionRecyclerView = view.findViewById(R.id.solution_recycler_view);
        solutionLoadingIndicator = view.findViewById(R.id.solution_loading_indicator);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        solutionRecyclerView.setLayoutManager(layoutManager);
        solutionListAdapter = new SolutionListAdapter(getContext(), solutions);
        solutionRecyclerView.setAdapter(solutionListAdapter);

        solutionService = RetrofitClient.getClient(SolutionService.BASE_URL).create(SolutionService.class);

        if (getArguments() != null) {
            showTop = getArguments().getBoolean("showTop");
        }

        fetchSolutions();
        setupScrollListener();

        return view;
    }

    private void fetchSolutions() {
        if (isLoading) return; // Prevent multiple calls during loading
        isLoading = true;

        solutionLoadingIndicator.setVisibility(currentPage == 0 ? View.VISIBLE : View.GONE);
        solutionRecyclerView.setVisibility(currentPage == 0 ? View.GONE : View.VISIBLE);

        if (showTop) {
            solutionService.getTop().enqueue(new Callback<List<Solution>>() {
                @Override
                public void onResponse(Call<List<Solution>> call, Response<List<Solution>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        solutions.clear();
                        solutions.addAll(response.body());
                        solutionListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("SolutionListFragment", "Failed to load top solutions");
                    }
                    solutionLoadingIndicator.setVisibility(View.GONE);
                    solutionRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<List<Solution>> call, Throwable t) {
                    Log.e("SolutionListFragment", "Error loading top solutions", t);
                    solutionLoadingIndicator.setVisibility(View.GONE);
                    solutionRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }
            });
        } else {
            solutionService.getAllPaginated(currentPage, pageSize, "name", true).enqueue(new Callback<SolutionService.SolutionAllResponse>() {
                @Override
                public void onResponse(Call<SolutionService.SolutionAllResponse> call, Response<SolutionService.SolutionAllResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        solutions.addAll(response.body().content);
                        totalSolutions = response.body().totalElements;
                        solutionListAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("SolutionListFragment", "Failed to load all solutions");
                    }
                    solutionLoadingIndicator.setVisibility(View.GONE);
                    solutionRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<SolutionService.SolutionAllResponse> call, Throwable t) {
                    Log.e("SolutionListFragment", "Error loading all solutions", t);
                    solutionLoadingIndicator.setVisibility(View.GONE);
                    solutionRecyclerView.setVisibility(View.VISIBLE);
                    isLoading = false;
                }
            });
        }
    }

    private void setupScrollListener() {
        solutionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !showTop &&
                        layoutManager.findLastCompletelyVisibleItemPosition() == solutions.size() - 1 &&
                        solutions.size() < totalSolutions) {
                    currentPage++;
                    fetchSolutions();
                }
            }
        });
    }
}
