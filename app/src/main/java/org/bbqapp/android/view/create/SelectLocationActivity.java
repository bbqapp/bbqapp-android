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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.view.ViewHelper;

import org.bbqapp.android.R;
import org.bbqapp.android.service.GeocodeService;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.view.BaseActivity;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import timber.log.Timber;

public class SelectLocationActivity extends BaseActivity implements OnMapReadyCallback, LocationSource, ObservableScrollViewCallbacks {

    private static final int GEOCODER_MAX_RESULTS = 5;

    @Bind(R.id.addresses)
    ListView addresses;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.google_map_wrapper)
    View googleMapWrapper;
    GoogleMap googleMap;

    @Inject
    LocationService locationService;
    @Inject
    GeocodeService geocodeService;
    @Inject
    @Named("main")
    Scheduler mainScheduler;
    @Inject
    @Named("io")
    Scheduler ioScheduler;

    private Subscription locationSubscription;
    private Location currentLocation;
    private boolean mapAnimatedAtStart = false;
    private Marker marker;
    private OnLocationChangedListener onLocationChangedListener;
    private Subscription resolveSubscription;
    private LocationListAdapter locationListAdapter;

    public static Intent createIntent(Context context) {
        return new Intent(context, SelectLocationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_location);

        setSupportActionBar(toolbar);
        setTitle(R.string.select_location_title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);

        locationListAdapter = new LocationListAdapter();
        addresses.setAdapter(locationListAdapter);
        addresses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Parcelable address = locationListAdapter.getItem(position);
                Intent data = new Intent();
                data.putExtras(new Bundle());
                data.getExtras().putParcelable("address", address);
                setResult(Activity.RESULT_OK, new Intent());
                finish();
            }
        });
        /*
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        mapWrapperHeight = windowSize.y / 2;
        googleMapWrapper.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mapWrapperHeight));

        scrollView.setScrollViewCallbacks(this);

        final LinearLayout headerView = new LinearLayout(this);
        headerView.setClickable(false);
        headerView.setLayoutParams(
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mapWrapperHeight));
        addresses.addHeaderView(headerView, null, false);*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationSubscription = locationService.getLocation()
                .observeOn(mainScheduler)
                .observeOn(ioScheduler).filter(new Func1<Location, Boolean>() {
                    private Location best;

                    @Override
                    public Boolean call(Location location) {
                        boolean isBetter = location.getTime() >= System.currentTimeMillis() - 15_000;
                        isBetter = isBetter && (best == null || location.getAccuracy() - best.getAccuracy() < -1.0);

                        if (isBetter) {
                            best = location;
                        }
                        return isBetter;
                    }
                })
                .timeout(10, TimeUnit.SECONDS, Observable.<Location>just(null))
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(new Subscriber<Location>() {
                    @Override
                    public void onCompleted() {
                        Timber.w("Location stream was completed unexpectedly");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Could not resolve addresses");
                    }

                    @Override
                    public void onNext(Location location) {
                        if (location == null) {
                            unsubscribe();
                            return;
                        }
                        updateLocation(location);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationSubscription != null) {
            locationSubscription.unsubscribe();
        }

        if (resolveSubscription != null) {
            resolveSubscription.unsubscribe();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setLocationSource(this);
        googleMap.setMyLocationEnabled(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setMarker(latLng);
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            private void addTempLocationInList(Marker marker) {
                locationListAdapter.clear();
                locationListAdapter.add(marker.getPosition());
            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                addTempLocationInList(marker);
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                addTempLocationInList(marker);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                setMarker(marker.getPosition(), true);
            }
        });

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                setMarker(toLatLng(currentLocation));
                return false;
            }
        });
    }

    private void setMarker(LatLng position) {
        setMarker(position, false);
    }

    private void setMarker(LatLng position, boolean force) {
        if (!force && marker != null && marker.getPosition().equals(position)) {
            return;
        }

        if (marker != null) {
            marker.setPosition(position);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.grill_marker);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .position(position)
                    .draggable(true);

            marker = googleMap.addMarker(markerOptions);
        }
        resolve();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
        updateLocation(currentLocation);
    }


    @Override
    public void deactivate() {
        this.onLocationChangedListener = null;
    }

    private void updateLocation(Location location) {
        Location lastLocation = currentLocation;
        currentLocation = location;

        if (location != null && onLocationChangedListener != null) {
            onLocationChangedListener.onLocationChanged(location);
            ifFirstStartZoomInAndSetMarker(location);
            if (marker == null || (lastLocation != null && marker.getPosition().equals(toLatLng(lastLocation)))) {
                setMarker(toLatLng(location));
            }
        }
    }

    private void ifFirstStartZoomInAndSetMarker(Location location) {
        if (!mapAnimatedAtStart) {
            mapAnimatedAtStart = true;
            zoomIn(location);
            setMarker(toLatLng(location));
        }
    }

    private void zoomIn(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        zoomIn(latLng);
    }

    private void zoomIn(LatLng latLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
    }

    private void resolve() {
        if (resolveSubscription != null) {
            resolveSubscription.unsubscribe();
        }

        locationListAdapter.clear();
        locationListAdapter.add(haveSamePosition(marker, currentLocation) ? currentLocation : marker.getPosition());

        resolveSubscription = geocodeService.resolve(marker.getPosition(), GEOCODER_MAX_RESULTS)
                .subscribeOn(ioScheduler)
                .unsubscribeOn(ioScheduler)
                .observeOn(mainScheduler)
                .subscribe(new Subscriber<Address>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Could not resolve addresses");
                    }

                    @Override
                    public void onNext(Address address) {
                        Timber.i("received resolved address %s", address.toString());
                        locationListAdapter.add(address);
                    }
                });
    }

    private static boolean haveSamePosition(Marker marker, Location location) {
        if (marker == null && location == null) {
            return true;
        } else if (marker == null || location == null) {
            return false;
        }
        return marker.getPosition().equals(toLatLng(location));
    }

    private static LatLng toLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private static LatLng toLatLng(Address address) {
        return new LatLng(address.getLatitude(), address.getLongitude());
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        ViewHelper.setTranslationY(googleMapWrapper, -scrollY / 2);
        //ViewHelper.setTranslationY(listBackground, Math.max(0, -scrollY + mapWrapperHeight));
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
}
