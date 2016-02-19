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

package org.bbqapp.android.view;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.view.LayoutInflater;

import org.bbqapp.android.AppModule;
import org.bbqapp.android.auth.Facebook;
import org.bbqapp.android.auth.GooglePlus;
import org.bbqapp.android.service.GeocodeService;
import org.bbqapp.android.service.LocationService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.halfbit.tinybus.TinyBus;

/**
 * Module for all activities and for all its fragments
 */
@Module(
        addsTo = AppModule.class,
        injects = MainActivity.class,
        library = true
)
public class ActivityModule {
    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @Singleton
    Activity provideActivity() {
        return activity;
    }

    @Provides
    @Singleton
    LayoutInflater provideLayoutInflater(Activity context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @Singleton
    LocationService provideLocationService(LocationManager locationManager) {
        return new LocationService(locationManager);
    }

    @Provides
    @Singleton
    GeocodeService provideAsyncGeocoder(Activity context) {
        return new GeocodeService(context);
    }

    @Provides
    @Singleton
    GooglePlus provideGooglePlus(Activity activity) {
        return new GooglePlus(activity);
    }

    @Provides
    @Singleton
    Facebook provideFacebook(Context context) {
        return new Facebook(context);
    }

    @Provides
    @Singleton
    LoginManager provideLoginManager(Context context, TinyBus bus, GooglePlus plus, Facebook facebook) {
        return new LoginManager(context, bus, plus, facebook);
    }
}
