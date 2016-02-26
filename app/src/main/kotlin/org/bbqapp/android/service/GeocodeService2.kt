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

package org.bbqapp.android.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import org.bbqapp.android.extension.getLatLng
import rx.Observable
import rx.subjects.ReplaySubject
import java.io.IOException

class GeocodeService2(val context: Context) {
    private val DEFAULT_MAX_RESULTS = 1

    fun resolve(position: LatLng) = resolve(position, DEFAULT_MAX_RESULTS)
    fun resolve(location: String) = resolve(location, DEFAULT_MAX_RESULTS)
    fun resolve(location: Location) = resolve(location.getLatLng(), DEFAULT_MAX_RESULTS)
    fun resolve(position: LatLng, maxResults: Int) = resolve(maxResults, { results: Int -> geocode(position, results) })
    fun resolve(location: String, maxResults: Int) = resolve(maxResults, { results: Int -> geocode(location, results) })
    private fun resolve(maxResults: Int, geocodeFun: (Int) -> List<Address>): Observable<Address> {
        val subject = ReplaySubject.createWithSize<Address>(maxResults)

        try {
            geocodeFun(maxResults).forEach({ subject.onNext(it) })
            subject.onCompleted()
        } catch (e: IOException) {
            subject.onError(e)
        }

        return subject
    }

    private fun createGeocoder() = Geocoder(context)

    @Throws(IOException::class)
    private fun geocode(location: String, maxResults: Int)
            = createGeocoder().getFromLocationName(location, maxResults)

    @Throws(IOException::class)
    private fun geocode(location: LatLng, maxResults: Int)
            = createGeocoder().getFromLocation(location.latitude, location.longitude, maxResults)
}