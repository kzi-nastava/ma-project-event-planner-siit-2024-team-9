package com.example.eventify.Helpers;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.eventify.R;

import java.util.ArrayList;

public class ComponentsSetup {

    public static Spinner spinnerOtherSetup(View view, int id, ArrayList<String> options, Context context, TextView newProposal) {

        Spinner spinner = view.findViewById(id);

        // Create an ArrayAdapter using the dynamic list and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.spinner, options);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the Spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == options.size() - 1) {  // "Other" is selected
                    newProposal.setVisibility(View.VISIBLE); // Show the input field to propose a new category
                } else {
                    newProposal.setVisibility(View.GONE); // Hide it if another category is selected
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }});

        return spinner;

    }

    public static Spinner spinnerSetup (View view, int id, ArrayList<String> options, Context context) {

        Spinner spinner = view.findViewById(id);

        // Create an ArrayAdapter using the dynamic list and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                R.layout.spinner, options);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the Spinner
        spinner.setAdapter(adapter);

        return spinner;

    }


}
