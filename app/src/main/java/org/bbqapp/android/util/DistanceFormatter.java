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

package org.bbqapp.android.util;

import java.text.DecimalFormat;

/**
 * Formats distance in meters to human readable text
 */
public class DistanceFormatter {
    public static final String METERS_UNIT = "m";
    public static final String KILOMETERS_UNIT = "km";
    private static final String MILES_UNIT = "mi";
    private static final double MILE_IN_METERS = 1_609.344d;

    protected static final String VALUE_UNIT_SEPARATOR = " ";

    private enum UnitSystem {
        METRIC,
        IMPERIAL
    }

    /**
     * Formats distance in meters to human readable string
     *
     * @param meters distance to format
     * @return human readable distance text
     */
    public static String format(double meters) {
        return format(meters, UnitSystem.METRIC);
    }


    /**
     * Formats distance in meters to human readable string
     *
     * @param meters distance to format
     * @param system unit system to use
     * @return human readable distance text
     */
    private static String format(double meters, UnitSystem system) {
        switch (system) {
            case IMPERIAL:
                return formatImperial(meters);
            default:
                return formatMetric(meters);
        }
    }

    protected static String formatMetric(double meters) {
        double absMeters = Math.abs(meters);
        if (absMeters < 1_000) {
            return new DecimalFormat("#,###").format(meters) + VALUE_UNIT_SEPARATOR + METERS_UNIT;
        } else if (absMeters < 100_000) {
            return new DecimalFormat("#,###.#").format(meters / 1_000d) + VALUE_UNIT_SEPARATOR + KILOMETERS_UNIT;
        } else {
            return new DecimalFormat("#,###").format(meters / 1_000d) + VALUE_UNIT_SEPARATOR + KILOMETERS_UNIT;
        }
    }

    protected static String formatImperial(double meters) {
        // TODO

        double miles = meters / MILE_IN_METERS;
        return new DecimalFormat("#,###.##").format(miles) + VALUE_UNIT_SEPARATOR + MILES_UNIT;
    }

}
