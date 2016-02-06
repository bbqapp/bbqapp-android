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

package org.bbqapp.android;

import android.content.Context;
import android.location.LocationManager;

import org.bbqapp.android.api.converter.LatLngConverterFactory;
import org.bbqapp.android.api.converter.LocationConverterFactory;
import org.bbqapp.android.api.service.PlaceService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.halfbit.tinybus.TinyBus;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Application module to hold global states
 */
@Module(library = true)
public class AppModule {
    private final App app;

    AppModule(App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return app;
    }

    @Provides
    @Singleton
    LocationManager provideLocationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    TinyBus provideTinyBus(Context context) {
        return new TinyBus(context);
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(LocationConverterFactory.create())
                .addConverterFactory(LatLngConverterFactory.create())
                .baseUrl("http://bbqapp.org")
                .build();
    }

    @Provides
    @Singleton
    PlaceService providePlaces2(Retrofit retrofit) {
        return retrofit.create(PlaceService.class);
    }

    @Provides
    Scheduler provideScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
