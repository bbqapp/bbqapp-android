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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.bbqapp.android.R;
import org.bbqapp.android.api.model.Place;
import org.bbqapp.android.service.LocationService;
import org.bbqapp.android.api.service.PlaceService;
import org.bbqapp.android.view.BaseFragment;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.Bind;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Fragment to display places in a clickable list
 */
public class ListFragment extends BaseFragment {

    @Bind(R.id.places_list) ListView placeList;
    private PlaceListAdapter placeAdapter;
    private Subscription locationSubscription;

    @Inject
    LayoutInflater layoutInflater;

    @Inject
    LocationService locationService;

    @Inject
    PlaceService placeService;
    @Inject
    @Named("main")
    Scheduler scheduler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        placeAdapter = new PlaceListAdapter(layoutInflater);
        placeList.setAdapter(placeAdapter);

        getActivity().setTitle(R.string.menu_list);
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

    public void onLocationChanged(Location location) {
        placeAdapter.setLocation(location);

        if (placeAdapter.getList() == null) {
            placeService.getPlaces(location, 10000)
                    .subscribeOn(Schedulers.io())
                    .observeOn(scheduler)
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<Place>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getActivity(),
                                    "Could not retrieve places: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(List<Place> places) {
                            placeAdapter.setPlaces(places);
                        }
                    });
        }
    }

}
