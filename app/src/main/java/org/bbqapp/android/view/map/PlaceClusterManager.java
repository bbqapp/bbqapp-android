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

package org.bbqapp.android.view.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.bbqapp.android.R;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class PlaceClusterManager {
    interface OnPlaceSelectionListener {
        /**
         * @param place selected place or {@code null} if place was unselected
         */
        void onPlaceSelection(Place place);
    }

    Context context;

    PlaceClusterManager(Context context) {
        this.context = context;
    }

    private PlaceManager manager;

    private GoogleMap map;

    public void init(GoogleMap map) {
        this.map = map;
        manager = new PlaceManager(context, map);
        manager.setRenderer(new PlaceRenderer(context, map, manager));
        manager.setOnClusterClickListener(new ClusterItemClickListener());
        map.setOnCameraChangeListener(manager);
        map.setOnMarkerClickListener(manager);
        map.setOnInfoWindowClickListener(manager);
    }

    public void setOnPlaceClickListener(final OnPlaceSelectionListener listener) {
        manager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<PlaceItem>() {
            @Override
            public boolean onClusterItemClick(PlaceItem placeMarker) {
                listener.onPlaceSelection(placeMarker.getPlace());
                return true;
            }
        });
    }

    public void setOnCameraChangeListener(GoogleMap.OnCameraChangeListener cameraChangeListener) {
        manager.cameraChangeListener = cameraChangeListener;
    }

    public void setPlaces(List<Place> places) {
        ArrayList<PlaceItem> markers = new ArrayList<>(places.size());
        manager.clearItems();

        for (Place place : places) {
            markers.add(new PlaceItem(place));
        }

        manager.addItems(markers);
    }

    private class PlaceManager extends ClusterManager<PlaceItem> {

        private GoogleMap.OnCameraChangeListener cameraChangeListener;

        public PlaceManager(Context context, GoogleMap map) {
            super(context, map);
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            super.onCameraChange(cameraPosition);

            if(cameraChangeListener != null) {
                cameraChangeListener.onCameraChange(cameraPosition);
            }
        }
    }

    private class PlaceRenderer extends DefaultClusterRenderer<PlaceItem> {

        public PlaceRenderer(Context context, GoogleMap map, ClusterManager<PlaceItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(PlaceItem item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.grill_marker);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<PlaceItem> cluster, MarkerOptions markerOptions) {
            String count = String.valueOf(cluster.getSize());

            super.onBeforeClusterRendered(cluster, markerOptions);
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.grill_marker);
            Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            bitmap = bitmap.copy(bitmapConfig, true);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            float scale = context.getResources().getDisplayMetrics().density;
            paint.setColor(Color.rgb(61, 61, 61));
            paint.setTextSize((int) (20 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

            Rect bounds = new Rect();
            paint.getTextBounds(count, 0, count.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width()) / 2;
            int y = (bitmap.getHeight() + bounds.height()) / 2;

            canvas.drawText(count, x, y, paint);

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    private static class PlaceItem implements ClusterItem {
        private Place place;

        public PlaceItem(Place place) {
            this.place = place;
        }

        @Override
        public LatLng getPosition() {
            Point point = place.getLocation().toPoint();
            return new LatLng(point.getLatitude(), point.getLongitude());
        }

        public Place getPlace() {
            return place;
        }
    }

    private class ClusterItemClickListener implements ClusterManager.OnClusterClickListener<PlaceItem> {
        @Override
        public boolean onClusterClick(Cluster<PlaceItem> cluster) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 13));
            return true;
        }
    }
}
