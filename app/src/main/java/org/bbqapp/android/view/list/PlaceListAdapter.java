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

package org.bbqapp.android.view.list;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bbqapp.android.R;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.model.Point;
import org.bbqapp.android.util.AbstractListAdapter;
import org.bbqapp.android.util.DistanceFormatter;

import java.util.Comparator;
import java.util.List;

/**
 * Adapter to render places in a list view
 */
class PlaceListAdapter extends AbstractListAdapter<Place> implements Comparator<Place> {

    private LayoutInflater layoutInflater;
    private List<Place> places;
    private Location location;

    public PlaceListAdapter(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    @Override
    public long getItemId(Place item) {
        return item.getId().hashCode();
    }

    @Override
    public View getView(Place place, View view, ViewGroup parent) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.view_list_place, parent, false);
        }

        TextView addressTextView = (TextView) view.findViewById(R.id.view_list_place_address);
        TextView distanceTextView = (TextView) view.findViewById(R.id.view_list_distance);
        addressTextView.setText(place.getId());


        distanceTextView.setText("Unknown");
        Double distance = getDistance(place);
        if (distance != null) {
            distanceTextView.setText(DistanceFormatter.format(distance));
        }

        return view;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
        sort(this);
        onInvalidated();
    }

    @Override
    public List<Place> getList() {
        return places;
    }

    public void setLocation(Location location) {
        this.location = location;
        sort(this);
        onChanged();
    }

    private Double getDistance(Place place) {
        Point l = place.getLocation().toPoint();
        if (location != null) {
            Location dist = new Location(this.getClass().getSimpleName());
            dist.setLatitude(l.getLatitude());
            dist.setLongitude(l.getLongitude());
            return new Double(location.distanceTo(dist));
        }

        return null;
    }

    @Override
    public int compare(Place lhs, Place rhs) {
        Double dlhs = getDistance(lhs);
        Double drhs = getDistance(rhs);
        if (dlhs == null && drhs == null) {
            return 0;
        } else if (dlhs != null && drhs == null) {
            return 1;
        } else if (dlhs == null && drhs != null) {
            return -1;
        } else {
            return dlhs.compareTo(drhs);
        }
    }
}
