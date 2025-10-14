package com.example.eventify.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventify.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SuspendDialogFragment extends DialogFragment {

    public static final String TAG = "SuspendDialogFragment";
    private static final String ARG_REMAINING = "arg_remaining";
    private CountDownTimer timer;

    public static SuspendDialogFragment newInstance(long remainingMs) {
        SuspendDialogFragment f = new SuspendDialogFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_REMAINING, remainingMs);
        f.setArguments(b);
        return f;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        long remaining = getArguments() != null ? getArguments().getLong(ARG_REMAINING, 0L) : 0L;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        android.view.View v = inflater.inflate(R.layout.fragment_suspend_dialog, null, false);
        TextView tvTimer = v.findViewById(R.id.tv_timer);

        startTimer(tvTimer, remaining);

        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setCancelable(true);

        return b.create();
    }

    private void startTimer(TextView tv, long remainingMs) {
        updateDisplay(tv, remainingMs);
        timer = new CountDownTimer(Math.max(remainingMs, 0L), 1000) {
            @Override public void onTick(long millisUntilFinished) {
                updateDisplay(tv, millisUntilFinished);
            }
            @Override public void onFinish() {
                if (getDialog() != null) dismiss();
            }
        }.start();
    }

    private void updateDisplay(TextView tv, long ms) {
        long totalSec = Math.max(ms, 0L) / 1000;
        long days = totalSec / 86400;
        long hours = (totalSec % 86400) / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        tv.setText(String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds));
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (timer != null) timer.cancel();
    }
}
