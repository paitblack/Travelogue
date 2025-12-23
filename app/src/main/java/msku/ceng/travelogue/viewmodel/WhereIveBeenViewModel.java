package msku.ceng.travelogue.viewmodel;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import msku.ceng.travelogue.Travel;

public class WhereIveBeenViewModel extends ViewModel {

    private static final String TAG = "WhereIveBeenViewModel";
    private final MutableLiveData<List<MapPin>> mapPinsLiveData = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authStateListener;
    private Context applicationContext;

    public void init(Context context) {
        this.applicationContext = context.getApplicationContext();
        setupAuthStateListener();
    }

    public LiveData<List<MapPin>> getMapPinsLiveData() {
        return mapPinsLiveData;
    }

    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d(TAG, "User is signed in. Fetching travels.");
                fetchTravelsAndCreatePins(user.getUid());
            } else {
                Log.d(TAG, "User is signed out. Clearing pins.");
                mapPinsLiveData.postValue(new ArrayList<>());
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void fetchTravelsAndCreatePins(String userId) {
        db.collection("travels").whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Travel> travels = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Travel travel = document.toObject(Travel.class);
                            travel.setId(document.getId());
                            travels.add(travel);
                        }
                        Log.d(TAG, "Fetched " + travels.size() + " travels from Firestore.");
                        createMapPinsFromTravels(travels);
                    } else {
                        Log.e(TAG, "Error fetching travels: ", task.getException());
                        mapPinsLiveData.postValue(new ArrayList<>());
                    }
                });
    }

    private void createMapPinsFromTravels(List<Travel> travels) {
        if (applicationContext == null) {
            Log.e(TAG, "Context is null, cannot create map pins.");
            return;
        }
        executorService.execute(() -> {
            Geocoder geocoder = new Geocoder(applicationContext, Locale.getDefault());
            List<MapPin> mapPins = new ArrayList<>();
            for (Travel travel : travels) {
                try {
                    String locationName = travel.getCity() + ", " + travel.getCountry();
                    List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        mapPins.add(new MapPin(travel.getId(), latLng, travel.getCity()));
                    } else {
                        Log.w(TAG, "No address found for location: " + locationName);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoder failed for location: " + travel.getCity() + ", " + travel.getCountry(), e);
                }
            }
            Log.d(TAG, "Created " + mapPins.size() + " map pins.");
            mapPinsLiveData.postValue(mapPins);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        executorService.shutdown();
    }
}
