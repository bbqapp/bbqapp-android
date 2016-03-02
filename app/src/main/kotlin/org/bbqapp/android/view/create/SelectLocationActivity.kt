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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks
import com.github.ksoichiro.android.observablescrollview.ScrollState
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.activity_select_location.*
import kotlinx.android.synthetic.main.toolbar.*
import org.bbqapp.android.R
import org.bbqapp.android.extension.*
import org.bbqapp.android.service.LocationService
import org.bbqapp.android.service.resolve
import org.bbqapp.android.view.BaseActivity
import rx.Observable
import rx.Subscription
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SelectLocationActivity : BaseActivity(), OnMapReadyCallback, LocationSource, ObservableScrollViewCallbacks {

    internal lateinit var googleMap: GoogleMap

    internal val locationService: LocationService by injector.instance()

    private var currentLocation: Location? = null
    private var mapAnimatedAtStart = false
    private var marker: Marker? = null
    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null
    private var resolveSubscription: Subscription? = null
    private lateinit var locationListAdapter: LocationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_select_location)

        setSupportActionBar(toolbar)
        setTitle(R.string.select_location_title)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        locationListAdapter = LocationListAdapter()
        addresses.adapter = locationListAdapter
        addresses.setOnItemClickListener { parent, view, position, id ->
            finish(locationListAdapter.getItem(position) as Address)
        }

        addresses.setScrollViewCallbacks(this)
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

    override fun onResume() {
        super.onResume()

        locationService.location.
                observeOnMainThread().
                observeOnIoThread().
                filter { it.time >= System.currentTimeMillis() - 15000 }.
                filterBest { best, next -> next.accuracy - best.accuracy < -1.0 }.
                timeout(10, TimeUnit.SECONDS, Observable.just<Location>(null)).
                filter { location -> location != null }.
                subscribeOnIoThread().
                unsubscribeOnIoThread().
                observeOnMainThread().
                bindToLifecycle(this).
                subscribe(
                        { updateLocation(it) },
                        { Timber.w(it, "Location stream was completed unexpectedly") })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_select_location_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            if (item.itemId == R.id.search_address) {
                val intent = SearchAddressActivity.createIntent(this)
                startActivityForResult(intent, SEARCH_ADDRESS_REQUEST_CODE)
                true
            } else {
                super.onOptionsItemSelected(item)
            }


    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        googleMap.setLocationSource(this)
        googleMap.isMyLocationEnabled = true

        googleMap.setOnMapClickListener { setMarker(it) }

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            private fun addTempLocationInList(marker: Marker) {
                resolveSubscription?.unsubscribe()
                locationListAdapter.set(marker.position)
            }

            override fun onMarkerDragStart(marker: Marker) {
                addTempLocationInList(marker)
            }

            override fun onMarkerDrag(marker: Marker) {
                addTempLocationInList(marker)
            }

            override fun onMarkerDragEnd(marker: Marker) {
                resolveSubscription?.unsubscribe()
                setMarker(marker.position, true)
            }
        })

        googleMap.setOnMyLocationButtonClickListener {
            currentLocation?.getLatLng()?.let { setMarker(it) }
            false
        }
    }

    private fun setMarker(position: LatLng, force: Boolean = false) {
        if (!force && marker?.position == position) {
            return
        }

        marker?.position = position
        if (marker == null) {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.grill_marker)
            val markerOptions = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap)).position(position).draggable(true)

            marker = googleMap.addMarker(markerOptions)
        }
        resolve()
    }

    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener
        currentLocation?.let { updateLocation(it) }
    }


    override fun deactivate() {
        this.onLocationChangedListener = null
    }

    private fun updateLocation(location: Location) {
        val lastLocation = currentLocation
        currentLocation = location

        onLocationChangedListener?.let {
            it.onLocationChanged(location)
            ifFirstStartZoomInAndSetMarker(location)
            if (marker == null || (lastLocation != null && marker?.position == lastLocation.getLatLng())) {
                setMarker(location.getLatLng())
            }
        }
    }

    private fun ifFirstStartZoomInAndSetMarker(location: Location) {
        if (!mapAnimatedAtStart) {
            mapAnimatedAtStart = true
            zoomIn(location)
            setMarker(location.getLatLng())
        }
    }

    private fun zoomIn(location: Location) {
        zoomIn(location.getLatLng())
    }

    private fun zoomIn(latLng: LatLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
    }

    private fun resolve() {
        resolveSubscription?.unsubscribe()

        if (marker == null || currentLocation == null) {
            return
        }

        locationListAdapter.set(if (marker?.position == currentLocation?.getLatLng()) currentLocation!! else marker!!.position)

        resolveSubscription = Geocoder(this).
                resolve(marker!!.position, GEOCODER_MAX_RESULTS).
                observeOnMainThread().
                bindToLifecycle(this).
                subscribe(
                        { locationListAdapter.add(it) },
                        { Timber.e(it, "Could not resolve addresses") })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SEARCH_ADDRESS_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                data?.extras?.getParcelable<Address>("address")?.let {
                    finish(it)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun finish(address: Address) {
        val data = Intent()
        data.putExtra("address", address)
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    override fun onScrollChanged(scrollY: Int, firstScroll: Boolean, dragging: Boolean) {
        //ViewHelper.setTranslationY(googleMapWrapper, -scrollY / 2);
        //ViewHelper.setY(addresses, googleMapWrapper.getHeight() - scrollY*2);
        //ViewHelper.setTranslationY(listBackground, Math.max(0, -scrollY + mapWrapperHeight));
    }

    override fun onDownMotionEvent() {

    }

    override fun onUpOrCancelMotionEvent(scrollState: ScrollState) {

    }

    companion object {

        private val GEOCODER_MAX_RESULTS = 5
        private val SEARCH_ADDRESS_REQUEST_CODE = 915

        fun createIntent(context: Context): Intent {
            return Intent(context, SelectLocationActivity::class.java)
        }
    }
}
