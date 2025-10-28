package msku.ceng.travelogue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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

    private static final int CAMERA_REQUEST = 1; //request -> camera and gall - any int
    private static final int GALLERY_REQUEST = 2;


    public AddTravel(){
        super(R.layout.addtravel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.addTravel_back);
        NavController navController = Navigation.findNavController(view);

        backButton.setOnClickListener(v -> navController.popBackStack());

        Button dateButton = view.findViewById(R.id.addTravel_date);
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

        countryAutoComplete = view.findViewById(R.id.addTravel_country);
        cityAutoComplete = view.findViewById(R.id.addTravel_city);

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

        Button addNote = view.findViewById(R.id.addTravel_addNote);
        Button addPhoto = view.findViewById(R.id.addTravel_addPhoto);

        addNote.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.add_a_note);

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            builder.setView(input);

            builder.setPositiveButton(R.string.add, (dialog, which) -> {
                String note = input.getText().toString();
                Toast.makeText(getContext(), R.string.note_added, Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

            builder.show();
        });

        addPhoto.setOnClickListener(v -> {
            final CharSequence[] options = {getString(R.string.take_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.add_photo);
            builder.setItems(options, (dialog, item) -> {
                if (options[item].equals(getString(R.string.take_photo))) {   // TODO : add permission to open camera if needed ??? (manifest)
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST);
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhotoIntent, GALLERY_REQUEST);
                } else if (options[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    // TODO: You have the photo as a Bitmap. You can display it in an ImageView.
                    Toast.makeText(getContext(), R.string.photo_taken, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                // TODO: You have the photo's URI. You can display it in an ImageView.
                Toast.makeText(getContext(), R.string.photo_selected, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
