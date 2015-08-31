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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test for {@link Point}
 */
public class PointTest {

    @Test
    public void testEmptyConstructor() {
        Point point = new Point();

        validatePoint(point, null, null);
    }

    @Test
    public void testLatLngConstructor() {
        Double lat = 2.5;
        Double lng = 3.5;
        Point point = new Point(lat, lng);

        validatePoint(point, lat, lng);
    }

    @Test
    public void testCoordinatesConstructor() {
        Double lat = 50.7;
        Double lng = 10.18;
        Point point = new Point(Arrays.asList(lng, lat));

        validatePoint(point, lat, lng);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWrongType() {
        Point point = new Point();
        point.setType("wrong_type");
    }

    @Test
    public void testParse() {
        Double lat = 5.78;
        Double lng = 85.789;
        String latLng = lat + Point.SEPARATOR + lng;
        Point point = new Point(latLng);

        validatePoint(point, lat, lng);

        // test with white spaces
        latLng = " " + lat + Point.SEPARATOR + "    " + lng;
        point = new Point(latLng);

        validatePoint(point, lat, lng);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWrongString() {
        Point point = new Point("12,abc");
    }

    public void validatePoint(Point point, Double lat, Double lng) {
        assertEquals(Point.TYPE, point.getType());

        assertEquals(lat, point.getLatitude());
        assertEquals(lng, point.getLongitude());
        assertEquals(2, point.getCoordinates().size());

        assertEquals(lat, point.getCoordinates().get(Point.LATITUDE_INDEX));
        assertEquals(lng, point.getCoordinates().get(Point.LONGITUDE_INDEX));
    }
}