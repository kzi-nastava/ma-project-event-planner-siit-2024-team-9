package com.example.eventify.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.others.Report;

import java.util.ArrayList;
import java.util.List;

public class GroupedReportsAdapter extends RecyclerView.Adapter<GroupedReportsAdapter.GroupVH> {

    public interface Listener {
        void onApproveAll(String userId);
        void onDeclineAll(String userId);
    }

    public static class Group {
        public final String userId;
        public final String userEmail;
        public final List<Report> reports = new ArrayList<>();
        public boolean expanded = false;

        public Group(String userId, String userEmail, List<Report> list) {
            this.userId = userId;
            this.userEmail = userEmail;
            if (list != null) this.reports.addAll(list);
        }
    }

    private final List<Group> data = new ArrayList<>();
    private final Listener listener;

    public GroupedReportsAdapter(Listener l) { this.listener = l; }

    public void setData(List<Group> groups) {
        data.clear();
        if (groups != null) data.addAll(groups);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_group, parent, false);
        return new GroupVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupVH h, int position) {
        Group g = data.get(position);

        h.tvEmail.setText(g.userEmail);
        h.tvCount.setText(g.reports.size() + " report(s)");
        h.body.setVisibility(g.expanded ? View.VISIBLE : View.GONE);
        h.chevron.setRotation(g.expanded ? 180f : 0f);

        // child list
        if (h.childAdapter == null) {
            h.childAdapter = new ReportRowsAdapter();
            h.rvChildren.setLayoutManager(new LinearLayoutManager(h.itemView.getContext()));
            h.rvChildren.setAdapter(h.childAdapter);
        }
        h.childAdapter.setItems(g.reports);

        h.header.setOnClickListener(v -> {
            g.expanded = !g.expanded;
            notifyItemChanged(position);
        });

        h.itemView.findViewById(R.id.btn_approve).setOnClickListener(v -> {
            if (listener != null) listener.onApproveAll(g.userId);
        });

        h.itemView.findViewById(R.id.btn_decline).setOnClickListener(v -> {
            if (listener != null) listener.onDeclineAll(g.userId);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class GroupVH extends RecyclerView.ViewHolder {
        LinearLayout header, body;
        TextView tvEmail, tvCount;
        ImageView chevron;
        RecyclerView rvChildren;
        ReportRowsAdapter childAdapter;

        GroupVH(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
            body = itemView.findViewById(R.id.body);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvCount = itemView.findViewById(R.id.tv_count);
            chevron = itemView.findViewById(R.id.iv_chevron);
            rvChildren = itemView.findViewById(R.id.rv_reports);
        }
    }

    // --- child adapter for single user's reports ---
    static class ReportRowsAdapter extends RecyclerView.Adapter<ReportRowsAdapter.RowVH> {
        private final List<Report> items = new ArrayList<>();

        void setItems(List<Report> list) {
            items.clear();
            if (list != null) items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull @Override
        public RowVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_row, parent, false);
            return new RowVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RowVH h, int position) {
            Report r = items.get(position);
            h.tvReason.setText("Reason: " + (r.getReason() == null ? "-" : r.getReason()));
            h.tvDesc.setText("Description: " + (r.getDescription() == null ? "-" : r.getDescription()));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class RowVH extends RecyclerView.ViewHolder {
            TextView tvReason, tvDesc;
            RowVH(@NonNull View itemView) {
                super(itemView);
                tvReason = itemView.findViewById(R.id.tv_reason);
                tvDesc = itemView.findViewById(R.id.tv_desc);
            }
        }
    }
}
