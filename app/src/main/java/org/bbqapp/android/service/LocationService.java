/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Location service to retrieve geo coordinates of current device location
 */
public final class LocationService implements LocationListener {
    private static LocationService instance;

    private LocationManager locationManager;

    private Location lastLocation;

    private Set<OnLocationListener> onLocationListeners = new HashSet<>();

    private LocationService(LocationManager locationManager) {
        this.locationManager = locationManager;

        // set last location
        lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation == null) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastLocation == null) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
    }

    /**
     * Returns {@link LocationService}
     *
     * @param context application context
     * @return instance of {@link LocationService}
     */
    public static LocationService getService(Context context) {
        return getService((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));
    }

    /**
     * Returns {@link LocationService}
     *
     * @param locationManager location manager
     * @return instance of {@link LocationService}
     */
    public static LocationService getService(LocationManager locationManager) {
        if (instance == null) {
            synchronized (LocationService.class) {
                if (instance == null) {
                    instance = new LocationService(locationManager);
                }
            }
        }

        return instance;
    }

    private LocationService() {
    }

    /**
     * Returns last known position or {@code null}
     *
     * @return last known position
     */
    public Location getLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        for (OnLocationListener listener : onLocationListeners) {
            listener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    protected Criteria getProviderCriteria() {
        Criteria criteria = new Criteria();
        criteria.setBearingRequired(true);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);

        return criteria;
    }

    /**
     * Checks if location service is enabled and listeners ({@link LocationService.OnLocationListener}) are added
     *
     * @return {@code true} if service is enabled and at lest listener exists otherwise {@code false}
     */
    public boolean isEnabled() {
        return !onLocationListeners.isEmpty();
    }

    protected void enableOrDisable() {
        if (isEnabled()) {
            locationManager.requestLocationUpdates(0, 0, getProviderCriteria(), this, null);
        } else {
            locationManager.removeUpdates(this);
        }
    }

    /**
     * Adds listener to notify about location changes
     *
     * @param listener listener to add
     */
    public void addOnLocationListener(OnLocationListener listener) {
        onLocationListeners.add(listener);
        enableOrDisable();
    }

    /**
     * Removes added listener
     *
     * @param listener listener to remove
     */
    public void removeOnLocationListener(OnLocationListener listener) {
        onLocationListeners.remove(listener);
        enableOrDisable();
    }

    /**
     * Location change listener
     */
    public interface OnLocationListener {
        /**
         * Gets called when location has been changed
         *
         * @param location new location
         */
        void onLocationChanged(Location location);
    }
}
