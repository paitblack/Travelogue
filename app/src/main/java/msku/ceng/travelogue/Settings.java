package msku.ceng.travelogue;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

// SULEYMAN EMRE PARLAK
public class Settings extends Fragment {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    public Settings() {
        super(R.layout.settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.settings_back);
        NavController navController = Navigation.findNavController(view);
        Button logoutButton = view.findViewById(R.id.settings_logout);
        Button languageButton = view.findViewById(R.id.settings_language);
        Button darkModeButton = view.findViewById(R.id.settings_dark);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        updateLanguageButtonText(languageButton);

        backButton.setOnClickListener(v -> navController.popBackStack());

        logoutButton.setOnClickListener(v -> signOut(navController));

        languageButton.setOnClickListener(this::showLanguageMenu);

        darkModeButton.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int newNightMode;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            AppCompatDelegate.setDefaultNightMode(newNightMode);

            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("night_mode", newNightMode);
            editor.apply();
        });
    }

    private void signOut(NavController navController) {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            navController.navigate(R.id.action_global_logout);
        });
    }

    private void updateLanguageButtonText(Button languageButton) {
        Locale currentLocale = getResources().getConfiguration().getLocales().get(0);
        String currentLanguage = currentLocale.getDisplayLanguage(currentLocale);
        languageButton.setText(Character.toUpperCase(currentLanguage.charAt(0)) + currentLanguage.substring(1));
    }

    private void showLanguageMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v, Gravity.CENTER);
        popup.getMenuInflater().inflate(R.menu.language_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            String langTag;
            int itemId = item.getItemId();
            if (itemId == R.id.lang_english) {
                langTag = "en";
            } else if (itemId == R.id.lang_turkish) {
                langTag = "tr";
            } else if (itemId == R.id.lang_german) {
                langTag = "de";
            } else {
                return false;
            }
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langTag);
            AppCompatDelegate.setApplicationLocales(appLocale);
            return true;
        });
        /// to relocate the pop-up (now its at the bottom center of the button - done)
        v.post(() -> {
            try {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                int xOffset = v.getWidth()/8;
                int yOffset = v.getHeight()/8;

                java.lang.reflect.Field mPopup = popup.getClass().getDeclaredField("mPopup");
                mPopup.setAccessible(true);
                Object menuPopupHelper = mPopup.get(popup);
                java.lang.reflect.Method showMethod = menuPopupHelper.getClass()
                        .getMethod("show", int.class, int.class);
                showMethod.invoke(menuPopupHelper, xOffset, yOffset);
            } catch (Exception e) {
                popup.show();
            }
        });
    }

}
