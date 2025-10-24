package msku.ceng.travelogue;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class HomePage extends Fragment {
    private DrawerLayout drLout;

    public HomePage(){
        super(R.layout.homepage);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        drLout = view.findViewById(R.id.drawer_layout);

        ImageButton hpMenu = view.findViewById(R.id.menu_btn_hp);
        Button yourTravelsButton = view.findViewById(R.id.menu_nav_your_travels);
        Button goalTravelsButton = view.findViewById(R.id.menu_nav_goal_travels);
        Button whereivebeenButton = view.findViewById(R.id.menu_nav_where_ive_been);
        Button settingsButton = view.findViewById(R.id.menu_nav_settings);

        hpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drLout.openDrawer(GravityCompat.END);
            }
        });

        yourTravelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homeFragment_to_yourTravelsFragment);
            }
        });

        goalTravelsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homeFragment_to_goalTravelsFragment);
            }
        });

        whereivebeenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homeFragment_to_whereivebeen);
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_homeFragment_to_settings);
            }
        });
    }
}
