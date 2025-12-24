package msku.ceng.travelogue;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomePage extends Fragment {
    private static final String TAG = "HomePage";
    private DrawerLayout drLout;

    private ImageView profileImageHp, profileImageMenu;
    private TextView nameTextHp, nameTextMenu;
    private TextView thisDayText, upcomingDestinationText, noUpcomingText;
    private TextView daysText, hoursText, minutesText, secondsText;
    private LinearLayout countdownContainer;
    private FrameLayout thisDayTravelContainer;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private HomepageTravelsAdapter homepageTravelsAdapter;
    private RecyclerView homepageTravelsRecycler;
    private CountDownTimer countDownTimer;

    private ActivityResultLauncher<Intent> galleryLauncher;

    public HomePage() {
        super(R.layout.homepage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        drLout = view.findViewById(R.id.drawer_layout);
        profileImageHp = view.findViewById(R.id.image_hp);
        profileImageMenu = view.findViewById(R.id.menu_profile_image);
        nameTextHp = view.findViewById(R.id.hi_name_hp);
        nameTextMenu = view.findViewById(R.id.menu_profile_name);
        thisDayText = view.findViewById(R.id.this_day_text);
        thisDayTravelContainer = view.findViewById(R.id.this_day_travel_container);
        homepageTravelsRecycler = view.findViewById(R.id.homepage_travels_recycler);

        countdownContainer = view.findViewById(R.id.countdown_container);
        upcomingDestinationText = view.findViewById(R.id.upcoming_destination_text);
        daysText = view.findViewById(R.id.days_text);
        hoursText = view.findViewById(R.id.hours_text);
        minutesText = view.findViewById(R.id.minutes_text);
        secondsText = view.findViewById(R.id.seconds_text);
        noUpcomingText = view.findViewById(R.id.no_upcoming_text);

        setupGalleryLauncher();
        loadUserInfo();
        setupNavigation(view);
        setupRecyclerView(view);
        fetchDynamicData();
    }

    private void setupNavigation(View view) {
        NavController navController = Navigation.findNavController(view);

        ImageButton hpMenu = view.findViewById(R.id.menu_btn_hp);
        hpMenu.setOnClickListener(v -> drLout.openDrawer(GravityCompat.END));
        view.<Button>findViewById(R.id.menu_nav_your_travels).setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_yourTravelsFragment));
        view.<Button>findViewById(R.id.menu_nav_goal_travels).setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_goalTravelsFragment));
        view.<Button>findViewById(R.id.menu_nav_where_ive_been).setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_whereivebeen));
        view.<Button>findViewById(R.id.menu_nav_settings).setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_settings));

        TextView seeAllButton = view.findViewById(R.id.see_all_button);
        seeAllButton.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_yourTravelsFragment));
    }

    private void setupRecyclerView(View view) {
        if (currentUser == null) return;

        Query query = db.collection("travels")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(5); // Limit to 5 most recent travels

        FirestoreRecyclerOptions<Travel> options = new FirestoreRecyclerOptions.Builder<Travel>()
                .setQuery(query, Travel.class)
                .build();

        homepageTravelsAdapter = new HomepageTravelsAdapter(options);
        homepageTravelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        homepageTravelsRecycler.setAdapter(homepageTravelsAdapter);

        homepageTravelsAdapter.setOnItemClickListener((documentSnapshot, position) -> {
            String travelId = documentSnapshot.getId();
            HomePageDirections.ActionHomeFragmentToTravelDetail action = HomePageDirections.actionHomeFragmentToTravelDetail(travelId);
            Navigation.findNavController(view).navigate(action);
        });
    }

    private void fetchDynamicData() {
        if (currentUser != null) {
            fetchThisDayLastYear();
            fetchUpcomingGoal();
        }
    }

    private void fetchThisDayLastYear() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        Calendar startCal = (Calendar) cal.clone();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = (Calendar) cal.clone();
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);

        db.collection("travels")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThanOrEqualTo("date", startCal.getTimeInMillis())
                .whereLessThanOrEqualTo("date", endCal.getTimeInMillis())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Travel travel = document.toObject(Travel.class);
                        thisDayText.setText(getString(R.string.homepage_on_this_day_last_year, travel.getCity()));

                        // Inflate the travel item view and add it to the container
                        thisDayTravelContainer.setVisibility(View.VISIBLE);
                        thisDayTravelContainer.removeAllViews();
                        View travelItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_travel, thisDayTravelContainer, false);

                        TextView travelName = travelItemView.findViewById(R.id.item_travel_name);
                        TextView travelLocation = travelItemView.findViewById(R.id.item_travel_location);
                        TextView travelDate = travelItemView.findViewById(R.id.item_travel_date);

                        travelName.setText(travel.getTravelName());
                        travelLocation.setText(String.format("%s, %s", travel.getCountry(), travel.getCity()));
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                        travelDate.setText(sdf.format(new Date(travel.getDate())));

                        thisDayTravelContainer.addView(travelItemView);

                        travelItemView.setOnClickListener(v -> {
                            HomePageDirections.ActionHomeFragmentToTravelDetail action = HomePageDirections.actionHomeFragmentToTravelDetail(document.getId());
                            Navigation.findNavController(requireView()).navigate(action);
                        });

                    } else if (isAdded()) {
                        thisDayText.setText(getString(R.string.homepage_no_travel_last_year));
                        thisDayTravelContainer.setVisibility(View.GONE);
                    }
                });
    }

    private void fetchUpcomingGoal() {
        Date now = new Date();

        db.collection("goals")
                .whereEqualTo("userId", currentUser.getUid())
                .whereGreaterThan("date", now.getTime())
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded() && task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Goal goal = document.toObject(Goal.class);
                        long diffInMillis = goal.getDate() - now.getTime();
                        startCountdown(diffInMillis, goal.getCity());
                    } else if (isAdded()) {
                        showNoUpcomingGoals();
                    }
                });
    }

    private void startCountdown(long duration, String destination) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        noUpcomingText.setVisibility(View.GONE);
        countdownContainer.setVisibility(View.VISIBLE);
        upcomingDestinationText.setVisibility(View.VISIBLE);
        
        upcomingDestinationText.setText(destination + " in");

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                daysText.setText(String.format(Locale.getDefault(), "%dd", days));
                hoursText.setText(String.format(Locale.getDefault(), "%dh", hours));
                minutesText.setText(String.format(Locale.getDefault(), "%dm", minutes));
                secondsText.setText(String.format(Locale.getDefault(), "%ds", seconds));
            }

            @Override
            public void onFinish() {
                if(isAdded()) showNoUpcomingGoals();
            }
        }.start();
    }

    private void showNoUpcomingGoals(){
        noUpcomingText.setVisibility(View.VISIBLE);
        countdownContainer.setVisibility(View.GONE);
        upcomingDestinationText.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (homepageTravelsAdapter != null) {
            homepageTravelsAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (homepageTravelsAdapter != null) {
            homepageTravelsAdapter.stopListening();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void loadUserInfo() {
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                nameTextHp.setText(getString(R.string.hi_user, displayName));
                nameTextMenu.setText(displayName);
            } else {
                nameTextHp.setText(getString(R.string.welcome_greeting));
                nameTextMenu.setText(getString(R.string.default_user_name));
            }

            Uri photoUrl = currentUser.getPhotoUrl();
            if (photoUrl != null) {
                loadProfileImage(photoUrl.toString());
            } else {
                profileImageHp.setImageResource(R.drawable.no_pp);
                profileImageMenu.setImageResource(R.drawable.no_pp);
            }

            if (!isGoogleSignIn()) {
                profileImageHp.setOnClickListener(v -> openGallery());
            }
        }
    }

    private void loadProfileImage(String url) {
        if (getContext() == null) return;
        Glide.with(getContext()).load(url).circleCrop().placeholder(R.drawable.no_pp).into(profileImageHp);
        Glide.with(getContext()).load(url).circleCrop().placeholder(R.drawable.no_pp).into(profileImageMenu);
    }

    private boolean isGoogleSignIn() {
        if (currentUser != null) {
            for (UserInfo userInfo : currentUser.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setupGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference fileRef = storageReference.child("profile_pictures/" + currentUser.getUid() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(this::updateUserProfile);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserProfile(Uri downloadUri) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadUri)
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                        loadProfileImage(downloadUri.toString());
                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "User profile update failed.", task.getException());
                    }
                });
    }
}
