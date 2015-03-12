package camera.mah.com.camera;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Muhamet Ademi on 2015-03-12.
 *
 * @class: GpsLocationListener.java
 * @author: Muhamet Ademi
 * @desc: Location listener which listens for changes in location
 */
public class GpsLocationListener implements LocationListener
{
    private double latitude = 0.0;
    private double longitude = 0.0;

    public void onLocationChanged(Location location) {
        // Update the values
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean hasFoundLocation()
    {
        if(latitude != 0.0 && longitude != 0.0)
            return true;

        return false;
    }
}