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

package org.bbqapp.android.util;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * Abstract {@link ListAdapter} implementation
 *
 * @param <T> type of objects to render
 */
public abstract class AbstractListAdapter<T> implements ListAdapter {

    private static final String TAG = AbstractListAdapter.class.getName();

    private Collection<DataSetObserver> observers = new HashSet<>();

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public final boolean isEnabled(int position) {
        return isEnabled(getItem(position));
    }

    /**
     * Checks if item is enabled
     *
     * @param item item to check
     * @return {@code true} if item is enabled, otherwise {@code false}
     */
    public boolean isEnabled(T item) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observers.remove(observer);
    }

    /**
     * Gets called when all data invalidated
     */
    protected void onInvalidated() {
        for (DataSetObserver observer : observers) {
            try {
                observer.onInvalidated();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    /**
     * Get calls when data has been changed
     */
    protected void onChanged() {
        for (DataSetObserver observer : observers) {
            try {
                observer.onChanged();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    }

    @Override
    public int getCount() {
        List<T> list = getList();
        return list == null ? 0 : list.size();
    }

    @Override
    public final T getItem(int position) {
        List<T> list = getList();
        return list != null ? list.get(position) : null;
    }

    @Override
    public final long getItemId(int position) {
        return getItemId(getItem(position));
    }

    /**
     * Returns item id. The id should not be changed if {@link #hasStableIds} is {@code true}
     *
     * @param item returns id if this item
     * @return item id
     */
    public long getItemId(T item) {
        return item.hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public final int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    /**
     * Returns item view type. Must be in range 0 to {@link #getViewTypeCount()}
     *
     * @param item item
     * @return type of the item
     */
    public int getItemViewType(T item) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        return getView(getItem(position), convertView, parent);
    }

    /**
     * Get the view which display items data
     *
     * @param item        item to display
     * @param convertView old view to reuse
     * @param parent      parent view
     * @return new or reused view
     */
    public abstract View getView(T item, View convertView, ViewGroup parent);

    @Override
    public boolean isEmpty() {
        List<T> list = getList();
        return list == null || list.isEmpty();
    }

    /**
     * Sorts list using item comparator
     *
     * @param comparator comparator to use for sorting
     */
    public void sort(Comparator<T> comparator) {
        if (!isEmpty()) {
            Collections.sort(getList(), comparator);
        }
    }

    /**
     * Sorts list in ascending natural order
     */
    public void sort() {
        if (!isEmpty()) {
            Collections.sort(getAsComparableList());
        }
    }

    private <S extends Comparable<? super S>> List<S> getAsComparableList() {
        return (List<S>) getList();
    }

    /**
     * List of objects to render
     *
     * @return objects to render
     */
    public abstract List<T> getList();
}
