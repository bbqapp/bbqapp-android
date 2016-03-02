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

package org.bbqapp.android.view.create

import android.content.Context
import android.location.Address
import android.location.Location
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.location_view.view.*
import org.bbqapp.android.R
import org.bbqapp.android.util.AbstractListAdapter
import java.util.*

class LocationListAdapter : AbstractListAdapter<Parcelable>() {
    private val locations = Collections.synchronizedList(ArrayList<Parcelable>())

    override fun getView(item: Parcelable, convertView: View?, parent: ViewGroup): View {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(R.layout.location_view, parent, false)
        val textView = view.location_text

        textView.text = when (item) {
            is Address -> toString(item)
            is LatLng -> item.toString()
            is Location -> item.toString()
            else -> item.toString()
        }
        return textView
    }

    override fun getList(): List<Parcelable> {
        return locations
    }

    fun set(items: List<Parcelable>?) {
        locations.clear()
        items?.let {
            locations.addAll(items)
        }

        onInvalidated()
    }

    fun set(vararg items: Parcelable) {
        set(items.asList())
    }

    fun add(vararg items: Parcelable) {
        add(items.asList())
    }

    fun add(items: List<Parcelable>?) {
        items?.let { locations.addAll(items) }

        onInvalidated()
    }

    fun clear() {
        locations.clear()

        onInvalidated()
    }

    fun toString(address: Address): String {
        val maxIndex = if (address.countryCode == Locale.getDefault().country) address.maxAddressLineIndex - 1 else address.maxAddressLineIndex
        val sb = StringBuilder()
        for (index in 0..maxIndex) {
            sb.append(address.getAddressLine(index))
            if (index != maxIndex)
                sb.append("\n")
        }
        return sb.toString()
    }
}
