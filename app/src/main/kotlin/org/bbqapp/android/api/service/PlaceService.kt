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

package org.bbqapp.android.api.service

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import org.bbqapp.android.api.model.*
import retrofit2.http.*
import rx.Observable

interface PlaceService {

    @GET("/api/places/{placeId}")
    fun getPlace(@Path("placeId") placeId: String): Observable<Place>

    @GET("/api/places")
    fun getPlaces(@Query("location") location: String): Observable<List<Place>>

    @GET("/api/places")
    fun getPlaces(@Query("location") location: String, @Query("radius") radius: Long): Observable<List<Place>>

    @GET("/api/places")
    fun getPlaces(@Query("location") location: LatLng, @Query("radius") radius: Long): Observable<List<Place>>

    @GET("/api/places")
    fun getPlaces(@Query("location") location: Location, @Query("radius") radius: Long): Observable<List<Place>>

    @POST("/api/places")
    fun postPlace(@Body place: Place): Observable<Id>

    @GET("/api/places/{placeId}/pictures/{imageId}")
    fun getPicture(@Path("placeId") placeId: String, @Path("imageId") imageId: String): Observable<Picture>

    @GET("/api/places/{placeId}/pictures/{imageId}")
    fun getPicture(@Path("placeId") placeId: HasId, @Path("imageId") imageId: HasId): Observable<Picture>

    @GET("/api{pictureUrl}")
    fun getPicture(@Path(value = "pictureUrl", encoded = true) pictureUrl: String): Observable<Picture>

    @POST("/api/places/{placeId}/pictures")
    fun postPicture(@Path("placeId") placeId: String, @Body picture: Picture): Observable<Id>

    @POST("/api/places/{placeId}/pictures")
    fun postPicture(@Path("placeId") placeId: HasId, @Body picture: Picture): Observable<Id>

    @GET("/api/places/{placeId}/pictures")
    fun getPicturesInfo(@Path("placeId") placeId: String): Observable<List<PictureInfo>>

    @GET("/api/places/{placeId}/pictures")
    fun getPicturesInfo(@Path("placeId") placeId: HasId): Observable<List<PictureInfo>>
}