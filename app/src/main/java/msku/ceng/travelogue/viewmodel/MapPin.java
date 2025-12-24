package msku.ceng.travelogue.viewmodel;

import com.google.android.gms.maps.model.LatLng;

// MERT SENGUN
public class MapPin {
    private final String travelId;
    private final LatLng latLng;
    private final String title;

    public MapPin(String travelId, LatLng latLng, String title) {
        this.travelId = travelId;
        this.latLng = latLng;
        this.title = title;
    }

    public String getTravelId() {
        return travelId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }
}
