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

import android.location.Location;
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
import com.squareup.picasso.Picasso;

import org.bbqapp.android.R;
import org.bbqapp.android.api.model.PictureInfo;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.api.service.PlaceService;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.view.BaseFragment;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, LocationSource,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener, PlaceClusterManager.OnPlaceSelectionListener {
    private static final String TAG = MapFragment.class.getName();

    private GoogleMap map;
    private Subscription locationSubscription;
    private Location lastLocation;
    private boolean mapAnimated = false;

    @Bind(R.id.view_detail_image)
    ImageView imageView;
    @Bind(R.id.view_detail_bottom)
    TextView tx;

    private OnLocationChangedListener onLocationChangedListener;

    SlidingUpPanelLayout view;

    @Inject
    PlaceService placeService;
    @Inject
    @Named("main")
    Scheduler mainScheduler;
    @Inject
    @Named("io")
    Scheduler ioScheduler;
    @Inject
    LocationService locationService;
    @Inject
    Picasso picasso;
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

        locationSubscription = locationService.getLocation().subscribe(new Action1<Location>() {
            @Override
            public void call(Location location) {
                onLocationChanged(location);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        locationSubscription.unsubscribe();
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

        onLocationChanged(lastLocation);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        onLocationChangedListener = null;
    }

    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (onLocationChangedListener != null && location != null) {
            onLocationChangedListener.onLocationChanged(location);
        }

        if (location != null && !mapAnimated && map != null) {
            mapAnimated = true;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
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

        return Math.max((long) Math.ceil(results[0] / 1_000d), 1L);

    }

    private void displayPlaces() {
        LatLng target = map.getCameraPosition().target;
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        long radius = getRadius(bounds);
        displayPlaces(target, radius);
    }

    private void displayPlaces(final LatLng target, final long radius) {
        Log.i(TAG, "Request places at " + target + " with radius " + radius);

        getProgressbar().setIndeterminate(true);

        placeService.getPlaces(target, radius)
                .subscribeOn(Schedulers.io())
                .observeOn(mainScheduler)
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Place>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Could not retrieve places for %s with radius %d", target.toString(), radius);
                        Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(List<Place> places) {
                        Log.i(TAG, "Received " + places.size() + " places");
                        placeClusterManager.setPlaces(places);
                        getProgressbar().setIndeterminate(false);
                    }
                });
    }

    public void setPlace(final Place place) {
        placeService.getPicturesInfo(place)
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(new Subscriber<List<PictureInfo>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Could not retrieve pictures information");
                    }

                    @Override
                    public void onNext(List<PictureInfo> picturesInfo) {
                        if (!picturesInfo.isEmpty()) {
                            int index = new Random().nextInt(picturesInfo.size());
                            PictureInfo pictureInfo = picturesInfo.get(index);

                            picasso.load(pictureInfo.getMeta().getUrl())
                                    .fit()
                                    .centerCrop()
                                    .into(imageView);
                        }
                    }
                });
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
