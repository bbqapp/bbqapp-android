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

package org.bbqapp.android.api.model;

import java.util.ArrayList;
import java.util.List;

public class Point extends Location {
    public static final int LONGITUDE_INDEX = 0;
    public static final int LATITUDE_INDEX = 1;
    public static final String TYPE = "Point";
    public static final String SEPARATOR = ",";

    public Point() {
        super(TYPE);
        List<Double> coordinates = new ArrayList<>(2);
        coordinates.add(null);
        coordinates.add(null);
        super.setCoordinates(coordinates);
    }

    public Point(android.location.Location location) {
        this();
        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
    }

    public Point(String latLng) {
        this();
        String[] latLngArray = latLng.split(SEPARATOR);

        if (latLngArray.length != 2) {
            throw new IllegalArgumentException("Given string cannot be parsed to Point object");
        }

        Double lat = Double.parseDouble(latLngArray[0].trim());
        Double lng = Double.parseDouble(latLngArray[1].trim());

        setLatitude(lat);
        setLongitude(lng);
    }

    public Point(Location location) {
        this();
        setType(location.getType());
        setCoordinates(location.getCoordinates());
    }

    public Point(List<Double> coordinates) {
        this();

        setCoordinates(coordinates);
    }

    public Point(double latitude, double longitude) {
        this();
        setLongitude(longitude);
        setLatitude(latitude);
    }

    @Override
    public void setType(String type) {
        if (!TYPE.equals(type)) {
            throw new IllegalArgumentException("Only Point type is allowed");
        }

        super.setType(type);
    }

    public Double getLongitude() {
        return super.getCoordinates().get(LONGITUDE_INDEX);
    }

    public void setLongitude(double longitude) {
        super.getCoordinates().set(LONGITUDE_INDEX, longitude);
    }

    public Double getLatitude() {
        return super.getCoordinates().get(LATITUDE_INDEX);
    }

    public void setLatitude(double latitude) {
        super.getCoordinates().set(LATITUDE_INDEX, latitude);
    }

    @Override
    public List<Double> getCoordinates() {
        return new ArrayList<>(super.getCoordinates());
    }

    @Override
    public void setCoordinates(List<Double> coordinates) {
        if (coordinates.size() != 2) {
            throw new IllegalArgumentException("Only list with two elements is allowed");
        }

        super.setCoordinates(new ArrayList<>(coordinates));
    }

    /**
     * Returns comma separated coordinates as string
     *
     * @return string representation of point
     */
    @Override
    public String toString() {
        return getLatitude() + SEPARATOR + getLongitude();
    }

    /**
     * Converts to generic location type
     *
     * @return new location instance
     */
    public Location toLocation() {
        return new Location(getType(), getCoordinates());
    }
}
