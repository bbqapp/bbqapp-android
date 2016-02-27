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

package org.bbqapp.android.view.list

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.bbqapp.android.R
import org.bbqapp.android.api.model.Place
import org.bbqapp.android.extension.getLatLng
import org.bbqapp.android.util.AbstractListAdapter
import org.bbqapp.android.util.DistanceFormatter
import java.util.*
import kotlin.comparisons.compareValues

class PlaceListAdapter(private val layoutInflater: LayoutInflater) : AbstractListAdapter<Place>(), Comparator<Place> {
    private var places: List<Place>? = null
    private var location: Location? = null

    override fun getItemId(item: Place): Long {
        return item.id!!.hashCode().toLong()
    }

    override fun getView(place: Place, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: layoutInflater.inflate(R.layout.view_list_place, parent, false);

        val addressTextView = view.findViewById(R.id.view_list_place_address) as TextView
        val distanceTextView = view.findViewById(R.id.view_list_distance) as TextView
        addressTextView.text = place.id


        distanceTextView.text = "Unknown"
        val distance = getDistance(place)
        distance?.let {
            distanceTextView.text = DistanceFormatter.format(distance.toDouble())
        }

        return view
    }

    fun setPlaces(places: List<Place>) {
        this.places = places
        sort(this)
        onInvalidated()
    }

    override fun getList() = places

    fun setLocation(location: Location) {
        this.location = location
        sort(this)
        onChanged()
    }

    private fun getDistance(place: Place): Float? {
        return location?.let {
            val latLng = place.location.getLatLng()
            val dist = Location(this.javaClass.simpleName)
            dist.latitude = latLng.latitude
            dist.longitude = latLng.longitude
            return location!!.distanceTo(dist)
        }
    }

    override fun compare(lhs: Place, rhs: Place) = compareValues(getDistance(lhs), getDistance(rhs));
}