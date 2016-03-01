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

import android.app.Application;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;
import de.halfbit.tinybus.TinyBus;
import org.bbqapp.android.api.service.PlaceService;
import org.bbqapp.android.service.GeocodeService;
import org.bbqapp.android.service.LocationService;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import javax.inject.Named;
import javax.inject.Singleton;

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
    Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    LocationService provideLocationService() {
        return app.getKodein().getJava().instance(LocationService.class);
    }

    @Provides
    @Singleton
    GeocodeService provideGeocodeService(Application context) {
        return new GeocodeService(context);
    }

    @Provides
    @Singleton
    RefWatcher provideRefWatcher(Application application) {
        return app.getKodein().getJava().instance(RefWatcher.class);
    }

    @Provides
    @Singleton
    TinyBus provideTinyBus(Application context) {
        return new TinyBus(context);
    }

    @Provides
    @Singleton
    Picasso providePicasso(Application application) {
        return app.getKodein().getJava().instance(Picasso.class);
    }

    @Provides
    @Singleton
    PlaceService providePlaceService(Application application) {
        return app.getKodein().getJava().instance(PlaceService.class);
    }

    @Provides
    @Named("main")
    Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named("io")
    Scheduler provideIoScheduler() {
        return Schedulers.io();
    }
}
