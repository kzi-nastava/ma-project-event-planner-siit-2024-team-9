package com.example.eventify.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.eventify.R;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.solutions.reservations.CreateServiceReservationRequest;
import com.example.eventify.models.solutions.reservations.ServiceReservation;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.solutions.ServiceReservationService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookServiceDialogFragment extends DialogFragment {

    public static final String TAG = "BookServiceDialog";

    private UUID serviceId;
    private UUID providerId;
    private UUID customerId;
    private UUID preselectedEventId;
    private String serviceName;
    private String providerName;

    private EditText etDate, etStart, etEnd, etNote;
    private Spinner spEvent;
    private TextView tvError, tvServiceName, tvProviderName, tvNoEvents;
    private View headerSection;
    private Button btnBook, btnCancel, btnCreateEvent;

    private final Calendar selectedDate = Calendar.getInstance();
    private final List<Event> userEvents = new ArrayList<>();
    private ArrayAdapter<String> eventAdapter;
    private EventService eventService;
    private ServiceReservationService reservationService;
    private UserSession userSession;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static BookServiceDialogFragment newInstance(UUID serviceId, UUID providerId, UUID customerId,
                                                        @Nullable UUID eventId,
                                                        @Nullable String serviceName, @Nullable String providerName) {
        BookServiceDialogFragment f = new BookServiceDialogFragment();
        Bundle b = new Bundle();
        b.putString("serviceId", serviceId.toString());
        b.putString("providerId", providerId.toString());
        b.putString("customerId", customerId.toString());
        if (eventId != null) b.putString("eventId", eventId.toString());
        if (serviceName != null) b.putString("serviceName", serviceName);
        if (providerName != null) b.putString("providerName", providerName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // koristimo application context da bude stabilno
        android.content.Context appCtx = requireContext().getApplicationContext();

        eventService = RetrofitClient.getClient(appCtx).create(EventService.class);
        reservationService = RetrofitClient.getClient(appCtx).create(ServiceReservationService.class);
        userSession = new UserSession(requireContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book_service_dialog, container, false);

        Bundle args = getArguments();
        serviceId = UUID.fromString(Objects.requireNonNull(args).getString("serviceId"));
        providerId = UUID.fromString(args.getString("providerId"));
        customerId = UUID.fromString(args.getString("customerId"));
        if (args.containsKey("eventId")) preselectedEventId = UUID.fromString(args.getString("eventId"));
        serviceName = args.getString("serviceName");
        providerName = args.getString("providerName");

        etDate = v.findViewById(R.id.etDate);
        etStart = v.findViewById(R.id.etStart);
        etEnd   = v.findViewById(R.id.etEnd);
        etNote  = v.findViewById(R.id.etNote);
        spEvent = v.findViewById(R.id.spEvent);
        tvError = v.findViewById(R.id.tvError);
        tvNoEvents = v.findViewById(R.id.tvNoEvents);
        btnCreateEvent = v.findViewById(R.id.btnCreateEvent);
        btnBook = v.findViewById(R.id.btnBook);
        btnCancel = v.findViewById(R.id.btnCancel);
        headerSection = v.findViewById(R.id.headerSection);
        tvServiceName = v.findViewById(R.id.serviceName);
        tvProviderName = v.findViewById(R.id.providerName);

        if (!TextUtils.isEmpty(serviceName) || !TextUtils.isEmpty(providerName)) {
            headerSection.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(serviceName)) tvServiceName.setText(serviceName);
            if (!TextUtils.isEmpty(providerName)) tvProviderName.setText(providerName);
        }

        Calendar min = Calendar.getInstance();
        min.set(Calendar.HOUR_OF_DAY, 0);
        min.set(Calendar.MINUTE, 0);
        min.set(Calendar.SECOND, 0);
        min.set(Calendar.MILLISECOND, 0);
        selectedDate.setTime(min.getTime());

        etDate.setOnClickListener(v1 -> {
            DatePickerDialog dp = new DatePickerDialog(requireContext(),
                    (view, y, m, d) -> {
                        selectedDate.set(Calendar.YEAR, y);
                        selectedDate.set(Calendar.MONTH, m);
                        selectedDate.set(Calendar.DAY_OF_MONTH, d);
                        etDate.setText(dateFmt.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            dp.getDatePicker().setMinDate(min.getTimeInMillis());
            dp.show();
        });

        etStart.setOnClickListener(v12 -> pickTime(etStart));
        etEnd.setOnClickListener(v13 -> pickTime(etEnd));

        btnCancel.setOnClickListener(v14 -> dismiss());
        btnBook.setOnClickListener(v15 -> tryBook());
        btnBook.setEnabled(false);

        SimpleTextWatcher watcher = new SimpleTextWatcher(this::validateForm);
        etDate.addTextChangedListener(watcher);
        etStart.addTextChangedListener(watcher);
        etEnd.addTextChangedListener(watcher);

        spEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { validateForm(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { validateForm(); }
        });

        loadUserEvents();
        return v;
    }

    private void pickTime(EditText target) {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog tp = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String hh = String.format(Locale.getDefault(), "%02d", hourOfDay);
                    String mm = String.format(Locale.getDefault(), "%02d", minute);
                    target.setText(hh + ":" + mm);
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true);
        tp.show();
    }

    private void loadUserEvents() {
        EventService eventService =RetrofitClient.getClient(requireContext()).create(EventService.class);
        UUID currentUserId = userSession.getCurrentUserId();

        eventService.getByOwner(currentUserId).enqueue(new Callback<List<Event>>() {
            @Override public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> resp) {
                if (resp.isSuccessful() && resp.body() != null) {
                    userEvents.clear();
                    userEvents.addAll(resp.body());
                    if (userEvents.isEmpty()) {
                        showNoEventsState(true);
                        validateForm();
                        return;
                    }
                    showNoEventsState(false);
                    List<String> names = new ArrayList<>();
                    for (Event e : userEvents) names.add(e.getName());
                    eventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, names);
                    spEvent.setAdapter(eventAdapter);
                    if (preselectedEventId != null) {
                        for (int i = 0; i < userEvents.size(); i++) {
                            UUID evId = toUuid(userEvents.get(i).getId());
                            if (preselectedEventId.equals(evId)) {
                                spEvent.setSelection(i);
                                break;
                            }
                        }
                    } else {
                        spEvent.setSelection(0);
                    }
                    validateForm();
                } else {
                    showError("Failed to load your events.");
                    showNoEventsState(true);
                    validateForm();
                }
            }
            @Override public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                showError("Failed to load your events.");
                showNoEventsState(true);
                validateForm();
            }
        });
    }

    private void tryBook() {
        hideError();
        String dateStr  = etDate.getText().toString().trim();
        String startStr = etStart.getText().toString().trim();
        String endStr   = etEnd.getText().toString().trim();

        if (TextUtils.isEmpty(dateStr)) { showError("Date is required"); return; }
        if (!startStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) { showError("Invalid start time (HH:mm)"); return; }
        if (!endStr.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) { showError("Invalid end time (HH:mm)"); return; }
        if (userEvents.isEmpty() || spEvent.getSelectedItemPosition() < 0) { showError("Event is required"); return; }

        Event selectedEvent = userEvents.get(spEvent.getSelectedItemPosition());
        UUID selectedEventId = toUuid(selectedEvent.getId());
        String startIso = toLocalIso(dateStr, startStr);
        String endIso   = toLocalIso(dateStr, endStr);

        if (isoToDate(endIso).compareTo(isoToDate(startIso)) <= 0) {
            showError("End time must be after start time.");
            return;
        }

        CreateServiceReservationRequest payload = new CreateServiceReservationRequest(
                serviceId, providerId, customerId, selectedEventId,
                startIso, endIso,
                textOrNull(etNote.getText().toString().trim())
        );

        btnBook.setEnabled(false);

        reservationService.create(payload).enqueue(new Callback<ServiceReservation>() {
            @Override public void onResponse(@NonNull Call<ServiceReservation> call,@NonNull Response<ServiceReservation> resp) {
                btnBook.setEnabled(true);
                if (resp.isSuccessful() && resp.body() != null) {
                    Bundle res = new Bundle();
                    res.putString("reservationId", resp.body().getId().toString());
                    getParentFragmentManager().setFragmentResult(TAG, res);
                    dismiss();
                } else {
                    showError("Failed to create reservation.");
                }
            }
            @Override public void onFailure(@NonNull Call<ServiceReservation> call, @NonNull Throwable t) {
                btnBook.setEnabled(true);
                showError("Failed to create reservation.");
            }
        });
    }

    private String textOrNull(String s) { return TextUtils.isEmpty(s) ? null : s; }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setText("");
        tvError.setVisibility(View.GONE);
    }

    private String toLocalIso(String ymd, String hhmm) {
        return ymd + "T" + hhmm + ":00";
    }

    private Date isoToDate(String iso) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(iso);
        } catch (Exception e) {
            return new Date(0);
        }
    }

    private void validateForm() {
        boolean hasDate = !TextUtils.isEmpty(etDate.getText().toString().trim());
        String start = etStart.getText().toString().trim();
        String end   = etEnd.getText().toString().trim();
        boolean startOk = start.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
        boolean endOk   = end.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
        boolean haveEvents = !userEvents.isEmpty();
        boolean eventSelected = haveEvents && spEvent.getSelectedItemPosition() >= 0;
        boolean timeOrderOk = false;
        if (startOk && endOk && hasDate) {
            Date s = isoToDate(toLocalIso(etDate.getText().toString().trim(), start));
            Date e = isoToDate(toLocalIso(etDate.getText().toString().trim(), end));
            timeOrderOk = e.after(s);
        }
        boolean formValid = hasDate && startOk && endOk && timeOrderOk && eventSelected && haveEvents;
        btnBook.setEnabled(formValid);
        if (!formValid) {
            if (!haveEvents) showError("You need at least one event to book a service.");
            else if (!eventSelected) showError("Event is required.");
            else if (!hasDate) showError("Date is required.");
            else if (!startOk) showError("Invalid start time (HH:mm).");
            else if (!endOk) showError("Invalid end time (HH:mm).");
            else if (!timeOrderOk) showError("End time must be after start time.");
        } else {
            hideError();
        }
    }

    private void showNoEventsState(boolean noEvents) {
        tvNoEvents.setVisibility(noEvents ? View.VISIBLE : View.GONE);
        btnCreateEvent.setVisibility(noEvents ? View.VISIBLE : View.GONE);
        spEvent.setEnabled(!noEvents);
        btnBook.setEnabled(!noEvents && btnBook.isEnabled());
        btnCreateEvent.setOnClickListener(v -> {
            Bundle res = new Bundle();
            res.putString("action", "CREATE_EVENT");
            getParentFragmentManager().setFragmentResult(TAG, res);
            dismiss();
        });
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    private UUID toUuid(Object id) {
        if (id == null) return null;
        if (id instanceof UUID) return (UUID) id;
        return UUID.fromString(String.valueOf(id));
    }
}
