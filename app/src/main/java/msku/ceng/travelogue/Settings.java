package msku.ceng.travelogue;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;

//TODOS: logout ayarlanacak.

public class Settings extends Fragment {
    public Settings(){
        super(R.layout.settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.settings_back);
        NavController navController = Navigation.findNavController(view);
        Button logoutButton = view.findViewById(R.id.settings_logout);
        Button languageButton = view.findViewById(R.id.settings_language);

        updateLanguageButtonText(languageButton);

        backButton.setOnClickListener(v -> navController.popBackStack());

        languageButton.setOnClickListener(v -> showLanguageDialog());
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
                switch (which) {
                    case 0:
                        setLocale("en");
                        break;
                    case 1:
                        setLocale("tr");
                        break;
                    case 2:
                        setLocale("de");
                        break;
                }
            }
        });
        builder.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        if (getActivity() != null) {
            getActivity().recreate();
        }
    }
}
