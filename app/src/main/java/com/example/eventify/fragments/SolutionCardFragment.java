package com.example.eventify.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eventify.models.solutions.Solution;
import com.example.eventify.R;

public class SolutionCardFragment extends Fragment {

    private static final String ARG_SOLUTION = "solution";

    private Solution solution;

    public SolutionCardFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment using the provided Solution object.
     *
     * @param solution The solution object to display in the card.
     * @return A new instance of fragment SolutionCardFragment.
     */
    public static SolutionCardFragment newInstance(Solution solution) {
        SolutionCardFragment fragment = new SolutionCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SOLUTION, solution);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            solution = getArguments().getParcelable(ARG_SOLUTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_solution_card, container, false);

        ImageView solutionImage = view.findViewById(R.id.solution_image);
        TextView solutionName = view.findViewById(R.id.solution_name);
        TextView solutionPrice = view.findViewById(R.id.solution_price);
        TextView solutionDiscount = view.findViewById(R.id.solution_discount);

        if (solution != null) {
            solutionName.setText(solution.getName());
            solutionPrice.setText("Price: $" + solution.getPrice());
            solutionDiscount.setText("Discount: " + solution.getDiscount() + "%");

            // Postavi hardkodovanu sliku iz drawable
            solutionImage.setImageResource(R.drawable.dummy_event_image);
        }

        return view;
    }
}
