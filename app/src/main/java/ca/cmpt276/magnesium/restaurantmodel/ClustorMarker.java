package ca.cmpt276.magnesium.restaurantmodel;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClustorMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String hazardLevel;
    private String address;
    private String snippet;
    private int iconPicture;

    public ClustorMarker(LatLng position, String title, String hazardLevel, String address, int iconPicture) {
        this.position = position;
        this.title = title;
        this.hazardLevel = hazardLevel;
        this.address = address;
        this.iconPicture = iconPicture;
        snippet = "Address: " + address + "\n" + "Hazard Level: " + hazardLevel.toString();
    }

    public ClustorMarker(){
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHazardLevel() {
        return hazardLevel;
    }

    public void setHazardLevel(String hazardLevel) {
        this.hazardLevel = hazardLevel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public void setIconPicture(int iconPicture) {
        this.iconPicture = iconPicture;
    }
}

