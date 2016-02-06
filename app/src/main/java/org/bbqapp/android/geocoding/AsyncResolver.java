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

package org.bbqapp.android.geocoding;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;

/**
 * Async geocoder task which is used by {@link AsyncGeocoder}
 */
class AsyncResolver extends AsyncTask<Void, Void, Void> {

    private String locationName;
    private Location location;
    private final int resultsCount = 1;

    private final Observable<Address> observable = createAddressObservable();
    private final Context context;

    private final Set<Subscriber<? super Address>> subscribers = Collections.synchronizedSet(new HashSet<Subscriber<?
            super Address>>());

    private final Object resultsLock = new Object();
    private List<Address> addresses;
    private Throwable cause;

    AsyncResolver(Context context) {
        this.context = context;
    }

    private Observable<Address> createAddressObservable() {
        return Observable.create(new Observable.OnSubscribe<Address>() {
            @Override
            public void call(Subscriber<? super Address> subscriber) {
                boolean hasResult;
                synchronized (resultsLock) {
                    hasResult = addresses != null || cause != null;
                    if (!hasResult) {
                        subscribers.add(subscriber);
                    }
                }

                if (hasResult && addresses != null) {
                    handleAndComplete(addresses, subscriber);
                } else if (hasResult && cause != null) {
                    handleAndComplete(cause, subscriber);
                }
            }
        });
    }

    Observable<Address> getObservable() {
        return observable;
    }

    @Override
    protected Void doInBackground(Void... params) {
        synchronized (resultsLock) {
            if (addresses != null || cause != null) {
                return null;
            }
        }
        try {
            Geocoder geocoder = new Geocoder(context);

            List<Address> addresses;
            if (locationName != null) {
                addresses = geocoder.getFromLocationName(locationName, resultsCount);
            } else {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), resultsCount);
            }

            synchronized (resultsLock) {
                this.addresses = Collections.synchronizedList(addresses);
            }

            for (Subscriber<? super Address> subscriber : subscribers) {
                handleAndComplete(addresses, subscriber);
            }
            subscribers.clear();
        } catch (IllegalArgumentException | IOException cause) {
            setError(cause);
        }
        return null;
    }

    public void setLocation(String location) {
        this.locationName = location;
        this.location = null;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.locationName = null;
    }

    void setError(Throwable throwable) {
        synchronized (resultsLock) {
            this.cause = throwable;
        }

        for (Subscriber<? super Address> subscriber : subscribers) {
            handleAndComplete(cause, subscriber);
        }

        subscribers.clear();
    }

    private void handleAndComplete(List<Address> addresses, Subscriber<? super Address> subscriber) {
        for (Address address : addresses) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(address);
                subscriber.onCompleted();
            }
        }
    }

    private void handleAndComplete(Throwable throwable, Subscriber<? super Address> subscriber) {
        if (!subscriber.isUnsubscribed()) {
            subscriber.onError(throwable);
            subscriber.onCompleted();
        }
    }
}