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

package org.bbqapp.android.view.create;

import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bbqapp.android.util.AbstractListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationListAdapter extends AbstractListAdapter<Parcelable> {
    private List<Parcelable> locations = Collections.synchronizedList(new ArrayList<Parcelable>());

    @Override
    public View getView(Parcelable item, View convertView, ViewGroup parent) {
        TextView textView = new TextView(parent.getContext());
        textView.setText(item.toString());
        return textView;
    }

    @Override
    public List<Parcelable> getList() {
        return locations;
    }

    public void setLocations(List<Parcelable> locations) {
        if (locations == null) {
            this.locations.clear();
        } else {
            this.locations.addAll(locations);
        }

        onInvalidated();
    }

    public void add(Parcelable object) {
        locations.add(object);

        onInvalidated();
    }

    public void clear() {
        locations.clear();

        onInvalidated();
    }
}
