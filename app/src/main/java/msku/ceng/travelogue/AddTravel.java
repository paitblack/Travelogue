package msku.ceng.travelogue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

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

    private TextView previewTravelName, previewDate, previewLocation;
    private LinearLayout previewContentContainer;

    private static final int CAMERA_REQUEST = 1; //access permissions to gall - cam (any int ok)
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

        TextInputEditText travelNameEditText = view.findViewById(R.id.addTravel_travelName);
        countryAutoComplete = view.findViewById(R.id.addTravel_country);
        cityAutoComplete = view.findViewById(R.id.addTravel_city);
        Button dateButton = view.findViewById(R.id.addTravel_date);

        previewTravelName = view.findViewById(R.id.preview_travel_name);
        previewDate = view.findViewById(R.id.preview_date);
        previewLocation = view.findViewById(R.id.preview_location);
        previewContentContainer = view.findViewById(R.id.preview_content_container);


        travelNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                previewTravelName.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextWatcher locationTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                previewLocation.setText(countryAutoComplete.getText().toString() + ", " + cityAutoComplete.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        countryAutoComplete.addTextChangedListener(locationTextWatcher);
        cityAutoComplete.addTextChangedListener(locationTextWatcher);

        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .build();

            picker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            picker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);
                String dateString = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
                dateButton.setText(dateString);
                previewDate.setText(dateString);
            });
        });

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
                TextView noteView = new TextView(getContext());
                noteView.setText(note);
                noteView.setGravity(Gravity.CENTER_HORIZONTAL);
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.abhaya_libre_semibold);
                noteView.setTypeface(typeface);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 8, 0, 8);
                noteView.setLayoutParams(params);
                previewContentContainer.addView(noteView);
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
                if (options[item].equals(getString(R.string.take_photo))) {
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
            ImageView imageView = new ImageView(getContext());

            int heightInDp = 200;
            final float scale = getResources().getDisplayMetrics().density;
            int heightInPixels = (int) (heightInDp * scale + 0.5f);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    heightInPixels
            );
            params.setMargins(0, 8, 0, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setBackgroundColor(Color.parseColor("#EAEAEA"));

            if (requestCode == CAMERA_REQUEST && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                    previewContentContainer.addView(imageView);
                    Toast.makeText(getContext(), R.string.photo_taken, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                imageView.setImageURI(selectedImage);
                previewContentContainer.addView(imageView);
                Toast.makeText(getContext(), R.string.photo_selected, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
