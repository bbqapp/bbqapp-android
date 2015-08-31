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
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;

/**
 * Async geocoder task which is used by {@link AsyncGeocoder}
 */
class AsyncResolver extends AsyncTask<Void, Void, Void> {
    private double lat;
    private double lng;

    private String locationName;

    private Context context;
    private AsyncGeocoder.Callback callback;

    private Address address;
    private Exception cause;

    private AsyncResolver(AsyncGeocoder.Callback callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    AsyncResolver(double lat, double lng, AsyncGeocoder.Callback callback, Context context) {
        this(callback, context);
        this.lat = lat;
        this.lng = lng;
    }

    AsyncResolver(String locationName, AsyncGeocoder.Callback callback, Context context) {
        this(callback, context);
        this.locationName = locationName;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            Geocoder geocoder = new Geocoder(context);

            List<Address> addresses;
            if (locationName != null) {
                addresses = geocoder.getFromLocationName(locationName, 1);
            } else {
                addresses = geocoder.getFromLocation(lat, lng, 1);
            }

            if (!addresses.isEmpty()) {
                address = addresses.get(0);
            }
        } catch (IllegalArgumentException cause) {
            cause = cause;
        } catch (IOException cause) {
            this.cause = cause;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (cause != null) {
            callback.onFailure(cause);
        } else {
            callback.onSuccess(address);
        }
    }
}