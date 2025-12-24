package msku.ceng.travelogue;

// MERT SENGUN
public class Goal {

    private String id;
    private String userId;
    private String country;
    private String city;
    private long date;

    public Goal() {
    }

    public Goal(String userId, String country, String city, long date) {
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.date = date;
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
}
