package com.example.eventify.fragments;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eventify.R;
import com.example.eventify.adapters.EventStatsEventAdapter;
import com.example.eventify.databinding.FragmentEventStatsBinding;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventAttendanceStatsDTO;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.others.PDFService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;
import com.example.eventify.utils.JwtUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventStatsFragment extends Fragment {

    private static final String TAG = "EventStatsFragment";

    private static final int REQUEST_CODE_STORAGE = 2012;

    private FragmentEventStatsBinding binding;
    private EventService eventService;
    private PDFService pdfService;
    private EventStatsEventAdapter adapter;
    private UserSession userSession;
    private String userRole;

    private Event selectedEvent;
    private EventAttendanceStatsDTO latestStats;
    private String pendingDownloadEventId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
        eventService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(EventService.class);
        pdfService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(PDFService.class);
        userRole = resolveUserRole();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEventStatsBinding.inflate(inflater, container, false);

        adapter = new EventStatsEventAdapter(requireContext(), this::onEventSelected);
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerEvents.setAdapter(adapter);

        setupChart();
        binding.buttonDownloadReport.setOnClickListener(v -> downloadAttendanceReport());

        loadEvents();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        selectedEvent = null;
        latestStats = null;
    }

    private void loadEvents() {
        if (!isAdded() || binding == null) return;

        showEventsLoading(true);

        if ("ADMIN".equalsIgnoreCase(userRole)) {
            fetchAllEventsForAdmin();
            return;
        }

        UUID userId = userSession.getCurrentUserId();
        if (userId == null) {
            showError(getString(R.string.event_stats_login_required));
            showEventsLoading(false);
            return;
        }

        eventService.getByOwner(userId).enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(@NonNull Call<List<Event>> call, @NonNull Response<List<Event>> response) {
                showEventsLoading(false);
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<Event> events = response.body();
                    adapter.setItems(events);
                    if (events.isEmpty()) {
                        binding.cardStats.setVisibility(View.GONE);
                        showError(getString(R.string.event_stats_no_events));
                        binding.buttonDownloadReport.setEnabled(false);
                    } else {
                        showError(null);
                        binding.cardStats.setVisibility(View.VISIBLE);
                        Event event = events.get(0);
                        adapter.setSelectedEventId(event.getId());
                        onEventSelected(event);
                    }
                } else {
                    Log.e(TAG, "Failed to load events: " + response.code());
                    showError(getString(R.string.event_stats_events_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Event>> call, @NonNull Throwable t) {
                showEventsLoading(false);
                if (!isAdded() || binding == null) return;
                Log.e(TAG, "Network error while loading events", t);
                showError(getString(R.string.event_stats_events_error));
            }
        });
    }

    private void fetchAllEventsForAdmin() {
        eventService.getAll().enqueue(new Callback<EventService.EventAllResponse>() {
            @Override
            public void onResponse(@NonNull Call<EventService.EventAllResponse> call,
                                   @NonNull Response<EventService.EventAllResponse> response) {
                showEventsLoading(false);
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().content != null) {
                    List<Event> events = response.body().content;
                    adapter.setItems(events);
                    if (events.isEmpty()) {
                        binding.cardStats.setVisibility(View.GONE);
                        showError(getString(R.string.event_stats_no_events));
                        binding.buttonDownloadReport.setEnabled(false);
                    } else {
                        showError(null);
                        binding.cardStats.setVisibility(View.VISIBLE);
                        Event event = events.get(0);
                        adapter.setSelectedEventId(event.getId());
                        onEventSelected(event);
                    }
                } else {
                    Log.e(TAG, "Failed to load all events for admin: " + response.code());
                    showError(getString(R.string.event_stats_events_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventService.EventAllResponse> call, @NonNull Throwable t) {
                showEventsLoading(false);
                if (!isAdded() || binding == null) return;
                Log.e(TAG, "Network error while loading admin events", t);
                showError(getString(R.string.event_stats_events_error));
            }
        });
    }

    private void onEventSelected(@NonNull Event event) {
        if (!isAdded() || binding == null) return;
        selectedEvent = event;
        adapter.setSelectedEventId(event.getId());
        binding.textSelectedEvent.setText(event.getName());
        latestStats = null;

        binding.buttonDownloadReport.setEnabled(false);
        binding.layoutSummary.setVisibility(View.GONE);
        binding.pieChart.setVisibility(View.GONE);
        binding.textNoStats.setVisibility(View.GONE);

        loadStats(event);
    }

    private void loadStats(@NonNull Event event) {
        if (event.getId() == null || binding == null) return;

        binding.progressStats.setVisibility(View.VISIBLE);
        eventService.getEventAttendanceStats(event.getId()).enqueue(new Callback<EventAttendanceStatsDTO>() {
            @Override
            public void onResponse(@NonNull Call<EventAttendanceStatsDTO> call,
                                   @NonNull Response<EventAttendanceStatsDTO> response) {
                if (!isAdded() || binding == null) return;
                binding.progressStats.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    latestStats = response.body();
                    updateStatsView(latestStats);
                } else {
                    Log.e(TAG, "Failed to load stats for event: " + response.code());
                    showStatsError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventAttendanceStatsDTO> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                binding.progressStats.setVisibility(View.GONE);
                Log.e(TAG, "Network error while loading stats", t);
                showStatsError();
            }
        });
    }

    private void updateStatsView(@NonNull EventAttendanceStatsDTO stats) {
        if (binding == null) return;

        binding.buttonDownloadReport.setEnabled(true);
        binding.layoutSummary.setVisibility(View.VISIBLE);

        String attendanceText = String.format(
                Locale.getDefault(),
                getString(R.string.event_stats_attendance_summary),
                stats.getAttendance(),
                stats.getMaxAttendees(),
                stats.getAttendancePercentage()
        );
        binding.textAttendance.setText(attendanceText);

        String ratingText = String.format(
                Locale.getDefault(),
                getString(R.string.event_stats_average_rating),
                stats.getAverageRating(),
                stats.getTotalReviews()
        );
        binding.textAverageRating.setText(ratingText);

        binding.textMostCommonGrade.setText(
                getString(R.string.event_stats_most_common_grade, stats.getMostCommonGrade())
        );

        updateChart(stats);
    }

    private void updateChart(@NonNull EventAttendanceStatsDTO stats) {
        if (binding == null) return;

        PieChart chart = binding.pieChart;
        Map<Integer, Long> distribution = stats.getGradeDistribution();
        List<PieEntry> entries = new ArrayList<>();

        if (distribution != null) {
            for (Map.Entry<Integer, Long> entry : distribution.entrySet()) {
                if (entry.getValue() != null && entry.getValue() > 0) {
                    String label = getString(R.string.event_stats_grade_label, entry.getKey());
                    entries.add(new PieEntry(entry.getValue().floatValue(), label));
                }
            }
        }

        if (entries.isEmpty()) {
            chart.clear();
            chart.setVisibility(View.GONE);
            binding.textNoStats.setVisibility(View.VISIBLE);
            return;
        }

        binding.textNoStats.setVisibility(View.GONE);
        chart.setVisibility(View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "");
        List<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        PieData data = new PieData(dataSet);
        data.setValueTextColor(android.graphics.Color.WHITE);
        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
    }

    private void setupChart() {
        PieChart chart = binding.pieChart;
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(false);
        chart.setNoDataText(getString(R.string.event_stats_no_data));
        chart.setNoDataTextColor(ContextCompat.getColor(requireContext(), R.color.gray));

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setWordWrapEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
    }

    private void downloadAttendanceReport() {
        if (!isAdded() || binding == null || selectedEvent == null || selectedEvent.getId() == null) {
            return;
        }

        String eventId = selectedEvent.getId();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            pendingDownloadEventId = eventId;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
            return;
        }

        startAttendanceDownload(eventId);
    }

    private void startAttendanceDownload(@NonNull String eventIdStr) {
        try {
            UUID eventId = UUID.fromString(eventIdStr);
            pendingDownloadEventId = null;
            binding.buttonDownloadReport.setEnabled(false);
            Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_downloading), Toast.LENGTH_SHORT).show();
            pdfService.getEventAttendancePdf(eventId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (!isAdded() || binding == null) return;
                    binding.buttonDownloadReport.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Attendance PDF download succeeded for event " + eventId + ", bytes=" + response.body().contentLength());
                        String fileName = buildFileName();
                        try {
                            Uri pdfUri = savePdfToStorage(response.body(), fileName);
                            if (pdfUri != null) {
                                Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_saved), Toast.LENGTH_LONG).show();
                                openPdfUri(pdfUri, fileName);
                            } else {
                                Log.e(TAG, "PDF save returned null URI");
                                Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error saving attendance PDF", e);
                            Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        int code = response.code();
                        String msg = response.message();
                        String errBody = null;
                        try {
                            if (response.errorBody() != null) {
                                errBody = response.errorBody().string();
                            }
                        } catch (IOException ioException) {
                            Log.w(TAG, "Failed to read error body", ioException);
                        }
                        Log.e(TAG, "Attendance PDF request failed: code=" + code + ", message=" + msg + ", body=" + errBody);
                        Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    if (!isAdded() || binding == null) return;
                    binding.buttonDownloadReport.setEnabled(true);
                    Log.e(TAG, "PDF download failed", t);
                    Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Invalid event id when requesting PDF", ex);
            Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    private Uri savePdfToStorage(@NonNull ResponseBody body, @NonNull String filename) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Eventify");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e(TAG, "Failed to insert into MediaStore");
                return null;
            }

            try (InputStream inputStream = body.byteStream();
                 OutputStream outputStream = resolver.openOutputStream(uri)) {
                if (outputStream == null) {
                    throw new IOException("Unable to open output stream for MediaStore URI");
                }
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }

            values.clear();
            values.put(MediaStore.Downloads.IS_PENDING, 0);
            resolver.update(uri, values, null, null);
            return uri;
        } else {
            File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Eventify");
            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                Log.w(TAG, "Failed to create Eventify directory in Downloads");
            }
            File pdfFile = new File(downloadsDir, filename);
            try (InputStream inputStream = body.byteStream();
                 OutputStream outputStream = new FileOutputStream(pdfFile)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            }
            return FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    pdfFile
            );
        }
    }

    private void openPdfUri(@NonNull Uri uri, @NonNull String fileName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Log.w(TAG, "No activity found to open PDF, uri=" + uri, e);
            Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_saved), Toast.LENGTH_LONG).show();
        }
    }

    private String buildFileName() {
        String name = selectedEvent != null ? selectedEvent.getName() : "attendance";
        if (name == null || name.trim().isEmpty()) {
            name = "attendance";
        }
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase(Locale.ROOT) + "_stats.pdf";
    }

    private void savePdfToMediaStore(@NonNull ResponseBody body, @NonNull String filename) throws IOException {
        try {
            ContentResolver resolver = requireContext().getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Downloads.IS_PENDING, 1);
            }

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                return;
            }

            try (OutputStream outputStream = resolver.openOutputStream(uri);
                 InputStream inputStream = body.byteStream()) {

                if (outputStream == null || inputStream == null) {
                    Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                    return;
                }

                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error writing PDF", e);
                Toast.makeText(requireContext(), getString(R.string.event_stats_pdf_error), Toast.LENGTH_LONG).show();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error saving PDF", e);
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(e);
        }
    }

    private void savePdfToFile(@NonNull ResponseBody body, @NonNull String filename) throws IOException {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir != null && !downloadsDir.exists()) {
            boolean ignored = downloadsDir.mkdirs();
        }

        File outFile = new File(downloadsDir, filename);
        try (InputStream inputStream = body.byteStream();
             OutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
    }

    private void showError(@Nullable String message) {
        if (binding == null) return;
        if (message == null || message.isEmpty()) {
            binding.textError.setVisibility(View.GONE);
        } else {
            binding.textError.setVisibility(View.VISIBLE);
            binding.textError.setText(message);
        }
    }

    private void showEventsLoading(boolean loading) {
        if (binding == null) return;
        binding.progressEvents.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showStatsError() {
        if (binding == null) return;
        binding.layoutSummary.setVisibility(View.GONE);
        binding.pieChart.setVisibility(View.GONE);
        binding.textNoStats.setVisibility(View.VISIBLE);
        binding.buttonDownloadReport.setEnabled(latestStats != null);
        Toast.makeText(requireContext(), getString(R.string.event_stats_stats_error), Toast.LENGTH_SHORT).show();
    }

    private String resolveUserRole() {
        try {
            String token = userSession.getAuthToken();
            if (token == null) return null;
            JwtUtils.JwtClaims claims = JwtUtils.decodeToken(token);
            return claims != null ? claims.getRole() : null;
        } catch (Exception e) {
            Log.w(TAG, "Failed to resolve user role", e);
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && pendingDownloadEventId != null) {
                String eventId = pendingDownloadEventId;
                pendingDownloadEventId = null;
                startAttendanceDownload(eventId);
            } else {
                pendingDownloadEventId = null;
                Toast.makeText(requireContext(), R.string.event_stats_pdf_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
