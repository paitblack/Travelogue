package msku.ceng.travelogue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class AddTravel extends Fragment {
    private AutoCompleteTextView countryAutoComplete;
    private AutoCompleteTextView cityAutoComplete;
    private JSONObject countriesAndCities;

    private TextView previewTravelName, previewDate, previewLocation;
    private LinearLayout previewContentContainer;

    private static final String TAG = "AddTravel";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private long selectedDateInMillis = 0;
    private ArrayList<String> notesForFirebase = new ArrayList<>();
    private ArrayList<Uri> photoUrisForUpload = new ArrayList<>();

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraImageUri;

    public AddTravel(){
        super(R.layout.addtravel);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupLaunchers();

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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { previewTravelName.setText(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextWatcher locationTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { previewLocation.setText(countryAutoComplete.getText().toString() + ", " + cityAutoComplete.getText().toString()); }
            @Override public void afterTextChanged(Editable s) {}
        };

        countryAutoComplete.addTextChangedListener(locationTextWatcher);
        cityAutoComplete.addTextChangedListener(locationTextWatcher);

        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().build();
            picker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDateInMillis = selection;
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
            while (keys.hasNext()) { countryList.add(keys.next()); }
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
                for (int i = 0; i < cities.length(); i++) { cityList.add(cities.getString(i)); }
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
                notesForFirebase.add(note);
                TextView noteView = new TextView(getContext());
                noteView.setText(note);
                noteView.setGravity(Gravity.CENTER_HORIZONTAL);
                Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.abhaya_libre_semibold);
                noteView.setTypeface(typeface);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    cameraImageUri = createImageUri();
                    if (cameraImageUri != null) {
                        cameraLauncher.launch(cameraImageUri);
                    }
                } else if (options[item].equals(getString(R.string.choose_from_gallery))) {
                    Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(pickPhotoIntent);
                } else if (options[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            });
            builder.show();
        });

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            String travelName = travelNameEditText.getText().toString().trim();
            String country = countryAutoComplete.getText().toString().trim();
            String city = cityAutoComplete.getText().toString().trim();

            if (TextUtils.isEmpty(travelName) || TextUtils.isEmpty(country) || TextUtils.isEmpty(city) || selectedDateInMillis == 0) {
                Toast.makeText(getContext(), "Please fill all fields and select a date.", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPhotosAndSaveTravel(travelName, country, city, navController);
        });
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            photoUrisForUpload.add(imageUri);
                            addPhotoToPreview(imageUri);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        if (cameraImageUri != null) {
                            photoUrisForUpload.add(cameraImageUri);
                            addPhotoToPreview(cameraImageUri);
                        }
                    }
                });
    }

    private Uri createImageUri() {
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imageFile != null) {
            return FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", imageFile);
        }
        return null;
    }

    private void addPhotoToPreview(Uri imageUri) {
        ImageView imageView = new ImageView(getContext());
        int heightInDp = 200;
        final float scale = getResources().getDisplayMetrics().density;
        int heightInPixels = (int) (heightInDp * scale + 0.5f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
        params.setMargins(0, 8, 0, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(Color.parseColor("#EAEAEA"));
        imageView.setImageURI(imageUri);
        previewContentContainer.addView(imageView);
        Toast.makeText(getContext(), R.string.photo_selected, Toast.LENGTH_SHORT).show();
    }

    private void uploadPhotosAndSaveTravel(String travelName, String country, String city, NavController navController) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        if (photoUrisForUpload.isEmpty()) {
            saveTravelDocument(userId, travelName, country, city, new ArrayList<>(), navController);
            return;
        }

        Toast.makeText(getContext(), "Uploading photos...", Toast.LENGTH_SHORT).show();
        List<String> downloadUrls = new ArrayList<>();
        final int totalPhotos = photoUrisForUpload.size();

        for (Uri photoUri : photoUrisForUpload) {
            StorageReference fileRef = storageReference.child("travel_photos/" + userId + "/" + UUID.randomUUID().toString());
            fileRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    downloadUrls.add(uri.toString());
                    if (downloadUrls.size() == totalPhotos) {
                        saveTravelDocument(userId, travelName, country, city, downloadUrls, navController);
                    }
                }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Photo upload failed: ", e);
                    Toast.makeText(getContext(), "A photo failed to upload.", Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void saveTravelDocument(String userId, String travelName, String country, String city, List<String> photoUrls, NavController navController) {
        Travel newTravel = new Travel(userId, travelName, country, city, selectedDateInMillis, notesForFirebase, photoUrls);

        db.collection("travels")
            .add(newTravel)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Travel document added with ID: " + documentReference.getId());
                Toast.makeText(getContext(), "Travel Added Successfully!", Toast.LENGTH_LONG).show();
                navController.popBackStack(); // Go back to the previous screen (YourTravels)
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error adding document", e);
                Toast.makeText(getContext(), "Error adding travel.", Toast.LENGTH_SHORT).show();
            });
    }
}
