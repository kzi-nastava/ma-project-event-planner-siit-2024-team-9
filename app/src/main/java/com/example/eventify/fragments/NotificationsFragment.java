package com.example.eventify.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.adapters.NotificationListAdapter;
import com.example.eventify.models.others.Notification;
import com.example.eventify.services.auth.LoginService;
import com.example.eventify.services.others.NotificationService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationRecyclerView;
    private NotificationListAdapter notificationListAdapter;
    private ProgressBar loadingIndicator;
    private View emptyState;

    private NotificationService notificationService;
    private final List<Notification> notifications = new ArrayList<>();

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_list, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        notificationRecyclerView = v.findViewById(R.id.notification_recycler_view);
        loadingIndicator = v.findViewById(R.id.notification_loading_indicator);
        emptyState = v.findViewById(R.id.empty_state);


        MaterialSwitch muteSwitch = v.findViewById(R.id.switch_mute_notifications);
        LoginService loginService = new LoginService(requireContext());
        muteSwitch.setChecked(loginService.isNotificationsMuted());

        muteSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loginService.setNotificationsMuted(isChecked);
            Toast.makeText(requireContext(),
                    isChecked ? "Notifications muted" : "Notifications unmuted",
                    Toast.LENGTH_SHORT).show();
        });


        notificationRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        notificationListAdapter = new NotificationListAdapter(notifications, new NotificationListAdapter.Listener() {
            @Override public void onItemClick(Notification n, int position) {
                if (!n.isRead()) {
                    n.setRead(true);
                    notificationListAdapter.notifyItemChanged(position);
                    notificationService.markAsRead(n.getId(), n).enqueue(new Callback<Notification>() {
                        @Override public void onResponse(Call<Notification> call, Response<Notification> response) { }
                        @Override public void onFailure(Call<Notification> call, Throwable t) { }
                    });
                }
            }

            @Override public void onDeleteClick(Notification n, int position) {
                notificationService.delete(n.getId()).enqueue(new Callback<Boolean>() {
                    @Override public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.isSuccessful()) {
                            notifications.remove(position);
                            notificationListAdapter.notifyItemRemoved(position);
                            toggleEmptyState();
                            Toast.makeText(requireContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<Boolean> call, Throwable t) {
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        notificationRecyclerView.setAdapter(notificationListAdapter);

        notificationService =RetrofitClient.getClient(requireContext()).create(NotificationService.class);

        fetchNotifications();
    }

    private void fetchNotifications() {
        showLoading(true);

        UserSession session = new UserSession(requireContext());
        String userId = session.isValidSession() ? session.getCurrentUserId().toString() : null;

        Call<List<Notification>> call = (userId != null)
                ? notificationService.getUserNotifications(userId)
                : notificationService.getAll();

        call.enqueue(new Callback<List<Notification>>() {
            @Override public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    notifications.clear();
                    notifications.addAll(response.body());
                    Collections.sort(notifications, new Comparator<Notification>() {
                        @Override public int compare(Notification a, Notification b) {
                            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                            if (a.getCreatedAt() == null) return 1;
                            if (b.getCreatedAt() == null) return -1;
                            return b.getCreatedAt().compareTo(a.getCreatedAt());
                        }
                    });
                    notificationListAdapter.notifyDataSetChanged();
                    toggleEmptyState();
                } else {
                    Toast.makeText(requireContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onFailure(Call<List<Notification>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        notificationRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) emptyState.setVisibility(View.GONE);
    }

    private void toggleEmptyState() {
        boolean empty = notifications.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        notificationRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
