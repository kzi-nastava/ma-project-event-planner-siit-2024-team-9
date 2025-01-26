package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.eventify.adapters.ServiceListAdapter;
import com.example.eventify.databinding.FragmentServicesBinding;
import com.example.eventify.models.solutions.Service;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.services.solutions.ServiceService;
import com.example.eventify.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicesFragment extends Fragment implements SolutionFilterFragment.OnFilterAppliedListener {

    private static final String ARG_PARAM = "param";

    private ArrayList<Service> mProducts = new ArrayList<>();

    public static ArrayList<Service> products = new ArrayList<>();
    private FragmentServicesBinding servicesBinding;
    private FragmentCardBinding cardBinding;

    private ServiceListAdapter adapter;

    private boolean searchOn = false;

    public ServicesFragment() {
        // Required empty public constructor
    }

    ServiceService service = RetrofitClient.getClient().create(ServiceService.class);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getServices();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        servicesBinding = FragmentServicesBinding.inflate(inflater, container, false);

        servicesBinding.filterBtn.setOnClickListener(v -> filterBtnHandler());
        servicesBinding.addBtn.setOnClickListener(v -> addBtnHandler());
        servicesBinding.searchBtn.setOnClickListener(v -> search(servicesBinding.search.getText().toString()));
        servicesBinding.cancelSearch.setOnClickListener(v -> cancelBtnHandler());

        changeButtons();
        getServices();

        return servicesBinding.getRoot();
    }

    private void setServices(Response<Collection<Service>> response) {
        mProducts.clear();
        mProducts.addAll(response.body());
        adapter = new ServiceListAdapter(requireContext(), mProducts, getParentFragmentManager());
        servicesBinding.recyclerView.setAdapter(adapter);
    }

    private void changeButtons() {
        servicesBinding.cancelSearch.setVisibility(searchOn ? View.VISIBLE:View.GONE);
        servicesBinding.searchBtn.setVisibility(searchOn ? View.GONE:View.VISIBLE);
        servicesBinding.filterBtn.setVisibility(searchOn ? View.GONE:View.VISIBLE);
    }

    private void getServices() {
        service.getAll().enqueue(new Callback<Collection<Service>>() {
            @Override
            public void onResponse(Call<Collection<Service>> call, Response<Collection<Service>> response) {
                setServices(response);
            }

            @Override
            public void onFailure(Call<Collection<Service>> call, Throwable t) {
                Log.e("RetrofitError", "Error: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void search(String searchItem) {
        searchOn = true;
        changeButtons();
        service.search(searchItem).enqueue(new Callback<Collection<Service>>() {
            @Override
            public void onResponse(Call<Collection<Service>> call, Response<Collection<Service>> response) {
                setServices(response);
            }

            @Override
            public void onFailure(Call<Collection<Service>> call, Throwable t) {

            }
        });
    }


    private void filterBtnHandler() {
            FrameLayout filterLayout = servicesBinding.filter;
            ViewGroup.LayoutParams params = filterLayout.getLayoutParams();


            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    320,
                    getResources().getDisplayMetrics()
            );

            filterLayout.setLayoutParams(params);

            if (getChildFragmentManager().findFragmentById(servicesBinding.filter.getId()) == null) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                String search = servicesBinding.search.getText().toString();
                transaction.add(servicesBinding.filter.getId(), SolutionFilterFragment.newInstance(search));
                transaction.addToBackStack("services");
                transaction.commit();
            }
            searchOn = true;
            changeButtons();

    }

    private void cancelBtnHandler() {
            FrameLayout filterLayout = servicesBinding.filter;
            ViewGroup.LayoutParams params = filterLayout.getLayoutParams();


            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    280,
                    getResources().getDisplayMetrics()
            );
            filterLayout.setLayoutParams(params);
            params.height = 0;
            filterLayout.setLayoutParams(params);
            searchOn = false;
            getChildFragmentManager().popBackStack();
            changeButtons();
            getServices();
    }



    private void addBtnHandler() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(servicesBinding.fragmentContent.getId(), ServiceFormFragment.newInstance(new Service()));
        transaction.addToBackStack("services");
        transaction.commit();
    }


    @Override
    public void onFilterApplied(Collection<Service> filteredCollection) {
        mProducts.clear();
        mProducts.addAll(filteredCollection);
        adapter = new ServiceListAdapter(requireContext(), mProducts, getParentFragmentManager());
        servicesBinding.recyclerView.setAdapter(adapter);
        searchOn = true;
        changeButtons();
    }
}