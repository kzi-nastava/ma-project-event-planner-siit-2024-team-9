package com.example.eventify.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.eventify.R;
import com.example.eventify.models.filters.EventFilterStatistics;
import com.example.eventify.models.filters.EventFilterOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventFilterDialogFragment extends DialogFragment {

    public interface OnFiltersApplied {
        void onApplied(EventFilterOptions filters);
    }

    private static final String ARG_STATS = "stats";
    private static final String ARG_FILTERS = "filters";

    private EventFilterStatistics stats;
    private EventFilterOptions filters;

    private TextInputEditText etStartDate, etEndDate, etEventTypes, etLocations, etMaxPrice;
    private Slider sliderMaxAttendees, sliderAttendance;

    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    public static EventFilterDialogFragment newInstance(EventFilterStatistics stats, EventFilterOptions filters) {
        EventFilterDialogFragment f = new EventFilterDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_STATS, stats);
        b.putSerializable(ARG_FILTERS, filters);
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter_events, null, false);

        stats   = (EventFilterStatistics) getArguments().getSerializable(ARG_STATS);
        filters = (EventFilterOptions) getArguments().getSerializable(ARG_FILTERS);

        etStartDate    = v.findViewById(R.id.et_start_date);
        etEndDate      = v.findViewById(R.id.et_end_date);
        etEventTypes   = v.findViewById(R.id.et_event_types);
        etLocations    = v.findViewById(R.id.et_locations);
        etMaxPrice     = v.findViewById(R.id.et_max_price);
        sliderMaxAttendees = v.findViewById(R.id.slider_max_attendees);
        sliderAttendance   = v.findViewById(R.id.slider_attendance);

        // Init slider bounds from stats
        sliderMaxAttendees.setValueTo(stats.maxAttendees);
        sliderAttendance.setValueTo(stats.maxAttendance);
        sliderMaxAttendees.setValue(filters.maxAttendees != null ? filters.maxAttendees : 0);
        sliderAttendance.setValue(filters.attendance != null ? filters.attendance : 0);

        if (filters.startDate != null) etStartDate.setText(df.format(filters.startDate));
        if (filters.endDate != null)   etEndDate.setText(df.format(filters.endDate));
        if (filters.maxPrice != null)  etMaxPrice.setText(String.valueOf(filters.maxPrice));

        etEventTypes.setText(String.join(", ", filters.eventTypes));
        etLocations.setText(String.join(", ", filters.locations));

        etStartDate.setOnClickListener(v1 -> pickDate(true));
        etEndDate.setOnClickListener(v12 -> pickDate(false));

        etEventTypes.setOnClickListener(v13 ->
                openMultiSelect(etEventTypes, filters.eventTypes, stats.eventTypes.toArray(new String[0])));
        etLocations.setOnClickListener(v14 ->
                openMultiSelect(etLocations, filters.locations, stats.locations.toArray(new String[0])));

        v.findViewById(R.id.btn_cancel).setOnClickListener(v15 -> dismiss());

        v.findViewById(R.id.btn_reset).setOnClickListener(v16 -> {
            filters.search = "";
            filters.startDate = null;
            filters.endDate = null;
            filters.maxAttendees = 0;
            filters.attendance = 0;
            filters.eventTypes = new ArrayList<>();
            filters.locations = new ArrayList<>();
            filters.maxPrice = 0.0;

            sliderMaxAttendees.setValue(0);
            sliderAttendance.setValue(0);
            etStartDate.setText(null);
            etEndDate.setText(null);
            etEventTypes.setText(null);
            etLocations.setText(null);
            etMaxPrice.setText(null);
        });

        v.findViewById(R.id.btn_apply).setOnClickListener(v17 -> {
            filters.maxAttendees = (int) sliderMaxAttendees.getValue();
            filters.attendance   = (int) sliderAttendance.getValue();

            String mp = String.valueOf(etMaxPrice.getText()).trim();
            if (!mp.isEmpty()) {
                try { filters.maxPrice = Double.parseDouble(mp); }
                catch (Exception e) { Toast.makeText(requireContext(), R.string.invalid_price, Toast.LENGTH_SHORT).show(); return; }
            } else {
                filters.maxPrice = 0.0;
            }

            OnFiltersApplied cb = (OnFiltersApplied) getParentFragment();
            if (cb != null) cb.onApplied(filters);
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(v)
                .create();
    }

    private void pickDate(boolean isStart) {
        final Calendar cal = Calendar.getInstance();
        android.app.DatePickerDialog dp = new android.app.DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth, 0, 0, 0);
                    Date d = c.getTime();
                    if (isStart) {
                        filters.startDate = d;
                        etStartDate.setText(df.format(d));
                    } else {
                        filters.endDate = d;
                        etEndDate.setText(df.format(d));
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis()); // [min]=today
        dp.show();
    }

    private void openMultiSelect(TextInputEditText target, java.util.List<String> selected, String[] allItems) {
        boolean[] checked = new boolean[allItems.length];
        for (int i = 0; i < allItems.length; i++) checked[i] = selected.contains(allItems[i]);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(target.getHint())
                .setMultiChoiceItems(allItems, checked, (dialog, which, isChecked) -> {
                    String item = allItems[which];
                    if (isChecked && !selected.contains(item)) selected.add(item);
                    if (!isChecked) selected.remove(item);
                })
                .setPositiveButton(R.string.ok, (d, w) -> target.setText(String.join(", ", selected)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
