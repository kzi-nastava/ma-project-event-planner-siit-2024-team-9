package com.example.eventify;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventify.databinding.FragmentSolutionListBinding;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SolutionListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SolutionListFragment extends Fragment {

    private ServiceListAdapter adapter;
    private static final String ARG_PARAM = "param";
    private ArrayList<Service> mProducts;
    private FragmentSolutionListBinding binding;

    public static SolutionListFragment newInstance(ArrayList<Service> products) {
        SolutionListFragment fragment = new SolutionListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM, products);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ShopApp", "onCreate Products List Fragment");

        // Retrieve the products list from arguments
        if (getArguments() != null) {
            mProducts = getArguments().getParcelableArrayList(ARG_PARAM);
            adapter = new ServiceListAdapter(getActivity(), mProducts, getParentFragmentManager());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout with View Binding
        binding = FragmentSolutionListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView with adapter
        if (mProducts != null) {
            binding.recyclerView.setAdapter(adapter);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Avoid memory leaks
    }
}

