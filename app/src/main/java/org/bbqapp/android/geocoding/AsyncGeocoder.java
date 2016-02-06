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
import android.location.Location;

import rx.Observable;
import rx.functions.Action1;

/**
 * Asynchronous geocoder and reverse geocoder
 */
public final class AsyncGeocoder {
    private static AsyncGeocoder instance;

    private Context context;

    private AsyncGeocoder(Context context) {
        this.context = context.getApplicationContext();
    }

    public static AsyncGeocoder getInstance(Context context) {
        if (instance == null) {
            synchronized (AsyncGeocoder.class) {
                if (instance == null) {
                    instance = new AsyncGeocoder(context);
                }
            }
        }
        return instance;
    }

    public Observable<Address> resolve(Observable<Location> location) {
        final AsyncResolver solver = new AsyncResolver(context);
        location.take(1).subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                solver.setLocation(location);
                solver.execute();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                solver.setError(throwable);
            }
        });

        return solver.getObservable();
    }

    public Observable<Address> resolve(String location) {
        final AsyncResolver solver = new AsyncResolver(context);
        solver.setLocation(location);

        return solver.getObservable();
    }

}
