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
import android.widget.Toast;

import com.example.eventify.adapters.ProductListAdapter;
import com.example.eventify.databinding.FragmentProductsBinding;
import com.example.eventify.models.solutions.Product;
import com.example.eventify.models.users.BusinessOwner;
import com.example.eventify.databinding.FragmentCardBinding;
import com.example.eventify.services.solutions.ProductService;
import com.example.eventify.services.users.BusinessOwnerService;
import com.example.eventify.utils.RetrofitClient;
import com.example.eventify.utils.UserSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsFragment extends Fragment implements ProductFilterFragment.OnFilterAppliedListener {

    private static final String ARG_PARAM = "param";

    private ArrayList<Product> mProducts = new ArrayList<>();

    public static ArrayList<Product> products = new ArrayList<>();
    private FragmentProductsBinding productsBinding;
    private FragmentCardBinding cardBinding;

    private ProductListAdapter adapter;
    private UserSession userSession;

    private boolean searchOn = false;

    public ProductsFragment() {
        // Required empty public constructor
    }

    ProductService productService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userSession = new UserSession(requireContext());
        productService = RetrofitClient.getClient(requireContext().getApplicationContext()).create(ProductService.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh products when fragment resumes to show latest changes
        getProducts();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        productsBinding = FragmentProductsBinding.inflate(inflater, container, false);

        productsBinding.filterBtn.setOnClickListener(v -> filterBtnHandler());
        productsBinding.addBtn.setOnClickListener(v -> addBtnHandler());
        productsBinding.searchBtn.setOnClickListener(v -> search(productsBinding.search.getText().toString()));
        productsBinding.cancelSearch.setOnClickListener(v -> cancelBtnHandler());

        changeButtons();
        getProducts();

        return productsBinding.getRoot();
    }

    private void setProducts(Response<Collection<Product>> response) {
        mProducts.clear();
        mProducts.addAll(response.body());
        adapter = new ProductListAdapter(requireContext(), mProducts, getParentFragmentManager());
        productsBinding.recyclerView.setAdapter(adapter);
    }

    private void changeButtons() {
        productsBinding.cancelSearch.setVisibility(searchOn ? View.VISIBLE:View.GONE);
        productsBinding.searchBtn.setVisibility(searchOn ? View.GONE:View.VISIBLE);
        productsBinding.filterBtn.setVisibility(searchOn ? View.GONE:View.VISIBLE);
    }

    private void getProducts() {
        // Get current user's ID from UserSession
        UUID userId = userSession.getCurrentUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        

        productService.getByOwner(userId).enqueue(new Callback<Collection<Product>>() {
                        @Override
                        public void onResponse(Call<Collection<Product>> call, Response<Collection<Product>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                setProducts(response);
                            } else {
                                Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Collection<Product>> call, Throwable t) {
                            Toast.makeText(requireContext(), "Network error loading products", Toast.LENGTH_SHORT).show();
                            Log.e("RetrofitError", "Error: " + t.getMessage());
                            t.printStackTrace();
                        }
                    });
            }


    private void search(String searchItem) {
        searchOn = true;
        changeButtons();
        productService.search(searchItem).enqueue(new Callback<Collection<Product>>() {
            @Override
            public void onResponse(Call<Collection<Product>> call, Response<Collection<Product>> response) {
                setProducts(response);
            }

            @Override
            public void onFailure(Call<Collection<Product>> call, Throwable t) {

            }
        });
    }



    private void filterBtnHandler() {
            FrameLayout filterLayout = productsBinding.filter;
            ViewGroup.LayoutParams params = filterLayout.getLayoutParams();


            params.height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    320,
                    getResources().getDisplayMetrics()
            );

            filterLayout.setLayoutParams(params);

            if (getChildFragmentManager().findFragmentById(productsBinding.filter.getId()) == null) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                String search = productsBinding.search.getText().toString();
                transaction.add(productsBinding.filter.getId(), ProductFilterFragment.newInstance(search));
                transaction.addToBackStack("products");
                transaction.commit();
            }
            searchOn = true;
            changeButtons();

    }

    private void cancelBtnHandler() {
            FrameLayout filterLayout = productsBinding.filter;
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
            getProducts();
    }



    private void addBtnHandler() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(productsBinding.fragmentContent.getId(), ProductFormFragment.newInstance(new Product()));
        transaction.addToBackStack("products");
        transaction.commit();
    }

    @Override
    public void onFilterApplied(Collection<Product> filteredCollection) {
        mProducts.clear();
        mProducts.addAll(filteredCollection);
        adapter = new ProductListAdapter(requireContext(), mProducts, getParentFragmentManager());
        productsBinding.recyclerView.setAdapter(adapter);
        searchOn = true;
        changeButtons();
    }
}
