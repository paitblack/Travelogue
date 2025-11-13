package msku.ceng.travelogue;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HomePage extends Fragment {
    private static final String TAG = "HomePage";
    private DrawerLayout drLout;

    private ImageView profileImageHp, profileImageMenu;
    private TextView nameTextHp, nameTextMenu;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> galleryLauncher;

    public HomePage() {
        super(R.layout.homepage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        drLout = view.findViewById(R.id.drawer_layout);
        profileImageHp = view.findViewById(R.id.image_hp);
        profileImageMenu = view.findViewById(R.id.menu_profile_image);
        nameTextHp = view.findViewById(R.id.hi_name_hp);
        nameTextMenu = view.findViewById(R.id.menu_profile_name);

        setupGalleryLauncher();
        loadUserInfo();

        ImageButton hpMenu = view.findViewById(R.id.menu_btn_hp);
        Button yourTravelsButton = view.findViewById(R.id.menu_nav_your_travels);
        Button goalTravelsButton = view.findViewById(R.id.menu_nav_goal_travels);
        Button whereivebeenButton = view.findViewById(R.id.menu_nav_where_ive_been);
        Button settingsButton = view.findViewById(R.id.menu_nav_settings);

        NavController navController = Navigation.findNavController(view);

        hpMenu.setOnClickListener(v -> drLout.openDrawer(GravityCompat.END));
        yourTravelsButton.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_yourTravelsFragment));
        goalTravelsButton.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_goalTravelsFragment));
        whereivebeenButton.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_whereivebeen));
        settingsButton.setOnClickListener(v -> navController.navigate(R.id.action_homeFragment_to_settings));
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
