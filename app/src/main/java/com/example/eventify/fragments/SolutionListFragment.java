package com.example.eventify.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.adapters.SolutionListAdapter;
import com.example.eventify.models.filters.SolutionFilterOptions;
import com.example.eventify.models.filters.SolutionFilterStatistics;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.SolutionQueryBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SolutionListFragment extends Fragment
        implements SolutionFilterDialogFragment.OnFiltersApplied {

    private RecyclerView solutionRecyclerView;
    private SolutionListAdapter solutionListAdapter;
    private ProgressBar solutionLoadingIndicator;

    private View topActionsContainer;
    private TextInputEditText searchInput;
    private ImageButton btnFilter, btnSort;

    private SolutionService solutionService;
    private final List<Solution> solutions = new ArrayList<>();
    private int totalSolutions = 0;
    private int pageSize = 5;
    private int currentPage = 0;
    private boolean isLoading = false;

    private boolean showTop;

    // sorting
    private boolean sortAscending = true;
    private String currentSortField = "name";
    private final String[] SORT_LABELS = {"Name", "Price", "Discount"};
    private final String[] SORT_KEYS   = {"name", "price", "discount"};

    // filters
    private SolutionFilterOptions currentFilters = new SolutionFilterOptions();
    private SolutionFilterStatistics cachedStats = null;

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

        if (getArguments() != null) {
            showTop = getArguments().getBoolean("showTop");
        }

        solutionRecyclerView = view.findViewById(R.id.solution_recycler_view);
        solutionLoadingIndicator = view.findViewById(R.id.solution_loading_indicator);
        topActionsContainer = view.findViewById(R.id.top_actions_container);
        searchInput = view.findViewById(R.id.search_input);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnSort = view.findViewById(R.id.btn_sort);

        if (!showTop) {
            topActionsContainer.setVisibility(View.VISIBLE);

            searchInput.setOnEditorActionListener((v1, actionId, event) -> {
                currentFilters.search = String.valueOf(v1.getText());
                restartAndFetch();
                return true;
            });

            btnFilter.setOnClickListener(v12 -> openFilterDialog());

            btnSort.setOnClickListener(v -> showSortFieldDialog());
            btnSort.setOnLongClickListener(v -> {
                sortAscending = !sortAscending;
                restartAndFetch();
                return true;
            });
        } else {
            topActionsContainer.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        solutionRecyclerView.setLayoutManager(layoutManager);
        solutionListAdapter = new SolutionListAdapter(requireContext(), solutions, requireActivity().getSupportFragmentManager());
        solutionRecyclerView.setAdapter(solutionListAdapter);

        solutionService =RetrofitClient.getClient(requireContext()).create(SolutionService.class);

        fetchSolutions();
        setupScrollListener();

        return view;
    }

    private void fetchSolutions() {
        if (isLoading) return;
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
            // server-side FILTER + pagination
            java.util.Map<String, String> q = SolutionQueryBuilder.fromFilters(currentFilters);

            solutionService.filter(q, currentPage, pageSize, currentSortField, sortAscending)
                    .enqueue(new Callback<com.example.eventify.services.solutions.SolutionService.SolutionAllResponse>() {
                        @Override
                        public void onResponse(Call<com.example.eventify.services.solutions.SolutionService.SolutionAllResponse> call,
                                               Response<com.example.eventify.services.solutions.SolutionService.SolutionAllResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                solutions.addAll(response.body().content);
                                totalSolutions = response.body().totalElements;
                                solutionListAdapter.notifyDataSetChanged();
                                if (cachedStats == null) cachedStats = buildStatsFromClient(solutions);
                            } else {
                                Log.e("SolutionListFragment", "Failed to load filtered solutions");
                            }
                            solutionLoadingIndicator.setVisibility(View.GONE);
                            solutionRecyclerView.setVisibility(View.VISIBLE);
                            isLoading = false;
                        }

                        @Override
                        public void onFailure(Call<com.example.eventify.services.solutions.SolutionService.SolutionAllResponse> call, Throwable t) {
                            Log.e("SolutionListFragment", "Error loading filtered solutions", t);
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

    private void openFilterDialog() {
        if (cachedStats == null) {
            cachedStats = buildStatsFromClient(solutions);
        }
        SolutionFilterDialogFragment dialog = SolutionFilterDialogFragment.newInstance(cachedStats, currentFilters);
        dialog.show(getChildFragmentManager(), "SolutionFilterDialog");
    }

    private SolutionFilterStatistics buildStatsFromClient(List<Solution> source) {
        SolutionFilterStatistics s = new SolutionFilterStatistics();
        int minPrice = Integer.MAX_VALUE, maxPrice = Integer.MIN_VALUE;
        int minDiscount = Integer.MAX_VALUE, maxDiscount = Integer.MIN_VALUE;
        int minDuration = Integer.MAX_VALUE, maxDuration = Integer.MIN_VALUE;
        int minEng = Integer.MAX_VALUE, maxEng = Integer.MIN_VALUE;

        for (Solution sol : source) {
            // categories
            if (sol.getCategory() != null && sol.getCategory().getName() != null) {
                String name = sol.getCategory().getName();
                if (!s.solutionCategories.contains(name)) s.solutionCategories.add(name);
            }
            // event types
            if (sol.getEventTypes() != null) {
                sol.getEventTypes().forEach(et -> {
                    if (et != null && et.getName() != null && !s.eventTypes.contains(et.getName())) {
                        s.eventTypes.add(et.getName());
                    }
                });
            }
            // price / discount
            minPrice   = Math.min(minPrice,   (int) Math.floor(sol.getPrice()));
            maxPrice   = Math.max(maxPrice,   (int) Math.ceil(sol.getPrice()));
            minDiscount= Math.min(minDiscount,(int) Math.floor(sol.getDiscount()));
            maxDiscount= Math.max(maxDiscount,(int) Math.ceil(sol.getDiscount()));

            if (sol.isService() && sol instanceof com.example.eventify.models.solutions.Service) {
                com.example.eventify.models.solutions.Service svc =
                        (com.example.eventify.models.solutions.Service) sol;

                Integer dur = svc.getDuration();
                Integer minE = svc.getMinEngagement();
                Integer maxE = svc.getMaxEngagement();

                if (dur != null) {
                    minDuration = Math.min(minDuration, dur);
                    maxDuration = Math.max(maxDuration, dur);
                }
                if (minE != null) minEng = Math.min(minEng, minE);
                if (maxE != null) maxEng = Math.max(maxEng, maxE);
            }
        }

        s.minPrice = (minPrice == Integer.MAX_VALUE) ? 0 : minPrice;
        s.maxPrice = (maxPrice == Integer.MIN_VALUE) ? 2000 : maxPrice;

        s.minDiscount = (minDiscount == Integer.MAX_VALUE) ? 0 : minDiscount;
        s.maxDiscount = (maxDiscount == Integer.MIN_VALUE) ? 20 : maxDiscount;

        s.minDuration = (minDuration == Integer.MAX_VALUE) ? 0 : minDuration;
        s.maxDuration = (maxDuration == Integer.MIN_VALUE) ? 12 : maxDuration;

        s.minEngagement = (minEng == Integer.MAX_VALUE) ? 0 : minEng;
        s.maxEngagement = (maxEng == Integer.MIN_VALUE) ? 500 : maxEng;

        return s;
    }


    private void restartAndFetch() {
        currentPage = 0;
        solutions.clear();
        solutionListAdapter.notifyDataSetChanged();
        fetchSolutions();
    }

    private void showSortFieldDialog() {
        int preselected = 0;
        for (int i = 0; i < SORT_KEYS.length; i++) {
            if (SORT_KEYS[i].equalsIgnoreCase(currentSortField)) {
                preselected = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sort_by)
                .setSingleChoiceItems(SORT_LABELS, preselected, (dialog, which) -> {
                    currentSortField = SORT_KEYS[which];
                })
                .setPositiveButton(R.string.apply, (d, w) -> restartAndFetch())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onApplied(SolutionFilterOptions filters) {
        this.currentFilters = filters;
        restartAndFetch();
    }
}
