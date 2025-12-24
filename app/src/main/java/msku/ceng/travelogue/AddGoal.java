package msku.ceng.travelogue;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

// MERT SENGUN
public class AddGoal extends Fragment {

    private AutoCompleteTextView countryAutoComplete;
    private AutoCompleteTextView cityAutoComplete;
    private JSONObject countriesAndCities;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private long selectedDateInMillis = 0;

    public AddGoal() {
        super(R.layout.addgoal);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = view.findViewById(R.id.addGoal_back);
        NavController navController = Navigation.findNavController(view);
        backButton.setOnClickListener(v -> navController.popBackStack());

        countryAutoComplete = view.findViewById(R.id.addGoal_country);
        cityAutoComplete = view.findViewById(R.id.addGoal_city);
        Button dateButton = view.findViewById(R.id.addGoal_date);

        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDateInMillis = selection;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);
                String dateString = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
                dateButton.setText(dateString);
            });
        });

        loadJson();

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            String country = countryAutoComplete.getText().toString().trim();
            String city = cityAutoComplete.getText().toString().trim();

            if (TextUtils.isEmpty(country) || TextUtils.isEmpty(city) || selectedDateInMillis == 0) {
                Toast.makeText(getContext(), "Please fill all fields and select a date.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveGoal(country, city);
        });
    }

    private void saveGoal(String country, String city) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        Goal newGoal = new Goal(userId, country, city, selectedDateInMillis);

        db.collection("goals")
                .add(newGoal)
                .addOnSuccessListener(requireActivity(), documentReference -> {
                    if (isAdded()) {
                        Bundle result = new Bundle();
                        result.putBoolean("goal_added_success", true);
                        getParentFragmentManager().setFragmentResult("add_goal_result", result);

                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext().getApplicationContext(), "Error adding goal.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadJson(){
        try {
            InputStream is = requireContext().getAssets().open("countries_cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            countriesAndCities = new JSONObject(json);

            Iterator<String> keys = countriesAndCities.keys();
            ArrayList<String> countryList = new ArrayList<>();
            while (keys.hasNext()) { countryList.add(keys.next()); }
            Collections.sort(countryList);

            ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, countryList);
            countryAutoComplete.setAdapter(countryAdapter);

            countryAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
                String selectedCountry = (String) parent.getItemAtPosition(position);
                cityAutoComplete.setText("");
                try {
                    JSONArray cities = countriesAndCities.getJSONArray(selectedCountry);
                    ArrayList<String> cityList = new ArrayList<>();
                    for (int i = 0; i < cities.length(); i++) { cityList.add(cities.getString(i)); }
                    Collections.sort(cityList);
                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, cityList);
                    cityAutoComplete.setAdapter(cityAdapter);
                    cityAutoComplete.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
