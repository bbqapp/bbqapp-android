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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Formats distance in meters to human readable text
 */
public class DistanceFormatter {
    public static final String METER_UNIT = "m";
    public static final String KILOMETER_UNIT = "km";
    private static final String MILE_UNIT = "mi";
    private static final String FEET_UNIT = "ft";
    private static final double METERS_IN_ONE_MILE = 1_609.344d;
    private static final double FEET_IN_ONE_MILE = 5280d;

    private static final List<String> COUNTRIES_WITH_IMPERIAL_UNIT = Collections.unmodifiableList(Arrays.asList(
            "US", "LR", "MM"));

    protected static final String VALUE_UNIT_SEPARATOR = " ";

    public enum Unit {
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
        return format(meters, Locale.getDefault());
    }

    public static String format(double meters, Locale locale) {

        return format(meters, getUnits(locale));
    }

    protected static Unit getUnits(Locale locale) {
        String country = locale.getCountry();
        if (country != null && COUNTRIES_WITH_IMPERIAL_UNIT.contains(country)) {
            return Unit.IMPERIAL;
        }
        return Unit.METRIC;
    }


    /**
     * Formats distance in meters to human readable string
     *
     * @param meters distance to format
     * @param system unit system to use
     * @return human readable distance text
     */
    private static String format(double meters, Unit system) {
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
            return new DecimalFormat("#,###").format(meters) + VALUE_UNIT_SEPARATOR + METER_UNIT;
        } else if (absMeters < 100_000) {
            return new DecimalFormat("#,###.#").format(meters / 1_000d) + VALUE_UNIT_SEPARATOR + KILOMETER_UNIT;
        } else {
            return new DecimalFormat("#,###").format(meters / 1_000d) + VALUE_UNIT_SEPARATOR + KILOMETER_UNIT;
        }
    }

    protected static String formatImperial(double meters) {
        double miles = meters / METERS_IN_ONE_MILE;
        double absMiles = Math.abs(miles);

        if (absMiles * FEET_IN_ONE_MILE < FEET_IN_ONE_MILE / 10) {
            return new DecimalFormat("###").format(miles * FEET_IN_ONE_MILE) + VALUE_UNIT_SEPARATOR + FEET_UNIT;
        } else if (absMiles < 1_000) {
            return new DecimalFormat("#,###.##").format(miles) + VALUE_UNIT_SEPARATOR + MILE_UNIT;
        } else {
            return new DecimalFormat("#,###").format(miles) + VALUE_UNIT_SEPARATOR + MILE_UNIT;
        }
    }
}
