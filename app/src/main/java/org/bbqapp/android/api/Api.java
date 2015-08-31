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

package org.bbqapp.android.api;

import java.util.concurrent.ConcurrentHashMap;

import retrofit.RestAdapter;

/**
 * Service interface factory
 */
public final class Api {

    private static ConcurrentHashMap<Class<?>, Object> services = new ConcurrentHashMap<>();

    private static RestAdapter restAdapter = new RestAdapter.Builder()
            .setClient(new ApiClient())
            .setConverter(new ApiConverter())
            .setErrorHandler(new ApiErrorHandler())
            .setEndpoint(getEndpoint())
            .build();

    private Api() {
    }

    /**
     * Creates service interface
     *
     * @param clazz Class of service interface to create
     * @param <T>   type
     * @return service interface instance
     */
    public static <T> T get(Class<T> clazz) {
        T service = (T) services.get(clazz);
        if (service == null) {
            synchronized (Api.class) {
                service = (T) services.get(clazz);
                if (service == null) {
                    service = restAdapter.create(clazz);
                    services.put(clazz, service);
                }
            }
        }

        return service;
    }

    /**
     * Returns endpoint of used backend
     *
     * @return hostname or ip of backend
     */
    public static String getEndpoint() {
        return "http://bbqapp.org";
    }
}
