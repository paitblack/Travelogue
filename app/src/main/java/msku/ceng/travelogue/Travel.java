package msku.ceng.travelogue;

import java.util.List;

// MERT SENGUN
public class Travel {

    private String id;
    private String userId;

    private String travelName;
    private String country;
    private String city;
    private long date;
    private List<String> notes;
    private List<String> photoUrls;

    public Travel() {
    }

    public Travel(String userId, String travelName, String country, String city, long date, List<String> notes, List<String> photoUrls) {
        this.userId = userId;
        this.travelName = travelName;
        this.country = country;
        this.city = city;
        this.date = date;
        this.notes = notes;
        this.photoUrls = photoUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
}
