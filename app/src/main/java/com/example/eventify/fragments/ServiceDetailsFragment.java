package com.example.eventify.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.eventify.adapters.ImageListAdapter;
import com.example.eventify.databinding.FragmentServiceDetailsBinding;
import com.example.eventify.models.events.Budget;
import com.example.eventify.models.events.Event;
import com.example.eventify.models.others.Purchase;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.events.BudgetService;
import com.example.eventify.services.events.EventService;
import com.example.eventify.services.solutions.ProductService;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.services.solutions.SolutionService;
import com.example.eventify.utils.RetrofitClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ServiceDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ServiceDetailsFragment extends Fragment {

    private ImageListAdapter adapter;

    private boolean isFavorite = true;
    private FragmentServiceDetailsBinding binding;

    private Solution showedSolution;
    private Service showedService;
    private Product showedProduct;

    String[] events;

    Event[] organizerEvents;

    EventService eventService = RetrofitClient.getClient().create(EventService.class);

    private boolean serviceDetails = true;

    private boolean isPurchased = false;
    private Purchase purchase;

    public ServiceDetailsFragment() {
        // Required empty public constructor
    }

    public static ServiceDetailsFragment newInstance(Solution solution) {
        ServiceDetailsFragment fragment = new ServiceDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("solution", solution);
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

        binding = FragmentServiceDetailsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            showedSolution = getArguments().getParcelable("solution");
            binding.setService(showedSolution);
            binding.setLifecycleOwner(this);
        }

        detailsBtnHandler();
        binding.right.setOnClickListener(v -> detailsBtnHandler());

        binding.favorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            binding.favorite.setSelected(isFavorite);
        });

        adapter = new ImageListAdapter(requireContext(), showedSolution.getImages(), getParentFragmentManager());
        binding.recyclerView.setAdapter(adapter);

        binding.star1.setOnClickListener( v -> rate1());
        binding.star2.setOnClickListener( v -> rate2());
        binding.star3.setOnClickListener( v -> rate3());
        binding.star4.setOnClickListener( v -> rate4());
        binding.star5.setOnClickListener( v -> rate5());

        return binding.getRoot();
    }

    private void getEvents() {
        SolutionService solutionService = RetrofitClient.getClient().create(SolutionService.class);
        eventService.getAllPaginated(0, 5, "name", true).enqueue(new Callback<EventService.EventAllResponse>() {
            @Override
            public void onResponse(Call<EventService.EventAllResponse> call, Response<EventService.EventAllResponse> response) {
                int i = 0;
                events = new String[response.body().totalElements];
                organizerEvents = new Event[response.body().totalElements];
                for (Event e:response.body().content) {
                    events[i] = e.getName();
                    organizerEvents[i] = e;
                    i++;
                }
                organizerEvents = Arrays.stream(organizerEvents)
                        .filter(Objects::nonNull)
                        .toArray(Event[]::new);
                purchase = new Purchase(organizerEvents, showedSolution);
                solutionService.isPurchased(purchase).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        isPurchased = response.body();
                        reviewPermission();
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {

                    }
                });
            }

            @Override
            public void onFailure(Call<EventService.EventAllResponse> call, Throwable t) {

            }
        });
    }

    private void buyProduct() {
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
                            budgetService.buy(UUID.fromString(budget.getId()), showedProduct).enqueue(new Callback<Budget>() {
                                @Override
                                public void onResponse(Call<Budget> call, Response<Budget> response) {
                                    isPurchased = true;
                                    reviewPermission();
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


    private void detailsBtnHandler() {

        FrameLayout details = binding.details;
        ViewGroup.LayoutParams params = details.getLayoutParams();

        if (serviceDetails) {

            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    600,
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("About us");
            details.setLayoutParams(params);
            if (showedSolution.isService()) {
                setService();
                binding.btnBook.setText("Book service");
            }
            else {
                setProduct();
                binding.btnBook.setOnClickListener(v -> buyProduct());
            }
        } else {
            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    630,
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("Solution");
            details.setLayoutParams(params);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(binding.details.getId(), PupDetailsForm.newInstance(showedSolution.getOwner()));
            serviceDetails = true;
            transaction.commit();
        }
    }

    private void reviewPermission() {
        binding.btnBook.setVisibility(isPurchased ? View.GONE:View.VISIBLE);
        binding.submitReview.setVisibility(isPurchased ? View.VISIBLE:View.GONE);
    }

    private void setService() {
        ServiceService service = RetrofitClient.getClient().create(ServiceService.class);
        service.get(showedSolution.getId()).enqueue(new Callback<Service>() {
            @Override
            public void onResponse(Call<Service> call, Response<Service> response) {
                showedService = response.body();
                serviceDetails = false;
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(binding.details.getId(), ServiceDetailsForm.newInstance(showedService));
                transaction.commit();
            }

            @Override
            public void onFailure(Call<Service> call, Throwable t) {

            }
        });
    }

    private void setProduct() {
        ProductService service = RetrofitClient.getClient().create(ProductService.class);
        service.get(showedSolution.getId()).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                showedProduct = response.body();
                getEvents();
                serviceDetails = false;
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.add(binding.details.getId(), ProductDetailsForm.newInstance(showedProduct));
                transaction.commit();
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {

            }
        });
    }

    private void rate1() {
        resetRatings();
        binding.star1.setSelected(true);
    }

    private void rate2() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
    }

    private void rate3() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
    }

    private void rate4() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
        binding.star4.setSelected(true);
    }

    private void rate5() {
        resetRatings();
        binding.star1.setSelected(true);
        binding.star2.setSelected(true);
        binding.star3.setSelected(true);
        binding.star4.setSelected(true);
        binding.star5.setSelected(true);
    }


    private void resetRatings() {
        binding.star1.setSelected(false);
        binding.star2.setSelected(false);
        binding.star3.setSelected(false);
        binding.star4.setSelected(false);
        binding.star5.setSelected(false);
    }



}