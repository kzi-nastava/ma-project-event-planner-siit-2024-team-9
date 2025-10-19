package com.example.eventify.adapters;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventify.R;
import com.example.eventify.models.events.Activity;
import com.example.eventify.models.others.Location;

import java.util.Calendar;
import java.util.List;

public class ActivityCreationAdapter extends RecyclerView.Adapter<ActivityCreationAdapter.ActivityViewHolder> {
    private List<Activity> activities;
    private OnActivityClickListener listener;
    private Context context;

    public interface OnActivityClickListener {
        void onRemoveActivity(int position);
    }

    public ActivityCreationAdapter(List<Activity> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_creation, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity, position);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private EditText etActivityName;
        private EditText etActivityDescription;
        private EditText etActivityStartDate;
        private EditText etActivityStartTime;
        private EditText etActivityEndDate;
        private EditText etActivityEndTime;
        private EditText etActivityLocationName;
        private EditText etActivityLocationCity;
        private EditText etActivityLocationAddress;
        private EditText etActivityLocationCountry;
        private EditText etActivityLocationLongitude;
        private EditText etActivityLocationLatitude;
        private Button btnRemoveActivity;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            etActivityName = itemView.findViewById(R.id.et_activity_name);
            etActivityDescription = itemView.findViewById(R.id.et_activity_description);
            etActivityStartDate = itemView.findViewById(R.id.et_activity_start_date);
            etActivityStartTime = itemView.findViewById(R.id.et_activity_start_time);
            etActivityEndDate = itemView.findViewById(R.id.et_activity_end_date);
            etActivityEndTime = itemView.findViewById(R.id.et_activity_end_time);
            etActivityLocationName = itemView.findViewById(R.id.et_activity_location_name);
            etActivityLocationCity = itemView.findViewById(R.id.et_activity_location_city);
            etActivityLocationAddress = itemView.findViewById(R.id.et_activity_location_address);
            etActivityLocationCountry = itemView.findViewById(R.id.et_activity_location_country);
            etActivityLocationLongitude = itemView.findViewById(R.id.et_activity_location_longitude);
            etActivityLocationLatitude = itemView.findViewById(R.id.et_activity_location_latitude);
            btnRemoveActivity = itemView.findViewById(R.id.btn_remove_activity);
        }

        public void bind(Activity activity, int position) {
            // Set current values
            etActivityName.setText(activity.getName());
            etActivityDescription.setText(activity.getDescription());
            etActivityStartDate.setText(activity.getStartDate());
            etActivityStartTime.setText(activity.getStartTime());
            etActivityEndDate.setText(activity.getEndDate());
            etActivityEndTime.setText(activity.getEndTime());
            
            if (activity.getLocation() != null) {
                etActivityLocationName.setText(activity.getLocation().getName());
                etActivityLocationCity.setText(activity.getLocation().getCity());
                etActivityLocationAddress.setText(activity.getLocation().getAddress());
                etActivityLocationCountry.setText(activity.getLocation().getCountry());
                etActivityLocationLongitude.setText(String.valueOf(activity.getLocation().getLongitude()));
                etActivityLocationLatitude.setText(String.valueOf(activity.getLocation().getLatitude()));
            }

            // Set up date and time pickers
            setupDateAndTimePickers(activity, position);

            // Set up text watchers to update the activity object
            etActivityName.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setName(s.toString());
                }
            });

            etActivityDescription.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setDescription(s.toString());
                }
            });

            etActivityStartDate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setStartDate(s.toString());
                }
            });

            etActivityStartTime.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setStartTime(s.toString());
                }
            });

            etActivityEndDate.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setEndDate(s.toString());
                }
            });

            etActivityEndTime.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    activity.setEndTime(s.toString());
                }
            });

            // Location text watchers
            etActivityLocationName.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    activity.getLocation().setName(s.toString());
                }
            });

            etActivityLocationCity.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    activity.getLocation().setCity(s.toString());
                }
            });

            etActivityLocationAddress.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    activity.getLocation().setAddress(s.toString());
                }
            });

            etActivityLocationCountry.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    activity.getLocation().setCountry(s.toString());
                }
            });

            etActivityLocationLongitude.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    try {
                        activity.getLocation().setLongitude(Double.parseDouble(s.toString()));
                    } catch (NumberFormatException e) {
                        activity.getLocation().setLongitude(0.0);
                    }
                }
            });

            etActivityLocationLatitude.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (activity.getLocation() == null) {
                        activity.setLocation(new Location("", "", "", "", 0.0, 0.0));
                    }
                    try {
                        activity.getLocation().setLatitude(Double.parseDouble(s.toString()));
                    } catch (NumberFormatException e) {
                        activity.getLocation().setLatitude(0.0);
                    }
                }
            });

            // Remove button
            btnRemoveActivity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveActivity(position);
                }
            });
        }

        private void setupDateAndTimePickers(Activity activity, int position) {
            // Start Date Picker
            etActivityStartDate.setOnClickListener(v -> showDatePicker(activity, "startDate"));
            etActivityStartDate.setFocusable(false);
            etActivityStartDate.setClickable(true);

            // Start Time Picker
            etActivityStartTime.setOnClickListener(v -> showTimePicker(activity, "startTime"));
            etActivityStartTime.setFocusable(false);
            etActivityStartTime.setClickable(true);

            // End Date Picker
            etActivityEndDate.setOnClickListener(v -> showDatePicker(activity, "endDate"));
            etActivityEndDate.setFocusable(false);
            etActivityEndDate.setClickable(true);

            // End Time Picker
            etActivityEndTime.setOnClickListener(v -> showTimePicker(activity, "endTime"));
            etActivityEndTime.setFocusable(false);
            etActivityEndTime.setClickable(true);
        }

        private void showDatePicker(Activity activity, String type) {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        if ("startDate".equals(type)) {
                            activity.setStartDate(selectedDate);
                            etActivityStartDate.setText(selectedDate);
                        } else if ("endDate".equals(type)) {
                            activity.setEndDate(selectedDate);
                            etActivityEndDate.setText(selectedDate);
                        }
                    }, year, month, day);

            // Set minimum date to today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        }

        private void showTimePicker(Activity activity, String type) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                    (view, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                        if ("startTime".equals(type)) {
                            activity.setStartTime(selectedTime);
                            etActivityStartTime.setText(selectedTime);
                        } else if ("endTime".equals(type)) {
                            activity.setEndTime(selectedTime);
                            etActivityEndTime.setText(selectedTime);
                        }
                    }, hour, minute, true);

            timePickerDialog.show();
        }
    }

    // Simple TextWatcher implementation
    private abstract class SimpleTextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}
