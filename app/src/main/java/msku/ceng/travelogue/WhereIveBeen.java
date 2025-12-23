package msku.ceng.travelogue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import msku.ceng.travelogue.viewmodel.MapPin;
import msku.ceng.travelogue.viewmodel.WhereIveBeenViewModel;

public class WhereIveBeen extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private NavController navController;
    private WhereIveBeenViewModel viewModel;

    public WhereIveBeen() {
        super(R.layout.whereivebeen);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(WhereIveBeenViewModel.class);
        viewModel.init(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.whereivebeen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        ImageButton backButton = view.findViewById(R.id.whereivebeen_back);
        backButton.setOnClickListener(v -> navController.popBackStack());

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.setOnMarkerClickListener(this);
        observeMapPins();
    }

    private void observeMapPins() {
        viewModel.getMapPinsLiveData().observe(getViewLifecycleOwner(), mapPins -> {
            if (googleMap != null && mapPins != null) {
                googleMap.clear();
                if (!mapPins.isEmpty()) {
                    LatLng firstLatLng = null;
                    BitmapDescriptor pinIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.pin);
                    for (MapPin pin : mapPins) {
                        if (firstLatLng == null) {
                            firstLatLng = pin.getLatLng();
                        }
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(pin.getLatLng()).title(pin.getTitle()).icon(pinIcon));
                        marker.setTag(pin.getTravelId());
                    }
                    if (firstLatLng != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 5));
                    }
                }
            }
        });
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String travelId = (String) marker.getTag();
        if (travelId != null) {
            Bundle bundle = new Bundle();
            bundle.putString("travelId", travelId);
            navController.navigate(R.id.action_whereivebeen_to_travelDetail, bundle);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (googleMap != null) {
            googleMap.clear();
            googleMap = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }
}
