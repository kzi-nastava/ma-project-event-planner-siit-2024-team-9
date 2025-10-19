package com.example.eventify.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.Toast;

import android.Manifest;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import okhttp3.ResponseBody;
import com.example.eventify.R;
import androidx.annotation.NonNull;
import com.example.eventify.databinding.FragmentPriceListBinding;
import com.example.eventify.models.others.Discount;
import com.example.eventify.models.others.Price;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.others.PDFService;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PriceListFragment extends Fragment {

    private FragmentPriceListBinding binding;
    private SolutionService service;
    private PDFService pdfService;
    private List<Solution> solutions = new ArrayList<>();
    private boolean editMode = false;
    private UserSession userSession;

    private static final int REQUEST_CODE_STORAGE = 2001;

    public PriceListFragment() {}

    public static PriceListFragment newInstance() {
        PriceListFragment fragment = new PriceListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
        service = RetrofitClient.getClient(requireContext().getApplicationContext()).create(SolutionService.class);
        pdfService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(PDFService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentPriceListBinding.inflate(inflater, container, false);

        // Set initial button states
        binding.savePrices.setVisibility(View.GONE);
        binding.editPrices.setVisibility(View.VISIBLE);

        // Set click listeners
        binding.savePrices.setOnClickListener(v -> save());
        binding.editPrices.setOnClickListener(v -> setEditMode());

        binding.downloadPrices.setOnClickListener(v -> downloadPriceList());

        // Load solutions
        loadSolutions();

        return binding.getRoot();
    }

    private void downloadPriceList() {
        if (!userSession.isValidSession()) {
            Toast.makeText(requireContext(), "Please log in to download price list", Toast.LENGTH_LONG).show();
            return;
        }

        // Check permissions based on Android version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // For Android 9 and below, need WRITE_EXTERNAL_STORAGE permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
                return;
            }
        }
        // For Android 10+, no special permissions needed for MediaStore API
        
        startPriceListDownload();
    }

    private void startPriceListDownload() {
        showLoading();
        UUID userId = userSession.getCurrentUserId();
        Log.d("PDFDownload", "Attempting to download PDF for userId: " + userId);
        pdfService.getPriceListPdf(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                hideLoading();
                Log.d("PDFDownload", "Response code: " + response.code() + ", URL: " + call.request().url());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PDFDownload", "Response body size: " + response.body().contentLength());
                    Log.d("PDFDownload", "Response body type: " + response.body().contentType());
                    savePdfToStorage(requireContext(), response.body(), "price-list.pdf");
                } else {
                    String errorMessage = "Failed to download PDF";
                    if (response.code() == 404) {
                        errorMessage = "No solutions found to generate price list";
                    } else if (response.code() == 401) {
                        errorMessage = "Please log in to download price list";
                    } else if (response.code() >= 500) {
                        errorMessage = "Server error. Please try again later";
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                hideLoading();
                String errorMessage = "Download error: " + t.getMessage();
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage = "Cannot connect to server. Check your internet connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Connection timeout. Please try again.";
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void savePdfToStorage(Context context, ResponseBody body, String filename) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore API for Android 10+
                savePdfToMediaStore(context, body, filename);
            } else {
                // Use traditional file API for older versions
                savePdfToFile(context, body, filename);
            }
        } catch (Exception e) {
            Log.e("PDFDownload", "Error saving PDF: " + e.getMessage(), e);
            Toast.makeText(requireContext(), 
                "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfToMediaStore(Context context, ResponseBody body, String filename) {
        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Downloads.DISPLAY_NAME, filename);
            values.put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(android.provider.MediaStore.Downloads.IS_PENDING, 1);

            android.content.ContentResolver resolver = context.getContentResolver();
            android.net.Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(uri);
                     InputStream inputStream = body.byteStream()) {

                    byte[] buffer = new byte[4096];
                    int read;
                    long totalBytes = 0;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                        totalBytes += read;
                    }

                    values.clear();
                    values.put(android.provider.MediaStore.Downloads.IS_PENDING, 0);
                    resolver.update(uri, values, null, null);

                    Log.d("PDFDownload", "File saved to MediaStore: " + uri);
                    Log.d("PDFDownload", "File size: " + totalBytes + " bytes");

                    Toast.makeText(requireContext(), 
                        "PDF saved to Downloads folder (" + totalBytes + " bytes)", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to create file in Downloads", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PDFDownload", "Error saving to MediaStore: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePdfToFile(Context context, ResponseBody body, String filename) {
        try {
            // Use public Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File file = new File(downloadsDir, filename);
            InputStream inputStream = body.byteStream();
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int read;
            long totalBytes = 0;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                totalBytes += read;
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            Log.e("PDFDownload", "Error saving to file: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPriceListDownload();
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSolutions() {
        if (!userSession.isValidSession()) {
            showError("Please log in to view your solutions");
            return;
        }

        showLoading();
        UUID userId = userSession.getCurrentUserId();

        service.getByOwner(userId.toString()).enqueue(new Callback<List<Solution>>() {
            @Override
            public void onResponse(Call<List<Solution>> call, Response<List<Solution>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    solutions = response.body();
                    if (solutions.isEmpty()) {
                        showError("No solutions found");
                    } else {
                        hideError();
                        fillTable();
                    }
                } else {
                    showError("Failed to load solutions");
                }
            }

            @Override
            public void onFailure(Call<List<Solution>> call, Throwable t) {
                hideLoading();
                showError("Error loading solutions: " + t.getMessage());
            }
        });
    }

    private void setEditMode() {
        editMode = !editMode;
        changeButtons();
        fillTable();
    }

    private void save() {
        showLoading();
        int totalSolutions = solutions.size();
        final int[] savedCount = {0};
        final int[] errorCount = {0};

        for (Solution solution : solutions) {
            // Update both price and discount independently
            updatePriceAndDiscount(solution, totalSolutions, savedCount, errorCount);
        }
    }

    private void updatePriceAndDiscount(Solution solution, int totalSolutions, int[] savedCount, int[] errorCount) {
        // Update price
        service.updatePrice(solution.getId().toString(), new Price(solution.getPrice()))
            .enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    boolean priceSuccess = response.isSuccessful() && Boolean.TRUE.equals(response.body());
                    
                    // Update discount regardless of price update result
                    Log.d("PriceListFragment", "Updating discount for solution " + solution.getId() + " to " + solution.getDiscount());
                    service.updateDiscount(solution.getId().toString(), new Discount(solution.getDiscount()))
                        .enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                boolean discountSuccess = response.isSuccessful() && Boolean.TRUE.equals(response.body());
                                Log.d("PriceListFragment", "Discount update response - Success: " + response.isSuccessful() + ", Body: " + response.body());
                                
                                if (priceSuccess && discountSuccess) {
                                    savedCount[0]++;
                                } else {
                                    errorCount[0]++;
                                    Log.e("PriceListFragment", "Update failed - Price: " + priceSuccess + ", Discount: " + discountSuccess);
                                }
                                checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                errorCount[0]++;
                                Log.e("PriceListFragment", "Discount update failed: " + t.getMessage());
                                checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                            }
                        });
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    // Even if price update fails, try discount update
                    Log.d("PriceListFragment", "Price update failed, trying discount update for solution " + solution.getId());
                    service.updateDiscount(solution.getId().toString(), new Discount(solution.getDiscount()))
                        .enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                boolean discountSuccess = response.isSuccessful() && Boolean.TRUE.equals(response.body());
                                Log.d("PriceListFragment", "Fallback discount update response - Success: " + response.isSuccessful() + ", Body: " + response.body());
                                if (discountSuccess) {
                                    savedCount[0]++;
                                } else {
                                    errorCount[0]++;
                                }
                                checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                errorCount[0]++;
                                Log.e("PriceListFragment", "Both price and discount updates failed");
                                checkSaveCompletion(totalSolutions, savedCount[0], errorCount[0]);
                            }
                        });
                }
            });
    }

    private void checkSaveCompletion(int total, int saved, int errors) {
        if (saved + errors == total) {
            hideLoading();
            if (errors > 0) {
                showError("Failed to save some changes");
            } else {
                Toast.makeText(requireContext(), "All changes saved successfully", Toast.LENGTH_SHORT).show();
                editMode = false;
                changeButtons();
                loadSolutions(); // Reload to get fresh data
            }
        }
    }

    private void changeButtons() {
        binding.editPrices.setVisibility(editMode ? View.GONE : View.VISIBLE);
        binding.savePrices.setVisibility(editMode ? View.VISIBLE : View.GONE);
    }

    private void fillTable() {
        binding.solutionPrices.removeAllViews();

        // Add header row
        View headerRow = LayoutInflater.from(requireContext()).inflate(R.layout.price_list_row, binding.solutionPrices, false);
        headerRow.setBackgroundColor(getResources().getColor(android.R.color.white));
        binding.solutionPrices.addView(headerRow);

        int solutionNumber = 0;
        for (Solution solution : solutions) {
            solutionNumber++;
            TableRow row = (TableRow) LayoutInflater.from(requireContext()).inflate(R.layout.price_list_row, binding.solutionPrices, false);

            // Set alternating row background
            row.setBackgroundColor(solutionNumber % 2 == 0 ? 
                getResources().getColor(android.R.color.white) : 
                Color.parseColor("#F5F5F5"));

            // Set up row views
            setupRowViews(row, solution, solutionNumber);

            binding.solutionPrices.addView(row);
        }
    }

    private void setupRowViews(TableRow row, Solution solution, int number) {
        // Number
        ((android.widget.TextView) row.findViewById(R.id.solutionNumber))
            .setText(String.valueOf(number));

        // Name
        ((android.widget.TextView) row.findViewById(R.id.solutionName))
            .setText(solution.getName());

        // Price
        EditText priceCell = row.findViewById(R.id.solutionPrice);
        priceCell.setText(String.format("%.2f", solution.getPrice()));
        priceCell.setEnabled(editMode);
        priceCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double price = Double.parseDouble(s.toString());
                    solution.setPrice(price);
                    updateDiscountedPrice(row, solution);
                } catch (NumberFormatException e) {
                    // Invalid number, ignore
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Discount
        EditText discountCell = row.findViewById(R.id.solutionDiscount);
        discountCell.setText(String.format("%.1f", solution.getDiscount()));
        discountCell.setEnabled(editMode);
        discountCell.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    double discount = Double.parseDouble(s.toString());
                    solution.setDiscount(discount);
                    updateDiscountedPrice(row, solution);
                } catch (NumberFormatException e) {
                    // Invalid number, ignore
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Final price
        updateDiscountedPrice(row, solution);
    }

    private void updateDiscountedPrice(TableRow row, Solution solution) {
        double finalPrice = solution.getPrice() * (1 - solution.getDiscount() / 100);
        ((android.widget.TextView) row.findViewById(R.id.solutionDiscounted))
            .setText(String.format("%.2f", finalPrice));
    }

    private void showLoading() {
        binding.loadingProgress.setVisibility(View.VISIBLE);
        binding.errorText.setVisibility(View.GONE);
    }

    public void hideLoading() {
        binding.loadingProgress.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.errorText.setText(message);
        binding.errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.errorText.setVisibility(View.GONE);
    }
}