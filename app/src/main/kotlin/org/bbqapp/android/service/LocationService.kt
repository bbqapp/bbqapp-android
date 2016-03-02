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

import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import rx.subjects.ReplaySubject
import timber.log.Timber

class LocationService(private val locationManager: LocationManager) {

    private var lastLocation: Location? = null

    private val subject = ReplaySubject.createWithSize<Location>(1)
    val location = subject.doOnSubscribe { subscribe() }.doOnUnsubscribe { unsubscribe() }.share()

    private val listener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLocation = location
            subject.onNext(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
        }
    }

    protected fun getBestLastLocation(): Location? {
        return if (lastLocation != null) lastLocation else getLastKnownLocation()
    }

    @Throws(SecurityException::class)
    protected fun getLastKnownLocation(): Location? {
        var location = getLastLocation(LocationManager.GPS_PROVIDER)

        if (location == null) {
            location = getLastLocation(LocationManager.NETWORK_PROVIDER)
        }
        if (location == null) {
            location = getLastLocation(LocationManager.PASSIVE_PROVIDER)
        }

        return location
    }

    @Throws(SecurityException::class)
    protected fun getLastLocation(provider: String): Location? {
        try {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Could not get last known exception of provider $provider")
            return null
        }

    }

    protected fun provideCriteria(): Criteria {
        val criteria = Criteria()
        //criteria.isBearingRequired = true
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.powerRequirement = Criteria.POWER_MEDIUM

        return criteria
    }

    @Synchronized private fun subscribe() {
        try {
            locationManager.requestLocationUpdates(0, 0f, provideCriteria(), listener, Looper.getMainLooper())

            if (subject.value == null) {
                val bestLastLocation = getBestLastLocation()
                bestLastLocation?.let { subject.onNext(it) }
            }
        } catch (e: SecurityException) {
            subject.onError(e)
        }
    }

    @Synchronized private fun unsubscribe() {
        try {
            locationManager.removeUpdates(listener)
        } catch (e: SecurityException) {
            subject.onError(e)
        }
    }
}
