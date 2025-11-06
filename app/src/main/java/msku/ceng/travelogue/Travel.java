package msku.ceng.travelogue;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "travel_table")
public class Travel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private  String travelName;
    private String country;
    private String city;
    private long date;
    private List<String> notes;
    private List<String> photoUris;

    public Travel(String travelName, String country, String city, long date, List<String> notes, List<String> photoUris) {
        this.travelName = travelName;
        this.country = country;
        this.city = city;
        this.date = date;
        this.notes = notes;
        this.photoUris = photoUris;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTravelName() {
        return travelName;
    }

    public void setTravelName(String travelName) {
        this.travelName = travelName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public List<String> getPhotoUris() {
        return photoUris;
    }

    public void setPhotoUris(List<String> photoUris) {
        this.photoUris = photoUris;
    }
}
