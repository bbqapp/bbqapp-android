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

package org.bbqaap.android.extension

import android.location.Address
import android.location.Location
import org.bbqapp.android.extension.getLatLng
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.bbqapp.android.api.model.Location as ApiLocation

class LocationUtilsTest {
    @Test fun testLocationToLatLng() {
        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(12.65)
        `when`(location.longitude).thenReturn(98.12)

        val latLng = location.getLatLng()

        assertEquals(latLng.latitude, 12.65, 0.001)
        assertEquals(latLng.longitude, 98.12, 0.001)
    }

    @Test fun testApiLocationToLatLng() {
        var location = ApiLocation(listOf(95.23, 12.54), "Point")

        var latLng = location.getLatLng()

        assertEquals(latLng.latitude, 12.54, 0.001)
        assertEquals(latLng.longitude, 95.23, 0.001)
    }

    @Test fun testAddressToLatLng() {
        var address = mock(Address::class.java)
        `when`(address.latitude).thenReturn(14.98)
        `when`(address.longitude).thenReturn(89.21)

        var latLng = address.getLatLng()

        assertEquals(latLng.latitude, 14.98, 0.001)
        assertEquals(latLng.longitude, 89.21, 0.001)
    }
}