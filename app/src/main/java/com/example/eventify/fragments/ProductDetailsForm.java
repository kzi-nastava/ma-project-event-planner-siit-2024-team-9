package com.example.eventify.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.databinding.FragmentProductDetailsFormBinding;
import com.example.eventify.databinding.FragmentServiceDetailsFormBinding;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.events.EventType;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.utils.RetrofitClient;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProductDetailsForm extends Fragment {

    Product product;

    public ProductDetailsForm() {
        // Required empty public constructor
    }

    String[] events;

    EventService eventService = RetrofitClient.getClient().create(EventService.class);

    FragmentProductDetailsFormBinding binding;


    public static ProductDetailsForm newInstance(Product product) {
        ProductDetailsForm fragment = new ProductDetailsForm();
        Bundle args = new Bundle();
        args.putParcelable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProductDetailsFormBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            product = getArguments().getParcelable("product");
            binding.setService(product);
        }

        getEvents();

        binding.btnBook.setOnClickListener(v -> showServiceDialog());

        setTypes();

        return binding.getRoot();
    }


    private void getEvents() {
        eventService.getAllPaginated(0, 5, "name", true).enqueue(new Callback<EventService.EventAllResponse>() {
            @Override
            public void onResponse(Call<EventService.EventAllResponse> call, Response<EventService.EventAllResponse> response) {
                int i = 0;
                events = new String[response.body().totalElements];
                for (Event e:response.body().content) {
                    events[i] = e.getName();
                    i++;
                }
            }

            @Override
            public void onFailure(Call<EventService.EventAllResponse> call, Throwable t) {

            }
        });
    }

    private void showServiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select your event");

        builder.setItems(events, (dialog, which) -> {
            eventService.getByName(events[which]).enqueue(new Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, Response<Event> response) {
                    Event event = response.body();
                    eventService.getBudget(event.getId()).enqueue(new Callback<Budget>() {
                        @Override
                        public void onResponse(Call<Budget> call, Response<Budget> response) {
                            Budget budget = response.body();
                            BudgetService budgetService = RetrofitClient.getClient().create(BudgetService.class);
                            budgetService.buy(UUID.fromString(budget.getId()), product).enqueue(new Callback<Budget>() {
                                @Override
                                public void onResponse(Call<Budget> call, Response<Budget> response) {

                                }

                                @Override
                                public void onFailure(Call<Budget> call, Throwable t) {

                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Budget> call, Throwable t) {

                        }
                    });
                }

                @Override
                public void onFailure(Call<Event> call, Throwable t) {

                }
            });
        });

        builder.show();
    }


    private void setTypes() {
        StringBuilder typesInfo = new StringBuilder();
        for (EventType type: product.getEventTypes()) {
            typesInfo.append(type.getName()).append(", ");
        }
        binding.eventTypesInfo.setText(typesInfo.toString());
    }


}