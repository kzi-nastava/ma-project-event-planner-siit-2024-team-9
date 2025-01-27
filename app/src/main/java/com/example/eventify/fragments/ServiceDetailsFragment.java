package com.example.eventify.fragments;

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
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.models.solutions.Solution;
import com.example.eventify.services.solutions.ProductService;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.utils.RetrofitClient;

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

    private boolean serviceDetails = true;

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

        binding.right.setOnClickListener(v -> detailsBtnHandler());

        if (getArguments() != null) {
            showedSolution = getArguments().getParcelable("solution");
            binding.setService(showedSolution);
            binding.setLifecycleOwner(this);
        }

        detailsBtnHandler();

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

    private void detailsBtnHandler() {

        FrameLayout details = binding.details;
        ViewGroup.LayoutParams params = details.getLayoutParams();

        if (serviceDetails) {

            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    630,
                    getResources().getDisplayMetrics()
            );
            binding.right.setText("About us");
            details.setLayoutParams(params);
            if (showedSolution.isService())
                setService();
            else
                setProduct();
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