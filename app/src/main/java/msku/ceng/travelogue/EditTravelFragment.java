package msku.ceng.travelogue;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.concurrent.atomic.AtomicInteger;

//SULEYMAN EMRE PARLAK
public class EditTravelFragment extends Fragment {

    private String travelId;
    private Travel currentTravel;

    private TextInputEditText travelNameEditText;
    private AutoCompleteTextView countryAutoComplete;
    private AutoCompleteTextView cityAutoComplete;
    private Button dateButton;
    private TextView previewTravelName, previewDate, previewLocation;
    private LinearLayout previewContentContainer;

    private JSONObject countriesAndCities;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private long selectedDateInMillis = 0;
    private final ArrayList<String> notesForFirebase = new ArrayList<>();
    private final ArrayList<Object> photosForFirebase = new ArrayList<>();

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraImageUri;

    public EditTravelFragment() {
        super(R.layout.addtravel);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travelId = getArguments().getString("travelId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.addtravel, container, false);

        TextView title = view.findViewById(R.id.toolbar_title_add);
        title.setText(R.string.title_edit_travel);
        Button doneButton = view.findViewById(R.id.add_button);
        doneButton.setText(R.string.done_button);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        setupLaunchers();
        initializeViews(view);
        loadJson();
        setupListeners(view);

        fetchTravelData();
    }

    private void initializeViews(View view) {
        travelNameEditText = view.findViewById(R.id.addTravel_travelName);
        countryAutoComplete = view.findViewById(R.id.addTravel_country);
        cityAutoComplete = view.findViewById(R.id.addTravel_city);
        dateButton = view.findViewById(R.id.addTravel_date);
        previewTravelName = view.findViewById(R.id.preview_travel_name);
        previewDate = view.findViewById(R.id.preview_date);
        previewLocation = view.findViewById(R.id.preview_location);
        previewContentContainer = view.findViewById(R.id.preview_content_container);
    }

    private void setupListeners(View view) {
        NavController navController = Navigation.findNavController(view);
        ImageButton backButton = view.findViewById(R.id.addTravel_back);
        backButton.setOnClickListener(v -> navController.popBackStack());

        Button doneButton = view.findViewById(R.id.add_button);
        doneButton.setOnClickListener(v -> saveChanges(navController));

        travelNameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { previewTravelName.setText(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        TextWatcher locationTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { previewLocation.setText(String.format("%s, %s", countryAutoComplete.getText().toString(), cityAutoComplete.getText().toString())); }
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

        view.findViewById(R.id.addTravel_addNote).setOnClickListener(v -> showAddNoteDialog());
        view.findViewById(R.id.addTravel_addPhoto).setOnClickListener(v -> showAddPhotoDialog());
    }

    private void fetchTravelData() {
        db.collection("travels").document(travelId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentTravel = documentSnapshot.toObject(Travel.class);
                if (currentTravel != null) {
                    populateUI();
                }
            }
        });
    }

    private void populateUI() {
        travelNameEditText.setText(currentTravel.getTravelName());
        countryAutoComplete.setText(currentTravel.getCountry(), false);
        cityAutoComplete.setText(currentTravel.getCity(), false);
        cityAutoComplete.setEnabled(true);

        selectedDateInMillis = currentTravel.getDate();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selectedDateInMillis);
        String dateString = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
        dateButton.setText(dateString);
        previewDate.setText(dateString);

        if (currentTravel.getNotes() != null) {
            notesForFirebase.addAll(currentTravel.getNotes());
        }
        if (currentTravel.getPhotoUrls() != null) {
            photosForFirebase.addAll(currentTravel.getPhotoUrls());
        }

        rebuildPreview();
    }

    private void rebuildPreview() {
        previewContentContainer.removeAllViews();
        for (String note : new ArrayList<>(notesForFirebase)) {
            addNoteToPreview(note);
        }
        for (Object photo : new ArrayList<>(photosForFirebase)) {
            addPhotoToPreview(photo);
        }
    }

    private void addNoteToPreview(final String note) {
        View noteView = createDeletableItem(note, v -> {
            notesForFirebase.remove(note);
            rebuildPreview();
        });
        previewContentContainer.addView(noteView);
    }

    private void addPhotoToPreview(final Object photo) {
        View photoView = createDeletableItem(photo, v -> {
            photosForFirebase.remove(photo);
            rebuildPreview();
        });
        previewContentContainer.addView(photoView);
    }

    private View createDeletableItem(Object item, View.OnClickListener deleteAction) {
        FrameLayout itemView = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.deletable_item, previewContentContainer, false);

        ImageButton deleteButton = itemView.findViewById(R.id.deletable_item_delete_button);
        deleteButton.setOnClickListener(deleteAction);

        if (item instanceof String && !(item.toString().startsWith("http"))) {
            TextView textView = itemView.findViewById(R.id.deletable_item_text);
            textView.setText((String) item);
            textView.setVisibility(View.VISIBLE);
        } else {
            ImageView imageView = itemView.findViewById(R.id.deletable_item_image);
            imageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(item).into(imageView);
        }

        return itemView;
    }

    private void saveChanges(NavController navController) {
        ArrayList<String> finalNotes = new ArrayList<>(notesForFirebase);
        ArrayList<String> finalPhotoUrls = new ArrayList<>();
        ArrayList<Uri> newPhotosToUpload = new ArrayList<>();

        for (Object photo : photosForFirebase) {
            if (photo instanceof String) {
                finalPhotoUrls.add((String) photo);
            } else if (photo instanceof Uri) {
                newPhotosToUpload.add((Uri) photo);
            }
        }

        if (currentTravel.getPhotoUrls() != null) {
            List<String> originalPhotoUrls = new ArrayList<>(currentTravel.getPhotoUrls());
            originalPhotoUrls.removeAll(finalPhotoUrls);
            for (String urlToDelete : originalPhotoUrls) {
                if (urlToDelete != null && !urlToDelete.isEmpty()){
                    FirebaseStorage.getInstance().getReferenceFromUrl(urlToDelete).delete();
                }
            }
        }

        if (!newPhotosToUpload.isEmpty()) {
            final int totalNewPhotos = newPhotosToUpload.size();
            final AtomicInteger uploadedCount = new AtomicInteger(0);
            for (Uri photoUri : newPhotosToUpload) {
                StorageReference fileRef = storageReference.child("travel_photos/" + mAuth.getCurrentUser().getUid() + "/" + UUID.randomUUID().toString());
                fileRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        finalPhotoUrls.add(uri.toString());
                        if (uploadedCount.incrementAndGet() == totalNewPhotos) {
                            updateFirestoreDocument(finalNotes, finalPhotoUrls, navController);
                        }
                    }))
                    .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.photo_upload_failed, Toast.LENGTH_SHORT).show());
            }
        } else {
            updateFirestoreDocument(finalNotes, finalPhotoUrls, navController);
        }
    }

    private void updateFirestoreDocument(List<String> notes, List<String> photoUrls, NavController navController) {
        String travelName = travelNameEditText.getText().toString();
        String country = countryAutoComplete.getText().toString();
        String city = cityAutoComplete.getText().toString();

        db.collection("travels").document(travelId)
                .update("travelName", travelName,
                        "country", country,
                        "city", city,
                        "date", selectedDateInMillis,
                        "notes", notes,
                        "photoUrls", photoUrls)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), R.string.travel_updated, Toast.LENGTH_SHORT).show();
                    navController.popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), R.string.travel_update_failed, Toast.LENGTH_SHORT).show());
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_a_note);
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        builder.setView(input);
        builder.setPositiveButton(R.string.add, (dialog, which) -> {
            String note = input.getText().toString();
            if (!TextUtils.isEmpty(note)) {
                notesForFirebase.add(note);
                rebuildPreview();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddPhotoDialog() {
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
    }

    private void setupLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            photosForFirebase.add(imageUri);
                            rebuildPreview();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        if (cameraImageUri != null) {
                            photosForFirebase.add(cameraImageUri);
                            rebuildPreview();
                        }
                    }
                });
    }

    private Uri createImageUri() {
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) { e.printStackTrace(); }

        if (imageFile != null) {
            return FileProvider.getUriForFile(requireContext(), requireContext().getApplicationContext().getPackageName() + ".provider", imageFile);
        }
        return null;
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
