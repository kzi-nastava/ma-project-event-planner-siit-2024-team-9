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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.eventify.R;
import com.example.eventify.adapters.GroupedReportsAdapter;
import com.example.eventify.models.others.Report;
import com.example.eventify.services.others.ReportService;
import com.example.eventify.utils.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsFragment extends Fragment {

    private SwipeRefreshLayout swipe;
    private RecyclerView rv;
    private View empty;
    private ProgressBar progress;

    private GroupedReportsAdapter adapter;
    private ReportService reportService;

    public static ReportsFragment newInstance() { return new ReportsFragment(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reports, container, false);

        swipe = v.findViewById(R.id.swipe);
        rv = v.findViewById(R.id.rv_groups);
        empty = v.findViewById(R.id.empty_state);
        progress = v.findViewById(R.id.progress);

        adapter = new GroupedReportsAdapter(new GroupedReportsAdapter.Listener() {
            @Override public void onApproveAll(String userId) { approveAll(userId); }
            @Override public void onDeclineAll(String userId) { declineAll(userId); }
        });
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        reportService =RetrofitClient.getClient(requireContext()).create(ReportService.class);
        swipe.setOnRefreshListener(this::loadPending);

        loadPending();
        return v;
    }

    private void loadPending() {
        if (!swipe.isRefreshing()) progress.setVisibility(View.VISIBLE);
        empty.setVisibility(View.GONE);

        reportService.getPending().enqueue(new Callback<Collection<Report>>() {
            @Override public void onResponse(@NonNull Call<Collection<Report>> call,
                                             @NonNull Response<Collection<Report>> resp) {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);

                if (resp.isSuccessful() && resp.body() != null) {
                    List<GroupedReportsAdapter.Group> groups = groupByUser(new ArrayList<>(resp.body()));
                    adapter.setData(groups);
                    toggleEmpty(groups.isEmpty());
                } else {
//                    showMsg("Failed to load reports");
//                    toggleEmpty(true);
                    String err = "";
                    try { err = resp.errorBody() != null ? resp.errorBody().string() : ""; } catch (Exception ignored) {}
                    android.util.Log.e("Reports", "getPending failed: code=" + resp.code() + ", msg=" + resp.message() + ", err=" + err);
                    showMsg("Failed to load reports (" + resp.code() + ")");
                    toggleEmpty(true);
                }
            }

            @Override public void onFailure(@NonNull Call<Collection<Report>> call, @NonNull Throwable t) {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);
                showMsg("Network error");
                toggleEmpty(true);
            }
        });
    }

    private List<GroupedReportsAdapter.Group> groupByUser(List<Report> reports) {
        Map<String, GroupedHolder> map = new LinkedHashMap<>();

        for (Report r : reports) {
            if (r.getReportedUser() == null || r.getReportedUser().getId() == null) continue;
            String uid = r.getReportedUser().getId();
            String email = r.getReportedUser().getEmail();

            GroupedHolder holder = map.get(uid);
            if (holder == null) {
                holder = new GroupedHolder(uid, email);
                map.put(uid, holder);
            }
            holder.reports.add(r);
        }

        List<GroupedReportsAdapter.Group> out = new ArrayList<>();
        for (GroupedHolder h : map.values()) {
            out.add(new GroupedReportsAdapter.Group(h.userId, h.email, h.reports));
        }
        return out;
    }

    private static class GroupedHolder {
        String userId;
        String email;
        List<Report> reports = new ArrayList<>();
        GroupedHolder(String userId, String email) { this.userId = userId; this.email = email; }
    }

    private void approveAll(String userId) {
        progress.setVisibility(View.VISIBLE);
        reportService.approve(safeUuid(userId)).enqueue(new Callback<Boolean>() {
            @Override public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    showMsg("Reports approved successfully.");
                    loadPending();
                } else showMsg("Failed to approve.");
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                progress.setVisibility(View.GONE);
                showMsg("Network error.");
            }
        });
    }

    private void declineAll(String userId) {
        progress.setVisibility(View.VISIBLE);
        reportService.decline(safeUuid(userId)).enqueue(new Callback<Boolean>() {
            @Override public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                progress.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    showMsg("Reports declined successfully.");
                    loadPending();
                } else showMsg("Failed to decline.");
            }
            @Override public void onFailure(Call<Boolean> call, Throwable t) {
                progress.setVisibility(View.GONE);
                showMsg("Network error.");
            }
        });
    }

    @Nullable
    private UUID safeUuid(@Nullable String id) {
        try { return id != null ? UUID.fromString(id) : null; }
        catch (IllegalArgumentException e) { return null; }
    }

    private void toggleEmpty(boolean isEmpty) {
        empty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showMsg(String m) {
        View root = getView();
        if (root != null) Snackbar.make(root, m, Snackbar.LENGTH_SHORT).show();
    }
}
