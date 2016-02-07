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

import rx.Observable;
import rx.functions.Action0;
import rx.subjects.ReplaySubject;
import timber.log.Timber;

/**
 * Location service to retrieve geo coordinates of current device location
 */
public final class LocationService implements LocationListener {

    private LocationManager locationManager;

    private Location lastLocation;

    private ReplaySubject<Location> subject = ReplaySubject.createWithSize(1);
    private Observable<Location> observable = subject.doOnSubscribe(new Action0() {
        @Override
        public void call() {
            LocationService.this.onSubscriptionOrOnUnsubscription(true);
        }
    }).doOnUnsubscribe(new Action0() {
        @Override
        public void call() {
            LocationService.this.onSubscriptionOrOnUnsubscription(false);
        }
    });

    private boolean subscribed = false;
    private long subscribers = 0;

    public LocationService(LocationManager locationManager) {

        this.locationManager = locationManager;
    }

    public Observable<Location> getLocation() {
        return observable;
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

        subject.onNext(location);
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

    private synchronized void subscribe() {
        try {
            locationManager.requestLocationUpdates(0, 0, getProviderCriteria(), this, null);
            subscribed = true;


            if (subject.getValue() == null) {
                subject.onNext(getBestLastLocation());
            }
        } catch (SecurityException e) {
            subject.onError(e);
        }
    }

    private synchronized void unsubscribe() {
        try {
            locationManager.removeUpdates(this);
            subscribed = false;
        } catch (SecurityException e) {
            subject.onError(e);
        }
    }

    private synchronized void onSubscriptionOrOnUnsubscription(boolean onSubscription) {
        subscribers = subscribers + (onSubscription ? 1 : -1);

        boolean hasObservers = subscribers > 0;
        if (hasObservers && !subscribed) {
            subscribe();
        } else if (!hasObservers && subscribed) {
            unsubscribe();
        }
    }
}
