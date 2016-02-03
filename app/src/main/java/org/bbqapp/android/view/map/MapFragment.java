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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.bbqapp.android.R;
import org.bbqapp.android.api.Callback;
import org.bbqapp.android.api.exception.ApiException;
import org.bbqapp.android.api.model.Picture;
import org.bbqapp.android.api.model.PictureInfo;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.service.Places;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.view.BaseFragment;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, LocationSource, GoogleMap
        .OnMapClickListener, GoogleMap.OnCameraChangeListener, LocationService.OnLocationListener, PlaceClusterManager
        .OnPlaceSelectionListener {
    private static final String TAG = MapFragment.class.getName();

    private GoogleMap map;

    @Bind(R.id.view_detail_image)
    ImageView imageView;
    @Bind(R.id.view_detail_bottom)
    TextView tx;

    private OnLocationChangedListener onLocationChangedListener;

    SlidingUpPanelLayout view;

    @Inject
    Places placesEP;

    @Inject
    LocationService locationService;

    @Inject
    PlaceClusterManager placeClusterManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = (SlidingUpPanelLayout) inflater.inflate(R.layout.view_map, container, false);
            ButterKnife.bind(this, view);

            view.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
            mapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.menu_map);

        locationService.addOnLocationListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        locationService.removeOnLocationListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        view = null;
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = super.getModules();
        modules.add(new MapFragmentModule());
        return modules;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        placeClusterManager.init(map);
        placeClusterManager.setOnPlaceClickListener(this);
        placeClusterManager.setOnCameraChangeListener(this);

        map.setLocationSource(this);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(this);

        Location l = locationService.getLocation();
        if (l != null) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;

        Location l = locationService.getLocation();
        if (l != null) {
            onLocationChangedListener.onLocationChanged(l);
        }
    }

    @Override
    public void deactivate() {
        onLocationChangedListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (onLocationChangedListener != null) {
            onLocationChangedListener.onLocationChanged(location);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        view.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    private long getRadius(LatLngBounds bounds) {
        float[] results = new float[3];
        Location.distanceBetween(bounds.northeast.latitude, bounds.northeast.longitude, bounds.southwest
                .latitude, bounds.southwest.longitude, results);

        return Math.round(results[0] / 1_000);

    }

    private void displayPlaces() {
        LatLng target = map.getCameraPosition().target;
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        long radius = getRadius(bounds);
        displayPlaces(target, radius);
    }

    private void displayPlaces(LatLng target, long radius) {
        Log.i(TAG, "Request places at " + target + " with radius " + radius);

        getProgressbar().setIndeterminate(true);

        placesEP.getPlaces(target.longitude + "," + target.latitude, radius, new Callback<List<Place>>() {

            @Override
            public void onSuccess(List<Place> places) {
                Log.i(TAG, "Received " + places.size() + " places");
                placeClusterManager.setPlaces(places);
                getProgressbar().setIndeterminate(false);
            }

            @Override
            public void onFailure(ApiException cause) {
                Toast.makeText(getActivity(), cause.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setPlace(final Place place) {
        new AsyncTask<Void, Void, BitmapDrawable>() {

            @Override
            protected BitmapDrawable doInBackground(Void... params) {
                List<PictureInfo> picturesInfo = placesEP.getPicturesInfo(place.getId());
                Log.i(TAG, "images found for " + place.getId() + ": " + picturesInfo.size());

                if (!picturesInfo.isEmpty()) {
                    int index = new Random().nextInt(picturesInfo.size());
                    PictureInfo pictureInfo = picturesInfo.get(index);

                    Log.i(TAG, "load image " + pictureInfo.getMeta().getUrl());
                    Picture picture = placesEP.getPicture(pictureInfo.getMeta().getUrl());

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(picture.in());
                        int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                        bitmap = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                        return new BitmapDrawable(getActivity().getResources(), bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, e.getLocalizedMessage(), e);
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(BitmapDrawable picture) {
                Log.i(TAG, "image set for: " + place);
                imageView.setImageDrawable(picture);
            }
        }.execute();
    }

    @Override
    public void onPlaceSelection(Place place) {
        view.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        tx.setText(place.getId());
        setPlace(place);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        displayPlaces();
    }
}
