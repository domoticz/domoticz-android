package nl.hnogames.domoticz.Containers;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

@SuppressWarnings("unused")
public class LocationInfo {
    String Name;
    LatLng Location;
    int id = 0;
    int switchidx = 0;
    int range = 120;//meters
    boolean enabled = false;


    public LocationInfo(int i, String n, LatLng l, int radius) {
        this.Name = n;
        this.Location = l;
        this.id = i;
    }


    public String getName() {return Name;}
    public boolean getEnabled() {return enabled;}
    public int getID() {return id;}
    public int getSwitchidx() {return switchidx;}
    public int getRange() {return range;}
    public void setSwitchidx(int idx) {switchidx = idx;}
    public void setEnabled(boolean e) {enabled = e;}
    public void setRange(int e) {range = e;}
    public LatLng getLocation() {return Location;}


    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(String.valueOf(id))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(Location.latitude, Location.longitude, range)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}