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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.ReplaySubject;

/**
 * Geocoder and reverse geocoder
 */
public final class GeocodeService {
    private static final int RESULTS_SIZE = 1;

    private Context context;

    public GeocodeService(Context context) {
        this.context = context;
    }

    public Observable<Address> resolve(Observable<Location> location) {
        final ReplaySubject<Address> subject = ReplaySubject.createWithSize(RESULTS_SIZE);

        location.subscribe(new Subscriber<Location>() {
            @Override
            public void onCompleted() {
                subject.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subject.onError(e);
            }

            @Override
            public void onNext(Location location) {
                unsubscribe();
                resolve(location).subscribe(new Subscriber<Address>() {
                    @Override
                    public void onCompleted() {
                        subject.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onError(e);
                    }

                    @Override
                    public void onNext(Address address) {
                        subject.onNext(address);
                    }
                });
            }
        });

        return subject;
    }

    public Observable<Address> resolve(Location location) {
        final ReplaySubject<Address> subject = ReplaySubject.createWithSize(RESULTS_SIZE);

        try {
            for (Address address : geocode(location)) {
                subject.onNext(address);
            }
            subject.onCompleted();
        } catch (IOException e) {
            subject.onError(e);
        }

        return subject;
    }

    public Observable<Address> resolve(String location) {
        final ReplaySubject<Address> subject = ReplaySubject.createWithSize(RESULTS_SIZE);

        try {
            for (Address address : geocode(location)) {
                subject.onNext(address);
            }
            subject.onCompleted();
        } catch (IOException e) {
            subject.onError(e);
        }

        return subject;
    }

    private Geocoder createGeocoder() {
        return new Geocoder(context);
    }

    private List<Address> geocode(String location) throws IOException {
        Geocoder geocoder = createGeocoder();
        return geocoder.getFromLocationName(location, RESULTS_SIZE);

    }

    private List<Address> geocode(Location location) throws IOException {
        Geocoder geocoder = createGeocoder();
        return geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), RESULTS_SIZE);
    }

}
