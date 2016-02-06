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

package org.bbqapp.android.api.service;

import android.location.Location;

import org.bbqapp.android.api.model.Id;
import org.bbqapp.android.api.model.Picture;
import org.bbqapp.android.api.model.PictureInfo;
import org.bbqapp.android.api.model.Place;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Rest interface for places
 */
public interface PlaceService {

    @GET("/api/places/{placeId}")
    Observable<Place> getPlace(@Path("placeId") String placeId);

    @GET("/api/places")
    Observable<List<Place>> getPlaces(@Query("location") String location);

    @GET("/api/places")
    Observable<List<Place>> getPlaces(@Query("location") String location, @Query("radius") long radius);

    @GET("/api/places")
    Observable<List<Place>> getPlaces(@Query("location") Location location, @Query("radius") long radius);

    @POST("/api/places")
    Observable<Id> postPlace(@Body Place place);

    @GET("/api/places/{placeId}/pictures/{imageId}")
    Observable<Picture> getPicture(@Path("placeId") String placeId, @Path("imageId") String imageId);

    @GET("/api{pictureUrl}")
    Observable<Picture> getPicture(@Path(value = "pictureUrl", encoded = true) String pictureUrl);

    @POST("/api/places/{placeId}/pictures")
    Observable<Id> postPicture(@Path("placeId") String placeId, @Body Picture picture);

    @GET("/api/places/{placeId}/pictures")
    Observable<List<PictureInfo>> getPicturesInfo(@Path("placeId") String placeId);
}
