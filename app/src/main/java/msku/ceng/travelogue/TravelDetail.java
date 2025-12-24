package msku.ceng.travelogue;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// SULEYMAN EMRE PARLAK
public class TravelDetail extends Fragment {

    private FirebaseFirestore db;
    private String travelId;
    private View rootView;
    private LinearLayout detailContentContainer;
    private View cardToSave;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    saveCardAsImage();
                } else {
                    Toast.makeText(getContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
    );

    public TravelDetail() {
        super(R.layout.fragment_travel_detail);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            travelId = getArguments().getString("travelId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar topAppBar = view.findViewById(R.id.topAppBar);
        NavController navController = Navigation.findNavController(view);
        topAppBar.setNavigationOnClickListener(v -> navController.popBackStack());

        detailContentContainer = view.findViewById(R.id.detail_content_container);
        cardToSave = view.findViewById(R.id.detail_card_to_save);
        Button saveButton = view.findViewById(R.id.save_as_photo_button);

        fetchTravelDetails();

        saveButton.setOnClickListener(v -> checkPermissionAndSave());
    }

    private void fetchTravelDetails() {
        if (travelId == null) return;

        db.collection("travels").document(travelId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Travel travel = documentSnapshot.toObject(Travel.class);
                if (travel != null) {
                    populateViews(travel);
                }
            }
        });
    }

    private void populateViews(Travel travel) {
        TextView travelName = rootView.findViewById(R.id.detail_travel_name);
        TextView date = rootView.findViewById(R.id.detail_date);
        TextView location = rootView.findViewById(R.id.detail_location);
        MaterialToolbar topAppBar = rootView.findViewById(R.id.topAppBar);

        travelName.setText(travel.getTravelName());
        topAppBar.setTitle(travel.getTravelName());
        location.setText(travel.getCountry() + ", " + travel.getCity());

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(travel.getDate());
        String dateString = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
        date.setText(dateString);

        int staticViewCount = 3;
        if (detailContentContainer.getChildCount() > staticViewCount) {
            detailContentContainer.removeViews(staticViewCount, detailContentContainer.getChildCount() - staticViewCount);
        }

        List<String> notes = travel.getNotes();
        List<String> photoUrls = travel.getPhotoUrls();
        if (notes != null) {
            for (String note : notes) {
                TextView noteView = createTextView(note);
                detailContentContainer.addView(noteView);
            }
        }

        if (photoUrls != null) {
            for (String url : photoUrls) {
                ImageView imageView = createImageView();
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.rounded_top_card_background)
                        .error(android.R.drawable.stat_notify_error)
                        .into(imageView);
                detailContentContainer.addView(imageView);
            }
        }
    }

    private TextView createTextView(String text) {
        TextView noteView = new TextView(getContext());
        noteView.setText(text);
        noteView.setGravity(Gravity.CENTER_HORIZONTAL);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.abhaya_libre_semibold);
        noteView.setTypeface(typeface);
        noteView.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        noteView.setLayoutParams(params);
        return noteView;
    }

    private ImageView createImageView() {
        ImageView imageView = new ImageView(getContext());
        int heightInDp = 200;
        final float scale = getResources().getDisplayMetrics().density;
        int heightInPixels = (int) (heightInDp * scale + 0.5f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
        params.setMargins(0, 8, 0, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    private void checkPermissionAndSave() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveCardAsImage(); // No permission needed for SDK 29+
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveCardAsImage();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void saveCardAsImage() {
        Bitmap bitmap = Bitmap.createBitmap(cardToSave.getWidth(), cardToSave.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        cardToSave.draw(canvas);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Travelogue_" + timeStamp + ".jpg";

        OutputStream fos;
        Uri imageUri;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Travelogue");

                imageUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContext().getContentResolver().openOutputStream(imageUri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Travelogue";
                File dir = new File(imagesDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File imageFile = new File(imagesDir, fileName);
                fos = new java.io.FileOutputStream(imageFile);
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(getContext(), R.string.saved_to_gallery, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), R.string.failed_to_save, Toast.LENGTH_SHORT).show();
        }
    }
}
