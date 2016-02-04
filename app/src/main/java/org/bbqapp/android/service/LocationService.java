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

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Location service to retrieve geo coordinates of current device location
 */
public final class LocationService implements LocationListener {
    private static LocationService instance;

    private LocationManager locationManager;

    private boolean requestedUpdates = false;

    private Location lastLocation;

    private Set<Subscriber<? super Location>> subscribers =
            Collections.synchronizedSet(new HashSet<Subscriber<? super Location>>());
    private Observable<Location> observable = Observable.create(new Observable.OnSubscribe<Location>() {
        @Override
        public void call(Subscriber<? super Location> subscriber) {
            onSubscription(subscriber);
        }
    });

    private LocationService(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    private void onSubscription(Subscriber<? super Location> subscriber) {
        try {
            Location location = getBestLastLocation();
            subscribers.add(subscriber);

            if (location != null) {
                subscriber.onNext(location);
            }

            enableOrDisable();
        } catch (SecurityException e) {
            subscriber.onError(e);
        }
    }

    public Observable<Location> getLocation() {
        return observable;
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

    protected Location getBestLastLocation() {
        return lastLocation != null ? lastLocation : getLastKnownLocation();
    }

    protected Location getLastKnownLocation() throws SecurityException {
        Location location = getLastLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = getLastLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            location = getLastLocation(LocationManager.PASSIVE_PROVIDER);
        }

        return location;
    }

    protected Location getLastLocation(String provider) throws SecurityException {
        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Could not get last known exception of provider %s", provider);
            return null;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        try {
            enableOrDisable();
            for (Subscriber<? super Location> subscriber : subscribers) {
                subscriber.onNext(location);
            }
        } catch (SecurityException e) {
            for (Subscriber<? super Location> subscriber : subscribers) {
                subscriber.onError(e);
            }
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

    protected void enableOrDisable() throws SecurityException {
        boolean enabled = clearSubscribers();

        if (enabled && !requestedUpdates) {
            locationManager.requestLocationUpdates(0, 0, getProviderCriteria(), this, null);
        } else if (!enabled && requestedUpdates) {
            locationManager.removeUpdates(this);
        }
        requestedUpdates = enabled;
    }

    private boolean clearSubscribers() {
        boolean enabled = false;

        Set<Subscriber<? super Location>> unsubscribedSubscribers = new HashSet<>();
        for (Subscriber<? super Location> subscriber : subscribers) {
            boolean unsubscribed = subscriber.isUnsubscribed();
            if (unsubscribed) {
                unsubscribedSubscribers.add(subscriber);
            }

            enabled = enabled || !unsubscribed;
        }
        subscribers.removeAll(unsubscribedSubscribers);

        return enabled;
    }
}
