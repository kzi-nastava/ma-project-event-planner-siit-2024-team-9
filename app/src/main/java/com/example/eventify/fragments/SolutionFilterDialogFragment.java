package com.example.eventify.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventify.R;
import com.example.eventify.models.filters.SolutionFilterOptions;
import com.example.eventify.models.filters.SolutionFilterStatistics;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SolutionFilterDialogFragment extends DialogFragment {

    public interface OnFiltersApplied {
        void onApplied(SolutionFilterOptions filters);
    }

    private static final String ARG_STATS = "stats";
    private static final String ARG_FILTERS = "filters";

    public static SolutionFilterDialogFragment newInstance(SolutionFilterStatistics stats, SolutionFilterOptions filters) {
        SolutionFilterDialogFragment f = new SolutionFilterDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_STATS, stats);
        b.putSerializable(ARG_FILTERS, filters);
        f.setArguments(b);
        return f;
    }

    private SolutionFilterStatistics stats;
    private SolutionFilterOptions filters;

    private RadioGroup rgType;
    private TextInputEditText etCategories, etEventTypes, etReservationMethods;
    private RangeSlider rsPrice, rsDiscount, rsDuration, rsEngagement;
    private MaterialSwitch swVisibility, swAvailability;
    private LinearLayout serviceSection;
    private TextInputEditText etReservationDeadline, etCancellationDeadline;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_solutions, null, false);

        stats = (SolutionFilterStatistics) getArguments().getSerializable(ARG_STATS);
        filters = (SolutionFilterOptions) getArguments().getSerializable(ARG_FILTERS);
        if (filters == null) filters = new SolutionFilterOptions();

        rgType = v.findViewById(R.id.rg_type);
        etCategories = v.findViewById(R.id.et_solution_categories);
        etEventTypes = v.findViewById(R.id.et_solution_event_types);
        etReservationMethods = v.findViewById(R.id.et_reservation_methods);

        rsPrice = v.findViewById(R.id.rs_price);
        rsDiscount = v.findViewById(R.id.rs_discount);
        rsDuration = v.findViewById(R.id.rs_duration);
        rsEngagement = v.findViewById(R.id.rs_engagement);

        swVisibility = v.findViewById(R.id.sw_visibility);
        swAvailability = v.findViewById(R.id.sw_availability);

        serviceSection = v.findViewById(R.id.service_section);
        etReservationDeadline = v.findViewById(R.id.et_reservation_deadline);
        etCancellationDeadline = v.findViewById(R.id.et_cancellation_deadline);

        // TYPE
        if (filters.isServiceSelected != null) {
            rgType.check(filters.isServiceSelected ? R.id.rb_service : R.id.rb_product);
        }
        serviceSection.setVisibility(
                (rgType.getCheckedRadioButtonId() == R.id.rb_service) ? View.VISIBLE : View.GONE
        );
        rgType.setOnCheckedChangeListener((g, checkedId) -> {
            serviceSection.setVisibility(checkedId == R.id.rb_service ? View.VISIBLE : View.GONE);
        });

        etCategories.setOnClickListener(v1 -> {
            showMultiChoice(etCategories, stats.solutionCategories, filters.solutionCategories);
        });

        etEventTypes.setOnClickListener(v12 -> {
            showMultiChoice(etEventTypes, stats.eventTypes, filters.eventTypes);
        });

        List<String> reservationOptions = new ArrayList<>();
        reservationOptions.add("AUTOMATIC");
        reservationOptions.add("MANUAL");
        etReservationMethods.setOnClickListener(v13 -> {
            showMultiChoice(etReservationMethods, reservationOptions, filters.reservationMethod);
        });

        setupRange(rsPrice, stats.minPrice, stats.maxPrice,
                filters.minPrice, filters.maxPrice);
        setupRange(rsDiscount, stats.minDiscount, stats.maxDiscount,
                filters.minDiscount, filters.maxDiscount);
        setupRange(rsDuration, stats.minDuration, stats.maxDuration,
                filters.minDuration, filters.maxDuration);
        setupRange(rsEngagement, stats.minEngagement, stats.maxEngagement,
                filters.minEngagement, filters.maxEngagement);


        if (filters.visibility != null) swVisibility.setChecked(filters.visibility);
        if (filters.availability != null) swAvailability.setChecked(filters.availability);

        if (filters.reservationDeadline != null)
            etReservationDeadline.setText(String.valueOf(filters.reservationDeadline));
        if (filters.cancellationDeadline != null)
            etCancellationDeadline.setText(String.valueOf(filters.cancellationDeadline));

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .setNegativeButton(R.string.cancel, (d, w) -> dismiss())
                .setNeutralButton(R.string.reset, (d, w) -> {
                    SolutionFilterOptions reset = new SolutionFilterOptions();
                    ((OnFiltersApplied) getParentFragment()).onApplied(reset);
                })
                .setPositiveButton(R.string.apply, (d, w) -> {
                    SolutionFilterOptions out = collectFilters();
                    ((OnFiltersApplied) getParentFragment()).onApplied(out);
                })
                .create();
    }

    private void setupRange(RangeSlider slider, int min, int max, Integer selMin, Integer selMax) {
        slider.setValueFrom(min);
        slider.setValueTo(max);
        slider.setStepSize(1f);
        float v1 = (selMin != null) ? selMin : min;
        float v2 = (selMax != null) ? selMax : max;
        slider.setValues(v1, v2);
    }

    private void showMultiChoice(TextInputEditText target, List<String> all, List<String> selected) {
        boolean[] checked = new boolean[all.size()];
        for (int i = 0; i < all.size(); i++) {
            checked[i] = selected.contains(all.get(i));
        }
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(target.getHint())
                .setMultiChoiceItems(all.toArray(new CharSequence[0]), checked, (dialog, which, isChecked) -> {
                    String item = all.get(which);
                    if (isChecked) {
                        if (!selected.contains(item)) selected.add(item);
                    } else {
                        selected.remove(item);
                    }
                })
                .setPositiveButton(R.string.apply, (d, w) -> {
                    target.setText(String.join(", ", selected));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private SolutionFilterOptions collectFilters() {
        SolutionFilterOptions out = new SolutionFilterOptions();
        out.isServiceSelected = (rgType.getCheckedRadioButtonId() == R.id.rb_service);

        out.solutionCategories = new ArrayList<>(filters.solutionCategories);
        out.eventTypes = new ArrayList<>(filters.eventTypes);
        out.reservationMethod = new ArrayList<>(filters.reservationMethod);

        out.minPrice = Math.round(rsPrice.getValues().get(0));
        out.maxPrice = Math.round(rsPrice.getValues().get(1));

        out.minDiscount = Math.round(rsDiscount.getValues().get(0));
        out.maxDiscount = Math.round(rsDiscount.getValues().get(1));

        out.visibility = swVisibility.isChecked();
        out.availability = swAvailability.isChecked();

        out.minDuration = Math.round(rsDuration.getValues().get(0));
        out.maxDuration = Math.round(rsDuration.getValues().get(1));

        out.minEngagement = Math.round(rsEngagement.getValues().get(0));
        out.maxEngagement = Math.round(rsEngagement.getValues().get(1));

        String resD = etReservationDeadline.getText() != null ? etReservationDeadline.getText().toString().trim() : "";
        String canD = etCancellationDeadline.getText() != null ? etCancellationDeadline.getText().toString().trim() : "";
        out.reservationDeadline = resD.isEmpty() ? null : Integer.valueOf(resD);
        out.cancellationDeadline = canD.isEmpty() ? null : Integer.valueOf(canD);

        out.search = (filters.search == null) ? "" : filters.search;

        return out;
    }
}
