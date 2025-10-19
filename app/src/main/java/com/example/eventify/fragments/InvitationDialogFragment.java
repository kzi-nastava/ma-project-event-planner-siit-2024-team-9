package com.example.eventify.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventify.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class InvitationDialogFragment extends DialogFragment {

    public static final String TAG = "InvitationDialogFragment";
    private static final String ARG_MAX = "arg_max";

    private int maxInvitations = 0;

    private ChipGroup chipGroup;
    private TextInputEditText etEmail;

    public static InvitationDialogFragment newInstance(int maxInvitations) {
        InvitationDialogFragment f = new InvitationDialogFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_MAX, maxInvitations);
        f.setArguments(b);
        return f;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invitation_dialog, container, false);

        maxInvitations = getArguments() != null ? getArguments().getInt(ARG_MAX, 0) : 0;

        chipGroup = v.findViewById(R.id.chips_emails);
        etEmail   = v.findViewById(R.id.et_email);

        v.findViewById(R.id.btn_add).setOnClickListener(view -> addEmail());
        v.findViewById(R.id.btn_send).setOnClickListener(view -> send());
        v.findViewById(R.id.btn_cancel).setOnClickListener(view -> dismiss());

        updateHint();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) return;

        getDialog().getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        );

        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        int marginPx = (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 24, dm);
        int maxPx = (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 520, dm);
        int targetWidth = Math.min(dm.widthPixels - 2 * marginPx, maxPx);

        getDialog().getWindow().setLayout(targetWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    private void addEmail() {
        if (chipGroup.getChildCount() >= maxInvitations && maxInvitations > 0) {
            etEmail.setError("Reached limit (" + maxInvitations + ")");
            return;
        }
        String raw = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            etEmail.setError("Enter email");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(raw).matches()) {
            etEmail.setError("Invalid email");
            return;
        }
        if (alreadyAdded(raw)) {
            etEmail.setError("Email already added");
            return;
        }

        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_email_chip, chipGroup, false);
        chip.setText(raw);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
        chipGroup.addView(chip);

        etEmail.setText("");
        etEmail.setError(null);
        updateHint();
    }

    private boolean alreadyAdded(String email) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip c = (Chip) chipGroup.getChildAt(i);
            if (TextUtils.equals(c.getText(), email)) return true;
        }
        return false;
    }

    private void send() {
        ArrayList<String> emails = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip c = (Chip) chipGroup.getChildAt(i);
            emails.add(c.getText().toString());
        }
        if (emails.isEmpty()) {
            etEmail.setError("Add at least one email");
            return;
        }

        Bundle result = new Bundle();
        result.putStringArrayList("emails", emails);
        getParentFragmentManager().setFragmentResult(TAG, result);
        dismiss();
    }

    private void updateHint() {
        int count = chipGroup.getChildCount();
        String base = (count == 0) ? "Enter email" :
                (count == 1) ? ((Chip) chipGroup.getChildAt(0)).getText().toString() :
                        ((Chip) chipGroup.getChildAt(0)).getText().toString() + " +" + (count - 1) + " others";
        etEmail.setHint(base);
    }
}
