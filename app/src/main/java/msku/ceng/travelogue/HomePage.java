package msku.ceng.travelogue;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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

        hpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drLout.openDrawer(GravityCompat.END);
            }
        });


        //TODO: AÇILAN MENÜDEKİ DİĞER NAVİGATİONLAR YAPILACAK.

    }
}
