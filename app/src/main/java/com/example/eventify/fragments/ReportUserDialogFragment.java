package com.example.eventify.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventify.R;
import com.example.eventify.models.enums.Status;
import com.example.eventify.models.others.Report;
import com.example.eventify.models.users.User;
import com.example.eventify.services.others.ReportService;
import com.example.eventify.utils.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportUserDialogFragment extends DialogFragment {

    public static final String TAG = "ReportUserDialogFragment";
    private static final String ARG_REPORTED_BY = "arg_reported_by";
    private static final String ARG_REPORTED_USER = "arg_reported_user";

    public static ReportUserDialogFragment newInstance(User reportedBy, User reportedUser) {
        ReportUserDialogFragment f = new ReportUserDialogFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_REPORTED_BY, reportedBy);
        b.putParcelable(ARG_REPORTED_USER, reportedUser);
        f.setArguments(b);
        return f;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        android.view.View v = inflater.inflate(R.layout.fragment_report_user_dialog, null, false);

        RadioGroup rg = v.findViewById(R.id.rgReasons);
        EditText et = v.findViewById(R.id.etDescription);

        String[] labels = getResources().getStringArray(R.array.report_reasons_labels);
        String[] values = getResources().getStringArray(R.array.report_reasons_values);
        for (int i = 0; i < labels.length; i++) {
            RadioButton rb = new RadioButton(getContext());
            rb.setText(labels[i]);
            rb.setTag(values[i]);
            rg.addView(rb);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setView(v);

        Dialog dialog = builder.create();

        v.findViewById(R.id.btnCancel).setOnClickListener(view -> dismiss());

        v.findViewById(R.id.btnSubmit).setOnClickListener(view -> {
            int checkedId = rg.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(requireContext(), "Please select a reason.", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton sel = rg.findViewById(checkedId);
            String reasonValue = (String) sel.getTag();
            String description = et.getText().toString().trim();

            User reportedBy = getArguments() != null ? getArguments().getParcelable(ARG_REPORTED_BY) : null;
            User reportedUser = getArguments() != null ? getArguments().getParcelable(ARG_REPORTED_USER) : null;

            if (reportedBy == null || reportedUser == null) {
                Toast.makeText(requireContext(), "Missing users.", Toast.LENGTH_SHORT).show();
                return;
            }

            Report report = new Report(
                    null,
                    reasonValue,
                    description,
                    reportedBy,
                    reportedUser,
                    Status.PENDING
            );

            ReportService service =RetrofitClient.getClient(requireContext()).create(ReportService.class);
            service.add(report).enqueue(new Callback<Report>() {
                @Override public void onResponse(Call<Report> call, Response<Report> resp) {
                    if (resp.isSuccessful()) {
                        Toast.makeText(requireContext(), "Report submitted.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to submit report.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override public void onFailure(Call<Report> call, Throwable t) {
                    Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        return dialog;
    }
}
