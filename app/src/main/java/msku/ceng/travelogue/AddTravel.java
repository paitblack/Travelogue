package msku.ceng.travelogue;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

public class AddTravel extends Fragment {
    private AutoCompleteTextView countryAutoComplete;
    private AutoCompleteTextView cityAutoComplete;
    private JSONObject countriesAndCities;
    public AddTravel(){
        super(R.layout.addtravel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.addTravel_back);
        NavController navController = Navigation.findNavController(view);

        backButton.setOnClickListener(v -> navController.popBackStack());

        Button dateButton = view.findViewById(R.id.date_button);
        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .build();

            picker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            picker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);
                dateButton.setText(c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR));
            });
        });

        countryAutoComplete = view.findViewById(R.id.country_auto_complete);
        cityAutoComplete = view.findViewById(R.id.city_auto_complete);

        try {
            InputStream is = getContext().getAssets().open("countries_cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            countriesAndCities = new JSONObject(json);

            Iterator<String> keys = countriesAndCities.keys();
            ArrayList<String> countryList = new ArrayList<>();
            while (keys.hasNext()) {
                countryList.add(keys.next());
            }
            Collections.sort(countryList);

            ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, countryList);
            countryAutoComplete.setAdapter(countryAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

        cityAutoComplete.setEnabled(false);

        countryAutoComplete.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedCountry = (String) parent.getItemAtPosition(position);
            cityAutoComplete.setText("");
            try {
                JSONArray cities = countriesAndCities.getJSONArray(selectedCountry);
                ArrayList<String> cityList = new ArrayList<>();
                for (int i = 0; i < cities.length(); i++) {
                    cityList.add(cities.getString(i));
                }
                Collections.sort(cityList);
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cityList);
                cityAutoComplete.setAdapter(cityAdapter);
                cityAutoComplete.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
