package msku.ceng.travelogue;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;

public class Settings extends Fragment {
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

        updateLanguageButtonText(languageButton);

        backButton.setOnClickListener(v -> navController.popBackStack());

        languageButton.setOnClickListener(v -> showLanguageDialog());

        darkModeButton.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int newNightMode;
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            AppCompatDelegate.setDefaultNightMode(newNightMode);

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("night_mode", newNightMode);
            editor.apply();
        });
    }

    private void updateLanguageButtonText(Button languageButton) {
        Locale currentLocale = getResources().getConfiguration().getLocales().get(0);
        String currentLanguage = currentLocale.getDisplayLanguage(currentLocale);
        languageButton.setText(Character.toUpperCase(currentLanguage.charAt(0)) + currentLanguage.substring(1));
    }

    private void showLanguageDialog() {
        final String[] languages = {"English", "Türkçe", "Deutsch"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(R.string.settings_language);

        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String langTag;
                switch (which) {
                    case 0:
                        langTag = "en";
                        break;
                    case 1:
                        langTag = "tr";
                        break;
                    case 2:
                        langTag = "de";
                        break;
                    default:
                        return;
                }
                LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langTag);
                AppCompatDelegate.setApplicationLocales(appLocale);
            }
        });
        builder.show();
    }
}
