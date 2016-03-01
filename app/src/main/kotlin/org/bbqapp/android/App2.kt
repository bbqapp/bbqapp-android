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

package org.bbqapp.android

import android.app.Application
import android.content.Context
import android.location.LocationManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.android.KodeinApplication
import com.github.salomonbrys.kodein.singleton
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import org.bbqapp.android.api.PicassoPictureRequestTransformer
import org.bbqapp.android.api.converter.IdConverterFactory
import org.bbqapp.android.api.converter.LatLngConverterFactory
import org.bbqapp.android.api.converter.LocationConverterFactory
import org.bbqapp.android.api.converter.PictureConverterFactory
import org.bbqapp.android.api.service.PlaceService
import org.bbqapp.android.service.LocationService
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

open class App2 : Application(), KodeinApplication {
    override val kodein = Kodein {

        bind<RefWatcher>() with singleton {
            if (BuildConfig.DEBUG) {
                RefWatcher.DISABLED
            } else {
                LeakCanary.install(this@App2)
            }
        }

        bind<LocationManager>() with singleton { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

        bind<LocationService>() with singleton { LocationService(instance()) }

        bind<OkHttpClient>() with singleton { OkHttpClient.Builder().build() }

        bind<ObjectMapper>() with singleton { ObjectMapper().registerModule(KotlinModule()) }

        bind<Retrofit>() with singleton {
            Retrofit.Builder().
                    client(instance()).
                    addCallAdapterFactory(RxJavaCallAdapterFactory.create()).
                    addConverterFactory(PictureConverterFactory.Companion.create()).
                    addConverterFactory(IdConverterFactory.Companion.create()).
                    addConverterFactory(LocationConverterFactory.Companion.create()).
                    addConverterFactory(LatLngConverterFactory.Companion.create()).
                    addConverterFactory(JacksonConverterFactory.create(instance())).
                    baseUrl(BuildConfig.API_HOST).
                    build();
        }

        bind<PlaceService>() with singleton { instance<Retrofit>().create(PlaceService::class.java) }

        bind<Picasso>() with singleton {
            Picasso.Builder(this@App2).
                    downloader(OkHttp3Downloader(instance<OkHttpClient>())).
                    requestTransformer(PicassoPictureRequestTransformer(BuildConfig.API_URL)).
                    build();
        }
    }
}
